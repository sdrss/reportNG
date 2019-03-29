package org.uncommons.reportng.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.CONSTRUCTOR, ElementType.TYPE })
public @interface NewFeature {
	String description() default "Please set a description for the new feature like: @Feature(description = \"Test this awesome new feature\")";
}
