package org.cubyte.edumon.client.messaging;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.cubyte.edumon.client.messaging.messagebody.BodyDeserializer;
import org.cubyte.edumon.client.messaging.messagebody.MessageBody;
import org.cubyte.edumon.client.messaging.messagebody.NameList;
import org.cubyte.edumon.client.messaging.messagebody.ThumbRequest;
import org.cubyte.edumon.client.messaging.messagebody.util.Dimensions;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class MessageTest {
    ObjectMapper mapper;
    StringWriter writer;

    @Before
    public void before() {
        mapper = new ObjectMapper();
        writer = new StringWriter();
    }

    @Test
    public void testDeSerialization() {
        SimpleModule module = new SimpleModule("CustomBodyDeserializer", new Version(1, 0, 0, "", "org.cubyte", "edumon-client"));
        module.addDeserializer(MessageBody.class, new BodyDeserializer());
        mapper.registerModule(module);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        ArrayList<String> list = new ArrayList<>();
        list.add("Jonas Dann");
        Message message1 = new Message(new Date(), "daosdoiage345sdfbv", "MODERATOR", "160C", new NameList(list, "160C", new Dimensions(5, 5)));
        Message message2;

        try {
            message2 = mapper.readValue("{\"type\":4,\"time\":1429093632,\"from\":\"daosdoiage345sdfbv\"," +
                    "\"to\":\"MODERATOR\",\"room\":\"160C\",\"body\":{\"error\":\"error\"}}", Message.class);
        } catch (IOException e) {
            assertEquals("Instantiation of [simple type, class org.cubyte.edumon.client.messaging.Message] value failed: " +
                            "null (through reference chain: org.cubyte.edumon.client.messaging.Message[\"body\"])",
                    e.getMessage());
        }

        try {
            mapper.writeValue(writer, message1);
            String json = writer.toString();
            writer.close();
            message2 = mapper.readValue(json, Message.class);
            assertEquals(message1, message2);
        } catch (IOException e) {
            assert false;
        }
    }

    @Test
    public void testThumbRequestSerialization() {
        ThumbRequest thumbRequest1 = new ThumbRequest(1, ThumbRequest.FeedbackType.thumb);
        try {
            mapper.writeValue(writer, thumbRequest1);
            String json = writer.toString();
            assert json.contains("\"type\":\"thumb\"");
            ThumbRequest thumbRequest2 = mapper.readValue(json, ThumbRequest.class);
            assertEquals(thumbRequest1, thumbRequest2);
        } catch (IOException e) {
            assert false;
        }
    }
}
