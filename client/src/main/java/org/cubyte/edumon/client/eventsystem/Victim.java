package org.cubyte.edumon.client.eventsystem;

public interface Victim<T extends Bullet> {
    void take(T bullet);
}
