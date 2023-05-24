/*
 * MessageCode.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.exception;

/**
 * Interface responsável por definir o contrato da instância que conterá o
 * código da Mensagem.
 * 
 * @author UEG
 */
public interface MessageCode {

	/**
	 * Retorna o código da mensagem disponível no *.properties de mensagem.
	 * 
	 * @return -
	 */
	String getCode();

	/**
	 * Retorna o Status HTTP referente a mensagem.
	 * 
	 * @return -
	 */
	Integer getStatus();
}
