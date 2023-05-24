/*
 * BusinessException.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.exception;

import br.ueg.prog.webi.api.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Exceção a ser lançada na ocorrência de falhas no fluxo de negócio.
 * 
 * @author UEG
 */
public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 7986864620634914985L;

	private boolean concat;
	private MessageCode code;
	private Object[] parameters;
	private MessageResponse response;

	/**
	 * Construtor da classe.
	 * 
	 * @param code - código do erro
	 * @param concat - concatenar mensagen
	 * @param parameters - parametros do erro
	 */
	public BusinessException(final MessageCode code, Boolean concat, final Object... parameters) {
		this.code = code;
		this.concat = concat;
		this.parameters = parameters;
	}

	/**
	 * Construtor da classe.
	 *
	 * @param code -
	 * @param parameters -
	 */
	public BusinessException(final MessageCode code, final Object... parameters) {
		this(code, Boolean.TRUE, parameters);
	}

	/**
	 * Construtor da classe.
	 *
	 * @param code -
	 */
	public BusinessException(final MessageCode code) {
		this.code = code;
	}

	/**
	 * Construtor da classe.
	 *
	 * @param e -
	 */
	public BusinessException(final Throwable e) {
		super(e);
	}

	/**
	 * Construtor a classe.
	 * 
	 * @param response -
	 */
	public BusinessException(MessageResponse response) {
		this.response = response;
	}

	/**
	 * @see Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		String message = super.getMessage();

		if (Util.isEmpty(super.getMessage())) {
			List<String> params = new ArrayList<>();

			if (code != null) {
				params.add("code: " + code);
			}

			params.add("concat: " + concat);

			if (hasParameters()) {
				String paramsConcat = Util.getValorConcatenado(", ", parameters);
				params.add("parameters: [" + paramsConcat + "]");
			}

			message = "{";
			message += Util.getValorConcatenado(", ", params.toArray());
			message += "}";
		}

		return message;
	}

	/**
	 * @return the response
	 */
	public MessageResponse getResponse() {
		return response;
	}

	/**
	 * @return the concat
	 */
	public boolean isConcat() {
		return concat;
	}

	/**
	 * @return the code
	 */
	public MessageCode getCode() {
		return code;
	}

	/**
	 * @return the parameters
	 */
	public Object[] getParameters() {
		return parameters;
	}

	/**
	 * @return the hasParameters
	 */
	public boolean hasParameters() {
		return parameters != null && parameters.length > 0;
	}

}
