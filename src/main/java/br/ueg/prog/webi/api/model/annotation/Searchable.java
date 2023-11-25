package br.ueg.prog.webi.api.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface Searchable {
    String label() default "";
    boolean listEntityValues() default false;
    boolean autoComplete() default false;
}
