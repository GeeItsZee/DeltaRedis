package com.yahoo.tracebachi.DeltaRedis.Spigot;

import com.yahoo.tracebachi.DeltaRedis.Shared.Cache.CachedPlayer;
import com.yahoo.tracebachi.DeltaRedis.Shared.Redis.Channels;
import com.yahoo.tracebachi.DeltaRedis.Shared.Redis.DRCommandSender;
import org.bukkit.Bukkit;

import java.util.Set;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/11/15.
 */
public class DeltaRedisApi
{
    private DRCommandSender deltaSender;
    private DeltaRedisPlugin plugin;

    public DeltaRedisApi(DRCommandSender deltaSender, DeltaRedisPlugin plugin)
    {
        this.deltaSender = deltaSender;
        this.plugin = plugin;
    }

    public void shutdown()
    {
        this.deltaSender = null;
        this.plugin = null;
    }

    /**
     * @return Name of the BungeeCord instance to which the server belongs.
     * This value is set in the configuration file for each server.
     */
    public String getBungeeName()
    {
        return deltaSender.getBungeeName();
    }

    /**
     * @return Name of the server (String). If the server is BungeeCord, the
     * server name will be {@link Channels#BUNGEECORD}.
     */
    public String getServerName()
    {
        return deltaSender.getServerName();
    }

    /**
     * @return An unmodifiable set of servers that are part of the
     * same BungeeCord. This method will retrieve the servers from the last
     * call to {@link DRCommandSender#getServers()}.
     */
    public Set<String> getCachedServers()
    {
        return deltaSender.getCachedServers();
    }

    /**
     * Asynchronously looks for the player in Redis. The callback is called synchronously
     * with the {@link CachedPlayer} or null.
     *
     * @param playerName Name of the player to find.
     * @param callback Callback to run when fetch is complete.
     */
    public void findPlayer(String playerName, DeltaRedisPlayerCallback callback)
    {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            CachedPlayer cachedPlayer = deltaSender.getPlayer(playerName);

            Bukkit.getScheduler().runTask(plugin, () -> {
                callback.call(cachedPlayer);
            });
        });
    }

    /**
     * Publishes a message to Redis.
     *
     * @param destination Server to send message to.
     * @param channel Channel of the message.
     * @param message The actual message.
     */
    public void publish(String destination, String channel, String message)
    {
        if(destination.equals(deltaSender.getServerName()))
        {
            throw new IllegalArgumentException("Target channel cannot be " +
                "the same as the server's own channel.");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
            deltaSender.publish(destination, channel, message));
    }
}
