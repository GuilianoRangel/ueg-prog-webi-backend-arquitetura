/*
 * JwtAuthenticationFilter.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.security;

import br.ueg.prog.webi.api.util.CollectionUtil;
import br.ueg.prog.webi.api.util.Util;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Filtro que verifica as credenciais de autenticação do usuário.
 * 
 * @author UEG.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static final String AUTH_HEADER = "Authorization";
	public static final String BEARER_PREFIX = "Bearer ";

	private final AuthenticationProvider authenticationProvider;
	private final String urlAuthController;

	/**
	 * Construtor da classe.
	 *
	 * @param authenticationProvider - provider de autenticação
	 * @param urlAuthController
	 */
	public JwtAuthenticationFilter(AuthenticationProvider authenticationProvider, String urlAuthController) {
		this.authenticationProvider = authenticationProvider;
		this.urlAuthController = urlAuthController;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doFilterInternal(final HttpServletRequest  servletRequest, final HttpServletResponse servletResponse,
									FilterChain chain) throws ServletException, IOException {
		if(!servletRequest.getRequestURI().contains(this.urlAuthController)) {
			final String token = getAccessToken(servletRequest);

			if (!Util.isEmpty(token) && isTokenBearer(servletRequest)) {
				Credential credential = null;
				credential = authenticationProvider.getAuthentication(token);
				Authentication authentication = getAuthentication(credential);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		};
		chain.doFilter(servletRequest, servletResponse);
	}

	/**
	 * Returns the instance {@link Authentication} by {@link Credential}.
	 * 
	 * @param credential -
	 * @return -
	 */
	private Authentication getAuthentication(final Credential credential) {
		Authentication authentication;

		if (credential == null) {
			authentication = new UsernamePasswordAuthenticationToken(null, null);
		} else {
			List<GrantedAuthority> grantedAuthorities = getGrantedAuthorities(credential);
			authentication = new UsernamePasswordAuthenticationToken(credential.getLogin(), credential,
					grantedAuthorities);
		}
		return authentication;
	}

	/**
	 * Returns the list of Granted Authorities according to the entered Credential.
	 * 
	 * @param credential -
	 * @return -
	 */
	private List<GrantedAuthority> getGrantedAuthorities(final Credential credential) {
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

		if (!CollectionUtil.isEmpty(credential.getRoles())) {
			credential.getRoles().forEach(role -> grantedAuthorities.add(new SimpleGrantedAuthority(role)));
		}
		return grantedAuthorities;
	}

	/**
	 * Retorna o token de acesso recuperados da instância
	 * {@link HttpServletRequest}.
	 * 
	 * @return -
	 */
	private String getAccessToken(final HttpServletRequest request) {
		String accessToken = null;

		if (request != null) {
			accessToken = request.getHeader(AUTH_HEADER);

			if (!Util.isEmpty(accessToken)) {
				accessToken = accessToken.replaceAll(BEARER_PREFIX, "").trim();
			}
		}
		return accessToken;
	}

	/**
	 * Verifica se o token de acesso da request é 'Bearer'.
	 * 
	 * @param request -
	 * @return -
	 */
	private boolean isTokenBearer(final HttpServletRequest request) {
		boolean valid = Boolean.FALSE;

		if (request != null) {
			String accessToken = request.getHeader(AUTH_HEADER);
			valid = !Util.isEmpty(accessToken) && accessToken.trim().startsWith(BEARER_PREFIX);
		}
		return valid;
	}

}
