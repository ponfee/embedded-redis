package redis.embedded;

import org.apache.commons.io.IOUtils;
import redis.embedded.exceptions.EmbeddedRedisException;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

abstract class AbstractRedisInstance implements Redis {

    private final int port;
    private final int tlsPort;
    private final List<String> args;

    private Process redisProcess;
    private ExecutorService executor;
    private volatile boolean active = false;

    protected AbstractRedisInstance(int port, List<String> args) {
        this(port, 0, args);
    }

    protected AbstractRedisInstance(int port, int tlsPort, List<String> args) {
        this.port = port;
        this.tlsPort = tlsPort;
        this.args = args;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public synchronized void start() throws EmbeddedRedisException {
        if (active) {
            throw new EmbeddedRedisException("This redis server instance is already running...");
        }
        try {
            redisProcess = createRedisProcessBuilder().start();
            installExitHook();
            logErrors();
            awaitRedisServerReady();
            active = true;
        } catch (IOException e) {
            throw new EmbeddedRedisException("Failed to start Redis instance", e);
        }
    }

    private void installExitHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "RedisInstanceCleaner"));
    }

    private void logErrors() {
        final InputStream errorStream = redisProcess.getErrorStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
        Runnable printReaderTask = new PrintReaderRunnable(reader);
        executor = Executors.newSingleThreadExecutor();
        executor.submit(printReaderTask);
    }

    private void awaitRedisServerReady() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(redisProcess.getInputStream()));
        try {
            StringBuilder builder = new StringBuilder();
            String outputLine;
            do {
                outputLine = reader.readLine();
                if (outputLine == null) {
                    //Something goes wrong. Stream is ended before server was activated.
                    throw new RuntimeException("Can't start redis server. Check logs for details. Redis process log: " + builder);
                } else {
                    builder.append("\n");
                    builder.append(outputLine);
                }
            } while (!outputLine.matches(redisReadyPattern()));
        } finally {
            IOUtils.closeQuietly(reader, null);
        }
    }

    protected abstract String redisReadyPattern();

    private ProcessBuilder createRedisProcessBuilder() {
        File executable = new File(args.get(0));
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(executable.getParentFile());
        return pb;
    }

    @Override
    public synchronized void stop() throws EmbeddedRedisException {
        if (active) {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
            }
            redisProcess.destroy();
            tryWaitFor();
            active = false;
        }
    }

    private void tryWaitFor() {
        try {
            redisProcess.waitFor();
        } catch (InterruptedException e) {
            throw new EmbeddedRedisException("Failed to stop redis instance", e);
        }
    }

    @Override
    public List<Integer> ports() {
        return port > 0 ? Collections.singletonList(port) : Collections.emptyList();
    }

    @Override
    public List<Integer> tlsPorts() {
        return tlsPort > 0 ? Collections.singletonList(tlsPort) : Collections.emptyList();
    }

    private static class PrintReaderRunnable implements Runnable {
        private final BufferedReader reader;

        private PrintReaderRunnable(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            try {
                readLines();
            } finally {
                IOUtils.closeQuietly(reader, null);
            }
        }

        public void readLines() {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
