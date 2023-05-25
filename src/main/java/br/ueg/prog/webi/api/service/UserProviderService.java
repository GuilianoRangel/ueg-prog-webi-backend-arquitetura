package br.ueg.prog.webi.api.service;

import br.ueg.prog.webi.api.dto.CredencialDTO;
import br.ueg.prog.webi.api.dto.UsuarioSenhaDTO;

public interface UserProviderService {
    CredencialDTO getCredentialByLogin(String username);
    CredencialDTO redefinirSenha(UsuarioSenhaDTO usuarioSenhaDTO);
    CredencialDTO getCredentialByEmail(String email);
}
