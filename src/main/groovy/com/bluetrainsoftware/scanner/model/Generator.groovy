package com.bluetrainsoftware.scanner.model

import groovy.transform.ToString
import org.apache.maven.plugins.annotations.Parameter

@ToString
class Generator extends BaseTemplate {
	@Parameter
	List<String> sourceBases = ["./src/main/java"]
	@Parameter
	String sourceBase = "./src/main/java"
	@Parameter
	boolean scanJarDependencies = false
	@Parameter
	List<Scan> scans
	@Parameter
	List<Template> templates
	@Parameter
	String template

	@Parameter
	Map<String, String> context = [:]


}
