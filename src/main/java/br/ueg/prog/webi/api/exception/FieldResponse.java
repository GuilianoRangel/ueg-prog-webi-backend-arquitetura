/*
 * FieldResponse.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * Classe de representação de Atributos de Resposta utilizada nas implementações
 * de 'ExceptionHandler'
 * 
 * @author UEG
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldResponse implements Serializable {

	private static final long serialVersionUID = -807504480597471148L;

	@Schema(description = "Nome do atributo")
	private String attribute;

	@Schema(description = "Descrição da validação")
	private String description;

	/**
	 * Construtor da classe.
	 */
	public FieldResponse() {

	}

	/**
	 * Construtor da classe.
	 * 
	 * @param attribute -
	 * @param description -
	 */
	public FieldResponse(final String attribute, final String description) {
		this.attribute = attribute;
		this.description = description;
	}

	/**
	 * @return the attribute
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * @param attribute the attribute to set
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
