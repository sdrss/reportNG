package org.uncommons.reportng.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface KnownDefect {
	String description() default "Please set the reason for the expected failure like: @KnownDefect(description = \"JIRA-123\")";
}
