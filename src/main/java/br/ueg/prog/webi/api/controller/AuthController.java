/*
 * AuthController.java  
 * Copyright UEG.
 *
 */
package br.ueg.prog.webi.api.controller;

import br.ueg.prog.webi.api.dto.AuthDTO;
import br.ueg.prog.webi.api.dto.CredencialDTO;
import br.ueg.prog.webi.api.dto.UsuarioSenhaDTO;
import br.ueg.prog.webi.api.exception.MessageResponse;
import br.ueg.prog.webi.api.service.AuthService;
import br.ueg.prog.webi.api.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Classe de controle de Autenticação de Usuário.
 * 
 * @author UEG
 */
@RestController
@Tag(name = "Auth API")
@RequestMapping("${app.api.security.url-auth-controller:${app.api.base}/auth}")
public class AuthController extends AbstractController {

	@Autowired
	private AuthService authService;


	/**
	 * Autentica o Usuário informado através dos parâmetros informados.
	 * 
	 * @param authTO
	 * @return
	 */
	@Operation(description = "Concede o token de acesso ao Usuário através do 'login' e 'senha'.",
			responses = {
				@ApiResponse(responseCode = "200", description = "Success",
						content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
								array = @ArraySchema(schema = @Schema(implementation = CredencialDTO.class)))),
				@ApiResponse(responseCode = "403", description = "Proibido",
						content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
								array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class)))),
				@ApiResponse(responseCode = "400", description = "Bad Request",
						content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
								array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class))))
	})
	@PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> login(@Parameter(description = "Informações de Autenticação", required = true) @Valid @RequestBody final AuthDTO authTO)  {
		CredencialDTO credencialTO = authService.login(authTO);
		return ResponseEntity.ok(credencialTO);
	}

	/**
	 * Concede um novo token de acesso conforme o token de refresh informado.
	 * 
	 * @param refreshToken
	 * @return
	 */
	@Operation(description = "Concede um novo token de acesso conforme o token de refresh informado.",
	responses = {
			@ApiResponse(responseCode = "200", description = "Success",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							array = @ArraySchema(schema = @Schema(implementation = CredencialDTO.class)))),
			@ApiResponse(responseCode = "403", description = "Proibido",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class)))),
			@ApiResponse(responseCode = "400", description = "Bad Request",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class))))
	})
	@GetMapping(path = "/refresh", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> refresh(
			@Parameter(description = "Token de refresh", required = true) @RequestParam() final String refreshToken) {
		CredencialDTO credencialTO = authService.refresh(refreshToken);
		return ResponseEntity.ok(credencialTO);
	}

	/**
	 * Retorna as informações do Usuário conforme o 'token' informado.
	 * 
	 * @param accessToken -
	 * @return -
	 */
	@Operation(description = "Recupera as informações do Usuário conforme o token informado.",
			responses = {
			@ApiResponse(responseCode = "200", description = "Success",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							array = @ArraySchema(schema = @Schema(implementation = CredencialDTO.class)))),
			@ApiResponse(responseCode = "403", description = "Proibido",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class)))),
			@ApiResponse(responseCode = "400", description = "Bad Request",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class))))
	})
	@GetMapping(path = "/info", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> getInfoByToken(
			@Parameter(description = "Token", required = true) @RequestHeader( name = "Authorization") final String accessToken) {
		CredencialDTO credencialTO = authService.getInfoByToken(accessToken);
		return ResponseEntity.ok(credencialTO);
	}

	/**
	 * Realiza a inclusão ou alteração de senha do {@link CredencialDTO}
	 * 
	 * @param tokenParam
	 * @param tokenHeader
	 * @param usuarioSenhaTO
	 * @return
	 */
	@Operation(description = "Inclusão ou alteração a senha do usuário.",
			responses = {
				@ApiResponse(responseCode = "200", description = "Success",
						content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
								array = @ArraySchema(schema = @Schema(implementation = CredencialDTO.class)))),
				@ApiResponse(responseCode = "400", description = "Bad Request",
						content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
								array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class))))
	})
	@PutMapping(path = "/senha", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> redefinirSenha(
			@Parameter(description = "Request Token") @RequestParam(name = "requestToken", required = false) final String tokenParam,
			@Parameter(description = "Request Token") @RequestHeader(name = "Request-Token", required = false) final String tokenHeader,
			@Parameter(description = "Informações da Redefinição de Senha", required = true) @Valid @RequestBody UsuarioSenhaDTO usuarioSenhaTO) {
		ResponseEntity<?> response = null;
		final String token = Util.isEmpty(tokenParam) ? tokenHeader : tokenParam;

		if (Util.isEmpty(token)) {
			response = ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		} else {
			CredencialDTO credencialTO = authService.redefinirSenha(usuarioSenhaTO, token);
			response = ResponseEntity.ok(credencialTO);
		}
		return response;
	}
	
	/**
	 * Realiza a solicitação de recuperar a senha do {@link CredencialDTO}
	 * 
	 * @param email -
	 * @return -
	 */
	@Operation(description = "Realiza a solicitação de recuperar a senha do usuário.",
			responses = {
				@ApiResponse(responseCode = "200", description = "Success",
						content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
								array = @ArraySchema(schema = @Schema(implementation = CredencialDTO.class)))),
				@ApiResponse(responseCode = "400", description = "Bad Request",
						content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
								array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class))))
	})
	@GetMapping(path = "/senha/solicitacao/{email}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> recuperarSenha(
			@Parameter(description = "EMail do Usuário", required = true) @PathVariable() final String email) {
		CredencialDTO credential = userProviderService.getCredentialByEmail(email);
		return ResponseEntity.ok(new UsuarioSenhaDTO(email));
	}

	/**
	 * Valida o token de alteração de senha.
	 * 
	 * @param tokenParam -
	 * @param tokenHeader -
	 * @return
	 */
	@Operation(description = "Valida o token de alteração de senha.",
			responses = {
			@ApiResponse(responseCode = "200", description = "Success",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							array = @ArraySchema(schema = @Schema(implementation = Boolean.class)))),
			@ApiResponse(responseCode = "403", description = "Proibido",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class)))),
			@ApiResponse(responseCode = "400", description = "Bad Request",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class))))
	})
	@GetMapping(path = "/senha/solicitacao/info", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> getInfoByTokenValidacao(
			@Parameter(description = "Request Token") @RequestParam(name = "requestToken", required = false) final String tokenParam,
			@Parameter(description = "Request Token") @RequestHeader(name = "Request-Token", required = false) final String tokenHeader) {
		final String token = Util.isEmpty(tokenParam) ? tokenHeader : tokenParam;
		boolean valido = authService.getInfoByTokenValidacao(token);
		return ResponseEntity.ok(valido);
	}
}
