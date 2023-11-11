package br.ueg.prog.webi.api.repository.model.conveters;

import br.ueg.prog.webi.api.repository.model.SearchType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class SearchTypeEnumSerializer extends JsonSerializer<SearchType> {

    @Override
    public void serialize(SearchType value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException{
        jgen.writeString(value.getId());
    }
}
