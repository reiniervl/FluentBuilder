package com.rvlstudio;

/**
 * FluentBuilderParser
 */
class FluentBuilderWriter {
	private FluentBuilderWriter() {}
	static void write(FluentBuilderElement element) {
		System.out.println(element.getName());
		element.getProperties().forEach((p) -> System.out.println(String.format("%s hasGetter: %b, hasSetter: %b", p.getName(), p.hasGetter(), p.hasSetter())));

	}
}