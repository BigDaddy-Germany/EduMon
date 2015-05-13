package org.cubyte.edumon.client.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import org.cubyte.edumon.client.eventsystem.Bullet;
import org.cubyte.edumon.client.messaging.messagebody.MessageBody;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Jonas
 */
public class Message implements Bullet {
    @JsonSerialize(converter = TypeToIntegerConverter.class)
    public final Type type;
    @JsonSerialize(converter = DateToIntegerConverter.class)
    public final Date time;
    public final String from;
    public final String to;
    public final String room;
    public final MessageBody body;

    @JsonCreator
    public Message(@JsonProperty("time") Date time, @JsonProperty("from") String from,
                   @JsonProperty("to") String to, @JsonProperty("room") String room,
                   @JsonProperty("body") MessageBody body) {
        this.type = Type.getType(body.getClass());
        this.time = time;
        this.from = from;
        this.to = to;
        this.room = room;
        this.body = body;
    }

    @Override
    public Class getBulletClass() {
        return body.getClass();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return body.equals(message.body) && from.equals(message.from) &&
                room.equals(message.room) && to.equals(message.to) && type == message.type;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + room.hashCode();
        result = 31 * result + body.hashCode();
        return result;
    }

    public enum Type {
        // Don't change the order of the elements!!!
        NONE(0),
        NAME_LIST(1),
        WHO_AM_I(2),
        LOGIN_FEEDBACK(3),
        SENSOR_DATA(4),
        THUMB_REQUEST(5),
        THUMB_FEEDBACK(6),
        THUMB_RESULT(7),
        BREAK_REQUEST(8);

        private static final Type[] orderedTypes = new Type[9];
        private static final HashMap<Type, Class<? extends MessageBody>> toClassMap = new HashMap<>();
        private static final HashMap<Class<? extends MessageBody>, Type> classToTypeMap = new HashMap<>();
        private int index;

        private Type(int index) {
            this.index = index;
        }

        static {
            String typeString;
            for (Type type : Type.values()) {
                orderedTypes[type.index] = type;
                if (type == Type.NONE) {
                    continue;
                }
                typeString = type.toString().toLowerCase();
                String[] split = typeString.split("_");
                typeString = "";
                for (String string : split) {
                    typeString += string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
                }
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends MessageBody> clazz = (Class<? extends MessageBody>) Class.forName("org.cubyte.edumon.client.messaging.messagebody." + typeString);
                    Type.toClassMap.put(type, clazz);
                    Type.classToTypeMap.put(clazz, type);
                } catch (ClassNotFoundException e) {
                    System.err.println("Could not load type " + typeString + ".");
                    System.err.println(e.getMessage());
                }
            }
        }

        public static Class<? extends MessageBody> getClass(int i) {
            return toClassMap.get(getType(i));
        }

        public static Type getType(Class<? extends MessageBody> clazz) {
            return classToTypeMap.get(clazz);
        }

        public static Type getType(int i) {
            return orderedTypes[i];
        }

        public static Class<? extends MessageBody> getClass(List<String> fields) {
            Type type = Type.NONE;
            boolean isType;
            for (Type currType : Type.values()) {
                isType = true;
                if (currType != Type.NONE) {
                    for (String field : fields) {
                        try {
                            toClassMap.get(currType).getField(field);
                        } catch (NoSuchFieldException e) {
                            isType = false;
                            break;
                        }
                    }
                } else {
                    continue;
                }
                if (isType) {
                    type = currType;
                    break;
                }
            }
            if (type == Type.NONE) {
                return null;
            }
            return toClassMap.get(type);
        }
    }

    public static class TypeToIntegerConverter implements Converter<Type, Integer> {
        @Override
        public Integer convert(Type type) {
            return type.ordinal();
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

    public static class DateToIntegerConverter implements Converter<Date, Integer> {
        @Override
        public Integer convert(Date date) {
            return (int) Math.round(date.getTime() / 1000d);
        }

        @Override
        public JavaType getInputType(TypeFactory typeFactory) {
            return typeFactory.constructType(Date.class);
        }

        @Override
        public JavaType getOutputType(TypeFactory typeFactory) {
            return typeFactory.constructType(Integer.class);
        }
    }
}
