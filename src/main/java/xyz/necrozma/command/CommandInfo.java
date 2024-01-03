package xyz.necrozma.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {
    String name();
    String description() default "";
    String usage() default "";
    String[] aliases() default "";
}
