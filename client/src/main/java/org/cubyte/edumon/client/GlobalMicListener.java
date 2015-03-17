package org.cubyte.edumon.client;

import javax.sound.sampled.*;

public class GlobalMicListener {
    private TargetDataLine line;

    public GlobalMicListener() {
        AudioFormat format = new AudioFormat(8000, 16, 1, true, true);
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

    public int fetchLevel() {
        int numBytesRead;
        byte[] data = new byte[line.getBufferSize() / 5];

        numBytesRead = line.read(data, 0, data.length);

        return numBytesRead;
    }
}
