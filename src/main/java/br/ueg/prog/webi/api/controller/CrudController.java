package br.ueg.prog.webi.api.controller;

import br.ueg.prog.webi.api.dto.SearchField;
import br.ueg.prog.webi.api.dto.SearchFieldValue;
import br.ueg.prog.webi.api.exception.ApiMessageCode;
import br.ueg.prog.webi.api.exception.BusinessException;
import br.ueg.prog.webi.api.exception.MessageResponse;
import br.ueg.prog.webi.api.mapper.BaseMapper;
import br.ueg.prog.webi.api.model.IEntidade;
import br.ueg.prog.webi.api.service.CrudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Function;

public abstract class CrudController<
        ENTIDADE extends IEntidade<PK_TYPE>,
        DTO,
        PK_TYPE,
        MAPPER extends BaseMapper<ENTIDADE, DTO>,
        SERVICE extends CrudService<ENTIDADE, PK_TYPE>
        > {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    protected MAPPER mapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    protected SERVICE service;

    @GetMapping()
    @Operation(description = "Listagem Geral", responses = {
            @ApiResponse(responseCode = "200", description = "Listagem geral",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema())),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de Negócio",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<List<DTO>> listAll(){
        List<ENTIDADE> modelo = service.listarTodos();
        return ResponseEntity.ok(mapper.toDTO(modelo));
    }
    @GetMapping(path = "/page")
    @Operation(description = "Listagem Geral paginada", responses = {
            @ApiResponse(responseCode = "200", description = "Listagem geral",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema())),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de Negócio",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<Page<DTO>> listAllPage(@PageableDefault(page = 0, size = 5)  Pageable page){
        Page<ENTIDADE> pageEntidade = service.listarTodosPage(page);
        return ResponseEntity.ok(mapPageEntityToDto(pageEntidade));
    }

    public Page<DTO> mapPageEntityToDto(Page<ENTIDADE> page){
        Page<DTO> dtoPage = page.map(new Function<ENTIDADE, DTO>() {
            @Override
            public DTO apply(ENTIDADE entity) {
                return mapper.toDTO(entity);
            }
        });
        return dtoPage;
    }


    @PostMapping
    @Operation(description = "Método utilizado para realizar a inclusão de um entidade", responses = {
            @ApiResponse(responseCode = "200", description = "Entidade Incluida",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de Negócio",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<DTO> incluir(@RequestBody DTO modeloDTO){
        //prepração para entrada.
        ENTIDADE modeloIncluir = this.mapper.toModelo(modeloDTO);
        //chamada do serviço
        System.out.println(modeloIncluir);
        modeloIncluir = this.service.incluir(modeloIncluir);

        //preparação para o retorno
        return ResponseEntity.ok(this.mapper.toDTO(modeloIncluir));
    }

    @PutMapping(path = "/{id}")
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
    public ResponseEntity<DTO> alterar(@RequestBody() DTO modeloDTO, @PathVariable(name = "id") PK_TYPE id
    ){
        ENTIDADE pModelo = mapper.toModelo(modeloDTO);
        ENTIDADE alterar = service.alterar(pModelo, id);
        return ResponseEntity.ok(mapper.toDTO(alterar));
    }
    @DeleteMapping(path ="/{id}")
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
    public ResponseEntity<DTO> remover(@PathVariable(name = "id") PK_TYPE id){
        ENTIDADE modeloExcluido = this.service.excluir(id);
        return ResponseEntity.ok(mapper.toDTO(modeloExcluido));
    }

    @GetMapping(path = "/{id}")
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
    public ResponseEntity<DTO> obterPorId(@PathVariable(name = "id") PK_TYPE id){
        ENTIDADE aluno = this.service.obterPeloId(id);
        return ResponseEntity.ok(this.mapper.toDTO(aluno));
    }

    @GetMapping(path = "/search-fields")
    @Operation(description = "Listagem dos campos de busca", responses = {
            @ApiResponse(responseCode = "200", description = "Listagem geral",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = SearchField.class)))),
            @ApiResponse(responseCode = "400", description = "Modelo não parametrizado para pesquisa",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<List<SearchField>> searchFieldsList(){
        List<SearchField> listSearchFields = service.listSearchFields();
        if(listSearchFields.isEmpty()){
            throw new BusinessException(ApiMessageCode.ERROR_SEARCH_PARAMETERS_NOT_DEFINED);
        }
        return ResponseEntity.ok(listSearchFields);
    }

    @PostMapping(path = "/search-fields")
    @Operation(description = "Realiza a busca pelos valores dos campos informados", responses = {
            @ApiResponse(responseCode = "200", description = "Listagem do resultado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema())),
            @ApiResponse(responseCode = "400", description = "falha ao realizar a busca",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<List<DTO>> searchFieldsAction(@RequestBody List<SearchFieldValue> searchFieldValues){
        List<ENTIDADE> listSearchFields = service.searchFieldValues(searchFieldValues);
        if(listSearchFields.isEmpty()){
            throw new BusinessException(ApiMessageCode.SEARCH_FIELDS_RESULT_NONE);
        }
        return ResponseEntity.ok(mapper.toDTO(listSearchFields));
    }

    @PostMapping(path = "/search-fields/page")
    @Operation(description = "Realiza a busca pelos valores dos campos informados", responses = {
            @ApiResponse(responseCode = "200", description = "Listagem do resultado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema())),
            @ApiResponse(responseCode = "400", description = "falha ao realizar a busca",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<List<DTO>> searchFieldsActionPage(
            @RequestBody List<SearchFieldValue> searchFieldValues,
            @PageableDefault(page = 0, size = 5)  Pageable page
    ){
        List<ENTIDADE> listSearchFields = service.searchFieldValues(searchFieldValues);
        if(listSearchFields.isEmpty()){
            throw new BusinessException(ApiMessageCode.SEARCH_FIELDS_RESULT_NONE);
        }
        return ResponseEntity.ok(mapper.toDTO(listSearchFields));
    }

}
