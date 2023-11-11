package br.ueg.prog.webi.api.exception;

import lombok.Getter;

@Getter
public enum ApiMessageCode implements MessageCode {


    ERRO_INESPERADO("ME001", 500),
    ERRO_REGISTRO_NAO_ENCONTRADO("ME002", 404),
    ERRO_BD("ME003", 400),
    ERRO_TOKEN_INVALIDO("ME004", 403),

    ERRO_CAMPOS_OBRIGATORIOS("ME005", 400),
    ERRO_USUARIO_NAO_ENCONTRADO("ME006", 404),
    ERRO_USUARIO_SENHA_NAO_CONFEREM("ME007", 400),
    ERRO_USUARIO_INATIVO("ME008",400),

    MSG_OPERACAO_REALIZADA_SUCESSO("MSG-000", 200),

    ERROR_SEARCH_PARAMETERS_NOT_DEFINED("ME009", 400),

    SEARCH_FIELDS_RESULT_NONE("ME010", 404)
    ;
    private final String code;

    private final Integer status;

    /**
     * Construtor da classe.
     *
     * @param code -
     * @param status -
     */
    ApiMessageCode(final String code, final Integer status) {
        this.code = code;
        this.status = status;
    }

    /**
     * @see Enum#toString()
     */
    @Override
    public String toString() {
        return code;
    }
}
