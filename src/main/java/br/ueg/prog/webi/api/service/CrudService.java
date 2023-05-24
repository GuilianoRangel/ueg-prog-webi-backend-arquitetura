package br.ueg.prog.webi.api.service;

import java.util.List;

public interface CrudService<ENTIDADE, PK_TYPE> {
    ENTIDADE incluir(ENTIDADE ENTIDADE);
    ENTIDADE alterar(ENTIDADE ENTIDADE, PK_TYPE id);
    ENTIDADE excluir(PK_TYPE id);
    ENTIDADE obterPeloId(PK_TYPE id);
    List<ENTIDADE> listarTodos();
}
