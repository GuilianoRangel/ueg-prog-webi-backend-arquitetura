package br.ueg.prog.webi.api.dto;

import br.ueg.prog.webi.api.interfaces.ISearchFieldData;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Classe utilizada para representar os campos de
 * busca de eum controlador
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public @Data class SearchField {
    /**
     * Nome do campo utilizado para reealizar a busca
     */
    private String name;

    /**
     * Nome do tipo do campo para buscar
     */
    private String type;
    /**
     * Texto Utilizado para apresentar para o usuário
     * nomeando o atributo que será pesquisado
     */
    private String label;

    /**
     * Em caso de tipo de dados que tem uma lista de valores possíveis,
     * esse atributo comtém os valores permitidos
     * IEentidade, Enuns,
     */
    private List<ISearchFieldData<?>> valueList;
}
