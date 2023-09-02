package br.ueg.prog.webi.api.util;

import br.ueg.prog.webi.api.exception.DevelopmentException;
import br.ueg.prog.webi.api.model.IEntidade;

import java.util.Objects;

public class CompositePkUtil {
    public static String getPkHash(IEntidade entidade){
        return null;
    }

    private static void validTableObject(IEntidade<?> table) {
        if(Objects.isNull(table)){
            throw new DevelopmentException("Entidade não informada: método=Reflexao.getJPATableName");
        }
    }
}
