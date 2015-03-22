package org.cubyte.edumon.client.messaging.messagebodies;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
@JsonTypeIdResolver(BodyResolver.class)
public abstract class MessageBody {
}
