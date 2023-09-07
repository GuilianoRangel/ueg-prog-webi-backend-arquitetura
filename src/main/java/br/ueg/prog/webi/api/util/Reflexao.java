package br.ueg.prog.webi.api.util;

import br.ueg.prog.webi.api.exception.DevelopmentException;
import br.ueg.prog.webi.api.model.BaseEntidade;
import br.ueg.prog.webi.api.model.IEntidade;
import br.ueg.prog.webi.api.model.annotation.PkComposite;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class Reflexao {
    public static String getJPATableName(IEntidade<?> table){
        validTableObject(table);
        Class<?> cls = table.getClass();
        Class<Table> tableAnotation = Table.class;
        if(cls.isAnnotationPresent(tableAnotation)){
            Table tableObj = cls.getAnnotation(tableAnotation);
            if(Strings.isNotEmpty(tableObj.name())){
                return tableObj.name();
            }
        }
        return cls.getSimpleName().toLowerCase();
    }

    private static void validTableObject(IEntidade<?> table) {
        if(Objects.isNull(table)){
            throw new DevelopmentException("Entidade não informada: método=Reflexao.getJPATableName");
        }
    }/*

    public static List<String> getJPATableIdFieldNames(IEntidade<?> table) {
        validTableObject(table);
        Class<?> cls = table.getClass();
        List<String> idFieldNames = new ArrayList<>();
        if(cls.isAnnotationPresent(IdClass.class)){
            IdClass clsIdClass = cls.getAnnotation(IdClass.class);
            Class clsPkComposite = clsIdClass.value();
            Field[] pkAttributes = getEntidadeFields(clsPkComposite);
            // TODO concluir o metodo;
            for(Field f : pkAttributes){

            }
            throw new DevelopmentException("Recurso em desenvolvimento-obter lista de IdName de chave composta");
        }else{
            Field[] declaredFields = getEntidadeFields(cls);
            for(Field f : declaredFields){
                if(f.isAnnotationPresent(Id.class)){
                    idFieldNames.add(f.getName());
                }
            }

        }
        return idFieldNames;
    }*/
    public static <T> T getJPATablePkObject(T type, IEntidade<?> entidade){
        validTableObject(entidade);
        Class<?> entidadeClass = entidade.getClass();
        if(((Class<?>)type).isAnnotationPresent(PkComposite.class)){
            return (T) getCompositoPkValue((Class<?>)type, entidade, entidadeClass);
        } else {
            return (T) getSinglePkValue(type, entidade, entidadeClass);
        }
    }

    public static <T> void setJPATablePkObject(T type, IEntidade<?> entidade, Object pkValue){
        validTableObject(entidade);
        Class<?> entidadeClass = entidade.getClass();
        if(((Class<?>)type).isAnnotationPresent(PkComposite.class)){
            getCompositoPkValue(type, entidade, entidadeClass);
        } else {
            setSinglePkValue(entidade, entidadeClass, pkValue);
        }
    }

    private static <T> T getSinglePkValue(T type, IEntidade<?> entidade, Class<?> entidadeClass) {
        String pkMethodGetFieldName = null ;
        for (Field field : getEntidadeFields(entidadeClass)) {
            if(field.isAnnotationPresent(Id.class)){
               pkMethodGetFieldName = "get"+uCFirst(field.getName());
               break;
            }
        }

        try {
            return (T) entidadeClass.getMethod(pkMethodGetFieldName).invoke(entidade);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    public static Boolean isEntidadeHavePkGenerated(IEntidade<?> entidade) {
        validTableObject(entidade);
        Class<?> entidadeClass = entidade.getClass();
        for (Field field : getEntidadeFields(entidadeClass)) {
            if(field.isAnnotationPresent(Id.class)){
                return field.isAnnotationPresent(GeneratedValue.class);
            }
        }
        return false;
    }

    private static void setSinglePkValue(IEntidade<?> entidade, Class<?> entidadeClass, Object pkValue) {
        String pkMethodGetFieldName = null ;
        Field pkField= null;
        for (Field field : getEntidadeFields(entidadeClass)) {
            if(field.isAnnotationPresent(Id.class)){
                pkMethodGetFieldName = "set"+uCFirst(field.getName());
                pkField = field;
            }
        }

        try {
            Method setPkMethod = entidadeClass.getMethod(pkMethodGetFieldName, pkField.getType());
            setPkMethod.invoke(entidade, pkValue);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T getCompositoPkValue(T type, IEntidade<?> entidade, Class<?> entidadeClass) {
        try {
            T objPk = getCompositePkObjectFromEntidade(type, entidade);
            Class<?> pkType = objPk.getClass();
            Field[] pkFields = pkType.getDeclaredFields();
            for (Field f : pkFields) {
                Field pkField = entidadeClass.getDeclaredField(f.getName());
                String pkMethodGetFieldName = "get" + uCFirst(pkField.getName());
                String pkMethodSetFieldName = "set" + uCFirst(pkField.getName());
                Method pkMethodGetField = getEntidadeMethod(entidadeClass,pkMethodGetFieldName);
                Method pkMethodSetField = pkType.getMethod(pkMethodSetFieldName, f.getType());
                Object pkPartialValue = pkMethodGetField.invoke(entidade);
                if (IEntidade.class.isAssignableFrom(pkMethodGetField.getReturnType())) {
                    IEntidade<?> pkObjectValue = (IEntidade<?>) pkPartialValue;
                    pkMethodSetField.invoke(objPk,getJPATablePkObject(f.getType(), pkObjectValue));
                } else {
                    pkMethodSetField.invoke(objPk, pkPartialValue);
                }
            }
            return objPk;
        } catch (NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Class<?>> Object getNewObjectFromType(T type) {
        Object aClass = null;
        try {
            if(type.isAnnotationPresent(PkComposite.class)){
                aClass =  type.getConstructor().newInstance();
            }

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return aClass;
    }

    public static <T> Object getNewEntidadeFromType(T type) {
        Object aClass = null;
        try {
            if(IEntidade.class.isAssignableFrom((Class<?>)type)){
                aClass =  ((Class<?>)type).getConstructor().newInstance();
            }

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return aClass;
    }

    public static <T> T getCompositePkObjectFromEntidade(T type, IEntidade entidade) {
        T pkObject = getCurrentCompositePkObject(type, entidade);
        if(Objects.isNull(pkObject)){
            pkObject = (T) getNewObjectFromType(type.getClass());
        }
        return pkObject;
    }

    /**
     * Recebe uma entidade como parametro e recupera um objeto com o valor da PK
     * não cria o objeto apenas recupera caso ele exista
     * @param entidade
     * @return
     */
    private static <T> T getCurrentCompositePkObject(T type, IEntidade entidade) {
        T pkObject;
        try {
            Field pkTypeObject1 = getEntidadeField(entidade.getClass(), IEntidade.COMPOSITE_PK_FIELD_NAME);
            String getPkObjectMethodName = "get"+uCFirst(pkTypeObject1.getName());
            Method getPkObject = getEntidadeMethod(entidade.getClass(),getPkObjectMethodName);
            pkObject = (T) getPkObject.invoke(entidade);
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return pkObject;
    }



    public static String uCFirst(String str){
        return str.substring(0,1).toUpperCase()+str.substring(1);
    }

    public static Field[] getEntidadeFields(Class<?> cls){
        List<Field> fields = new ArrayList<>();
        Class<?> localCls = cls;
        do{
            Field[] localFields = localCls.getDeclaredFields();
            fields.addAll(List.of(localFields));
            localCls = localCls.getSuperclass();
        }while(IEntidade.class.isAssignableFrom(localCls));

        return fields.toArray(new Field[0]);
    }
    public static Method[] getEntidadeMethods(Class<?> cls){
        List<Method> fields = new ArrayList<>();
        Class<?> localCls = cls;
        do{
            Method[] localFields = localCls.getDeclaredMethods();
            fields.addAll(List.of(localFields));
            localCls = localCls.getSuperclass();
        }while(IEntidade.class.isAssignableFrom(localCls));

        return fields.toArray(new Method[0]);
    }
    public static Field getEntidadeField(Class<?> cls, String fieldName) throws NoSuchFieldException {
        Field fieldResult = null;
        for (Field entidadeField : getEntidadeFields(cls)) {
            if(entidadeField.getName().equals(fieldName)){
                fieldResult = entidadeField;
                break;
            }
        }
        if (fieldResult == null) {
            throw new NoSuchFieldException(fieldName);
        }
        return fieldResult;
    }

    public static Method getEntidadeMethod(Class<?> cls, String fieldName) throws NoSuchMethodException {
        Method MethodResult = null;
        for (Method entidadeField : getEntidadeMethods(cls)) {
            if(entidadeField.getName().equals(fieldName)){
                MethodResult = entidadeField;
                break;
            }
        }
        if (MethodResult == null) {
            throw new NoSuchMethodException(fieldName);
        }
        return MethodResult;
    }

    public static <T> String getJPATablePkHash(T type, IEntidade<?> entidade) {
        validTableObject(entidade);
        T pkObject = getJPATablePkObject(type, entidade);
        String pkString = null;
        if(((Class<?>)type).isAnnotationPresent(PkComposite.class)){
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                pkString = objectMapper.writeValueAsString(pkObject);

            } catch (JsonProcessingException e) {
                throw new DevelopmentException("Erro ao Serializar a PK:",e);
            }
        }else{
            pkString = pkObject.toString();
        }
        pkString = Base64.getEncoder().encodeToString(pkString.getBytes());
        return pkString;
    }

    public static <T, TCLASS extends Class<?>> T getJPATablePkObjectFromHash(TCLASS typeClass, T type, String pkHash) {
        T pkObject = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            pkObject = (T) objectMapper.readValue(new String(Base64.getDecoder().decode(pkHash.getBytes())), (Class<?>)typeClass);
        } catch (JsonProcessingException e) {
            throw new DevelopmentException("Erro ao Serializar a PK:",e);
        } catch (IOException e) {
            throw new DevelopmentException("Erro ao Serializar a PK:",e);
        }
        return pkObject;
    }
}
