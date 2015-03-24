package org.cubyte.edumon.client.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.cubyte.edumon.client.eventsystem.Revolver;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageQueue extends Revolver<Message> {
    private CopyOnWriteArrayList<Message> queuedMessages;
    private CloseableHttpClient httpClient;
    private String serverAddress;
    private String room;
    private String sessionId;
    private ObjectMapper mapper;

    public MessageQueue(String serverAddress, String room) {
        this.queuedMessages = new CopyOnWriteArrayList<>();
        this.httpClient = HttpClients.createDefault();
        this.serverAddress = serverAddress;
        this.room = room;
        this.sessionId = "";
        this.mapper = new ObjectMapper();
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

        HttpPost post = new HttpPost(serverAddress);
        if (!"".equals(sessionId)) {post.addHeader("Cookie", "PHPSESSID=" + sessionId);}
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("room", room));
        params.add(new BasicNameValuePair("outbox", jsonString));
        try {
            post.setEntity(new UrlEncodedFormEntity(params));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(System.err);
        }
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            sessionId = response.getFirstHeader("Set-Header").getValue();
            Message[] messages = mapper.readValue(response.getEntity().getContent(), Message[].class);
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