package br.ueg.prog.webi.api.service;

import br.ueg.prog.webi.api.dto.SearchField;
import br.ueg.prog.webi.api.dto.SearchFieldValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CrudService<ENTIDADE, PK_TYPE> {
    ENTIDADE incluir(ENTIDADE ENTIDADE);
    ENTIDADE alterar(ENTIDADE ENTIDADE, PK_TYPE id);
    ENTIDADE excluir(PK_TYPE id);
    ENTIDADE obterPeloId(PK_TYPE id);
    List<ENTIDADE> listarTodos();
    Page<ENTIDADE> listarTodosPage(Pageable page);

    /**
     * Retorna uma lista dos campos utilizáveis para busca no controlador
     * @return lista de campos
     */
    List<SearchField> listSearchFields();

    List<ENTIDADE> searchFieldValues(List<SearchFieldValue> searchFieldValues);

    Page<ENTIDADE> searchFieldValuesPage(Pageable page, List<SearchFieldValue> searchFieldValues);
}
