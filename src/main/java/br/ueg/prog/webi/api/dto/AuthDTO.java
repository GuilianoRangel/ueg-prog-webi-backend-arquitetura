/* 	 
 * AuthTO.java  
 * Copyright UEG.
 *
 */
package br.ueg.prog.webi.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Classe de transferência referente aos dados de autenticação.
 *
 * @author UEG
 */
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Entidade de transferência de dados de Autenticação")
public @Data class AuthDTO implements Serializable {

	private static final long serialVersionUID = 5374096682432769206L;

	@Schema(description = "Login do Usuário", required = true)
	private String login;

	@Schema(description = "Senha do Usuário", required = true)
	private String senha;

}
