package br.ueg.prog.webi.api.repository.model;

import br.ueg.prog.webi.api.dto.SearchFieldValue;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public interface ISearchTypePredicate {
    Predicate execute(Root<?> root, CriteriaBuilder cb, SearchFieldValue searchFieldValue);
}
