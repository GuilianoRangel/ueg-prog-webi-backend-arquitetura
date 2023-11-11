package br.ueg.prog.webi.api.converters;

import br.ueg.prog.webi.api.interfaces.IConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
public class LocalDateConverter implements IConverter {
    private static final Logger LOG =
            LoggerFactory.getLogger(LocalDateConverter.class);
    @Override
    public Object converter(String value) {
        if(Objects.nonNull(value)){
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return LocalDate.parse(value, formatter);
            }catch (Exception e){
                LOG.error("Erro ao Converter valor(%s) para LocalDate",value);
            }
        }
        return null;
    }
}
