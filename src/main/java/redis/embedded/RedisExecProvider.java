package redis.embedded;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import redis.embedded.util.Architecture;
import redis.embedded.util.JarUtil;
import redis.embedded.util.OS;
import redis.embedded.util.OsArchitecture;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class RedisExecProvider {

    private final Map<OsArchitecture, String> executables = Maps.newHashMap();

    public static RedisExecProvider defaultProvider() {
        return new RedisExecProvider();
    }

    private RedisExecProvider() {
        executables.put(OsArchitecture.UNIX_x86,        Constants.DEFAULT_UNIX_X86);
        executables.put(OsArchitecture.UNIX_x86_64,     Constants.DEFAULT_UNIX_X86_64);
        executables.put(OsArchitecture.UNIX_arm64,      Constants.DEFAULT_UNIX_ARM64);

        executables.put(OsArchitecture.MAC_OS_X_x86,    Constants.DEFAULT_MAC_OS_X_X86);
        executables.put(OsArchitecture.MAC_OS_X_x86_64, Constants.DEFAULT_MAC_OS_X_X86_64);
        executables.put(OsArchitecture.MAC_OS_X_arm64,  Constants.DEFAULT_MAC_OS_X_ARM64);

        executables.put(OsArchitecture.WINDOWS_x86,     Constants.DEFAULT_WINDOWS_X86);
        executables.put(OsArchitecture.WINDOWS_x86_64,  Constants.DEFAULT_WINDOWS_X86_64);
    }

    public RedisExecProvider override(OS os, String executable) {
        Preconditions.checkNotNull(executable);
        for (Architecture arch : Architecture.values()) {
            override(os, arch, executable);
        }
        return this;
    }

    public RedisExecProvider override(OS os, Architecture arch, String executable) {
        Preconditions.checkNotNull(executable);
        executables.put(new OsArchitecture(os, arch), executable);
        return this;
    }

    public File get() throws IOException {
        OsArchitecture osArch = OsArchitecture.detect();

        if (!executables.containsKey(osArch)) {
            throw new IllegalArgumentException("No Redis executable found for " + osArch);
        }

        String executablePath = executables.get(osArch);
        if (fileExists(executablePath)) {
            System.out.println("Get local file redis server: " + osArch + ", " + executablePath);
            return new File(executablePath);
        } else {
            System.out.println("Get jar file redis server: " + osArch + ", " + executablePath);
            return JarUtil.extractExecutableFromJar(executablePath);
        }
    }

    private boolean fileExists(String executablePath) {
        return new File(executablePath).exists();
    }

}
