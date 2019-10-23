/**
 * 
 */
package org.adam.gensource;

import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import org.adam.gensource.handler.VersionHandler;

/**
 * @author nixiaorui
 *
 */
@SupportedAnnotationTypes("org.adam.gensource.VersionAnnotation")
public class VersionProcessor extends AbstractProcessor {

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		System.out.println("changing version begin");
		if (!roundEnv.processingOver()) { // 判断是否为最终轮
			for (Element element : roundEnv.getElementsAnnotatedWith(VersionAnnotation.class)) {
				if (!(element instanceof TypeElement) || element.getKind() != ElementKind.CLASS) {
					continue;
				}
				changeVersion(findAnnoValue(element));
			}
		}
		return false;
	}

	private void changeVersion(String[] findAnnoValue) {
		if (null == findAnnoValue[0] || "".equals(findAnnoValue[0])) {
			findAnnoValue[0] = "0.0.1";
		}
		if (null == findAnnoValue[1]) {
			findAnnoValue[1] = "";
		}
		System.out.println("version value is " + findAnnoValue[0]);
		System.out.println("version path is " + findAnnoValue[1]);
		VersionHandler.handle(findAnnoValue[0], findAnnoValue[1]);
	}

	private String[] findAnnoValue(Element element) {
		String[] vals = new String[2];
		for (AnnotationMirror anno : element.getAnnotationMirrors()) {
			if (!VersionAnnotation.class.getName().equals(anno.getAnnotationType().toString())) {
				continue;
			}
			for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : anno.getElementValues()
					.entrySet()) {
				String key = entry.getKey().getSimpleName().toString();
				if ("value".equals(key)) {
					vals[0] = entry.getValue().getValue().toString();
				} else if ("path".equals(key)) {
					vals[1] = entry.getValue().getValue().toString();
				}
			}
			return vals;
		}
		return vals;
	}

}
