/*
 * Util.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.util;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Classe utilitária para manipulação de coleções.
 * 
 * @author UEG
 */
public final class CollectionUtil {

	private final static String DEFAULT_SEPARATOR = ",";

	/**
	 * Construtor privado para garantir o Singleton.
	 */
	private CollectionUtil() {

	}

	/**
	 * Verifica se a {@link Collection} informada é nula, ou vazia.
	 * 
	 * @param collection
	 * @return
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return CollectionUtils.isEmpty(collection);
	}

	/**
	 * Implementação generica para remover objetos duplicados a partir de um
	 * atribudo da entidade informada.
	 * 
	 * @param extractor
	 * @return
	 */
	public static <T> Predicate<T> distinctPorAtributo(Function<? super T, Object> extractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return object -> map.putIfAbsent(extractor.apply(object), Boolean.TRUE) == null;
	}

	/**
	 * Returns the concatenated informed collection, considering the separator.
	 *
	 * @param collection
	 * @param separator
	 * @return
	 */
	public static String getCollectionAsString(final Collection<?> collection, final String separator) {
		StringBuilder builder = new StringBuilder();

		if (collection != null && !collection.isEmpty()) {
			Iterator<?> iterator = collection.iterator();

			while (iterator.hasNext()) {
				builder.append(iterator.next());

				if (iterator.hasNext()) {
					builder.append(separator.trim()).append(" ");
				}
			}
		}
		return builder.toString();
	}

	/**
	 * Returns the concatenated informed collection, considering the separator.
	 * 
	 * @param collection
	 * @return
	 */
	public static String getCollectionAsString(final Collection<?> collection) {
		return getCollectionAsString(collection, DEFAULT_SEPARATOR);
	}

}
