package org.cubyte.edumon.client.eventsystem;

/**
 * @author Jonas
 */
public interface Victim<T extends Bullet> {
    void take(T bullet);
}
