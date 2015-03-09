package org.cubyte.edumon.core.protocol.serialization.serializers;

import org.cubyte.edumon.core.protocol.serialization.Serialization;
import org.cubyte.edumon.core.protocol.serialization.SerializationException;
import org.cubyte.edumon.core.protocol.serialization.Serializer;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;

public class ReflectiveObjectSerializer<T> implements Serializer<T>
{
    private final Class<T> clazz;
    private final InstanceFactory<T> factory;
    private final LinkedList<Field> fields;

    public ReflectiveObjectSerializer(final Class<T> clazz)
    {
        this(clazz, new DefaultInstanceFactory<T>(clazz));
    }

    private static boolean isFieldApplicable(Field f)
    {
        final int modifiers = f.getModifiers();
        if (Modifier.isFinal(modifiers))
        {
            return false;
        }
        if (Modifier.isTransient(modifiers))
        {
            return false;
        }
        if (Modifier.isStatic(modifiers))
        {
            return false;
        }
        return true;
    }

    public ReflectiveObjectSerializer(Class<T> clazz, InstanceFactory<T> factory)
    {
        this.clazz = clazz;
        this.factory = factory;
        this.fields = new LinkedList<Field>();

        Class<?> c = clazz;
        do
        {
            for (Field field : c.getDeclaredFields())
            {
                if (isFieldApplicable(field))
                {
                    field.setAccessible(true);
                    this.fields.add(field);
                }
            }
            c = c.getSuperclass();
        }
        while (c != null);
    }

    @Override
    public int serialize(T object, ByteBuf data) throws SerializationException
    {
        if (object == null)
        {
            throw new SerializationException("can't serialize null object!");
        }

        int bytesWritten = 0;
        for (Field f : this.fields)
        {
            try
            {
                bytesWritten += Serialization.serialize(f.get(object), data);
            }
            catch (IllegalAccessException e)
            {
                throw new SerializationException("Unable to get field " + f.getName() + " in class " + this.clazz.getName(), e);
            }
        }
        return bytesWritten;
    }

    @Override
    public T deserialize(ByteBuf data) throws SerializationException
    {
        T object = this.factory.newInstance();
        if (object == null)
        {
            throw new SerializationException("Failed to create a new instance of the class: " + this.clazz.getName());
        }

        for (Field f : this.fields)
        {
            try
            {
                f.set(object, Serialization.deserialize(f.getType(), data));
            }
            catch (IllegalAccessException e)
            {
                throw new SerializationException("Unable to set field " + f.getName() + " in class " + this.clazz.getName(), e);
            }
        }

        return object;
    }

    private interface InstanceFactory<T>
    {
        T newInstance();
    }

    private static class DefaultInstanceFactory<T> implements InstanceFactory<T>
    {
        private final Class<T> clazz;

        public DefaultInstanceFactory(Class<T> clazz)
        {
            this.clazz = clazz;
        }

        @Override
        public T newInstance()
        {
            try
            {
                return clazz.newInstance();
            }
            catch (InstantiationException e)
            {
                e.printStackTrace(System.err);
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace(System.err);
            }
            return null;
        }
    }
}
