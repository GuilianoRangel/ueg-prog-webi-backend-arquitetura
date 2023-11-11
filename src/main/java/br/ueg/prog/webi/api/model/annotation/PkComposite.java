package br.ueg.prog.webi.api.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Anotação para ser adicionada em classes de PK composta para reflexão idenificar.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface PkComposite {
}
