package com.bluetrainsoftware.scanner.model

import groovy.transform.ToString
import org.apache.maven.plugins.annotations.Parameter

/**
 * Created by Richard Vowles on 30/06/17.
 */
@ToString
class Generator {
	@Parameter
	List<String> sourceBases = ["./src/main/java"]
	@Parameter
	String sourceBase = "./src/main/java"
	@Parameter
	boolean scanJarDependencies = false
	@Parameter
	List<Scan> scans
	@Parameter
	List<Group> groups
	@Parameter
	String template
	@Parameter
	String className
	@Parameter
	Map<String, String> context = [:]

	private String simpleName
	private String packageName

	String getSimpleName() {
		return simpleName
	}

	String getPackageName() {
		return packageName
	}

	public void setClassName(String cName) {
		this.className = cName

		int packageSep = cName.lastIndexOf('.')
		if (packageSep != -1) {
			this.simpleName = cName.substring(packageSep + 1)
			this.packageName = cName.substring(0, packageSep)
		} else {
			this.simpleName = className
			this.packageName = ""
		}
	}
}
