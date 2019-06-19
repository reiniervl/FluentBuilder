package com.rvlstudio;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FluentBuilderElement
 */
class FluentBuilderElement {
	static Types types;
	static Elements elements;

	private String name;
	private List<Property> properties = new ArrayList<>();

	FluentBuilderElement(Element element) {
		this.name = element.getSimpleName().toString();
		parseProperties(element);
	}

	private void parseProperties(Element element) {
		List<? extends Element> members = elements.getAllMembers((TypeElement) element)
						.stream()
						.filter((m) -> m.getKind().isField())
						.filter((m) -> m.getModifiers()
										.stream()
										.noneMatch((mod) -> mod.equals(Modifier.STATIC)))
						.collect(Collectors.toList());
		List<? extends Element> methods = elements.getAllMembers((TypeElement) element)
						.stream()
						.filter((m) -> m.getKind() == ElementKind.METHOD)
						.filter((m) -> m.getModifiers()
										.stream()
										.noneMatch((mod) -> mod.equals(Modifier.STATIC)))
						.collect(Collectors.toList());

		members.forEach((m) -> properties.add(checkMethods(m.getSimpleName().toString(), methods)));
		types.directSupertypes(element.asType()).forEach((t) -> parseProperties(types.asElement(t)) );
	}

	String getName() {
		return name;
	}

	List<Property> getProperties() {
		return properties;
	}

	private static Property checkMethods(String name, List<? extends Element> methods) {
		String upper = firstToUpper(name);
		String setterName = "set" + upper;
		String getterName = "get" + upper;
		boolean hasSetter = methods.stream().anyMatch((m) -> m.getSimpleName().toString().equals(setterName));
		boolean hasGetter = methods.stream().anyMatch((m) -> m.getSimpleName().toString().equals(getterName));

		return new Property(name, hasGetter, hasSetter);
	}

	private static String firstToUpper(String string) {
		return (string != null && string.length() > 0) ?
			 Character.toUpperCase(string.charAt(0)) + string.substring(1) :
						string;
	}

	public static class Property {
		private String name;
		private boolean getter, setter;

		Property(String name, boolean getter, boolean setter) {
			this.name = name;
			this.getter = getter;
			this.setter = setter;
		}

		String getName() { return name; }

		boolean hasGetter() { return getter; }

		boolean hasSetter() { return setter; }
	}
}