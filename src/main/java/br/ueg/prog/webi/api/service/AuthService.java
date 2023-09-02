/*
 * AuthService.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.service;

import br.ueg.prog.webi.api.config.Constante;
import br.ueg.prog.webi.api.dto.AuthDTO;
import br.ueg.prog.webi.api.dto.CredencialDTO;
import br.ueg.prog.webi.api.dto.UsuarioSenhaDTO;
import br.ueg.prog.webi.api.exception.ApiMessageCode;
import br.ueg.prog.webi.api.exception.BusinessException;
import br.ueg.prog.webi.api.security.KeyToken;
import br.ueg.prog.webi.api.security.TokenBuilder;
import br.ueg.prog.webi.api.security.TokenBuilder.JWTToken;
import br.ueg.prog.webi.api.util.Util;
import com.auth0.jwt.interfaces.Claim;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class de serviço responsável por prover as logicas de negócio referente a
 * Autenticação/Autorização.
 *
 * @author UEG
 */
@Component
public class AuthService {

    @Autowired
    private KeyToken keyToken;

    @Autowired
    private UserProviderService userProviderService;

    @Value("${app.api.security.jwt.token-expire-in:600}")
    private Long tokenExpireIn;

    @Value("${app.api.security.jwt.token-refresh-in:600}")
    private Long tokenRefreshExpireIn;

    /**
     * Autentica o Usuário concede um token de acesso temporário.
     *
     * @param authDTO -
     * @return -
     */
    public CredencialDTO login(final AuthDTO authDTO) {
        return loginAccess(authDTO);
    }

    public static Boolean loginByPassword(CredencialDTO usuario, AuthDTO authDTO) {
        return UserPasswordService.loginByPassword(usuario, authDTO);
    }

    /**
     * Autentica o Usuário informado através do 'login' e 'senha' e concede um token
     * de acesso temporário.
     *
     * @param authDTO -
     * @return -
     */
    public CredencialDTO loginAccess(final AuthDTO authDTO) {
        CredencialDTO credencialDTO = null;

        validarCamposObrigatoriosLoginAcesso(authDTO);

        CredencialDTO usuario = userProviderService.getCredentialByLogin(authDTO.getLogin());
        validarUsuarioLogin(usuario);

        if (!loginByPassword(usuario, authDTO)) {
            throw new BusinessException(ApiMessageCode.ERRO_USUARIO_SENHA_NAO_CONFEREM);
        }

        credencialDTO = usuario;

        TokenBuilder builder = new TokenBuilder(keyToken);
        builder.addNome(usuario.getNome());
        builder.addLogin(usuario.getLogin());
        builder.addParam(Constante.PARAM_EMAIL, usuario.getEmail());
        builder.addParam(Constante.PARAM_ID_USUARIO, usuario.getId());
        builder.addParam(Constante.PARAM_EXPIRES_IN, tokenExpireIn);
        builder.addParam(Constante.PARAM_REFRESH_EXPIRES_IN, tokenRefreshExpireIn);

        List<String> roles = null;

        roles = usuario.getRoles();

        JWTToken accessToken = builder.buildAccess(tokenExpireIn);
        credencialDTO.setExpiresIn(accessToken.getExpiresIn());
        credencialDTO.setAccessToken(accessToken.getToken());

        JWTToken refreshToken = builder.buildRefresh(tokenRefreshExpireIn);
        credencialDTO.setRefreshExpiresIn(refreshToken.getExpiresIn());
        credencialDTO.setRefreshToken(refreshToken.getToken());
        credencialDTO.setRoles(roles);


        registerCredentialInSecurityContext(credencialDTO);
        credencialDTO.setSenha(null);

        return credencialDTO;
    }

