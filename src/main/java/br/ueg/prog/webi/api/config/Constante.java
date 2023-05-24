/*
 * Constante.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.config;

/**
 * Classe responsável por manter as constantes da aplicação.
 * 
 * @author UEG
 */
public final class Constante {

	/** JWT - Security */
	public static final String PARAM_TYPE = "type";
	public static final String PARAM_NAME = "nome";
	public static final String PARAM_EMAIL = "email";
	public static final String PARAM_LOGIN = "login";
	public static final String PARAM_ROLES = "roles";
	public static final String PARAM_LINK = "link";
	public static final String PARAM_DISABLED = "disabled";
	public static final String PARAM_ID_USUARIO = "idUsuario";
	public static final String PARAM_EXPIRES_IN = "expiresIn";
	public static final String PARAM_REFRESH_EXPIRES_IN = "refreshExpiresIn";
	public static final String PARAM_TIPO_REDEFINICAO_SENHA = "tipoRedefinicaoSenha";

	public static final Long PARAM_TIME_TOKEN_VALIDATION = 86400L;

	/** Authorization */
	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String HEADER_AUTHORIZATION_BEARER = "Bearer ";
	public static final Long NUMERO_MAXIMO_DIAS_SEM_ACESSO = 90L;

	/**
	 * Construtor privado para garantir o singleton.
	 */
	private Constante() {
	}
}
