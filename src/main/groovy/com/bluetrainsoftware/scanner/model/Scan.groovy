package com.bluetrainsoftware.scanner.model

import groovy.transform.CompileStatic

/**
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@CompileStatic
class Scan {
	String template
	String templateType = "spring"
	List<String> packages = []
	boolean recursePackages = true
	List<String> classes = []
	List<String> requiredAnnotations = []
	List<String> followAnnotations = []
	List<String> excludes
	List<String> includes
	List<String> generatedClassName

	boolean interestingClass(String name) {
		return (!includes && !excludes) ||
		(includes && includes.contains(name)) ||
			(excludes && !excludes.contains(name))
	}
}
