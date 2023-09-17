package br.ueg.prog.webi.api.controller;

import br.ueg.prog.webi.api.exception.MessageResponse;
import br.ueg.prog.webi.api.mapper.BaseMapper;
import br.ueg.prog.webi.api.model.IEntidade;
import br.ueg.prog.webi.api.service.CrudService;
import br.ueg.prog.webi.api.util.Reflexao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

public class CrudEntityIdHashController<
        ENTIDADE extends IEntidade<PK_TYPE>,
        DTO,
        PK_TYPE,
        MAPPER extends BaseMapper<ENTIDADE, DTO>,
        SERVICE extends CrudService<ENTIDADE, PK_TYPE>
        >  extends CrudController<ENTIDADE, DTO, PK_TYPE, MAPPER, SERVICE> {

    @PutMapping(path = "/hash/{id}")
    @Operation(description = "Método utilizado para altlerar os dados de uma entidiade", responses = {
            @ApiResponse(responseCode = "200", description = "Listagem geral",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de Negócio",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class)))
    }
    )
    public ResponseEntity<DTO> alterarIdHash(@RequestBody() DTO modeloDTO, @PathVariable(name = "id") String id
    ){
        // TODO validar se a PK está preenchidia
        ENTIDADE pModelo = mapper.toModelo(modeloDTO);
        PK_TYPE pk = pModelo.getIdFromHash(id);
        ENTIDADE alterar = service.alterar(pModelo, pk);
        return ResponseEntity.ok(mapper.toDTO(alterar));
    }

    @DeleteMapping(path ="/hash/{id}")
    @Operation(description = "Método utilizado para remover uma entidiade pela id informado", responses = {
            @ApiResponse(responseCode = "200", description = "Entidade Removida",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<DTO> removerIdHash(@PathVariable(name = "id") String id){

        ENTIDADE entidadeObject = getEntidadeObject();
        PK_TYPE pk = entidadeObject.getIdFromHash(id);
        ENTIDADE modeloExcluido = this.service.excluir(pk);
        return ResponseEntity.ok(mapper.toDTO(modeloExcluido));
    }

    private ENTIDADE getEntidadeObject() {
        ENTIDADE entidadeObject;
        Class<PK_TYPE> entidadeClass = (Class<PK_TYPE>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        entidadeObject = (ENTIDADE) Reflexao.getNewEntidadeFromType(entidadeClass);
        return entidadeObject;
    }

    @GetMapping(path = "/hash/{id}")
    @Operation(description = "Obter os dados completos de uma entidiade pelo id informado!", responses = {
            @ApiResponse(responseCode = "200", description = "Entidade encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<DTO> ObterPorIdHash(@PathVariable(name = "id") String id){
        ENTIDADE entidadeObject = getEntidadeObject();
        PK_TYPE pk = entidadeObject.getIdFromHash(id);
        ENTIDADE aluno = this.service.obterPeloId(pk);
        return ResponseEntity.ok(this.mapper.toDTO(aluno));
    }
}
