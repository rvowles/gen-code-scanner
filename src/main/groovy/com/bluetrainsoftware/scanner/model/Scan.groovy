package com.bluetrainsoftware.scanner.model

import groovy.transform.CompileStatic
import org.apache.maven.plugins.annotations.Parameter

/**
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@CompileStatic
class Scan {
	@Parameter
	List<String> joinGroups
	@Parameter
	List<String> packages = []
	@Parameter
	boolean recursePackages = true
	@Parameter
	boolean limitToSource = true
	@Parameter
	List<String> classes = []
	@Parameter
	List<String> requiredAnnotations = []
	@Parameter
	List<String> followAnnotations = []
	@Parameter
	List<String> excludes
	@Parameter
	List<String> includes


	boolean interestingClass(String name) {
		return (!includes && !excludes) ||
		(includes && includes.contains(name)) ||
			(excludes && !excludes.contains(name))
	}

	public void setJoinGroup(String group) {
		if (!this.joinGroups) {
			this.joinGroups = [group]
		}
	}

	public void setPackage(String pkg) {
		if (!this.packages) {
			this.packages = []
		}

		parsePackage(pkg)
	}

	private void parsePackage(String pkg) {
		if (pkg.contains(':')) {
			String basePackage = pkg.substring(0, pkg.indexOf(':'))
			Arrays.stream( pkg.substring(pkg.indexOf(':')+1).split(',')).map({ String s -> s.trim()})
			  .filter({ String s -> s.length() > 0})
			  .forEach({ String extra ->
				this.packages.add(basePackage + '.' + extra)
			})
		} else {
			this.packages.add(pkg)
		}
	}

	public void setPackages(List<String> pkgs) {
		if (!this.packages) {
			this.packages = []
		}

		pkgs?.each {
			parsePackage(it)
		}
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
