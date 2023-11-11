package br.ueg.prog.webi.api.dto;

import br.ueg.prog.webi.api.repository.model.SearchType;
import br.ueg.prog.webi.api.repository.model.conveters.SearchTypeEnumDeserializer;
import br.ueg.prog.webi.api.repository.model.conveters.SearchTypeEnumSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;

/**
 * Classe utilizada para representar os campos de
 * busca com seus valores
 */
@Builder
public @Data class SearchFieldValue {
    /**
     * Nome do campo utilizado para reealizar a busca
     */
    private String name;

    private String type;
    /**
     * Texto Utilizado para apresentar para o usuário
     * nomeando o atributo que será pesquisado
     */
    private String value;

    /**
     * Indica qual o tipo de comparação será utilizada
     */
    @JsonDeserialize(using = SearchTypeEnumDeserializer.class)
    @JsonSerialize(using = SearchTypeEnumSerializer.class)
    private SearchType searchType;

    /**
     * Valor do campo de busca confome o tipo informano no atributo Type
     */
    @JsonIgnore
    private Object objectValue;
}
