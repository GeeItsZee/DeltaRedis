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
package com.gmail.tracebachi.DeltaRedis.Bungee;

import com.gmail.tracebachi.DeltaRedis.Shared.Cache.CachedPlayer;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DeltaRedisCommandSender;
import com.gmail.tracebachi.DeltaRedis.Shared.Servers;
import com.google.common.base.Preconditions;
import net.md_5.bungee.BungeeCord;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisChannels.*;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/11/15.
 */
public class DeltaRedisApi
{
    private static DeltaRedisApi instance;

    private DeltaRedisCommandSender deltaSender;
    private DeltaRedis plugin;

    /**
     * @return Singleton instance of DeltaRedisApi
     */
    public static DeltaRedisApi instance()
    {
        return instance;
    }

    /**
     * @return Name of the BungeeCord instance to which the server belongs
     * <p>This value is set in the configuration file for each server</p>
     */
    public String getBungeeName()
    {
        return plugin.getBungeeName();
    }

    /**
     * @return Name of the current server
     * <p>This value is set in the configuration file for each server</p>
     */
    public String getServerName()
    {
        return plugin.getServerName();
    }

    /**
     * @return An unmodifiable set of servers that are part of the same
     * BungeeCord (from last call to {@link DeltaRedisCommandSender#getServers()})
     */
    public Set<String> getCachedServers()
    {
        return deltaSender.getCachedServers();
    }

    /**
     * @return True if the BungeeCord instance was last known to be online
     * or false
     */
    public boolean isBungeeCordOnline()
    {
        return deltaSender.isBungeeCordOnline();
    }

    /**
     * @return An unmodifiable set of player names that are part of the
     * same BungeeCord (from last call to {@link DeltaRedisCommandSender#getPlayers()})
     */
    public Set<String> getCachedPlayers()
    {
        return deltaSender.getCachedPlayers();
    }

    /**
     * Publishes a message built from string message parts
     *
     * @param destination   Server to send message to
     * @param channel       Channel of the message
     * @param messagePieces The parts of the message
     */
    public void publish(String destination, String channel, String... messagePieces)
    {
        publish(destination, channel, Arrays.asList(messagePieces));
    }

    /**
     * Publishes a message to Redis
     *
     * @param destination  Server to send message to
     * @param channel      Channel of the message
     * @param messageParts The actual message
     */
    public void publish(String destination, String channel, List<String> messageParts)
    {
        Preconditions.checkNotNull(destination, "destination");
        Preconditions.checkNotNull(channel, "channel");
        Preconditions.checkNotNull(messageParts, "messageParts");

        if(plugin.getServerName().equals(destination))
        {
            plugin.onRedisMessageEvent(destination, channel, messageParts);
            return;
        }

        BungeeCord.getInstance().getScheduler().runAsync(
            plugin,
            () -> deltaSender.publish(destination, channel, messageParts));
    }

    /**
     * Sends a command that will run as OP by the receiving server
     *
     * @param destServer Destination server name, {@link Servers#SPIGOT},
     *                   or {@link Servers#BUNGEECORD}
     * @param command    Command to send
     * @param sender     Name to record in the logs as having run the command
     */
    public void sendServerCommand(String destServer, String command, String sender)
    {
        Preconditions.checkNotNull(destServer, "destServer");
        Preconditions.checkNotNull(command, "command");
        Preconditions.checkNotNull(sender, "sender");

        if(plugin.getServerName().equals(destServer))
        {
            BungeeCord instance = BungeeCord.getInstance();
            instance.getPluginManager().dispatchCommand(instance.getConsole(), command);
            return;
        }

        BungeeCord.getInstance().getScheduler().runAsync(
            plugin,
            () -> deltaSender.publish(destServer, RUN_CMD, sender, command));
    }

    /**
     * This method sends a message to a player on an unknown server. The
     * message will not reach the player if they have logged off by the
     * time the message reaches the server or if no player is online
     * by the specified name.
     *
     * @param playerName Name of the player to send message to
     * @param message    Message to send
     */
    public void sendMessageToPlayer(String playerName, String message)
    {
        Preconditions.checkNotNull(playerName, "playerName");
        Preconditions.checkNotNull(message, "message");

        BungeeCord.getInstance().getScheduler().runAsync(
            plugin,
            () ->
            {
                CachedPlayer cachedPlayer = deltaSender.getPlayer(playerName);

                if(cachedPlayer == null) { return; }

                deltaSender.publish(
                    cachedPlayer.getServer(),
                    SEND_MESSAGE,
                    playerName,
                    message);
            });
    }

    /**
     * This method sends a message to a player on an unknown server. The
     * message will not reach the player if they have logged off by the
     * time the message reaches the server or if no player is online
     * by the specified name.
     *
     * @param server     Name of the server to send message to
     * @param playerName Name of the player to send message to
     * @param message    Message to send
     */
    public void sendMessageToPlayer(String server, String playerName, String message)
    {
        Preconditions.checkNotNull(playerName, "playerName");
        Preconditions.checkNotNull(message, "message");
        Preconditions.checkArgument(
            !server.equals(Servers.BUNGEECORD),
            "Server set to BUNGEECORD");

        BungeeCord.getInstance().getScheduler().runAsync(
            plugin,
            () -> deltaSender.publish(
                server,
                SEND_MESSAGE,
                playerName,
                message));
    }

    /**
     * Sends an announcement to all players on a server
     *
     * @param destServer   Destination server name or {@link Servers#SPIGOT}
     * @param announcement Announcement to send
     */
    public void sendServerAnnouncement(String destServer, String announcement)
    {
        sendServerAnnouncement(destServer, announcement, "");
    }

    /**
     * Sends an announcement to all players on a server with a specific
     * permission
     *
     * @param destServer   Destination server name or {@link Servers#SPIGOT}
     * @param announcement Announcement to send
     * @param permission   Permission required by players to view announcement or ""
     */
    public void sendServerAnnouncement(String destServer, String announcement, String permission)
    {
        Preconditions.checkNotNull(destServer, "destServer");
        Preconditions.checkNotNull(announcement, "announcement");
        Preconditions.checkNotNull(permission, "permission");

        BungeeCord.getInstance().getScheduler().runAsync(
            plugin,
            () -> deltaSender.publish(
                destServer,
                SEND_ANNOUNCEMENT,
                permission,
                announcement));
    }

    /**
     * Private constructor
     */
    private DeltaRedisApi(DeltaRedisCommandSender deltaSender, DeltaRedis plugin)
    {
        this.deltaSender = deltaSender;
        this.plugin = plugin;
    }

    /**
     * Sets up the api instance
     */
    static void setup(DeltaRedisCommandSender deltaSender, DeltaRedis plugin)
    {
        if(instance != null)
        {
            shutdown();
        }

        instance = new DeltaRedisApi(deltaSender, plugin);
    }

    /**
     * Cleans up the api instance
     */
    static void shutdown()
    {
        if(instance != null)
        {
            instance.deltaSender = null;
            instance.plugin = null;
            instance = null;
        }
    }
}
