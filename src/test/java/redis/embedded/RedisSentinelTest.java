package redis.embedded;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class RedisSentinelTest {
    private String bindAddress;

    private RedisSentinel sentinel;
    private RedisServer server;

    @Before
    public void setup() throws Exception {
        // Jedis translates ”localhost” to getLocalHost().getHostAddress() (see Jedis HostAndPort#getLocalHostQuietly),
        // which can vary from 127.0.0.1 (most notably, Debian/Ubuntu return 127.0.1.1)
        if (bindAddress == null) {
            bindAddress = Inet4Address.getLocalHost().getHostAddress();
        }
    }

    @Test(timeout = 10000L)
    public void testSimpleRun() throws Exception {
        server = RedisServer.builder().port(RedisServer.DEFAULT_REDIS_PORT).build();
        sentinel = RedisSentinel.builder().bind(bindAddress).build();
        sentinel.start();
        server.start();
        TimeUnit.SECONDS.sleep(1);
        server.stop();
        sentinel.stop();
    }

    @Test
    public void shouldAllowSubsequentRuns() throws Exception {
        sentinel = RedisSentinel.builder().bind(bindAddress).build();
        sentinel.start();
        sentinel.stop();

        sentinel.start();
        sentinel.stop();

        sentinel.start();
        sentinel.stop();
    }

    @Test
    public void testSimpleOperationsAfterRun() throws InterruptedException {
        //given
        server = RedisServer.builder().port(RedisServer.DEFAULT_REDIS_PORT).build();
        sentinel = RedisSentinel.builder().bind(bindAddress).build();
        server.start();
        sentinel.start();
        TimeUnit.SECONDS.sleep(5);

        try (
            JedisSentinelPool pool = new JedisSentinelPool("mymaster", Sets.newHashSet("localhost:26379"));
            Jedis jedis = pool.getResource()
        ) {
            jedis.mset("abc", "1", "def", "2");

            //then
            assertEquals("1", jedis.mget("abc").get(0));
            assertEquals("2", jedis.mget("def").get(0));
            assertNull(jedis.mget("xyz").get(0));
            System.out.println("testSimpleOperationsAfterRun end.");
        } finally {
            sentinel.stop();
            server.stop();
        }
    }

    @Test
    public void testAwaitRedisSentinelReady() throws Exception {
        String readyPattern = RedisSentinel.builder().build().redisReadyPattern();

        assertReadyPattern(new BufferedReader(
                        new InputStreamReader(getClass()
                                .getClassLoader()
                                .getResourceAsStream("redis-2.x-sentinel-startup-output.txt"))),
                readyPattern);

        assertReadyPattern(new BufferedReader(
                        new InputStreamReader(getClass()
                                .getClassLoader()
                                .getResourceAsStream("redis-3.x-sentinel-startup-output.txt"))),
                readyPattern);

        assertReadyPattern(new BufferedReader(
                        new InputStreamReader(getClass()
                                .getClassLoader()
                                .getResourceAsStream("redis-4.x-sentinel-startup-output.txt"))),
                readyPattern);
    }

    private void assertReadyPattern(BufferedReader reader, String readyPattern) throws IOException {
        String outputLine;
        do {
            outputLine = reader.readLine();
            assertNotNull(outputLine);
        } while (!outputLine.matches(readyPattern));
    }
}
