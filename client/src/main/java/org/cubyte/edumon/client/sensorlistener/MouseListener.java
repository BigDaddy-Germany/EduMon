package org.cubyte.edumon.client.sensorlistener;

import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

import java.util.concurrent.atomic.AtomicInteger;

public class MouseListener implements NativeMouseInputListener {
    private AtomicInteger clicks;
    private AtomicInteger distance;

    private AtomicInteger oldX;
    private AtomicInteger oldY;

    public MouseListener() {
        this.clicks = new AtomicInteger(0);
        this.distance = new AtomicInteger(0);

        this.oldX = new AtomicInteger(0);
        this.oldY = new AtomicInteger(0);
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
        clicks.incrementAndGet();
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
        int dx = x - oldX.get();
        int dy = y - oldY.get();

        oldX.set(x);
        oldY.set(y);

        distance.addAndGet((int) Math.sqrt(dx * dx + dy * dy)); //TODO normalize to screendiagonal
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {

    }

    public int fetchClicks() {
        return clicks.getAndSet(0);
    }

    public int fetchDistance() {
        return distance.getAndSet(0);
    }
}
