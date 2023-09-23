package br.ueg.prog.webi.api.mapper;

import br.ueg.prog.webi.api.model.IEntidade;
import org.mapstruct.*;

import java.util.List;

public interface BaseMapper<ENTIDADE extends IEntidade, DTO> {
    ENTIDADE toModelo(DTO dto);
    DTO toDTO(ENTIDADE modelo);
    List<DTO> toDTO(List<ENTIDADE> lista);
    @BeanMapping(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateModel(ENTIDADE source, @MappingTarget ENTIDADE target);
}
