package redis.embedded;

import com.google.common.collect.Lists;
import redis.embedded.exceptions.EmbeddedRedisException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RedisCluster implements Redis {

    private final List<Redis> sentinels = new LinkedList<>();
    private final List<Redis> servers = new LinkedList<>();

    RedisCluster(List<Redis> sentinels, List<Redis> servers) {
        this.sentinels.addAll(sentinels);
        this.servers.addAll(servers);
    }

    @Override
    public boolean isActive() {
        for(Redis redis : sentinels) {
            if(!redis.isActive()) {
                return false;
            }
        }
        for(Redis redis : servers) {
            if(!redis.isActive()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void start() throws EmbeddedRedisException {
        for(Redis redis : sentinels) {
            redis.start();
        }
        for(Redis redis : servers) {
            redis.start();
        }
    }

    @Override
    public void stop() throws EmbeddedRedisException {
        for(Redis redis : sentinels) {
            redis.stop();
        }
        for(Redis redis : servers) {
            redis.stop();
        }
    }

    @Override
    public List<Integer> ports() {
        List<Integer> ports = new ArrayList<>();
        ports.addAll(sentinelPorts());
        ports.addAll(serverPorts());
        return ports;
    }

    @Override
    public List<Integer> tlsPorts() {
        List<Integer> ports = new ArrayList<>();
        ports.addAll(sentinelTlsPorts());
        ports.addAll(serverTlsPorts());
        return ports;
    }

    public List<Redis> sentinels() {
        return Lists.newLinkedList(sentinels);
    }

    public List<Integer> sentinelPorts() {
        List<Integer> ports = new ArrayList<>();
        for(Redis redis : sentinels) {
            ports.addAll(redis.ports());
        }
        return ports;
    }

    public List<Integer> sentinelTlsPorts() {
        List<Integer> ports = new ArrayList<>();
        for(Redis redis : sentinels) {
            ports.addAll(redis.tlsPorts());
        }
        return ports;
    }

    public List<Redis> servers() {
        return Lists.newLinkedList(servers);
    }

    public List<Integer> serverPorts() {
        List<Integer> ports = new ArrayList<>();
        for(Redis redis : servers) {
            ports.addAll(redis.ports());
        }
        return ports;
    }

    public List<Integer> serverTlsPorts() {
        List<Integer> ports = new ArrayList<>();
        for(Redis redis : servers) {
            ports.addAll(redis.tlsPorts());
        }
        return ports;
    }

    public static RedisClusterBuilder builder() {
        return new RedisClusterBuilder();
    }

}
