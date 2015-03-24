package org.cubyte.edumon.client.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.cubyte.edumon.client.messaging.messagebody.BreakRequest;
import org.cubyte.edumon.client.messaging.messagebody.SensorData;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class MessageTest {
    @Test
    public void testDeSerialization() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        StringWriter writer = new StringWriter();
        //Message message1 = new MessageFactory(0, "Jonas", "Mod", "160C").create(new SensorData(12, 1673, 3, 0.87));
        Message message1 = new MessageFactory(0, "Jonas", "Mod", "160C").create(new BreakRequest());
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
