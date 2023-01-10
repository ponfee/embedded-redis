package redis.embedded;

import com.google.common.io.Resources;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.embedded.exceptions.RedisBuildingException;
import redis.embedded.util.Architecture;
import redis.embedded.util.OS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

public class RedisServerTest {

    private RedisServer redisServer;

    @Test(timeout = 7000L)
    public void testSimpleRun() throws InterruptedException {
        redisServer = RedisServer.builder().port(6379).build();
        redisServer.start();
        Thread.sleep(1000L);
        redisServer.stop();
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotAllowMultipleRunsWithoutStop() {
        try {
            redisServer = RedisServer.builder().port(6379).build();
            redisServer.start();
            redisServer.start();
        } finally {
            redisServer.stop();
        }
    }

    @Test
    public void shouldAllowSubsequentRuns() {
        redisServer = RedisServer.builder().port(6379).build();
        redisServer.start();
        redisServer.stop();

        redisServer.start();
        redisServer.stop();

        redisServer.start();
        redisServer.stop();
    }

    @Test
    public void testSimpleOperationsAfterRun() {
        redisServer = RedisServer.builder().port(6379).build();
        redisServer.start();

        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = new JedisPool("localhost", 6379);
            jedis = pool.getResource();
            jedis.mset("abc", "1", "def", "2");

            assertEquals("1", jedis.mget("abc").get(0));
            assertEquals("2", jedis.mget("def").get(0));
            assertNull(jedis.mget("xyz").get(0));
        } finally {
            if (jedis != null) {
                pool.returnResource(jedis);
            }
            redisServer.stop();
        }
    }

    @Test
    public void shouldIndicateInactiveBeforeStart() {
        redisServer = RedisServer.builder().port(6379).build();
        assertFalse(redisServer.isActive());
    }

    @Test
    public void shouldIndicateActiveAfterStart() {
        redisServer = RedisServer.builder().port(6379).build();
        redisServer.start();
        assertTrue(redisServer.isActive());
        redisServer.stop();
    }

    @Test
    public void shouldIndicateInactiveAfterStop() {
        redisServer = RedisServer.builder().port(6379).build();
        redisServer.start();
        redisServer.stop();
        assertFalse(redisServer.isActive());
    }

    @Test
    public void shouldOverrideDefaultExecutable() {
        RedisExecProvider customProvider = RedisExecProvider.defaultProvider()
            .override(OS.WINDOWS,  Architecture.x86,     Resources.getResource(Constants.DEFAULT_WINDOWS_X86).getFile())
            .override(OS.WINDOWS,  Architecture.x86_64,  Resources.getResource(Constants.DEFAULT_WINDOWS_X86_64).getFile())

            .override(OS.UNIX,     Architecture.x86,     Resources.getResource(Constants.DEFAULT_UNIX_X86).getFile())
            .override(OS.UNIX,     Architecture.x86_64,  Resources.getResource(Constants.DEFAULT_UNIX_X86_64).getFile())
            .override(OS.UNIX,     Architecture.arm64,   Resources.getResource(Constants.DEFAULT_UNIX_ARM64).getFile())

            .override(OS.MAC_OS_X, Architecture.x86,     Resources.getResource(Constants.DEFAULT_MAC_OS_X_X86).getFile())
            .override(OS.MAC_OS_X, Architecture.x86_64,  Resources.getResource(Constants.DEFAULT_MAC_OS_X_X86_64).getFile())
            .override(OS.MAC_OS_X, Architecture.arm64,   Resources.getResource(Constants.DEFAULT_MAC_OS_X_ARM64).getFile());

        redisServer = RedisServer.builder()
            .redisExecProvider(customProvider)
            .build();
    }

    @Test(expected = RedisBuildingException.class)
    public void shouldFailWhenBadExecutableGiven() {
        RedisExecProvider buggyProvider = RedisExecProvider.defaultProvider()
            .override(OS.UNIX, "some")
            .override(OS.MAC_OS_X, "some");

        redisServer = RedisServer.builder()
            .redisExecProvider(buggyProvider)
            .build();
    }

    @Test
    public void testAwaitRedisServerReady() throws IOException {
        String readyPattern = RedisServer.builder().build().redisReadyPattern();
        assertReadyPattern(loadResource("redis-2.x-standalone-startup-output.txt"), readyPattern);
        assertReadyPattern(loadResource("redis-3.x-standalone-startup-output.txt"), readyPattern);
        assertReadyPattern(loadResource("redis-4.x-standalone-startup-output.txt"), readyPattern);
        assertReadyPattern(loadResource("redis-6.x-standalone-startup-output.txt"), readyPattern);
    }

    private BufferedReader loadResource(String path) {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("redis-6.x-standalone-startup-output.txt");
        return new BufferedReader(new InputStreamReader(resourceAsStream));
    }

    private static void assertReadyPattern(BufferedReader reader, String readyPattern) throws IOException {
        String outputLine;
        do {
            outputLine = reader.readLine();
            assertNotNull(outputLine);
        } while (!outputLine.matches(readyPattern));
    }
}
