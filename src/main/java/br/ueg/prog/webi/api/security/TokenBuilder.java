/*
 * TokenBuilder.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.security;

import br.ueg.prog.webi.api.config.Constante;
import br.ueg.prog.webi.api.util.Util;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Classe que proverá as chaves necessárias para o acesso a aplicação.
 * 
 * @author UEG
 */
public class TokenBuilder {

	private final Log logger = LogFactory.getLog(getClass());

	private JWTCreator.Builder builder;

	private final KeyToken keyToken;

	public enum TokenType {
		ACCESS, REFRESH, VALIDACAO
	}

	/**
	 * Construtor da classe.
	 * 
	 * @param keyToken -
	 */
	public TokenBuilder(final KeyToken keyToken) {
		this.keyToken = keyToken;
		this.builder = JWT.create();
		this.builder.withIssuedAt(new Date());
		this.builder.withIssuer(keyToken.getIssuer());
	}

	/**
	 * Construtor da classe.
	 * 
	 * @param secret -
	 * @param issuer -
	 */
	public TokenBuilder(final String secret, final String issuer) {
		this(new KeyToken(secret, issuer));
	}

	/**
	 * Adiciona o parâmetro que comporá o token JWT.
	 * 
	 * @param name -
	 * @param value -
	 * @return -
	 */
	public TokenBuilder addParam(final String name, final Long value) {
		builder.withClaim(name, value);
		return this;
	}

	/**
	 * Adiciona o parâmetro que comporá o token JWT.
	 * 
	 * @param name -
	 * @param value -
	 * @return -
	 */
	public TokenBuilder addParam(final String name, final String value) {
		builder.withClaim(name, value);
		return this;
	}

	/**
	 * Adiciona o parâmetro que comporá o token JWT.
	 * 
	 * @param name   -
	 * @param value -
	 * @return -
	 */
	public TokenBuilder addParam(final String name, final Boolean value) {
		builder.withClaim(name, value);
		return this;
	}

	/**
	 * Adiciona o 'Nome' do Usuário aos parâmetros.
	 * 
	 * @param nome -
	 * @return -
	 */
	public TokenBuilder addNome(final String nome) {
		builder.withClaim(Constante.PARAM_NAME, nome);
		return this;
	}

	/**
	 * Adiciona o 'Login' do Usuário aos parâmetros.
	 * 
	 * @param login -
	 * @return -
	 */
	public TokenBuilder addLogin(final String login) {
		builder.withClaim(Constante.PARAM_LOGIN, login);
		return this;
	}

	/**
	 * Adiciona o 'Roles' do Usuário aos parâmetros.
	 * 
	 * @param roles -
	 * @return -
	 */
	public TokenBuilder addRoles(final List<String> roles) {
		builder.withArrayClaim(Constante.PARAM_ROLES, roles.toArray(new String[roles.size()]));
		return this;
	}

	/**
	 * Retorna o 'Token' gerado segundo a especificação JWT - Json Web Tokens.
	 * 
	 * @param expiry -
	 * @return -
	 */
	public JWTToken buildRefresh(final Long expiry) {

		if (expiry == null) {
			throw new IllegalArgumentException("O parâmetro 'expiry' deve ser especificado.");
		}
		Date expiresAt = getExpiresAt(expiry);
		builder.withExpiresAt(expiresAt);

		builder.withClaim(Constante.PARAM_TYPE, TokenType.REFRESH.toString());
		Algorithm algorithm = Algorithm.HMAC256(keyToken.getSecret());

		String token = builder.sign(algorithm);
		return new JWTToken(token, expiry);
	}

	/**
	 * Retorna o 'Token' gerado segundo a especificação JWT - Json Web Tokens.
	 * 
	 * @param expiry -
	 * @return -
	 */
	public JWTToken buildAccess(final Long expiry) {

		if (expiry == null) {
			throw new IllegalArgumentException("O parâmetro 'expiry' deve ser especificado.");
		}
		Date expiresAt = getExpiresAt(expiry);
		builder.withExpiresAt(expiresAt);

		builder.withClaim(Constante.PARAM_TYPE, TokenType.ACCESS.toString());
		Algorithm algorithm = Algorithm.HMAC256(keyToken.getSecret());

		String token = builder.sign(algorithm);
		return new JWTToken(token, expiry);
	}
	
	/**
	 * Retorna o 'Token' gerado segundo a especificação JWT - Json Web Tokens.
	 * 
	 * @param expiry -
	 * @return -
	 */
	public JWTToken buildValidation(final Long expiry) {
		if (expiry == null) {
			throw new IllegalArgumentException("O parâmetro 'expiry' deve ser especificado.");
		}
		Date expiresAt = getExpiresAt(expiry);
		builder.withExpiresAt(expiresAt);
		
		builder.withClaim(Constante.PARAM_TYPE, TokenType.VALIDACAO.toString());
		Algorithm algorithm = Algorithm.HMAC256(keyToken.getSecret());
		
		String token = builder.sign(algorithm);
		return new JWTToken(token, expiry);
	}

	/**
	 * Retorna os 'Parâmetros' associados ao Token JWT.
	 * 
	 * @param token -
	 * @return -
	 */
	public Map<String, Claim> getClaims(final String token) {

		if (Util.isEmpty(token)) {
			throw new IllegalArgumentException("O parâmetro 'token' deve ser especificado.");
		}

		try {
			Algorithm algorithm = Algorithm.HMAC256(keyToken.getSecret());
			JWTVerifier verifier = JWT.require(algorithm).withIssuer(keyToken.getIssuer()).build();
			DecodedJWT jwt = verifier.verify(token);
			return jwt.getClaims();
		} catch (JWTVerificationException e) {
			logger.warn("Token Invalido!", e);
			return null;
		}
	}

	/**
	 * Retorna a instância {@link Date} referente a expiração do 'Token'.
	 * 
	 * @param expiry -
	 * @return -
	 */
	private Date getExpiresAt(final Long expiry) {
		LocalDateTime current = LocalDateTime.now().plusSeconds(expiry);
		return Date.from(current.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Representação de Token JWT.
	 * 
	 * @author UEG
	 */
	public class JWTToken implements Serializable {

		private static final long serialVersionUID = 386422358555303425L;

		private String token;
		private Long expiresIn;

		/**
		 * Construtor da classe.
		 * 
		 * @param token -
		 * @param expiresIn -
		 */
		public JWTToken(final String token, final Long expiresIn) {
			this.token = token;
			this.expiresIn = expiresIn;
		}

		/**
		 * @return the token
		 */
		public String getToken() {
			return token;
		}

		/**
		 * @param token the token to set
		 */
		public void setToken(String token) {
			this.token = token;
		}

		/**
		 * @return the expiresIn
		 */
		public Long getExpiresIn() {
			return expiresIn;
		}

		/**
		 * @param expiresIn the expiresIn to set
		 */
		public void setExpiresIn(Long expiresIn) {
			this.expiresIn = expiresIn;
		}
	}
}
