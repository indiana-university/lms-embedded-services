package edu.iu.uits.lms.variablereplacement;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Add this annotation to an {@code @Configuration} class to expose the
 * {@link edu.iu.uits.lms.common.variablereplacement.VariableReplacementService} as a bean.
 * @since 4.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(VariableReplacementConfig.class)
@Configuration(proxyBeanMethods = false)
public @interface EnableVariableReplacementService {

}
