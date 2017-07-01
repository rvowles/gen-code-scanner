package com.bluetrainsoftware.scanner.collected

import com.github.javaparser.ast.expr.MemberValuePair
import com.github.javaparser.ast.expr.NormalAnnotationExpr

/**
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
class CollectedAnnotation extends HashMap<String, String> {
	private final NormalAnnotationExpr annotation

	CollectedAnnotation(NormalAnnotationExpr annotation) {
		this.annotation = annotation

		annotation.pairs.each { MemberValuePair mvp ->
			put(mvp.name.asString(), mvp.value.toString())
		}
	}

	NormalAnnotationExpr getAnnotation() {
		return annotation
	}
}
