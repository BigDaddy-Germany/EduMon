package org.cubyte.edumon.client.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

public class MessageQueue {
    private CopyOnWriteArrayList<Message> quequedMessages;
    private AsyncHttpClient asyncHttpClient;
    private String serverAddress;
    private ObjectMapper mapper;

    public MessageQueue(String serverAddress) {
        quequedMessages = new CopyOnWriteArrayList<>();
        asyncHttpClient = new AsyncHttpClient();
        this.serverAddress = serverAddress;
        mapper = new ObjectMapper();
    }

    public void send() /*TODO throws cannotSendException*/ {
        StringWriter writer = new StringWriter();
        String jsonString = "["; // TODO dont write array if there is only one message
        for (Message message: quequedMessages) {
            try {
                mapper.writeValue(writer, message);
            } catch (IOException e) {
                //TODO
            }
            jsonString += writer.toString() + ",";
            writer.flush();
        }
        jsonString += "]";
        try {
            writer.close();
        } catch (IOException e) {
            //TODO
        }
        quequedMessages.clear();

        try {
            ArrayList<Message> messages = asyncHttpClient.preparePost(serverAddress).addFormParam("messages", jsonString).execute(new RetrievedMessagesHandler()).get();
        } catch (InterruptedException e) {
            //TODO
        } catch (ExecutionException e) {
            //TODO
        }
        //TODO throw Events
    }

    public void queue(Message message) {
        quequedMessages.add(message);
    }

    private class RetrievedMessagesHandler extends AsyncCompletionHandler<ArrayList<Message>> {
        @Override
        public ArrayList<Message> onCompleted(Response response) throws Exception {
            return null;
        }
    }
}