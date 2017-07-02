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

	public List<CollectedClass> getSortedTypes() {
		List<CollectedClass> sTypes = []
		sTypes.addAll(types)

		sTypes.sort(new Comparator<CollectedClass>() {
			@Override
			int compare(CollectedClass o1, CollectedClass o2) {
				return o1.name.compareTo(o2.name)
			}
		})

		return sTypes
	}

	public List<Class<?>> getSortedClasses() {
		List<Class<?>> sClasses = []
		sClasses.addAll(classTypes)

		sClasses.sort(new Comparator<Class<?>>() {
			@Override
			int compare(Class<?> o1, Class<?> o2) {
				return o1.name.compareTo(o2.name)
			}
		})

		return sClasses
	}


	@Override
	String toString() {
		return "group: ${group?.groupName} : ${types*.name}"
	}
}
