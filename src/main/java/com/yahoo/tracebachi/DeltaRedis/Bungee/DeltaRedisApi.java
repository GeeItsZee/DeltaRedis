package com.yahoo.tracebachi.DeltaRedis.Bungee;

import com.google.common.base.Preconditions;
import com.yahoo.tracebachi.DeltaRedis.Shared.Redis.DRCommandSender;
import net.md_5.bungee.BungeeCord;

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
     * Publishes a message to Redis.
     *
     * @param destination Server to send message to.
     * @param channel Channel of the message.
     * @param message The actual message.
     */
    public void publish(String destination, String channel, String message)
    {
        Preconditions.checkNotNull(destination, "Destination cannot be null.");
        Preconditions.checkNotNull(channel, "Channel cannot be null.");
        Preconditions.checkNotNull(message, "Message cannot be null.");

        if(destination.equals(deltaSender.getServerName()))
        {
            throw new IllegalArgumentException("Target channel cannot be " +
                "the same as the server's own channel.");
        }

        BungeeCord.getInstance().getScheduler().runAsync(plugin, () ->
        {
            deltaSender.publish(destination, channel, message);
        });
    }
}
