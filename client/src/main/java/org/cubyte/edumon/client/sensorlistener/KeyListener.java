package org.cubyte.edumon.client.sensorlistener;

import org.cubyte.edumon.client.Scene;
import org.cubyte.edumon.client.controller.ThumbfeedbackController;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jonas
 */
public class KeyListener implements NativeKeyListener {
    private boolean f1Down = false;
    private boolean f2Down = false;
    private AtomicInteger strokes = new AtomicInteger(0);

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        strokes.incrementAndGet();
        if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_F1 && !f1Down) {
            f1Down = true;
            ((ThumbfeedbackController) Scene.THUMBFEEDBACK.getController()).handleKeyDown();
        } else if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_F2 && !f2Down) {
            ((ThumbfeedbackController) Scene.THUMBFEEDBACK.getController()).handleMentallyAbsent();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_F1) {
            f1Down = false;
            ((ThumbfeedbackController) Scene.THUMBFEEDBACK.getController()).handleKeyUp();
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    public int fetchStrokes() {
        return strokes.getAndSet(0);
    }
}
