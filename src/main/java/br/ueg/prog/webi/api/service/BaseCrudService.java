package br.ueg.prog.webi.api.service;

import br.ueg.prog.webi.api.dto.SearchField;
import br.ueg.prog.webi.api.dto.SearchFieldValue;
import br.ueg.prog.webi.api.exception.ApiMessageCode;
import br.ueg.prog.webi.api.exception.BusinessException;
import br.ueg.prog.webi.api.exception.DevelopmentException;
import br.ueg.prog.webi.api.interfaces.IConverter;
import br.ueg.prog.webi.api.model.IEntidade;
import br.ueg.prog.webi.api.repository.model.ISearchTypePredicate;
import br.ueg.prog.webi.api.util.Reflexao;
import br.ueg.prog.webi.api.util.SearchReflection;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public abstract class BaseCrudService<
        ENTIDADE extends IEntidade<PK_TYPE>,
        PK_TYPE,
        REPOSITORY extends JpaRepository<ENTIDADE, PK_TYPE>
        > implements CrudService<ENTIDADE, PK_TYPE>{
    private static final Logger log = LoggerFactory.getLogger(BaseCrudService.class);
    public static final String CONVETER_PACKAGE = "br.ueg.prog.webi.api.converters.";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    protected REPOSITORY repository;

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    protected ApplicationContext context;
    private Class<PK_TYPE> entityClass;

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
     * @param id - identificador da entidade
     * @return - a entidade do identificação informado
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

    @Override
    public Page<ENTIDADE> listarTodosPage(Pageable page) {
        return (Page<ENTIDADE>) repository.findAll(page);
    }

    @Override
    public List<SearchField> listSearchFields() {
        return SearchReflection.getSearchFieldList(this.context, this.getEntityType());
    }

    private Class<PK_TYPE> getEntityType() {
        if(Objects.isNull(this.entityClass)){
            this.entityClass = (Class<PK_TYPE>) ((ParameterizedType) this.getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0];
        }
        return this.entityClass;
    }

    public class SearchEntity implements Specification<ENTIDADE> {
        private final List<SearchFieldValue> searchFieldValues;
        private final Class<?> entityClass;

        public SearchEntity(Class<?> entityClass, List<SearchFieldValue> searchFieldValues) {
            this.searchFieldValues = searchFieldValues;
            this.entityClass = entityClass;
        }

        @Override
        public Predicate toPredicate(Root<ENTIDADE> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            //TODO tratar como objeto e não como strinig;
            //String strToSearch = searchFieldValue.getValue().toString().toLowerCase();
            List<Predicate> listPredicate = new ArrayList<>();
            for (SearchFieldValue fieldValue : searchFieldValues) {
                listPredicate.add(getPredicate(root, cb, fieldValue));
            }
            Predicate firstPredicate = listPredicate.get(0);

            for (int i = 1; i < listPredicate.size(); i++) {
                firstPredicate = cb.and(firstPredicate, listPredicate.get(i));
            }


            return firstPredicate;
        }

        private Predicate getPredicate(Root<ENTIDADE> root, CriteriaBuilder cb, SearchFieldValue valueSearch) {
            Object value = getValue(valueSearch);
            valueSearch.setObjectValue(value);

            validSearchFieldName(valueSearch);
            ISearchTypePredicate searchTypePredicate =  valueSearch.getSearchType().getPredicateExecute();

/*            //testes
            if(IEntidade.class.isAssignableFrom(fieldEntity.getType()) ){
                String findName = valueSearch.getName().split("\\.")[1];
                return searchTypePredicate.execute(root,cb,valueSearch, root.join(fieldEntity.getName()).get(findName));
                return cb.like(root.join(fieldEntity.getName()).get(findName), searchFieldValue.getObjectValue().toString());
            }*/

            if(Objects.nonNull(searchTypePredicate)){
                return searchTypePredicate.execute(root, cb, valueSearch);
            }else{
                throw new DevelopmentException("tipo Busca:" + valueSearch.getSearchType() + " não implementado !");
            }
            /*//TODO Verificar busca case insensitive
            switch (valueSearch.getSearchType()) {
                case EQUAL -> {
                    return cb.equal(root.get(valueSearch.getName()), valueSearch.getObjectValue());
                    //return cb.equal(departmentJoin(root).<String>get(searchCriteria.getFilterKey()), searchCriteria.getValue());
                }
                case BEGINS_WITH -> {
                    return cb.like(cb.lower(root.get(valueSearch.getName())), valueSearch.getObjectValue().toString().toLowerCase() + "%");
                }
                default -> {
                    throw new DevelopmentException("tipo Busca:" + valueSearch.getSearchType() + " não existe !");
                }
            }*/
        }

        private Field validSearchFieldName(SearchFieldValue valueSearch) {
            Field entidadeField;
            String fieldName = valueSearch.getName();
            if(fieldName.contains(".")){
                fieldName = fieldName.split("\\.")[0];
            }
            try {
                entidadeField = Reflexao.getEntidadeField(entityClass, fieldName);
            } catch (NoSuchFieldException e) {
                throw new DevelopmentException("Campo informado para busca:" + fieldName + " não existe na entidade: " + this.entityClass.getName());
            }
            return entidadeField;
        }
    }

    public List<ENTIDADE> searchFieldValues(List<SearchFieldValue> searchFieldValues){
        try{
            Class<?> entityClass = this.getEntityType();


            JpaRepository entityRepository = Reflexao.getEntityRepository(this.context, this.getEntityType());
            if(entityRepository instanceof  JpaSpecificationExecutor){
                JpaSpecificationExecutor<ENTIDADE> jpaSpecificationExecutor = (JpaSpecificationExecutor<ENTIDADE>) entityRepository;
                List<ENTIDADE> all = jpaSpecificationExecutor.findAll(new SearchEntity(entityClass, searchFieldValues));
                return all;
            }else{
                throw new DevelopmentException("Repository not implement JpaSpecificationExecutor:"+ entityRepository.getClass().getName());
            }

            /*switch (valueSearch.getType()){
                case "Long":
                    value = Long.valueOf(valueSearch.getValue());
                    break;
                case "Integer"
            }*/
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private Object getValue(SearchFieldValue valueSearch) {
        Object value;
        //String pacote = CONVETER_PACKAGE;
        String converterClass = valueSearch.getType().concat("Converter");
        converterClass = converterClass.substring(0,1).toLowerCase().concat(converterClass.substring(1));
        //String conversorName = pacote.concat(converterClass);
        try {
            //Class classConverter = Class.forName(conversorName);
            //IConverter converter = (IConverter) classConverter.getConstructor().newInstance();
            IConverter converter = (IConverter) this.appContext.getBean(converterClass);
            value = converter.converter(valueSearch.getValue());
        //TODO tratar classe não existe
        }catch (Exception e){
            log.info("Erro ao Convereter, ou Converter Não encontrado: "+converterClass);
            value = valueSearch.getValue();
        }
        return value;
    }
}
