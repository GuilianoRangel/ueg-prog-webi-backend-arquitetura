/*
 * KeyToken.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.security;

import br.ueg.prog.webi.api.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Classe que representa a chave JWT - Json Web Tokens, necessária na geração,
 * validação da chave e recuperação dos dados contidos na chave.
 * 
 * @author UEG
 */
@Component
public class KeyToken {

	@Value("${app.api.security.jwt.secret:defaultSecretKeyGuard}")
	private String secret;

	@Value("${app.api.security.jwt.issuer:ProjetoUeg}")
	private String issuer;

	/**
	 * Construtor da classe.
	 */
	public KeyToken() {

	}

	/**
	 * Construtor da classe.
	 * 
	 * @param secret -
	 * @param issuer -
	 */
	public KeyToken(final String secret, final String issuer) {

		if (Util.isEmpty(secret) || Util.isEmpty(issuer)) {
			throw new IllegalArgumentException("Os parâmetros do construtor devem ser especificados.");
		}
		this.issuer = issuer;
		this.secret = secret;
	}

	/**
	 * @return the secret
	 */
	public byte[] getSecret() {
		return secret.getBytes();
	}

	/**
	 * @return the issuer
	 */
	public String getIssuer() {
		return issuer;
	}
}
