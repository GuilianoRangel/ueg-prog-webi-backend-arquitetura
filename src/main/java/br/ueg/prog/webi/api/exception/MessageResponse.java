/*
 * MessageResponse.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.exception;


import br.ueg.prog.webi.api.util.CollectionUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de representação de Mensagem de Resposta utilizada nas implementações
 * 'ExceptionHandler'
 *
 * @author UEG
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 4878825827657916191L;

    @Schema(description = "Código da Mensagem")
    private String code;

    @Schema(description = "Status HTTP")
    private Integer status;

    @Schema(description = "Descrição erro HTTP")
    private String error;

    @Schema(description = "Mensagem de negócio")
    private String message;

    @Schema(description = "Parâmetros da mensagem")
    private Object[] parameters;

    @Schema(description = "Atributos de validação")
    private List<FieldResponse> attributes;

    /**
     * Adiciona a instância de {@link FieldResponse}.
     *
     * @param field -
     */
    public void addAttribute(final FieldResponse field) {
        if (CollectionUtil.isEmpty(attributes)) {
            attributes = new ArrayList<>();
        }
        attributes.add(field);
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * @return the error
     */
    public String getError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the parameters
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the attributes
     */
    public List<FieldResponse> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(List<FieldResponse> attributes) {
        this.attributes = attributes;
    }

}
