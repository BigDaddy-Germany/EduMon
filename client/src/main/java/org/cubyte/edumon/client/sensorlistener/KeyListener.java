package org.cubyte.edumon.client.sensorlistener;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.concurrent.atomic.AtomicInteger;

public class KeyListener implements NativeKeyListener {
    private AtomicInteger strokes = new AtomicInteger(0);

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        strokes.incrementAndGet();
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    public int fetchStrokes() {
        return strokes.getAndSet(0);
    }
}
