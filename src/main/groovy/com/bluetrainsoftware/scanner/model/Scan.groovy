package com.bluetrainsoftware.scanner.model

import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.apache.maven.plugins.annotations.Parameter

/**
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@CompileStatic
@ToString
class Scan {
	@Parameter
	List<String> joinGroups = []
	@Parameter
	List<String> packages = []
	@Parameter
	boolean recursePackages = true
	@Parameter
	boolean limitToSource = true
	@Parameter
	boolean includeAbstract = false
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

	@ToString
	private class SpecificPackageMap {
		String packageName
		private int len
		Set<String> groups = new HashSet<>()

		void setPackageName(String p) {
			this.packageName = p
			this.len = p.length()
		}

		int getLen() {
			return len
		}
	}

	private List<SpecificPackageMap> specificPackageMaps = []

	public List<String> getPackageGroups(String pkg) {
		List<String> groups = []

		if (joinGroups) {
			groups.addAll(joinGroups)
		}

		SpecificPackageMap found = null

		specificPackageMaps.each { SpecificPackageMap spm ->
			println "comparing ${pkg} with ${spm.packageName}"
			if (pkg.startsWith(spm.packageName) && found?.len < spm.len) {
				found = spm
				println "found"
			}
		}

		if (found) {
			groups.addAll(found.groups)
		}

		return groups
	}

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

	private void addPackageGroupMap(String pkg, String... groupList) {
		if (!specificPackageMaps.find({it.packageName == pkg})) {
			SpecificPackageMap spm = new SpecificPackageMap()
			spm.packageName = pkg

			for(String g : groupList) {
				if (g == null) { continue }
				Arrays.stream(g.split(","))
					.map({ String s -> s.trim() })
				  .filter({ String s -> s.length() > 0 })
				  .forEach( { String s ->
						spm.groups.add(s)
				})
			}

			specificPackageMaps.addAll(spm)
		}
	}

	/**
	 * this gives us
	 * com.bluetrainsoftware=jersey, spring: resource=jersey2, resources=jersey3
	 * com.bluetrainsoftware=jersey
	 * com.bluetrainsoftware: resource=jersey2, jersey3, resources=jersey3
	 * etc
	 * @param pkg
	 */
	private void parsePackage(String pkg) {
		if (pkg.contains(':')) {
			String basePackage = pkg.substring(0, pkg.indexOf(':')).trim()
			String packageGroupMap = null
			// allow com.bluetrainsoftware=jersey: resource, resources -> always map to those two (+ joinGroups)
			int groupIndex = basePackage.indexOf('=')
			if (groupIndex != -1) {
				packageGroupMap = basePackage.substring(groupIndex +1).trim()
				basePackage = basePackage.substring(0, groupIndex).trim()
			}
			Arrays.stream( pkg.substring(pkg.indexOf(':')+1).split(',')).map({ String s -> s.trim()})
			  .filter({ String s -> s.length() > 0})
			  .forEach({ String extra ->
				groupIndex = extra.indexOf('=')
				String extraGroups = null
				if (groupIndex != -1) {
					extra = extra.substring(0, groupIndex).trim()
					extraGroups = extra.substring(groupIndex+1).trim()
				}
				String pkgName = basePackage + '.' + extra
				this.packages.add(pkgName)
				
				addPackageGroupMap(pkgName, packageGroupMap, extraGroups)
			})
		} else {
			int groupIndex = pkg.indexOf('=')
			String packageGroupMap = null
			if (groupIndex != -1) {
				packageGroupMap = pkg.substring(groupIndex +1).trim()
				pkg = pkg.substring(0, groupIndex).trim()
			}
			this.packages.add(pkg)
			addPackageGroupMap(pkg, packageGroupMap)
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
