package br.ueg.prog.webi.api.service;

import br.ueg.prog.webi.api.exception.ApiMessageCode;
import br.ueg.prog.webi.api.exception.BusinessException;
import br.ueg.prog.webi.api.model.IEntidade;
import br.ueg.prog.webi.api.util.Reflexao;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public abstract class BaseCrudService<
        ENTIDADE extends IEntidade<PK_TYPE>,
        PK_TYPE,
        REPOSITORY extends JpaRepository<ENTIDADE, PK_TYPE>
        > implements CrudService<ENTIDADE, PK_TYPE>{
    private static final Logger log = LoggerFactory.getLogger(BaseCrudService.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    protected REPOSITORY repository;

    @Autowired
    protected ApplicationContext context;

    @Override
    public ENTIDADE incluir(ENTIDADE modelo) {
        if(Reflexao.isEntidadeHavePkGenerated(modelo)) {
            modelo.setId(null);
        }else{
            modelo.setNew();
        }
        this.setListReferences(modelo);
        this.setAndSaveNewForeignEntitiesMaps(modelo);
        this.validarCamposObrigatorios(modelo);
        this.validarDados(modelo);
        this.prepararParaIncluir(modelo);
        ENTIDADE entidadeIncluido = this.gravarDados(modelo);
        return entidadeIncluido;
    }

    private void setListReferences(ENTIDADE modelo) {
        for (Field entidadeField : Reflexao.getEntidadeFields(modelo.getClass())) {
            if(
                 Collection.class.isAssignableFrom(entidadeField.getType())
            ){
                ParameterizedType listType = (ParameterizedType) entidadeField.getGenericType();
                Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];
                if(IEntidade.class.isAssignableFrom(listClass)) {
                    var list = (Collection<IEntidade<?>>) Reflexao.getFieldValue(modelo, entidadeField.getName());
                    for (IEntidade<?> iEntidade : list) {
                        Field[] entidadeFields = Reflexao.getEntidadeFields(iEntidade.getClass());
                        for (Field fieldAux : entidadeFields) {
                            if(fieldAux.getType().isAssignableFrom(modelo.getClass())){
                                Reflexao.setFieldValue(iEntidade, fieldAux.getName(), modelo);
                            }
                        }

                    }
                }
            }
        }

    }

    private IEntidade<?> getEntityFromOld(Map<Field, IEntidade<?>> maps, String fieldName){
        for ( var entityField : maps.keySet()) {
            if(entityField.getName().equals(fieldName)) {
                return maps.get(entityField);
            }
        }
        return null;
    }

    protected void setAndSaveNewForeignEntitiesMaps(ENTIDADE modelo){
        Map<Field, IEntidade<?>> foreignEntityOld = Reflexao.getForeignEntity(modelo);
        Map<String, IEntidade<?>> foreignEntity = Reflexao.setForeignEntitiesMaps(modelo, this.context);
        for ( var entityField : foreignEntity.keySet()) {
            IEntidade<?> iEntidade = foreignEntity.get(entityField);
            IEntidade<?> oldForeign = this.getEntityFromOld(foreignEntityOld, entityField);
            if(Objects.isNull(iEntidade)){
                //Salvar uma nova pessoa
                IEntidade<?> newForeign = (IEntidade<?>) Reflexao.getFieldValue(iEntidade, entityField);
                IEntidade<?> newForeignSave = Reflexao.saveNewForeignEntity(this.context, newForeign);
                Reflexao.setFieldValue(modelo, entityField, newForeignSave);
            }else{
                if(Objects.nonNull(oldForeign)) {
                    Reflexao.updateModel(this.context, oldForeign, iEntidade, true);
                }
                //pessoaMapper.updateModel(oldForeign, funcionario.getPessoa());
            }
            setAndSaveNewForeignEntitiesMaps((ENTIDADE) iEntidade);
        }
        modelo.setForeignEntitiesMaps(foreignEntity);
    }

    abstract protected void prepararParaIncluir(ENTIDADE entidade) ;

    private ENTIDADE gravarDados(ENTIDADE entidade) {
        try {
            ENTIDADE save = repository.saveAndFlush(entidade);
            return save;
        }catch (ConstraintViolationException | DataIntegrityViolationException cev){
            throw new BusinessException(ApiMessageCode.ERRO_BD,cev.getMessage());
        }
    }

    abstract protected  void validarDados(ENTIDADE entidade) ;

    abstract protected void validarCamposObrigatorios(ENTIDADE entidade) ;

    @Override
    public ENTIDADE alterar(ENTIDADE entidade, PK_TYPE id) {

        this.setListReferences(entidade);
        this.setAndSaveNewForeignEntitiesMaps(entidade);
        this.validarCamposObrigatorios(entidade);
        this.validarDados(entidade);

        ENTIDADE entidadeBD = recuperarEntidadeOuGeraErro(id);
        entidade.setId(id);

        return this.gravarDados(entidade);
    }

    protected ENTIDADE recuperarEntidadeOuGeraErro(PK_TYPE id) {
        ENTIDADE entidade = repository
                .findById(id)
                .orElseThrow(
                        () -> new BusinessException(ApiMessageCode.ERRO_REGISTRO_NAO_ENCONTRADO)
                );
        return entidade;
    }

    /**
     * retorna a entidade se existir ou null
     * @param id
     * @return
     */
    protected ENTIDADE recuperarEntidade(PK_TYPE id) {
        ENTIDADE entidade = repository
                .findById(id)
                .orElse(null);
        return entidade;
    }

    @Override
    public ENTIDADE excluir(PK_TYPE id) {
        ENTIDADE entidadeExcluir = this.recuperarEntidadeOuGeraErro(id);
        this.repository.delete(entidadeExcluir);
        return entidadeExcluir;
    }

    @Override
    public ENTIDADE obterPeloId(PK_TYPE id) {
        return this.recuperarEntidadeOuGeraErro(id);
    }

    @Override
    public List<ENTIDADE> listarTodos() {
        return (List<ENTIDADE>) repository.findAll();
    }
}
