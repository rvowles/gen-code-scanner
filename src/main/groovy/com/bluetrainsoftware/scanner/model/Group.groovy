package com.bluetrainsoftware.scanner.model

import com.github.javaparser.symbolsolver.model.declarations.ReferenceTypeDeclaration
import com.github.javaparser.symbolsolver.model.declarations.TypeDeclaration
import groovy.transform.CompileStatic
import groovy.transform.ToString

@CompileStatic
class Group {
	Set<TypeDeclaration> types = new HashSet<>()
	String groupName

	@Override
	String toString() {
		return "group: ${groupName} : ${types*.name}"
	}
}
