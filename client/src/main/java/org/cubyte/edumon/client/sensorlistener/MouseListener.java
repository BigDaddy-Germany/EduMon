package org.cubyte.edumon.client.sensorlistener;

import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class MouseListener implements NativeMouseInputListener {
    private int clicks;
    private int distance;

    private int oldX;
    private int oldY;

    public MouseListener() {
        this.clicks = 0;
        this.distance = 0;

        this.oldX = 0;
        this.oldY = 0;
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
        clicks++;
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {
        int x = nativeMouseEvent.getX();
        int y = nativeMouseEvent.getY();
        int dx = x - oldX;
        int dy = y - oldY;

        oldX = x;
        oldY = y;

        distance += (int) Math.sqrt(dx * dx + dy * dy); //TODO normalize to screendiagonal
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {

    }

    public int fetchClicks() {
        int c = clicks;
        clicks = 0;
        return c;
    }
    public int fetchDistance() {
        int d = distance;
        distance = 0;
        return d;
    }
}
