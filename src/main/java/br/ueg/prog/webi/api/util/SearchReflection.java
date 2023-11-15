package br.ueg.prog.webi.api.util;

import br.ueg.prog.webi.api.dto.SearchField;
import br.ueg.prog.webi.api.dto.SearchFieldData;
import br.ueg.prog.webi.api.interfaces.ISearchFieldData;
import br.ueg.prog.webi.api.model.IEntidade;
import br.ueg.prog.webi.api.model.annotation.Searchable;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SearchReflection {
    /**
     * Método de reflexão para obeter o campos de busca de um modelo
     *
     * @param context - aplication contecxt
     * @param entity  modelo obter a lista de campos de busca
     * @return lista de campos de busca
     */
    public static List<SearchField> getSearchFieldList(ApplicationContext context, Class<?> entity){
        List<SearchField> searchFieldList = new ArrayList<>();
        for (Field entityField : Reflexao.getEntidadeFields(entity)) {
            if(entityField.isAnnotationPresent(Searchable.class)){
                Searchable searchable = entityField.getAnnotation(Searchable.class);
                if(IEntidade.class.isAssignableFrom(entityField.getType()) && !searchable.listEntityValues()){
                    List<SearchField> searchFieldList1 = getEntitySearchFields(context, entityField, searchable);
                    searchFieldList.addAll(searchFieldList1);
                }else if(IEntidade.class.isAssignableFrom(entityField.getType()) && searchable.listEntityValues()){
                    JpaRepository entityRepository = Reflexao.getEntityRepository(context, entityField.getType());

                    List<ISearchFieldData<?>> entityListAll = new ArrayList<>();
                    SearchField searchField = getSimpleSearchField(entityField, searchable);
                    for (Object enumConstant : entityRepository.findAll()) {
                        ISearchFieldData<?> value = (ISearchFieldData<?>) enumConstant;
                        entityListAll.add(new SearchFieldData(value));
                    }
                    searchField.setValueList(entityListAll);
                    searchFieldList.add(searchField);
                }else if(Set.class.isAssignableFrom(entityField.getType())){
                    ParameterizedType stringListType = (ParameterizedType) entityField.getGenericType();
                    Class<?> entityFieldListType = (Class<?>) stringListType.getActualTypeArguments()[0];
                    List<SearchField> searchFieldList1 = getEntitySearchFields(context, entityField.getName(), entityFieldListType, searchable);
                    searchFieldList.addAll(searchFieldList1);

                } else {
                    SearchField searchField = getSimpleSearchField(entityField, searchable);
                    searchFieldList.add(searchField);
                }
            }
        }
        return searchFieldList;
    }

    private static List<SearchField> getEntitySearchFields(ApplicationContext context, Field entityField, Searchable searchable) {
        //TODO tratar nível
        return getEntitySearchFields(context, entityField.getName(), entityField.getType(), searchable);
    }



    private static List<SearchField> getEntitySearchFields(ApplicationContext context, String fieldName, Class<?> fieldType, Searchable searchable) {
        //TODO tratar nível
        String fieldLabel = searchable.label();
        if(Strings.isEmpty(fieldLabel)){
            fieldLabel = Reflexao.uCFirst(fieldName);
        }
        List<SearchField> searchFieldList1 = getSearchFieldList(context, fieldType);
        String finalFieldLabel = fieldLabel;
        searchFieldList1.forEach(searchField -> {
            searchField.setName(fieldName +"."+ searchField.getName());
            searchField.setLabel(searchField.getLabel()+ " de "+finalFieldLabel);
        });
        return searchFieldList1;
    }

    private static SearchField getSimpleSearchField(Field entityField, Searchable searchable) {
        String fieldName = entityField.getName();
        String fieldLabel = searchable.label();
        if(Strings.isEmpty(fieldLabel)){
            fieldLabel = Reflexao.uCFirst(fieldName);
        }
        SearchField searchField = SearchField.builder()
                .name(fieldName)
                .type(entityField.getType().getSimpleName())
                .label(fieldLabel)
                .build();
        setSearchFieldListValues(searchField, entityField);
        return searchField;
    }

    private static void setSearchFieldListValues(SearchField searchField, Field entityField) {
        if(entityField.getType().isEnum() &&
                ISearchFieldData.class.isAssignableFrom(entityField.getType()) ){
            List<ISearchFieldData<?>> listValues = new ArrayList<>();
            for (Object enumConstant : entityField.getType().getEnumConstants()) {
                ISearchFieldData<?> value = (ISearchFieldData<?>) enumConstant;
                listValues.add(new SearchFieldData(value));
            }
            searchField.setValueList(listValues);
        }
    }
}
