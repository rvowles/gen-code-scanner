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
			String value = mvp.value.toString()?.trim()
			put(mvp.name.asString(), value)
		}
	}

	NormalAnnotationExpr getAnnotation() {
		return annotation
	}
}