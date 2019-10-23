/**
 * 
 */
package org.adam.gensource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author USER
 *
 */
@Retention(RetentionPolicy.SOURCE) // 只保留到编译阶段
@Target(ElementType.TYPE) // 可用于类, 接口..
public @interface VersionAnnotation {

	String value() default "0.0.1";

	String path() default "version";

}
