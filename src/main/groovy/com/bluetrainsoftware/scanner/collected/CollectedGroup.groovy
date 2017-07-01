package com.bluetrainsoftware.scanner.collected

import com.bluetrainsoftware.scanner.model.Group
import com.github.javaparser.symbolsolver.model.declarations.TypeDeclaration

/**
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
class CollectedGroup {
	Group group

	String name

	/**
	 * these are source classes, and not backed by compiled code. They are
	 * from the project itself.
	 */
	Set<CollectedClass> types = new HashSet<>()

	/**
	 * these are compiled classes, not backed by source code. They typically come
	 * from dependencies
	 */
	Set<Class<?>> classTypes = new HashSet<>()

	String packageName
	String simpleClassName

	public void setGroup(Group group) {
		this.group = group

		if (group.className) {
			packageName = group.className.substring(0, group.className.lastIndexOf('.'))
			simpleClassName = group.className.substring(group.className.lastIndexOf('.') + 1)
		}
	}

	@Override
	String toString() {
		return "group: ${group?.groupName} : ${types*.name}"
	}
}
