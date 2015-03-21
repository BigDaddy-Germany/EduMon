package org.cubyte.edumon.client;

import javax.sound.sampled.*;

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

    public double calculateRMSLevel(byte[] audioData)
    { // audioData might be buffered data read from a data line
        int length = audioData.length;
        long lSum = 0;
        for(int i=0; i<length; i++)
            lSum = lSum + audioData[i];

        if (lSum == 0) {
            return 0;
        }
        double dAvg = lSum / length;

        double sumMeanSquare = 0d;
        for(int j=0; j<audioData.length; j++)
            sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - dAvg, 2d);

        double averageMeanSquare = sumMeanSquare / audioData.length;
        return (Math.pow(averageMeanSquare,0.5d) + 0.5);
    }

    public double fetchLevel() {
        int available = line.available();
        if (available == 0) {
            return 0;
        }
        byte[] data = new byte[available];
        double[] amplitude = new double[available];
        double level = 0;

        line.read(data, 0, data.length);

        if (data[0] < 0) data[0] += 256;
        for (int i = 0; i < available - 1; i++) {
            if (data[i + 1] < 0) data[i + 1] += 256;
            amplitude[i] = (double) (data[i + 1] << 8 | data[i] & 0xFF) / 32767.0;
            level += amplitude[i];
        }
        //return calculateRMSLevel(data);
        return level;
    }
}
