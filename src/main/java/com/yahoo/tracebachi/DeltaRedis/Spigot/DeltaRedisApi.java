package com.yahoo.tracebachi.DeltaRedis.Spigot;

import com.google.common.base.Preconditions;
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
    public static final String RUN_CMD_CHANNEL = "DR-RunCmd";
    public static final String SEND_MESSAGE_CHANNEL = "DR-SendMess";

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
        Preconditions.checkNotNull(playerName, "Player name cannot be null.");
        Preconditions.checkNotNull(callback, "Callback cannot be null.");

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
        Preconditions.checkNotNull(destination, "Destination cannot be null.");
        Preconditions.checkNotNull(channel, "Channel cannot be null.");
        Preconditions.checkNotNull(message, "Message cannot be null.");

        if(destination.equals(deltaSender.getServerName()))
        {
            throw new IllegalArgumentException("Target channel cannot be " +
                "the same as the server's own channel.");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
            deltaSender.publish(destination, channel, message));
    }

    /**
     * Sends a command that will run as OP by the receiving server.
     *
     * @param destServer Destination server name, ALL, or SPIGOT.
     * @param command Command to send.
     */
    public void sendCommandToServer(String destServer, String command)
    {
        Preconditions.checkNotNull(destServer, "Destination server cannot be null.");
        Preconditions.checkNotNull(command, "Command cannot be null.");

        if(deltaSender.getServerName().equals(destServer))
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return;
        }

        if(destServer.equals(Channels.SPIGOT))
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                deltaSender.publish(Channels.SPIGOT, RUN_CMD_CHANNEL, command);
            });
        }
        else
        {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                deltaSender.publish(destServer, RUN_CMD_CHANNEL, command);
            });
        }
    }

    /**
     * Sends a message to a player on a different server.
     *
     * The purpose of this method is to allow plugins to send "replies" to players.
     * It does not send a message if the player is offline. This method should
     * not be used to send messages to players that are online on the current server.
     *
     * @param playerName Name of the player to try and send a message to.
     * @param message Message to send.
     */
    public void sendMessageToPlayer(String playerName, String message)
    {
        Preconditions.checkNotNull(playerName, "Player name cannot be null.");
        Preconditions.checkNotNull(message, "Message cannot be null.");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            CachedPlayer cachedPlayer = deltaSender.getPlayer(playerName);

            if(cachedPlayer != null)
            {
                deltaSender.publish(cachedPlayer.getServer(), SEND_MESSAGE_CHANNEL,
                    playerName + "/\\" + message);
            }
        });
    }
}
