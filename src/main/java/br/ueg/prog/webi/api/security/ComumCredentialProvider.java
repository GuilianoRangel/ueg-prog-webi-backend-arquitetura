/*
 * ComumCredentialProvider.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.security;

/**
 * Classe Provider responsável por encapsular a complexidade na recuperação da
 * {@link Credential} do Usuário.
 *
 * @author UEG
 */
public interface ComumCredentialProvider {

	/**
	 * Retorna a instância corrente de {@link Credential}.
	 * 
	 * @return
	 */
	public Credential getCurrentInstance();
}
