package org.cubyte.edumon.client.sensorlistener;

import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MouseListener implements NativeMouseInputListener {
    private AtomicInteger clicks;
    private double distance;

    private AtomicInteger oldX;
    private AtomicInteger oldY;

    private double screenDiagonal;

    public MouseListener() {
        this.clicks = new AtomicInteger(0);

        this.oldX = new AtomicInteger(0);
        this.oldY = new AtomicInteger(0);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screenWidth = screenSize.getWidth();
        double screenHeight = screenSize.getHeight();
        screenDiagonal = Math.sqrt(screenWidth * screenWidth + screenHeight * screenHeight);
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

        distance += (Math.sqrt(dx * dx + dy * dy) / screenDiagonal);
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {

    }

    public int fetchClicks() {
        return clicks.getAndSet(0);
    }

    public double fetchDistance() {
        double dist = distance;
        distance = 0;
        return dist;
    }
}