    /**
     * Registra a credencial que acabou de fazer login no Contexto de segurança
     * Motivação: criado para poder registrar auditoria das alterações realizadas na entidade
     * de usuários durante o login.
     *
     * @param credencialDTO -
     */
    private void registerCredentialInSecurityContext(CredencialDTO credencialDTO) {
        //Cria instancia da autenticação para ter informações para a auditoria
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(credencialDTO.getLogin(), credencialDTO);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    /**
     * Gera um novo token de acesso atráves do refresh token informado.
     *
     * @param refreshToken -
     * @return -
     */
    public CredencialDTO refresh(final String refreshToken) {
        AuthClaimResolve resolve = getClaimResolve(refreshToken);
        TokenBuilder builder = new TokenBuilder(keyToken);

        if (!resolve.isTokenTypeRefresh()) {
            throw new BusinessException(ApiMessageCode.ERRO_TOKEN_INVALIDO);
        }

        List<String> roles = null;
        CredencialDTO credencialDTO = userProviderService.getCredentialByLogin(resolve.getLogin());


        roles = Objects.nonNull(credencialDTO) ? credencialDTO.getRoles() : Arrays.asList();


        credencialDTO.setNome(resolve.getNome());
        credencialDTO.setEmail(resolve.getEmail());
        credencialDTO.setLogin(resolve.getLogin());
        credencialDTO.setId(resolve.getIdUsuario());

        if (resolve.getIdUsuario() != null) {
            builder.addNome(resolve.getNome());
            builder.addLogin(resolve.getLogin());
            builder.addParam(Constante.PARAM_EMAIL, resolve.getEmail());
            builder.addParam(Constante.PARAM_ID_USUARIO, resolve.getIdUsuario());
        }

        Long expiresIn = resolve.getExpiresIn();
        builder.addParam(Constante.PARAM_EXPIRES_IN, expiresIn);

        Long refreshExpiresIn = resolve.getRefreshExpiresIn();
        builder.addParam(Constante.PARAM_REFRESH_EXPIRES_IN, refreshExpiresIn);

        JWTToken accessToken = builder.buildAccess(expiresIn);
        credencialDTO.setExpiresIn(accessToken.getExpiresIn());
        credencialDTO.setAccessToken(accessToken.getToken());

        JWTToken newRefreshToken = builder.buildRefresh(refreshExpiresIn);
        credencialDTO.setRefreshExpiresIn(newRefreshToken.getExpiresIn());
        credencialDTO.setRefreshToken(newRefreshToken.getToken());
        credencialDTO.setRoles(roles);
        return credencialDTO;
    }

    /**
     * Retorna as informações do {@link CredencialDTO} conforme o 'token' informado.
     *
     * @param token -
     * @return -
     */
    public CredencialDTO getInfoByToken(final String token) {
        AuthClaimResolve resolve = getClaimResolve(token);

        if (!resolve.isTokenTypeAccess()) {
            throw new BusinessException(ApiMessageCode.ERRO_TOKEN_INVALIDO);
        }

        List<String> roles = null;
        CredencialDTO credencialDTO = userProviderService.getCredentialByLogin(resolve.getLogin());

        // TODO verificar se vai fucnionar com o login
        roles = Objects.nonNull(credencialDTO) ? credencialDTO.getRoles() : Arrays.asList();

        credencialDTO.setId(resolve.getIdUsuario());
        credencialDTO.setLogin(resolve.getLogin());
        credencialDTO.setEmail(resolve.getEmail());
        credencialDTO.setNome(resolve.getNome());
        credencialDTO.setRoles(roles);
        credencialDTO.setSenha(null);
        return credencialDTO;
    }

    /**
     * Realiza a inclusão ou alteração de senha.
     *
     * @param usuarioSenhaDTO -
     * @param token           -
     * @return -
     */
    public CredencialDTO redefinirSenha(final UsuarioSenhaDTO usuarioSenhaDTO, final String token) {
        AuthClaimResolve resolve = getClaimResolve(token);

        usuarioSenhaDTO.setIdUsuario(resolve.getIdUsuario());
        usuarioSenhaDTO.setTipo(resolve.getTipoRedefinicaoSenha());
        CredencialDTO usuario = userProviderService.redefinirSenha(usuarioSenhaDTO);

        AuthDTO authDTO = new AuthDTO();
        authDTO.setLogin(usuario.getLogin());
        authDTO.setSenha(usuarioSenhaDTO.getNovaSenha());
        return loginAccess(authDTO);
    }

    /**
     * Valida o token de alteração de senha.
     *
     * @param token -
     */
    public boolean getInfoByTokenValidacao(final String token) {
        AuthClaimResolve resolve = getClaimResolve(token);

        UsuarioSenhaDTO usuarioSenhaTO = new UsuarioSenhaDTO();
        usuarioSenhaTO.setTipo(resolve.getTipoRedefinicaoSenha());

        Long idUsuario = resolve.getIdUsuario();
        CredencialDTO usuario = userProviderService.getCredentialByLogin(resolve.getLogin());//usuarioService.getById(idUsuario);
        return usuarioSenhaTO.isRecuperacao() || (usuarioSenhaTO.isAtivacao() && !usuario.isStatusAtivo());
    }


    /**
     * Verifica se os campos de preechimento obrigatório foram informados.
     *
     * @param authDTO -
     */
    private void validarCamposObrigatoriosLoginAcesso(final AuthDTO authDTO) {
        if (Util.isEmpty(authDTO.getLogin()) || Util.isEmpty(authDTO.getSenha())) {
            throw new BusinessException(ApiMessageCode.ERRO_CAMPOS_OBRIGATORIOS);
        }
    }

    /**
     * Verifica se o {@link CredencialDTO} informado é valido no momento do login.
     *
     * @param usuario -
     */
    private void validarUsuarioLogin(CredencialDTO usuario) {
        if (usuario == null) {
            throw new BusinessException(ApiMessageCode.ERRO_USUARIO_SENHA_NAO_CONFEREM);
        }

        registerCredentialInSecurityContext(usuario);

        if (!usuario.isStatusAtivo()) {
            throw new BusinessException(ApiMessageCode.ERRO_USUARIO_INATIVO);
        }
    }

    /**
     * Retorna a instância de {@link AuthClaimResolve}.
     *
     * @param token -
     * @return -
     */
    private AuthClaimResolve getClaimResolve(final String token) {
        String value = getAccessToken(token);
        TokenBuilder builder = new TokenBuilder(keyToken);
        Map<String, Claim> claims = builder.getClaims(value);

        if (claims == null) {
            throw new BusinessException(ApiMessageCode.ERRO_TOKEN_INVALIDO);
        }
        return AuthClaimResolve.newInstance(claims);
    }

    /**
     * Retorna o token de acesso recuperados da instância
     * {@link HttpServletRequest}.
     *
     * @return -
     */
    private String getAccessToken(final String value) {
        String accessToken = null;

        if (!Util.isEmpty(value)) {
            accessToken = value.replaceAll(Constante.HEADER_AUTHORIZATION_BEARER, "").trim();
        }
        return accessToken;
    }
}
