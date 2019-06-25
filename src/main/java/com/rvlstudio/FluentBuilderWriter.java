package com.rvlstudio;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

/**
 * FluentBuilderParser
 */
class FluentBuilderWriter {
	private String packageName;
	private String className;
	private String typeName;
	private Element enclosing;

	private final String head = "package %3$s;\n" +
			"\n" +
			"import java.util.function.Consumer;\n" +
			"\n" +
			"public class %1$sBuilder<T> {\n" +
			"\tprivate %2$s result = new %2$s();\n" +
			"\tprivate T callback;\n" +
			"\tprivate Consumer<%2$s> consumer;\n" +
			"\n" +
			"\tprivate %1$sBuilder() {}\n" +
			"\n" +
			"\tprivate %1$sBuilder(T callback, Consumer<%2$s> consumer) {\n" +
			"\t\tthis.callback = callback;\n" +
			"\t\tthis.consumer = consumer;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic static %1$sBuilder<%2$s> start() {\n" +
			"\t\t%1$sBuilder<%2$s> builder = new %1$sBuilder<>();\n" +
			"\t\tbuilder.callback = builder.result;\n" +
			"\t\treturn builder;\n" +
			"\t}\n" +
			"\n" +
			"\tpublic static <T> %1$sBuilder<T> start(T callback, Consumer<%2$s> consumer) {\n" +
			"\t\treturn new %1$sBuilder<>(callback, consumer);\n" +
			"\t}";

	private final String nonFluentField = "\n\n" +
			"\tpublic %3$sBuilder<T> with%1$s(%4$s %2$s) {\n" +
			"\t\tsetVar(result, %2$s, \"%2$s\");\n" +
			"\t\treturn this;\n" +
			"\t}";

	private final String fluentField = "\n\n" +
			"\tpublic %2$sBuilder<%3$sBuilder<T>> with%4$s() {\n" +
			"\t\t%2$sBuilder<%3$sBuilder<T>> builder = %2$sBuilder.start(this, (s) -> setVar(result, s, \"%1$s\"));\n" +
			"\t\treturn builder;\n" +
			"\t}";

	private final String getField = "\n\nprivate static java.lang.reflect.Field getField(Class<?> clazz, String name) {\n" +
			"\t\tjava.lang.reflect.Field field = null;\n" +
			"\t\twhile (clazz != null && field == null) {\n" +
			"\t\t\ttry { field = clazz.getDeclaredField(name); }\n" +
			"\t\t\tcatch (Exception e) { }\n" +
			"\t\t\tclazz = clazz.getSuperclass();\n" +
			"\t\t}\n" +
			"\t\treturn field;\n" +
			"\t}";

	private final String setField = "\n" +
			"\n" +
			"\tprivate static void setVar(Object target, Object var, String fieldName) {\n" +
			"\t\ttry {\n" +
			"\t\t\tjava.lang.reflect.Field field = getField(target.getClass(), fieldName);\n" +
			"\t\t\tfield.setAccessible(true);\n" +
			"\t\t\tfield.set(target, var);\n" +
			"\t\t} catch(SecurityException | IllegalAccessException e) {\n" +
			"\t\t\te.printStackTrace();\n" +
			"\t\t}\n" +
			"\t}";

	private final String tail = "\n\n\tpublic T end() {\n" +
			"\t\tif(consumer != null) consumer.accept(result);\n" +
			"\t\treturn callback;\n" +
			"\t}\n" +
			"}";


	private FluentBuilderWriter(FluentBuilderElement element, ProcessingEnvironment env) {
		this.enclosing = element.getEnclosing();
		this.className= element.getName();
		this.typeName = element.getEnclosing().asType().toString();
		this.packageName = FluentBuilderElement.elements.getPackageOf(element.getEnclosing()).getQualifiedName().toString();
		StringBuilder sb = new StringBuilder();
		sb.append((String.format(this.head, className, typeName, packageName)));
		element.getProperties().forEach((p) -> sb.append(generateField(p)));
		sb.append(getField);
		sb.append(setField);
		sb.append(tail);

		try {
			JavaFileObject file = env.getFiler().createSourceFile(packageName + "." + className + "Builder");
			try(Writer writer = file.openWriter()) {
				writer.write(sb.toString());
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private String generateField(FluentBuilderElement.Property property) {
		if(property.isFluent() && !property.getElement().asType().getKind().isPrimitive()) {
			TypeElement te = FluentBuilderElement.elements.getTypeElement(property.getElement().asType().toString());
			boolean samePackage = FluentBuilderElement.elements.getPackageOf(te).equals(FluentBuilderElement.elements.getPackageOf(enclosing));
			return String.format(fluentField, property.getName(), samePackage ? te.getSimpleName() : te.getQualifiedName(), className, property.getCapitalizedName());

		} else {
			return String.format(nonFluentField, property.getCapitalizedName(), property.getName(), className, property.getElement().asType().toString());
		}
	}

	static void write(FluentBuilderElement element, ProcessingEnvironment environment) {
		new FluentBuilderWriter(element, environment);
	}
}