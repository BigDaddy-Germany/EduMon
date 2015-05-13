package org.cubyte.edumon.client.eventsystem;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Jonas
 */
public interface Bullet {
    @JsonIgnore
    public Class getBulletClass();
}
