package com.bluetrainsoftware.scanner

import com.bluetrainsoftware.scanner.collected.CollectedClass
import com.bluetrainsoftware.scanner.collected.CollectedGroup
import com.bluetrainsoftware.scanner.model.Generator
import com.bluetrainsoftware.scanner.model.Group
import com.bluetrainsoftware.scanner.model.Scan
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration
import com.github.javaparser.symbolsolver.model.declarations.ReferenceTypeDeclaration
import com.github.javaparser.symbolsolver.model.methods.MethodUsage
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceType
import com.github.javaparser.symbolsolver.model.typesystem.Type
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionClassDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Template
import groovy.transform.CompileStatic
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import org.apache.maven.project.MavenProjectHelper

/**
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@CompileStatic
@Mojo(name = "generate",
	defaultPhase = LifecyclePhase.GENERATE_SOURCES,
	configurator = "include-project-dependencies",
	requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
class ScannerMojo extends AbstractMojo {
	protected Map<String, CollectedGroup> groups = new HashMap<String, CollectedGroup>().withDefault { String key ->
		return new CollectedGroup(name: key)
	}
	private JavaParserFacade facade
	private CombinedTypeSolver combinedTypeSolver

	@Component
	protected MavenProjectHelper projectHelper;

	@Parameter(required = true)
	protected Generator scanner

	@Parameter(defaultValue = '${project}', readonly = true)
	protected MavenProject project

	@Parameter(defaultValue = '${project.build.directory}/generated-sources/modules/')
	File javaOutFolder

	@Parameter(defaultValue = '${project.directory}')
	File projectDir

	private final String TARGET_CLASSES = File.separator + "target" + File.separator + "classes"
	private final String TARGET_CLASSES2 = File.separator + "target" + File.separator + "classes" + File.separator

	public ScannerMojo() {}

	public ScannerMojo(Generator scanner) {
		this.scanner = scanner
	}

	/**
	 * go figure out what our classpath is, including the dependencies of this project
	 */
	private void createResolver() {
		combinedTypeSolver = new CombinedTypeSolver()
		combinedTypeSolver.add(new ReflectionTypeSolver())

		// add any extra ones we have been given
		scanner.sourceBases?.each { String sourceBase ->
			combinedTypeSolver.add(new JavaParserTypeSolver(new File(sourceBase)))
		}

		// add the one specified
		combinedTypeSolver.add(new JavaParserTypeSolver(new File(scanner.sourceBase)))

		// now check if we have a url classpath
		if (getClass().classLoader instanceof URLClassLoader) {
			URLClassLoader urlClassLoader = URLClassLoader.class.cast(getClass().classLoader)

			urlClassLoader.URLs.each { URL url ->
				String sUrl = url.toString()

				if ( sUrl.startsWith("jar:")) {
					println "jar -> " + sUrl
					combinedTypeSolver.add(new JarTypeSolver(sUrl.substring(4)))
				} else if (sUrl.startsWith("file:")) {
					if (sUrl.endsWith(".jar")) {
						println "jarfile -> " + sUrl
						combinedTypeSolver.add(new JarTypeSolver(new File(url.toURI()).absolutePath))
					} else if (sUrl.endsWith(TARGET_CLASSES) || sUrl.endsWith(TARGET_CLASSES2)) {
						File realFolder = new File(new File(url.toURI()).getParentFile().getParentFile(), "/src/main/java")

						if (realFolder.exists()) {
							println "dependent project -> " + realFolder.absolutePath

							combinedTypeSolver.add(new JavaParserTypeSolver(realFolder))
						}
					} else {
						combinedTypeSolver.add(new JavaParserTypeSolver(new File(url.toURI())))
						println "file -> " + sUrl
					}
				} else {
					println "no idea -> " + sUrl
				}
			}
		} else {
			println "not using a url classloader"
		}
	}

	private void fillGroups() {
		scanner.groups?.each { Group group ->
			groups[group.groupName].group = group
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!scanner.scans || !scanner.sourceBase) {
			getLog().warn("No scans, source base, bailing. ${scanner}")
			return
		}

		createResolver()
		fillGroups()

		facade = JavaParserFacade.get(combinedTypeSolver)

		scanner.scans.each { Scan scan ->
			scan.packages.each { String pkg ->
				File dir = new File(new File(scanner.sourceBase), pkg.replace('.', File.separator));

				processFiles(scan, dir)
			}
		}

		processTemplates()
	}

	private void processTemplates() {

		List<CollectedGroup> missedGroups = []
		missedGroups.addAll(groups.values())
		Map<String, Object> context = [:]

		scanner.groups?.each { Group group ->
			if (group.template && group.className) {
				if (group.context) {
					context.putAll(group.context)
				}

				context.put(group.groupName, groups[group.groupName].types);
				writeTemplate(context, group.template, group.className)

				missedGroups.remove(missedGroups.find({it.group == group}))
			}
		}

		if (scanner.className && scanner.template && missedGroups) {
			context.put("packageName", scanner.packageName)
			context.put("simpleName", scanner.simpleName)
			context.put("className", scanner.className)

			if (scanner.context) {
				context.putAll(scanner.context)
			}

			missedGroups.each { CollectedGroup group ->
				context.put(group.name, group)
			}

			writeTemplate(context, scanner.template, scanner.className)
		} else if (missedGroups) {
			getLog().warn("Ignoring groups ${missedGroups*.name} because no template/classname exists at group or top level.")
		}
	}

	private void writeTemplate(Map<String, Object> context, String template, String className) {
		Template mapperTemplate = Mustache.compiler().compile(new InputStreamReader( getTemplate(template)));
		Writer writer = createWriter(className)
		mapperTemplate.execute(context, writer)
		writer.close()
		context.clear()
	}

	private Writer createWriter(String className) {
		File outFile = new File(javaOutFolder, className.replace('.', File.separator) + ".java")
		outFile.parentFile.mkdirs()
		getLog().info("writing ${outFile.absolutePath}")
		return new FileWriter(outFile)
	}

	private InputStream getTemplate(String name) throws MojoExecutionException {
		InputStream stream = getClass().getResourceAsStream(name);

		if (stream == null) {
			if (!name.startsWith(File.separator)) {
				name = File.separator + name;
			}
			// try local project directory src/main/resources
			File f = new File(projectDir, "src/main/resources" + name);
			getLog().info("looking in " + f.getAbsolutePath());
			if (f.exists()) {
				try {
					stream = new FileInputStream(f);
				} catch (FileNotFoundException e) { // hard to see how this can happen
					throw new MojoExecutionException("Cannot find file", e);
				}
			}
		}

		if (stream == null) {
			// try local project directory src/test/resources
			File f = new File(projectDir, "src/test/resources" + name);
			if (f.exists()) {
				try {
					stream = new FileInputStream(f);
				} catch (FileNotFoundException e) { // hard to see how this can happen
					throw new MojoExecutionException("Cannot find file", e);
				}
			}
		}

		if (stream == null) {
			throw new MojoExecutionException("Cannot find resource named " + name);
		}

		return stream;
	}

	void processFiles(Scan scan, File file) {
		file.listFiles().each { File f ->
			if (f.isDirectory() && scan.recursePackages) {
				processFiles(scan, f)
			} else if (f.name.endsWith(".java")) {
				processFile(scan, f);
			}
		}
	}

	// ensure we get the same compilation unit for the same same file across different scan requests
	Map<File, CompilationUnit> sourceCache = new HashMap<File, CompilationUnit>().withDefault { File f ->
		println "trying file ${f.absolutePath}"
		return JavaParser.parse(f)
	}


	private boolean processFile(Scan scan, File file) {
		if (!file.exists()) {
			return false
		}
		
		CompilationUnit cu = sourceCache[file];

		if (cu.types == null) { // nothing in this file
			return false;
		}

		try {
			for (TypeDeclaration td : cu.types) {
				String name = td.name.toString()

				ReferenceTypeDeclaration rd = facade.getTypeDeclaration(td)

				if (rd.isClass() && scan.interestingClass(name)) {
					CollectedClass cc = new CollectedClass(rd, td)

					if (scan.requiredAnnotations) {
						for(AnnotationExpr annotation : td.annotations) {
							if (scan.requiredAnnotations.contains(annotation.name.asString())) {
								if (annotation instanceof NormalAnnotationExpr) {
									cc.originalAnnotation =  NormalAnnotationExpr.class.cast(annotation)
									break
								}
							}
						}
					}

					if (!scan.requiredAnnotations || cc.annotation) {
						// this is an interesting class, therefore it should go in all of the groups outlined by the scan
						addTypeToGroups(scan, cc)

						if (scan.followAnnotations) {
							discoverFollowingAnnotations(rd, td, scan)
						}
					}
				}
			}
		} catch (Exception ex) {
			getLog().error("Failed", ex)
		}

		return true
	}

	private addTypeToGroups(Scan scan, CollectedClass cc) {
		scan.joinGroups.each { String group ->
			if (!groups[group].types.contains(cc)) {
				groups[group].types.add(cc)
			}
		}
	}

	private addTypeToGroups(Scan scan, ReferenceType td) {
		scan.joinGroups.each { String name ->
			CollectedGroup group = groups[name]
			if (!(scan.limitToSource || group.group?.limitFollowToSource)) {
				if (td.getTypeDeclaration() instanceof JavaParserClassDeclaration) {
					CollectedClass cc = new CollectedClass(td.getTypeDeclaration(), JavaParserClassDeclaration.class.cast(td.getTypeDeclaration()).getWrappedNode())
					addTypeToGroups(scan, cc)
				} else if (td.getTypeDeclaration() instanceof ReflectionClassDeclaration) {
					group.classTypes.add(Class.forName(td.describe(), false, this.getClass().getClassLoader()));
				}
			}
		}
	}


	private void discoverFollowingAnnotations(ReferenceTypeDeclaration rd, TypeDeclaration td, Scan scan) {
		List<String> fields = []
		List<String> members = []

		td.members.each { member ->
			if (member instanceof FieldDeclaration) {
				FieldDeclaration fld = FieldDeclaration.class.cast(member)
				if (scan.followAnnotations.any { String annotation -> fld.isAnnotationPresent(annotation)}) {
					fields.add(fld.variables[0].name.asString())
				}
			} else if (member instanceof MethodDeclaration) {
				MethodDeclaration method = MethodDeclaration.class.cast(member)
				if (scan.followAnnotations.any({ String annotation -> method.isAnnotationPresent(annotation)})) {
					members.add(method.name.asString())
				}
			}
		}

		fields.each { String name ->
			com.github.javaparser.symbolsolver.model.declarations.FieldDeclaration fld = rd.getField(name)
			if (fld.type instanceof ReferenceType) {
				addTypeToGroups(scan, ReferenceType.class.cast(fld.type))
			}

			println "found"
//			addTypeToGroups(scan, rd.getField(name).declaringType())
		}

		rd.getAllMethods().each { MethodUsage mu ->
			if (members.contains(mu.name)) {
				if (mu.paramTypes) {
					mu.paramTypes.each { Type t ->
						if (t instanceof ReferenceType) {
							addTypeToGroups(scan, ReferenceType.class.cast(t))
						}
					}
				}
			}
		}
	}

	private static boolean hasRequiredAnnotations(TypeDeclaration td, List<String> annotations) {
		return annotations.any { String annotation -> td.isAnnotationPresent(annotation)}
	}
}
