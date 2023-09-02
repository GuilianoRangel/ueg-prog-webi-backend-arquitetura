package br.ueg.prog.webi.api.model;

public interface IEntidade<PK_TYPE> {
    public static final String COMPOSITE_PK_FIELD_NAME ="compositePkEntidadeObject";
    String getTabelaNome();
    PK_TYPE getId();
    String getIdHash();
    void setId(PK_TYPE id);
}
