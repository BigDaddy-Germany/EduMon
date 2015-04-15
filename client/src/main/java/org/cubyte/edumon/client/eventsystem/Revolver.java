package org.cubyte.edumon.client.eventsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Revolver<T extends Bullet> {
    private final BlockingQueue<T> bullets;
    private final Map<Class, List<Victim>> bulletVictimMap;

    public Revolver() {
        bullets = new LinkedBlockingQueue<>();
        bulletVictimMap = new HashMap<>();
    }

    public void execute() {
        T bullet = null;
        try {
            bullet = bullets.take();
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted");
        }
        if (bullet != null) {
            List<Victim> victims = bulletVictimMap.get(bullet.getBulletClass());
            for (Victim victim : victims) {
                victim.take(bullet);
            }
        }
    }

    public void aimAt(Class clazz, Victim victim) {
        if (bulletVictimMap.get(clazz) == null) {
            bulletVictimMap.put(clazz, new ArrayList<Victim>());
        }
        bulletVictimMap.get(clazz).add(victim);
    }

    public void load(T bullet) {
        try {
            bullets.put(bullet);
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted");
        }
    }

    public boolean hasBullets() {
        return bullets.size() > 0;
    }

    public boolean peek() {
        return bullets.peek() != null;
    }
}
