package org.cubyte.edumon.client.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import org.cubyte.edumon.client.messaging.messagebodies.MessageBody;

import java.util.Date;
import java.util.HashMap;

public class Message {
    public enum Type {
        NAMELIST,
        MYNAMEANDSEAT,
        LOGINSUCCESS,
        SENSORDATA,
        THUMBFEEDBACK,
        BREAKREQUEST,
        NONE;

        private static final HashMap<Type, Class<? extends MessageBody>> toClassMap = new HashMap<>();
        private static final HashMap<Class<? extends MessageBody>, Type> classToTypeMap = new HashMap<>();

        static {
            String typeString;
            for(Type type: Type.values()) {
                if (type == Type.NONE) {continue;}
                typeString = type.toString().toLowerCase();
                typeString = typeString.substring(0, 1).toUpperCase() + typeString.substring(1).toLowerCase();
                try {
                    Class<? extends MessageBody> clazz = (Class<? extends MessageBody>) Class.forName("org.cubyte.edumon.client.messaging.messagebodies." + typeString);
                    Type.toClassMap.put(type, clazz);
                    Type.classToTypeMap.put(clazz, type);
                } catch (ClassNotFoundException e) {
                    //TODO
                }
            }
        }

        public static Class<? extends MessageBody> getClass(String string) {
            return toClassMap.get(Type.valueOf(string));
        }

        public static Class<? extends MessageBody> getClass(int i) {
            return toClassMap.get(Type.values()[i - 1]);
        }

        public static Type getType(Class<? extends MessageBody> clazz) {
            return classToTypeMap.get(clazz);
        }

        public static Type getType(int i) {
            return Type.values()[i - 1];
        }

        public int getIndex() {
            return this.ordinal() + 1;
        }
    }

    @JsonSerialize(converter = TypeToIntegerConverter.class)
    @JsonDeserialize(converter = IntegerToTypeConverter.class)
    public final Type type;
    public final int id;
    public final Date time;
    public final String from;
    public final String to;
    public final String room;
    public final MessageBody body;

    @JsonCreator
    public Message(@JsonProperty("id") int id, @JsonProperty("time") Date time, @JsonProperty("from") String from,
                   @JsonProperty("to") String to, @JsonProperty("room") String room,
                   @JsonProperty("body") MessageBody body) {
        this.type = Type.getType(body.getClass());
        this.id = id;
        this.time = time;
        this.from = from;
        this.to = to;
        this.room = room;
        this.body = body;
    }

    public static class IntegerToTypeConverter implements Converter<Integer, Type> {
        @Override
        public Type convert(Integer i) {
            return Type.getType(i);
        }
        @Override
        public JavaType getInputType(TypeFactory typeFactory) {
            return typeFactory.constructType(Integer.class);
        }
        @Override
        public JavaType getOutputType(TypeFactory typeFactory) {
            return typeFactory.constructType(Type.class);
        }
    }
    public static class TypeToIntegerConverter implements Converter<Type, Integer> {
        @Override
        public Integer convert(Type type) {
            return type.getIndex();
        }
        @Override
        public JavaType getInputType(TypeFactory typeFactory) {
            return typeFactory.constructType(Type.class);
        }
        @Override
        public JavaType getOutputType(TypeFactory typeFactory) {
            return typeFactory.constructType(Integer.class);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (id != message.id) return false;
        if (!body.equals(message.body)) return false;
        if (!from.equals(message.from)) return false;
        if (!room.equals(message.room)) return false;
        if (!time.equals(message.time)) return false;
        if (!to.equals(message.to)) return false;
        if (type != message.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + id;
        result = 31 * result + time.hashCode();
        result = 31 * result + from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + room.hashCode();
        result = 31 * result + body.hashCode();
        return result;
    }
}
