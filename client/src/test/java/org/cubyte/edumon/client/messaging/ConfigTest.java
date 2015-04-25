package org.cubyte.edumon.client.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cubyte.edumon.client.ClientConfig;
import org.cubyte.edumon.client.RoomState;
import org.cubyte.edumon.client.messaging.messagebody.NameList;
import org.cubyte.edumon.client.messaging.messagebody.util.Dimensions;
import org.cubyte.edumon.client.messaging.messagebody.util.Position;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static junit.framework.Assert.assertEquals;

public class ConfigTest {
    @Test
    public void testSavingOfConfig() {
        ConcurrentMap<String, RoomState> roomStateMap = new ConcurrentHashMap<>();
        List<String> nameList = new ArrayList<>();
        nameList.add("Jonas Dann");
        nameList.add("Marco Dörfler");
        roomStateMap.put("160C", new RoomState("testsessid", "Jonas Dann", new Position(1, 3),
                new NameList(nameList, "160C", new Dimensions(5, 5)), new Date()));
        roomStateMap.put("170C", new RoomState("oldsessid", "Jonas Dann", new Position(1, 3),
                new NameList(nameList, "170C", new Dimensions(5, 5)), new Date(new Date().getTime() - 4000000)));
        ClientConfig clientConfig1 = new ClientConfig("test.org", "160C", "Jonas Dann",
                new Position(1, 1), true, false, true, roomStateMap);

        File file;
        String separator = File.separator;
        if ("\\".equals(separator)) {
            File folder = new File(System.getenv("APPDATA") + separator + "EduMon");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            file = new File(System.getenv("APPDATA") + separator + "EduMon" + separator + "testconfig");
        } else {
            File folder = new File(System.getProperty("user.home") + separator + ".EduMon");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            file = new File(System.getProperty("user.home") + separator + ".EduMon" + separator + "testconfig");
        }
        ObjectMapper mapper = new ObjectMapper();

        clientConfig1.cleanRoomStateMap();
        try {
            mapper.writeValue(file, clientConfig1);
        } catch (IOException e) {
            assert false;
        }

        try {
            ClientConfig clientConfig2 = mapper.readValue(file, ClientConfig.class);
            assertEquals(clientConfig1, clientConfig2);
        } catch (IOException e) {
            assert false;
        }
    }
}
