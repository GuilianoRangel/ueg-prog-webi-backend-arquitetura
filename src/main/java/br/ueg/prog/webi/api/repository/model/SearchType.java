package br.ueg.prog.webi.api.repository.model;

import jakarta.persistence.criteria.*;
import lombok.Getter;

import br.ueg.prog.webi.api.dto.SearchFieldValue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

public @Getter enum SearchType {

    CONTAINS("cn", "Contém", SearchType::contains),
    DOES_NOT_CONTAIN("nc","Não Contém", null),
    EQUAL("eq", "Igual", SearchType::equal),
    NOT_EQUAL("ne", "Diferente", null),
    BEGINS_WITH( "bw", "Começa com", SearchType::beginWith),
    DOES_NOT_BEGIN_WITH("bn", "Não começa com",null),
    ENDS_WITH("ew", "Termina com", null),
    DOES_NOT_END_WITH("en", "Não termina com", null),
    NUL("nu", "Vazio", null),
    NOT_NULL("nn", "Não Vazio", null),
    GREATER_THAN("gt", "Maior que", SearchType::greaterThan),
    GREATER_THAN_EQUAL("ge", "Maior ou igual a", null),
    LESS_THAN("lt", "Menor que", null),
    LESS_THAN_EQUAL("le", "Menor ou Igual a", null),
    ANY("any", "Qualquer", null),
    ALL("all", "Todos", SearchType::all);

    private final String id;
    private final String description;
    private final ISearchTypePredicate predicateExecute;
    SearchType(String id, String description, ISearchTypePredicate predicateExecute){
        this.id = id;
        this.description = description;
        this.predicateExecute = predicateExecute;
    }

    public static SearchType getSimpleOperation(final String input) {

        SearchType returnSearchTypeShortName = Arrays.stream(values())
                .filter(searchType -> searchType.id.equalsIgnoreCase(input)).findFirst().orElse(null);
        if(Objects.isNull(returnSearchTypeShortName)){
            returnSearchTypeShortName = Arrays.stream(values())
                    .filter(searchType -> searchType.name().equalsIgnoreCase(input)).findFirst().orElse(null);
        }
        return returnSearchTypeShortName;
    }

    public static String getSearchTypeId(final SearchType value){
        return  Arrays.stream(values())
                .filter(searchType -> searchType.equals(value))
                .map(searchType -> searchType.id)
                .findFirst().orElse(null);
    }
    private static Path<?> getPath(Root<?> root, CriteriaBuilder cb, SearchFieldValue searchFieldValue){
        if(searchFieldValue.getName().contains(".")) {
            String[] findInfo = searchFieldValue.getName().split("\\.");
            String findNameEntity = findInfo[0];
            String findNameField = findInfo[1];
            return root.join(findNameEntity).get(findNameField);
        }else{
            return root.get(searchFieldValue.getName());
        }
    }

    public static Predicate equal(Root<?> root, CriteriaBuilder cb, SearchFieldValue searchFieldValue){
        return cb.equal(getPath(root,cb, searchFieldValue), searchFieldValue.getObjectValue());
        /*if(searchFieldValue.getName().contains(".")){
            String[] findInfo = searchFieldValue.getName().split("\\.");
            String findNameEntity = findInfo[0];
            String findNameField = findInfo[1];
            return cb.equal(root.join(findNameEntity).get(findNameField), searchFieldValue.getObjectValue().toString());
        }else {
            return cb.equal(root.get(searchFieldValue.getName()), searchFieldValue.getObjectValue());
        }*/
    }

    public static Predicate beginWith(Root<?> root, CriteriaBuilder cb, SearchFieldValue searchFieldValue){
        return cb.like(cb.lower((Expression<String>) getPath(root, cb, searchFieldValue)), searchFieldValue.getObjectValue().toString().toLowerCase() + "%");
    }

    public static Predicate contains(Root<?> root, CriteriaBuilder cb, SearchFieldValue searchFieldValue){
        return cb.like(cb.lower((Expression<String>) getPath(root, cb, searchFieldValue)), "%"+searchFieldValue.getObjectValue().toString().toLowerCase() + "%");
    }
    public static Predicate all(Root<?> root, CriteriaBuilder cb, SearchFieldValue searchFieldValue){
        return cb.and();
    }



    public static Predicate greaterThan(Root<?> root, CriteriaBuilder cb, SearchFieldValue searchFieldValue){
        if(searchFieldValue.getObjectValue() instanceof LocalDate){
            Path<LocalDate> yPath = (Path<LocalDate>) getPath(root, cb, searchFieldValue);
            return cb.greaterThan(yPath, (LocalDate)searchFieldValue.getObjectValue());
        } else if (searchFieldValue.getObjectValue() instanceof Long){
            Path<Long> yPath = (Path<Long>) getPath(root, cb, searchFieldValue);
            return cb.greaterThan(yPath, (Long) searchFieldValue.getObjectValue());
        } else if (searchFieldValue.getObjectValue() instanceof LocalDateTime) {
            Path<LocalDateTime> yPath = (Path<LocalDateTime>) getPath(root, cb, searchFieldValue);
            return cb.greaterThan(yPath, (LocalDateTime)searchFieldValue.getObjectValue());
        } else if (searchFieldValue.getObjectValue() instanceof BigDecimal) {
            Path<BigDecimal> yPath = (Path<BigDecimal>) getPath(root, cb, searchFieldValue);
            return cb.greaterThan(yPath, (BigDecimal) searchFieldValue.getObjectValue());
        } else {
            Path<String> yPath = (Path<String>) getPath(root, cb, searchFieldValue);
            return cb.greaterThan(yPath, searchFieldValue.getObjectValue().toString());
        }
    }



}
