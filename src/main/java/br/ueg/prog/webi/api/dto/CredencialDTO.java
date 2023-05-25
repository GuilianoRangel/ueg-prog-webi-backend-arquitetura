/* 	 
 * CredencialTO.java  
 * Copyright UEG.
 *
 */
package br.ueg.prog.webi.api.dto;

import br.ueg.prog.webi.api.security.Credential;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Classe de transferência responsável por representar a Credencial do Usuário.
 *
 * @author UEG
 */
@JsonInclude(Include.NON_NULL)
@Schema(description = "Representação de Credencial do Usuário")
@Builder
public @Data class CredencialDTO implements Serializable, Credential {

	private static final long serialVersionUID = 7616722014159043532L;

	@Schema(description = "Id do Usuário")
	private Long id;

	@Schema(description = "Nome do Usuário")
	private String nome;

	@Schema(description = "Login do Usuário")
	private String login;

	@Schema(description = "Email do Usário")
	private String email;

	@Schema(description = "Lista de permissões do Usuário")
	private List<String> roles;

	@Schema(description = "Token de acesso")
	private String accessToken;

	@Schema(description = "Tempo de expiração do token de acesso")
	private Long expiresIn;

	@Schema(description = "Token de refresh")
	private String refreshToken;

	@Schema(description = "Tempo de expiração do token de refresh")
	private Long refreshExpiresIn;

	@Schema(description = "Indica se o usuário está ativo")
	private boolean statusAtivo;

	@Schema(description = "Senha do usuário")
	@Hidden
	private String senha;

}
