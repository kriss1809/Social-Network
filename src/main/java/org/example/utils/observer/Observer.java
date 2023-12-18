package org.example.utils.observer;

import org.example.service.Service;
import org.example.utils.events.ChangeEvent;
import org.example.utils.events.Event;

public interface Observer <E extends Event> {
    void update(E e, Service service);
}