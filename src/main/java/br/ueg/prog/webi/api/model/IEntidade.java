package br.ueg.prog.webi.api.model;

import org.springframework.data.domain.Persistable;

import java.util.Map;

public interface IEntidade<PK_TYPE> extends Persistable<PK_TYPE> {
    public static final String COMPOSITE_PK_FIELD_NAME ="compositePkEntidadeObject";
    String getTabelaNome();
    PK_TYPE getId();
    String getIdHash();
    PK_TYPE getIdFromHash(String hash);
    void setId(PK_TYPE id);

    void setNew();

    void setForeignEntitiesMaps(Map<String, IEntidade<?>> foreignEntities);
    Map<String, IEntidade<?>> getForeignEntitiesMaps();
}
