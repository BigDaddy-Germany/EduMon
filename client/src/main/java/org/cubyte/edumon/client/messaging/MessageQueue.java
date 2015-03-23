package org.cubyte.edumon.client.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageQueue {
    private CopyOnWriteArrayList<Message> queuedMessages;
    private CloseableHttpClient httpClient;
    private String serverAddress;
    private ObjectMapper mapper;

    public MessageQueue(String serverAddress) {
        queuedMessages = new CopyOnWriteArrayList<>();
        httpClient = HttpClients.createDefault();
        this.serverAddress = serverAddress;
        mapper = new ObjectMapper();
    }

    public void send() /*TODO throws cannotSendException*/ {
        StringWriter writer = new StringWriter();
        int queueSize = queuedMessages.size();
        String jsonString = (queueSize > 1) ? "[" : "";
        for (int i = 0; i < queueSize; i++) {
            try {
                mapper.writeValue(writer, queuedMessages.get(i));
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
            jsonString += writer.toString() + ((i == queueSize - 1) ? "" : ",");
            writer.flush();
        }
        jsonString += (queueSize > 1) ? "]" : "";
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        queuedMessages.clear(); //TODO only clear if messages can be sent

        HttpPost post = new HttpPost(serverAddress);
        try {
            post.setEntity(new StringEntity(jsonString));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(System.err);
        }
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(post);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        //TODO throw Events
    }

    public void queue(Message message) {
        queuedMessages.add(message);
    }
}