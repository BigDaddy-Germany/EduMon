package org.cubyte.edumon.client.messaging;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.cubyte.edumon.client.ClientConfig;
import org.cubyte.edumon.client.Main;
import org.cubyte.edumon.client.messaging.messagebody.*;
import org.cubyte.edumon.client.messaging.messagebody.util.Dimensions;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class MessageTest {
    @Test
    public void testDeSerialization() {
        SimpleModule module = new SimpleModule("CustomBodyDeserializer", new Version(1, 0, 0, "", "org.cubyte", "edumon-client"));
        module.addDeserializer(MessageBody.class, new BodyDeserializer());
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        StringWriter writer = new StringWriter();
        ArrayList<String> list = new ArrayList<>();
        list.add("Jonas Dann");
        Message message1 = new Message(new Date(), "daosdoiage345sdfbv", "MODERATOR", "160C", new NameList(list, "160C", new Dimensions(5, 5)));
        Message message2;
        String json;

        try {
            mapper.writeValue(writer, message1);
            json = writer.toString();
            writer.close();
            message2 = mapper.readValue(json, Message.class);
            assertEquals(message1, message2);
        } catch (IOException e) {
            assert false;
        }
    }
}
