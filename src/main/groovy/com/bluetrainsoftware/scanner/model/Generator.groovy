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
	boolean includeJarDependencies = false
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

	String simpleClassName
	String packageName

	public void setClassName(String cName) {
		this.className = cName
		this.simpleClassName = cName.substring(cName.lastIndexOf('.') + 1)
		this.packageName = cName.substring(0, cName.lastIndexOf('.'))
	}
}
