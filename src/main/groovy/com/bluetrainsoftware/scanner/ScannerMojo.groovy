package com.bluetrainsoftware.scanner

import com.bluetrainsoftware.scanner.model.Generator
import com.bluetrainsoftware.scanner.model.Group
import com.bluetrainsoftware.scanner.model.Scan
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.declarations.FieldDeclaration
import com.github.javaparser.symbolsolver.model.declarations.ReferenceTypeDeclaration
import com.github.javaparser.symbolsolver.model.methods.MethodUsage
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import groovy.transform.CompileStatic

/**
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@CompileStatic
class ScannerMojo {
	protected Generator scanner
	protected Map<String, Group> groups = new HashMap<String, Group>().withDefault { String key ->
		return new Group(groupName: key)
	}
	private JavaParserFacade facade

	public ScannerMojo() {}

	public ScannerMojo(Generator scanner) {
		this.scanner = scanner
	}

	public void scan() {
		if (!scanner.scans || !scanner.sourceBases)  return

		CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver()
		combinedTypeSolver.add(new ReflectionTypeSolver())
		scanner.sourceBases.each { String sourceBase ->
			combinedTypeSolver.add(new JavaParserTypeSolver(new File(sourceBase)))
		}

		facade = JavaParserFacade.get(combinedTypeSolver)

		scanner.scans.each { Scan scan ->
			scan.packages.each { String pkg ->
				File dir = new File(new File(scanner.sourceBase), pkg.replace('.', File.separator));

				processFiles(scan, dir)
			}
		}

		groups.values().each { println it }
	}

	void processFiles(Scan scan, File file) {
		file.listFiles().each { File f ->
			println "checking ${f.name}"
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


	private void processFile(Scan scan, File file) {
		CompilationUnit cu = sourceCache[file];

		if (cu.getTypes() == null) { // nothing in this file
			return;
		}

		try {
			for (TypeDeclaration td : cu.getTypes()) {
				String name = td.name.toString()

				ReferenceTypeDeclaration rd = facade.getTypeDeclaration(td)

				if (rd.isClass() && scan.interestingClass(name)) {
					if (!scan.requiredAnnotations || (scan.requiredAnnotations && hasRequiredAnnotations(td, scan.requiredAnnotations))) {
						// this is an interesting class, therefore it should go in all of the groups outlined by the scan
						addTypeToGroups(scan, rd)

						if (scan.followAnnotations) {
							discoverFollowingAnnotations(rd, td, scan)
						}

					}
				}


			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private addTypeToGroups(Scan scan, com.github.javaparser.symbolsolver.model.declarations.TypeDeclaration td) {
		scan.joinGroups.each { group ->
			groups[group].types.add(td)
		}

	}

	private void discoverFollowingAnnotations(ReferenceTypeDeclaration rd, TypeDeclaration td, Scan scan) {
		List<String> fields = []
		List<String> members = []

		td.members.each { member ->
			if (member instanceof com.github.javaparser.ast.body.FieldDeclaration) {
				com.github.javaparser.ast.body.FieldDeclaration fld = (com.github.javaparser.ast.body.FieldDeclaration)member
				if (scan.followAnnotations.any { String annotation -> fld.isAnnotationPresent(annotation)}) {
					fields.add(fld.variables[0].name.asString())
				}
			} else if (member instanceof com.github.javaparser.ast.body.MethodDeclaration) {
				com.github.javaparser.ast.body.MethodDeclaration method = (com.github.javaparser.ast.body.MethodDeclaration)member
				if (scan.followAnnotations.any({ String annotation -> method.isAnnotationPresent(annotation)})) {
					members.add(method.name.asString())
				}
			}
		}

		fields.each { String name ->
			addTypeToGroups(scan, rd.getField(name).declaringType())
		}

		rd.getAllMethods().each { MethodUsage mu ->
			if (members.contains(mu.name)) {
				addTypeToGroups(scan, mu.declaringType())
			}
		}
	}

	private static boolean hasRequiredAnnotations(TypeDeclaration td, List<String> annotations) {
		return annotations.any { String annotation -> td.isAnnotationPresent(annotation)}
	}
}
