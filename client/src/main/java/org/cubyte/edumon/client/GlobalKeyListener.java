package org.cubyte.edumon.client;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GlobalKeyListener implements NativeKeyListener {
    private int strokes = 0;

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        strokes++;
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    public int fetchStrokes() {
        int s = strokes;
        strokes = 0;
        return s;
    }
}
