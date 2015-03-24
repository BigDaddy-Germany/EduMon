package org.cubyte.edumon.client.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.cubyte.edumon.client.eventsystem.Revolver;

import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageQueue extends Revolver<Message> {
    private CopyOnWriteArrayList<Message> queuedMessages;
    private CloseableHttpClient httpClient;
    private String serverAddress;
    private String room;
    private String sessionId;
    private boolean mod;
    private ObjectMapper mapper;

    public MessageQueue(String serverAddress, String room) {
        this.queuedMessages = new CopyOnWriteArrayList<>();
        this.httpClient = HttpClients.createDefault();
        this.serverAddress = serverAddress;
        this.room = room;
        this.sessionId = "";
        this.mapper = new ObjectMapper();
        this.mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public MessageQueue(String serverAddress, String room, boolean mod) {
        this.queuedMessages = new CopyOnWriteArrayList<>();
        this.httpClient = HttpClients.createDefault();
        this.serverAddress = serverAddress;
        this.room = room;
        this.sessionId = "";
        this.mod = mod;
        this.mapper = new ObjectMapper();
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
        if (!"".equals(sessionId)) {post.addHeader("Cookie", "PHPSESSID=" + sessionId);}
        post.setEntity(new StringEntity(jsonString, ContentType.create("application/json", "utf-8")));

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            sessionId = response.getFirstHeader("Set-Cookie").getValue().substring(10, 36);
            //BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            Message[] messages = mapper.readValue(response.getEntity().getContent(), Message[].class); //TODO handle error messages
            for (Message message: messages) {
                load(message);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public void queue(Message message) {
        queuedMessages.add(message);
    }
}