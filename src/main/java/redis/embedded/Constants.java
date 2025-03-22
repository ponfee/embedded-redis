package redis.embedded;

/**
 * Constants utility
 *
 * <p>downloads page
 * <ul>
 *   <li><a href="https://github.com/microsoftarchive/redis/releases">Windows-2.x+</a></li>
 *   <li><a href="https://github.com/tporadowski/redis/releases">Windows-4.x+</a></li>
 *   <li><a href="https://github.com/zkteco-home/redis-windows/releases">Windows-6.x+</a></li>
 *   <li><a href="https://github.com/redis-windows/redis-windows/releases">Windows-6.x+</a></li>
 *   <li><a href="https://redis.io/download">Linux and MacOS</a></li>
 *   <li><a href="https://github.com/redis/redis/releases">Redis source</a></li>
 * </ul>
 *
 * @author Ponfee
 */
public class Constants {

    public static final String DEFAULT_WINDOWS_X86     = "redis-server-7.4.2-win-amd64.exe";
    public static final String DEFAULT_WINDOWS_X86_64  = "redis-server-7.4.2-win-amd64.exe";

    public static final String DEFAULT_UNIX_X86        = "redis-server-7.4.1-linux-amd64";
    public static final String DEFAULT_UNIX_X86_64     = "redis-server-7.4.1-linux-amd64";
    public static final String DEFAULT_UNIX_ARM64      = "redis-server-7.4.1-linux-arm64";

    public static final String DEFAULT_MAC_OS_X_X86    = "redis-server-7.4.1-darwin-amd64";
    public static final String DEFAULT_MAC_OS_X_X86_64 = "redis-server-7.4.1-darwin-amd64";
    public static final String DEFAULT_MAC_OS_X_ARM64  = "redis-server-7.4.1-darwin-arm64";

}
