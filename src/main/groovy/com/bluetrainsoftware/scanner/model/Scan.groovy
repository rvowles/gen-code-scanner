package com.bluetrainsoftware.scanner.model

import groovy.transform.CompileStatic

/**
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@CompileStatic
class Scan {
	List<String> joinGroups
	List<String> packages = []
	boolean recursePackages = true
	boolean limitToSource = true
	List<String> classes = []
	List<String> requiredAnnotations = []
	List<String> followAnnotations = []
	boolean limitFollowToSource
	List<String> excludes
	List<String> includes


	boolean interestingClass(String name) {
		return (!includes && !excludes) ||
		(includes && includes.contains(name)) ||
			(excludes && !excludes.contains(name))
	}

	/**
	 * scans:
	 * e.g. joinGroup: servlets
	 * packages: blah.servlets
	 * requiredAnnotations: WebServlet
	 *
	 * joinGroup: filters, springreg
	 * packages: blah.filters
	 * requiredAnnotations: WebFilter
	 *
	 * joinGroup: springreg
	 * packages: blah.servlet, blah.filters
	 * requiredAnnotations: WebServlet, WebFilter
	 * followAnnotations: Inject
	 * limitFollowToSource: true
	 *
	 * joinGroup: springreg
	 * packages: blah.resources
	 * followAnnotations: Inject
	 * limitFollowToSource: true
	 *
	 * joinGroup: springclient
	 * packages: blah.client
	 * limitToSource: false
	 * followAnnotations: Inject
	 * limitFollowToSource: false
	 *
	 * templates:
	 * template: springreg (/resource/blah.mustache)
	 * generatedClass: blah.module
	 * groups: springreg, filters, servlets
	 */
}
