package br.ueg.prog.webi.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ArchRepository<ENTITY, PK_TYPE> extends JpaRepository<ENTITY, PK_TYPE>, JpaSpecificationExecutor<ENTITY> {
}
