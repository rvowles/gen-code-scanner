package com.bluetrainsoftware.scanner.collected

import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.symbolsolver.model.declarations.ReferenceTypeDeclaration
import groovy.transform.CompileStatic

/**
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@CompileStatic
class CollectedClass {
	@Delegate
	private ReferenceTypeDeclaration clazz
	private TypeDeclaration src
	CollectedAnnotation annotation

	CollectedClass(ReferenceTypeDeclaration clazz, TypeDeclaration src) {
		this.clazz = clazz
		this.src = src
	}

	ReferenceTypeDeclaration getClazz() {
		return clazz
	}

	TypeDeclaration getSrc() {
		return src
	}

	public void setOriginalAnnotation(NormalAnnotationExpr normalAnnotationExpr) {
		this.annotation = new CollectedAnnotation(normalAnnotationExpr)
	}
}
