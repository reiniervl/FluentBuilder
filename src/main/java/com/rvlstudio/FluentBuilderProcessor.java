package com.rvlstudio;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * FluentBuilderProcessor
 */
@SupportedAnnotationTypes({"com.rvlstudio.FluentBuilder"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class FluentBuilderProcessor extends AbstractProcessor {
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		FluentBuilderElement.types = processingEnv.getTypeUtils();
		FluentBuilderElement.elements = processingEnv.getElementUtils();

		for(TypeElement annotation : annotations) {
			for(Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
				FluentBuilderWriter.write(new FluentBuilderElement(element));			
			}
		}
		return false;
	}
}