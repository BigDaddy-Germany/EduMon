package org.cubyte.edumon.client;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

public class GlobalMicListener {
    private TargetDataLine line;

    public GlobalMicListener() {
        AudioFormat format = new AudioFormat(8000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            //TODO
        }
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (LineUnavailableException e) {
            //TODO
        }
        line.start();
    }

    public double fetchLevel() {
        ArrayList<Byte> audioData = new ArrayList<>();
        int available = line.available();
        while(available > 0) {
            byte[] data = new byte[available];
            line.read(data, 0, data.length);
            for (byte b: data) {
                audioData.add(b);
            }
            available = line.available();
        }
        line.flush();

        return calculateLevel(audioData);
    }

    private float calculateLevel(List<Byte> buffer) {
        int maxLevel = 0;
        int level = 0;
        for (int i = 0; i < buffer.size(); i += 2) {
            level += (buffer.get(i + 1) << 8) | buffer.get(i);
            maxLevel = Math.max(maxLevel, (buffer.get(i + 1) << 8) | buffer.get(i));
        }
        //return (float) maxLevel / Short.MAX_VALUE;
        return (float) (level / Short.MAX_VALUE) / (buffer.size() / 2);
    }
}
