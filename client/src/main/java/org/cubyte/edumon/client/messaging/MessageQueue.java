package org.cubyte.edumon.client.messaging;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.cubyte.edumon.client.eventsystem.Revolver;
import org.cubyte.edumon.client.messaging.messagebody.BodyDeserializer;
import org.cubyte.edumon.client.messaging.messagebody.MessageBody;

import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageQueue extends Revolver<Message> {
    private CopyOnWriteArrayList<Message> queuedMessages;
    private CloseableHttpClient httpClient;
    private CookieStore cookieStore;
    private String serverAddress;
    private String room;
    private String sessionId;
    private boolean mod;
    private ObjectMapper mapper;

    public MessageQueue(String serverAddress, String room) {
        this(serverAddress, room, false);
    }

    public MessageQueue(String serverAddress, String room, boolean mod) {
        this.queuedMessages = new CopyOnWriteArrayList<>();
        this.cookieStore = new BasicCookieStore();
        this.httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        this.serverAddress = serverAddress;
        this.room = room;
        this.sessionId = "";
        this.mod = mod;
        SimpleModule module = new SimpleModule("CustomBodyDeserializer", new Version(1, 0, 0, null));
        module.addDeserializer(MessageBody.class, new BodyDeserializer());
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(module);
        this.mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public void send() /*TODO throws cannotSendException*/ {
        int queueSize = queuedMessages.size();
        String jsonString = "";
        if (queueSize > 0) {
            StringWriter writer = new StringWriter();
            jsonString = "[";
            for (int i = 0; i < queueSize; i++) {
                try {
                    mapper.writeValue(writer, queuedMessages.get(i));
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
                jsonString += writer.toString() + ((i == queueSize - 1) ? "" : ",");
                writer.flush();
            }
            jsonString += "]";
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
        queuedMessages.clear(); //TODO only clear if messages can be sent

        String address = serverAddress + "?room=" + room;
        if (mod) {address += "&moderatorPassphrase=alohomora";}
        HttpPost post = new HttpPost(address);
        post.setEntity(new StringEntity(jsonString, ContentType.create("application/json", "utf-8")));

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            Response jsonResponse = mapper.readValue(response.getEntity().getContent(), Response.class); //TODO handle error messages
            sessionId = jsonResponse.clientId;
            for (Message message: jsonResponse.inbox) {
                load(message);
            }
            System.out.println(jsonResponse.errorMessages);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public void queue(Message message) {
        queuedMessages.add(message);
    }

    public String getSessionId() {
        return sessionId;
    }
}