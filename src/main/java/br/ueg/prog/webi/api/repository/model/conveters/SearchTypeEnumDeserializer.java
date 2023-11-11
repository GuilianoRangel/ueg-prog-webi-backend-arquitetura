package br.ueg.prog.webi.api.repository.model.conveters;

import br.ueg.prog.webi.api.repository.model.SearchType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class SearchTypeEnumDeserializer extends JsonDeserializer<SearchType> {
    @Override
    public SearchType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        System.out.println("Teste");
        final ObjectCodec objectCodec = jsonParser.getCodec();
        final JsonNode node = objectCodec.readTree(jsonParser);
        final String type = node.asText();
        return SearchType.getSimpleOperation(type.toLowerCase());
    }
}
