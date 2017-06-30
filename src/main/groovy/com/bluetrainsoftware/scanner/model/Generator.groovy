package com.bluetrainsoftware.scanner.model

/**
 * Created by Richard Vowles on 30/06/17.
 */
class Generator {
	List<String> sourceBases = ["./src/main/java"]
	String sourceBase = "./src/main/java"
	boolean includeJarDependencies = false
	List<Scan> scans
	List<Group> groups
}
