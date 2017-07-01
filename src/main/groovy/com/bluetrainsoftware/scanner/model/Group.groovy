package com.bluetrainsoftware.scanner.model

import com.github.javaparser.symbolsolver.model.declarations.ReferenceTypeDeclaration
import com.github.javaparser.symbolsolver.model.declarations.TypeDeclaration
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.apache.maven.plugins.annotations.Parameter

@CompileStatic
class Group {
	@Parameter
	String groupName
	@Parameter
	String template
	@Parameter
	String className
	@Parameter
	boolean createGraph
	@Parameter
	boolean limitFollowToSource
	@Parameter
	Set<String> limitFollowPackages
	@Parameter
	List<String> postProcessors
	@Parameter
	Map<String, String> context = [:]
}
