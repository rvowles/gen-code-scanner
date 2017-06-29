package com.bluetrainsoftware.scanner

import com.bluetrainsoftware.scanner.model.Scan
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import groovy.transform.CompileStatic

/**
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@CompileStatic
class ScannerMojo {
	protected List<Scan> scans;

	protected File getProjectSource() {
		return new File("./src/main/java");
	}

	public void scan() {
		if (!scans)  return

		scans.each { Scan scan ->
			scan.packages.each { String pkg ->
				File dir = new File(getProjectSource(), pkg.replace('.', File.separator));

				processFiles(scan, dir)
				
				if (scan.recursePackages) {
					processSubpackages(scan, pkg, dir);
				}
			}
		}
	}

	void processSubpackages(Scan scan, String packageName, File baseFolder) {

	}

	void processFiles(Scan scan, File file) {
		processFile(scan, file);
	}


	private void processFile(Scan scan, File file) {
		CompilationUnit cu;

		try {
			cu = JavaParser.parse(file);

			if (cu.getTypes() == null) { // nothing in this file
				return;
			}

			for (TypeDeclaration td : cu.getTypes()) {
				String name = td.name.asString()
				if (scan.interestingClass(name)) {
					if (!scan.requiredAnnotations || (scan.requiredAnnotations && hasRequiredAnnotations(td, scan.requiredAnnotations))) {
						td.fields.each { FieldDeclaration fd ->
							
						}
					}
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static boolean hasRequiredAnnotations(TypeDeclaration td, List<String> annotations) {
		return annotations.any { String annotation -> td.isAnnotationPresent(annotation)}
	}
}
