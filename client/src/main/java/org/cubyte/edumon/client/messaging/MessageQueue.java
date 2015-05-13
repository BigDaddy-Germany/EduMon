package org.cubyte.edumon.client.messaging;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.cubyte.edumon.client.Main;
import org.cubyte.edumon.client.eventsystem.Revolver;
import org.cubyte.edumon.client.messaging.messagebody.BodyDeserializer;
import org.cubyte.edumon.client.messaging.messagebody.MessageBody;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Jonas
 */
public class MessageQueue extends Revolver<Message> {
    private Main owner;

    private BlockingQueue<Message> queuedMessages;
    private CloseableHttpClient httpClient;
    private CookieStore cookieStore;
    private boolean isModerator;
    private ObjectMapper mapper;

    public MessageQueue(Main owner) {
        this(owner, false);
    }

    public MessageQueue(Main owner, boolean isModerator) {
        this.owner = owner;

        this.queuedMessages = new LinkedBlockingQueue<>();
        this.cookieStore = new BasicCookieStore();
        this.httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        this.isModerator = isModerator;
        SimpleModule module = new SimpleModule("CustomBodyDeserializer", new Version(1, 0, 0, "", "org.cubyte", "edumon-client"));
        module.addDeserializer(MessageBody.class, new BodyDeserializer());
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(module);
        this.mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public void send() {
        int queueSize = queuedMessages.size();
        List<Message> messages = new ArrayList<>();
        queuedMessages.drainTo(messages);
        StringWriter writer = new StringWriter();
        String jsonString = "";
        try {
            mapper.writeValue(writer, messages);
            jsonString = writer.toString();
        } catch (IOException e) {
            System.err.println("Could not write Json value.");
            System.err.println(e.getMessage());
        }
        writer.flush();
        try {
            writer.close();
        } catch (IOException e) {
            System.err.println("Could not close Json writer.");
            System.err.println(e.getMessage());
        }

        URI uri;
        try {
            uri = new URI(owner.getServer() + "/mailbox.php");
            uri = new URIBuilder(uri).addParameter("room", owner.getRoom()).build();
        } catch (URISyntaxException e) {
            System.err.println("Invalid URI.");
            System.err.println(e.getMessage());
            return;
        }

        HttpPost post = new HttpPost(uri);
        post.setEntity(new StringEntity(jsonString, ContentType.create("application/json", "utf-8")));

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                //System.err.println("Something didn't work!!!");
                queuedMessages.addAll(messages);
                return;
            }
            owner.incSent(queueSize);
            Response jsonResponse = mapper.readValue(response.getEntity().getContent(), Response.class);
            for (Message message : jsonResponse.inbox) {
                owner.incReceived(1);
                load(message);
            }
            if (jsonResponse.errorMessages.size() > 0) {
                System.err.println("Response error messages: ");
            }
            for (String errorMessage : jsonResponse.errorMessages) {
                System.err.println(errorMessage);
            }
        } catch (IOException e) {
            System.err.println("Could not parse response.");
            System.err.println(e.getMessage());
        }
    }

    public boolean ping() {
        HttpPost post = new HttpPost(owner.getServer() + "/mailbox.php");
        owner.incSent(1);
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            Response jsonResponse = mapper.readValue(response.getEntity().getContent(), Response.class);
            if (jsonResponse.errorMessages.size() == 1) {
                if ("Please select a valid room.".equals(jsonResponse.errorMessages.get(0))) {
                    return true;
                }
            }
        } catch (IOException e) {
        }
        return false;
    }

    public void queue(Message message) {
        queuedMessages.add(message);
    }

    public void setSessionId() {
        BasicClientCookie cookie = new BasicClientCookie("EDUMONSESSID", owner.getRoomState().sessionId);
        cookie.setDomain(URI.create(owner.getServer()).getHost());
        cookie.setPath("/");
        cookieStore.addCookie(cookie);
    }

    public String getSessionId() {
        for (Cookie cookie : cookieStore.getCookies()) {
            if ("EDUMONSESSID".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void resetSessionId() {
        cookieStore.clear();
    }

    public void clear() {
        queuedMessages.clear();
    }
}
