package org.cubyte.edumon.client.messaging.messagebody;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.cubyte.edumon.client.messaging.Message;

public class BodyResolver implements TypeIdResolver {
    TypeFactory typeFactory;
    JavaType baseType;

    public BodyResolver() {
        typeFactory = TypeFactory.defaultInstance();
    }

    @Override
    public void init(JavaType javaType) {
        this.baseType = javaType;
    }

    @Override
    public String idFromValue(Object o) {
        return String.valueOf(Message.Type.getType((Class<? extends MessageBody>)o.getClass()).getIndex());
    }

    @Override
    public String idFromValueAndType(Object o, Class<?> aClass) {
        return idFromValue(o);
    }

    @Override
    public String idFromBaseType() {
        return idFromValueAndType(null, baseType.getRawClass());
    }

    @Override
    public JavaType typeFromId(String s) {
        return typeFactory.constructType(Message.Type.getClass(Integer.parseInt(s)));
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }
}
