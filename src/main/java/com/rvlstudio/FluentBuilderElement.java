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

	private Element enclosing;
	private String name;
	private List<Property> properties = new ArrayList<>();

	FluentBuilderElement(Element element) {
		this.enclosing = element;
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

		members.forEach((m) -> properties.add(checkMethods(m, methods)));
		types.directSupertypes(element.asType()).forEach((t) -> parseProperties(types.asElement(t)) );
	}

	String getName() {
		return name;
	}

	List<Property> getProperties() {
		return properties;
	}

	public Element getEnclosing() {
		return enclosing;
	}

	private static Property checkMethods(Element element, List<? extends Element> methods) {
		String upper = firstToUpper(element.getSimpleName().toString());
		String setterName = "set" + upper;
		String getterName = "get" + upper;
		boolean hasSetter = methods.stream().anyMatch((m) -> m.getSimpleName().toString().equals(setterName));
		boolean hasGetter = methods.stream().anyMatch((m) -> m.getSimpleName().toString().equals(getterName));
		boolean isFluent = element.getAnnotation(FluentProperty.class) != null;

		return new Property(element, hasGetter, hasSetter, isFluent);
	}

	private static String firstToUpper(String string) {
		return (string != null && string.length() > 0) ?
			 Character.toUpperCase(string.charAt(0)) + string.substring(1) :
						string;
	}

	public static class Property {
		private Element element;
		private boolean getter, setter, fluent;

		Property(Element element, boolean getter, boolean setter, boolean fluent) {
			this.element = element;
			this.getter = getter;
			this.setter = setter;
			this.fluent = fluent;
		}

		Element getElement() { return element; }
		String getName() { return element.getSimpleName().toString(); }
		String getCapitalizedName() { return firstToUpper(getName()); }
		boolean hasGetter() { return getter; }
		boolean hasSetter() { return setter; }
		boolean isFluent() { return fluent; }
	}
}