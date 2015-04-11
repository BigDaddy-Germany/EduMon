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
import org.cubyte.edumon.client.Main;
import org.cubyte.edumon.client.eventsystem.Revolver;
import org.cubyte.edumon.client.messaging.messagebody.BodyDeserializer;
import org.cubyte.edumon.client.messaging.messagebody.MessageBody;

import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageQueue extends Revolver<Message> {
    private Main owner;

    private CopyOnWriteArrayList<Message> queuedMessages;
    private CloseableHttpClient httpClient;
    private CookieStore cookieStore;
    private String sessionId;
    private boolean isModerator;
    private ObjectMapper mapper;

    public MessageQueue(Main owner) {
        this(owner, false);
    }

    public MessageQueue(Main owner, boolean isModerator) {
        this.owner = owner;

        this.queuedMessages = new CopyOnWriteArrayList<>();
        this.cookieStore = new BasicCookieStore();
        this.httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        this.sessionId = "";
        this.isModerator = isModerator;
        SimpleModule module = new SimpleModule("CustomBodyDeserializer", new Version(1, 0, 0, "", "org.cubyte", "edumon-client"));
        module.addDeserializer(MessageBody.class, new BodyDeserializer());
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(module);
        this.mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public void send() {
        int queueSize = queuedMessages.size();
        String jsonString = "";
        if (queueSize > 0) {
            StringWriter writer = new StringWriter();
            jsonString = "[";
            for (int i = 0; i < queueSize; i++) {
                try {
                    mapper.writeValue(writer, queuedMessages.get(i));
                } catch (IOException e) {
                    System.err.println("Could not write Json value.");
                    System.err.println(e.getMessage());
                }
                jsonString += writer.toString() + ((i == queueSize - 1) ? "" : ",");
                writer.flush();
            }
            jsonString += "]";
            try {
                writer.close();
            } catch (IOException e) {
                System.err.println("Could not close Json writer.");
                System.err.println(e.getMessage());
            }
        }

        String address = owner.getServer() + "?room=" + owner.getRoom();
        if (isModerator) {address += "&moderatorPassphrase=alohomora";}
        HttpPost post = new HttpPost(address);
        post.setEntity(new StringEntity(jsonString, ContentType.create("application/json", "utf-8")));

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            //TODO handle server not found
            Response jsonResponse = mapper.readValue(response.getEntity().getContent(), Response.class);
            sessionId = jsonResponse.clientId;
            for (Message message: jsonResponse.inbox) {
                load(message);
            }
            if (jsonResponse.errorMessages.size() > 0) {
                System.err.println("Response error messages: ");
            }
            for (String errorMessage: jsonResponse.errorMessages) {
                System.err.println(errorMessage);
            }
        } catch (IOException e) {
            System.err.println("Could not get response.");
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            return;
        }
        queuedMessages.clear();
    }

    public void queue(Message message) {
        queuedMessages.add(message);
    }

    public String getSessionId() {
        return sessionId;
    }
}