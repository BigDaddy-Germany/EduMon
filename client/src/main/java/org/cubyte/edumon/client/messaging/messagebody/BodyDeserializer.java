package org.cubyte.edumon.client.messaging.messagebody;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cubyte.edumon.client.messaging.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BodyDeserializer extends StdDeserializer<MessageBody> {

    public BodyDeserializer() {
        super(MessageBody.class);
    }

    @Override
    public MessageBody deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        ObjectNode root = mapper.readTree(jsonParser);
        Class<? extends MessageBody> messageBodyClass;
        Iterator<Map.Entry<String, JsonNode>> elementsIterator = root.fields();
        List<String> fieldList = new ArrayList<>();
        while (elementsIterator.hasNext()) {
            Map.Entry<String, JsonNode> element = elementsIterator.next();
            fieldList.add(element.getKey());
        }
        messageBodyClass = Message.Type.getClass(fieldList);
        if (messageBodyClass == null) {
            return null;
        }
        return mapper.readValue(root.traverse(), messageBodyClass);
    }
}
