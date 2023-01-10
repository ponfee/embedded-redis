package redis.embedded;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedisServer extends AbstractRedisInstance {
    private static final String REDIS_READY_PATTERN = ".*(R|r)eady to accept connections.*";
    public static final int DEFAULT_REDIS_PORT = 6379;

    RedisServer(int port, int tlsPort, List<String> args) {
        super(port, tlsPort, new ArrayList<>(args));
    }

    public RedisServer(int port, File executable) {
        super(port, Arrays.asList(executable.getAbsolutePath(), "--port", Integer.toString(port)));
    }

    public RedisServer(int port, RedisExecProvider redisExecProvider) throws IOException {
        super(port,Arrays.asList(redisExecProvider.get().getAbsolutePath(), "--port", Integer.toString(port)));
    }

    @Override
    protected String redisReadyPattern() {
        return REDIS_READY_PATTERN;
    }

    public static RedisServerBuilder builder() {
        return new RedisServerBuilder();
    }
}
