/*
 * This file is part of DeltaRedis.
 *
 * DeltaRedis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DeltaRedis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeltaRedis.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.DeltaRedis.Spigot;

import com.gmail.tracebachi.DeltaRedis.Shared.Cache.CachedPlayer;
import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisChannels;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DRCommandSender;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.Servers;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/11/15.
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
        return plugin.getBungeeName();
    }

    /**
     * @return Name of the server (String). If the server is BungeeCord, the
     * server name will be {@link Servers#BUNGEECORD}.
     */
    public String getServerName()
    {
        return plugin.getServerName();
    }

    /**
     * @return An unmodifiable set of servers that are part of the same
     * BungeeCord as the current server. This method will retrieve the
     * servers from the last call to {@link DRCommandSender#getServers()}.
     */
    public Set<String> getCachedServers()
    {
        return deltaSender.getCachedServers();
    }

    /**
     * @return True if the BungeeCord instance was last known to be online.
     * False if it was not.
     */
    public boolean isBungeeCordOnline()
    {
        return deltaSender.isBungeeCordOnline();
    }

    /**
     * @return An unmodifiable set of players (names) that are part of the
     * same BungeeCord. This method will retrieve the players from the last
     * call to {@link DRCommandSender#getPlayers()}.
     */
    public Set<String> getCachedPlayers()
    {
        return deltaSender.getCachedPlayers();
    }

    /**
     * @param partial Non-null string that is the beginning of a name
     *
     * @return A list of player names that begins with the partial
     * sent to this method.
     */
    public List<String> matchStartOfPlayerName(String partial)
    {
        List<String> result = new ArrayList<>();
        partial = partial.toLowerCase();

        for(String name : getCachedPlayers())
        {
            if(name.startsWith(partial))
            {
                result.add(name);
            }
        }
        return result;
    }

    /**
     * @param partial Non-null string that is the beginning of a name
     *
     * @return A list of player names that begins with the partial
     * sent to this method.
     */
    public List<String> matchStartOfServerName(String partial)
    {
        List<String> result = new ArrayList<>();
        partial = partial.toLowerCase();

        for(String name : getCachedServers())
        {
            if(name.toLowerCase().startsWith(partial))
            {
                result.add(name);
            }
        }
        return result;
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

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {
            CachedPlayer cachedPlayer = deltaSender.getPlayer(playerName);

            Bukkit.getScheduler().runTask(plugin,
                () -> callback.call(cachedPlayer));
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

        if(destination.equals(plugin.getServerName()))
        {
            throw new IllegalArgumentException("Target channel cannot be " +
                "the same as the server's own channel.");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin,
            () -> deltaSender.publish(destination, channel, message));
    }

    /**
     * Sends a command that will run as OP by the receiving server.
     *
     * @param destServer Destination server name, {@link Servers#SPIGOT},
     *                   or {@link Servers#BUNGEECORD}.
     * @param command Command to send.
     */
    public void sendCommandToServer(String destServer, String command)
    {
        Preconditions.checkNotNull(destServer, "Destination server cannot be null.");
        Preconditions.checkNotNull(command, "Command cannot be null.");

        if(plugin.getServerName().equals(destServer))
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return;
        }

        if(destServer.equals(Servers.SPIGOT))
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                deltaSender.publish(Servers.SPIGOT, DeltaRedisChannels.RUN_CMD, command));
        }
        else
        {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                deltaSender.publish(destServer, DeltaRedisChannels.RUN_CMD, command));
        }
    }

    /**
     * Sends a message to a player on a different server. It fails quietly if
     * the player is not online. This method should not be used to send messages
     * to players that are on the same server. That message will be ignored.
     *
     * @param playerName Name of the player to try and send a message to.
     * @param message Message to send.
     */
    public void sendMessageToPlayer(String playerName, String message)
    {
        Preconditions.checkNotNull(playerName, "Player name cannot be null.");
        Preconditions.checkNotNull(message, "Message cannot be null.");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {
            CachedPlayer cachedPlayer = deltaSender.getPlayer(playerName);

            if(cachedPlayer != null)
            {
                deltaSender.publish(cachedPlayer.getServer(),
                    DeltaRedisChannels.SEND_MESSAGE,
                    playerName + "/\\" + message);
            }
        });
    }

    /**
     * Sends an announcement to all players on a different server. This method
     * should not be used to send announcements on the current server.
     * That message will be ignored.
     *
     * It does not send a message if the player is offline. This method should
     * not be used to send messages to players that are online on the current server.
     *
     * @param destServer Destination server name or {@link Servers#SPIGOT}.
     * @param announcement Announcement to send.
     */
    public void sendAnnouncementToServer(String destServer, String announcement)
    {
        Preconditions.checkNotNull(destServer, "Destination server cannot be null.");
        Preconditions.checkNotNull(announcement, "Announcement cannot be null.");

        if(destServer.equals(Servers.SPIGOT))
        {
            Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> deltaSender.publish(Servers.SPIGOT,
                    DeltaRedisChannels.SEND_ANNOUNCEMENT, announcement));
        }
        else
        {
            Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> deltaSender.publish(destServer,
                    DeltaRedisChannels.SEND_ANNOUNCEMENT, announcement));
        }
    }
}
