/*
 * Util.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.util;

import br.com.caelum.stella.format.CNPJFormatter;
import br.com.caelum.stella.format.CPFFormatter;
import br.com.caelum.stella.validation.CNPJValidator;
import br.com.caelum.stella.validation.CPFValidator;
import br.com.caelum.stella.validation.InvalidStateException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Classe utilitária padrão referente as aplicações UEG.
 * 
 * @author UEG
 */
public final class Util {

	public static final Long TAMANHO_CPF = 11L;

	private static final String IE_ZERO = "00000000000";

	private static final String CPF_ZERO = "00000000000";

	private static final String CNPJ_ZERO = "00000000000000";

	/**
	 * Construtor privado para garantir o singleton.
	 */
	private Util() {

	}

	/**
	 * Verifica se o valor informado está vazio.
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isEmpty(final String value) {
		return StringUtils.isEmpty(value);
	}

	/**
	 * Verifica se o CPF informado é válido.
	 * 
	 * @param cpf
	 * @return
	 */
	public static boolean isCpfValido(final String cpf) {
		boolean valido = Boolean.TRUE;

		try {
			new CPFValidator(Boolean.FALSE).assertValid(cpf);

			if (CPF_ZERO.equals(cpf)) {
				valido = Boolean.FALSE;
			}
		} catch (InvalidStateException e) {
			valido = Boolean.FALSE;
		}
		return valido;
	}

	/**
	 * Verifica se o e-mail informado é válido.
	 * 
	 * @param mail
	 * @return
	 */
	public static boolean isValidEmail(final String mail) {
		boolean valid = Boolean.TRUE;

		try {
			new InternetAddress(mail).validate();
		} catch (AddressException e) {
			valid = Boolean.FALSE;
		}
		return valid;
	}

	/**
	 * Verifica se o CNPJ informado é válido.
	 * 
	 * @param cnpj
	 * @return
	 */
	public static boolean isCnpjValido(final String cnpj) {
		boolean valido = Boolean.TRUE;

		try {
			new CNPJValidator(Boolean.FALSE).assertValid(cnpj);

			if (CNPJ_ZERO.equals(cnpj)) {
				valido = Boolean.FALSE;
			}
		} catch (InvalidStateException e) {
			valido = Boolean.FALSE;
		}
		return valido;
	}

	/**
	 * Retorna o CPF formatado considrando o seguinte padrão '000.000.000-00'.
	 * 
	 * @param cpf
	 * @return
	 */
	public static String getCpfFormatado(final String cpf) {
		return new CPFFormatter().format(cpf);
	}

	/**
	 * Retorna o CPF formatado considrando o seguinte padrão '00.000.000/0000-00'.
	 * 
	 * @param cnpj -
	 * @return -
	 */
	public static String getCnpjFormatado(final String cnpj) {
		return new CNPJFormatter().format(cnpj);
	}

	/**
	 * Retorna o CPF/CNPJ considerando sua mascara padrão.
	 * 
	 * @param cpfCnpj
	 * @return
	 */
	public static String getCpfCnpjFormatado(final String cpfCnpj) {
		String cpfCnpjFormatado = null;

		if (!isEmpty(cpfCnpj)) {
			cpfCnpjFormatado = Util.removerCaracteresNaoNumericos(cpfCnpj);

			if (cpfCnpjFormatado.length() == TAMANHO_CPF) {
				cpfCnpjFormatado = Util.getCpfFormatado(cpfCnpjFormatado);
			} else {
				cpfCnpjFormatado = Util.getCnpjFormatado(cpfCnpjFormatado);
			}
		}
		return cpfCnpjFormatado;
	}

	/**
	 * Remove os caracteres não numéricos do 'valor' informado.
	 * 
	 * @param valor
	 * @return
	 */
	public static String removerCaracteresNaoNumericos(String valor) {

		if (!Util.isEmpty(valor)) {
			valor = valor.replaceAll("[^\\d]", "");
		}
		return valor;
	}

	/**
	 * Retorna a string com os dados do array de objetos concatenados conforme o
	 * separador informado.
	 * 
	 * @param separador
	 * @param parametros
	 * @return
	 */
	public static String getValorConcatenado(final String separador, Object... parametros) {
		return Util.getValorConcatenado(separador, Arrays.asList(parametros));
	}

	/**
	 * Retorna a string com os dados da {@link List} concatenados conforme o
	 * separador informado.
	 * 
	 * @param separador
	 * @param parametros
	 * @return
	 */
	public static String getValorConcatenado(final String separador, final List<Object> parametros) {
		StringBuilder build = new StringBuilder();
		Iterator<?> iterator = parametros.iterator();

		while (iterator.hasNext()) {
			Object valor = iterator.next().toString();
			build.append(valor);

			if (iterator.hasNext()) {
				build.append(separador);
			}
		}
		return build.toString();
	}

	/**
	 * Retorna o e-mail obfuscado no seguinte modelo 3 primeiras
	 * letras antes do @ e demais obfuscadas com ***
	 * 
	 * @param email
	 * @return
	 */
	public static String getEmailObfuscado(final String email) {
		return email.replaceAll("(^[^@]{3}|(?!^)\\G)[^@]", "$1*");
	}

	/**
	 * Retorna o map serializado no formato json.
	 * 
	 * @param data
	 * @return
	 */
	public static String toJson(final Map<String, Object> data) {
		String json = "";

		try {
			json = new ObjectMapper().writeValueAsString(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return json;
	}
}
