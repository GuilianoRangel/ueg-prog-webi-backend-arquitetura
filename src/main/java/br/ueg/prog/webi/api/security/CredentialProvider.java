/*
 * CredentialProvider.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Classe Provider responsável por encapsular a complexidade na recuperação da
 * {@link Credential} do Usuário.
 *
 * @author UEG
 */
@Component
public class CredentialProvider implements ComumCredentialProvider {

	/**
	 * Fabrica de instância {@link CredentialProvider}.
	 * 
	 * @return
	 */
	public static CredentialProvider newInstance() {
		return new CredentialProvider();
	}

	/**
	 * Retorna a instância corrente de {@link Credential}.
	 * 
	 * @return
	 */
	@Override
	public Credential getCurrentInstance() {
		Credential credential = null;
		Object security = SecurityContextHolder.getContext().getAuthentication().getCredentials();

		if (security instanceof Credential) {
			credential = Credential.class.cast(security);
		}
		return credential;
	}

	/**
	 * Retorna a instância corrente de {@link Credential}.
	 * 
	 * @param <T>
	 * 
	 * @return
	 */
	public <T> T getCurrentInstance(Class<T> clazz) {
		Credential credential = getCurrentInstance();
		return credential != null ? clazz.cast(credential) : null;
	}
}
