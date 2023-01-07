package redis.embedded.ports;

import redis.embedded.PortProvider;
import redis.embedded.exceptions.RedisBuildingException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class PredefinedPortProvider implements PortProvider {
    private final Iterator<Integer> current;

    public PredefinedPortProvider(Collection<Integer> ports) {
        this.current = new LinkedList<>(ports).iterator();
    }

    @Override
    public synchronized int next() {
        if (!current.hasNext()) {
            throw new RedisBuildingException("Run out of Redis ports!");
        }
        return current.next();
    }
}
