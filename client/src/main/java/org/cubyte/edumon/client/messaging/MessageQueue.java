package org.cubyte.edumon.client.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MessageQueue {
    private CopyOnWriteArrayList<Message> queuedMessages;
    private AsyncHttpClient asyncHttpClient;
    private String serverAddress;
    private ObjectMapper mapper;

    public MessageQueue(String serverAddress) {
        queuedMessages = new CopyOnWriteArrayList<>();
        AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder().setConnectTimeout(1000).setReadTimeout(1000).build();
        asyncHttpClient = new AsyncHttpClient(config);
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
                System.out.println(e.getMessage());
            }
            jsonString += writer.toString() + ((i == queueSize - 1) ? "" : ",");
            writer.flush();
        }
        jsonString += (queueSize > 1) ? "]" : "";
        try {
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        queuedMessages.clear();

        try {
            Future<ArrayList<Message>> future = asyncHttpClient.preparePost(serverAddress).addFormParam("messages", jsonString).execute(new RetrievedMessagesHandler());
            ArrayList<Message> messages = future.get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e.getMessage());
        }
        //TODO throw Events
    }

    public void queue(Message message) {
        queuedMessages.add(message);
    }

    private class RetrievedMessagesHandler extends AsyncCompletionHandler<ArrayList<Message>> {
        @Override
        public ArrayList<Message> onCompleted(Response response) throws Exception {
            return null;
        }
    }
}