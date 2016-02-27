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
import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisChannels;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DRCommandSender;
import com.gmail.tracebachi.DeltaRedis.Shared.Servers;
import com.gmail.tracebachi.DeltaRedis.Shared.Shutdownable;
import com.google.common.base.Preconditions;
import net.md_5.bungee.BungeeCord;

import java.util.Set;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/11/15.
 */
public class DeltaRedisApi implements Shutdownable
{
    private DRCommandSender deltaSender;
    private DeltaRedis plugin;

    public DeltaRedisApi(DRCommandSender deltaSender, DeltaRedis plugin)
    {
        this.deltaSender = deltaSender;
        this.plugin = plugin;
    }

    @Override
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
     * Publishes a message built from string message pieces joined by
     * the "/\" (forward-slash, back-slash) to Redis.
     *
     * @param destination Server to send message to.
     * @param channel Channel of the message.
     * @param messagePieces The parts of the message.
     */
    public void publish(String destination, String channel, String... messagePieces)
    {
        String joinedMessage = String.join("/\\", messagePieces);
        publish(destination, channel, joinedMessage);
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

        BungeeCord.getInstance().getScheduler().runAsync(plugin,
            () -> deltaSender.publish(destination, channel, message));
    }

    /**
     * Sends a message to a player on an unknown server. The message will not
     * reach the player if they have logged off by the time the message
     * reaches the server or if not player is online by the specified name.
     *
     * @param playerName Name of the player to send message to.
     * @param message Message to send.
     */
    public void sendMessageToPlayer(String playerName, String message)
    {
        Preconditions.checkNotNull(playerName, "Player name cannot be null.");
        Preconditions.checkNotNull(message, "Message cannot be null.");

        BungeeCord.getInstance().getScheduler().runAsync(plugin, () ->
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
     * Sends a message to a player in the specified server. The message will not
     * reach the player if they have logged off by the time the message
     * reaches the server.
     *
     * @param server Name of the server to send the message to.
     * @param playerName Name of the player to send message to.
     * @param message Message to send.
     */
    public void sendMessageToPlayer(String server, String playerName, String message)
    {
        Preconditions.checkNotNull(playerName, "Player name cannot be null.");
        Preconditions.checkNotNull(message, "Message cannot be null.");
        Preconditions.checkArgument(!server.equals(Servers.BUNGEECORD),
            "Server cannot be BungeeCord.");

        BungeeCord.getInstance().getScheduler().runAsync(plugin,
            () -> deltaSender.publish(server,
                DeltaRedisChannels.SEND_MESSAGE,
                playerName + "/\\" + message));
    }

    /**
     * Sends an announcement to all players on a server.
     *
     * @param destServer Destination server name or {@link Servers#SPIGOT}.
     * @param announcement Announcement to send.
     */
    public void sendAnnouncementToServer(String destServer, String announcement)
    {
        sendAnnouncementToServer(destServer, announcement, "");
    }

    /**
     * Sends an announcement to all players on a server with a specific
     * permission.
     *
     * @param destServer Destination server name or {@link Servers#SPIGOT}.
     * @param announcement Announcement to send.
     * @param permission Permission that a player must have to receive the
     *                   announcement. The empty string, "", can be used to
     *                   signal that a permission is not required.
     */
    public void sendAnnouncementToServer(String destServer, String announcement, String permission)
    {
        Preconditions.checkNotNull(destServer, "Destination server cannot be null.");
        Preconditions.checkNotNull(announcement, "Announcement cannot be null.");
        Preconditions.checkNotNull(permission, "Permission cannot be null");

        BungeeCord.getInstance().getScheduler().runAsync(plugin,
            () -> deltaSender.publish(destServer,
                DeltaRedisChannels.SEND_ANNOUNCEMENT, announcement + "/\\" + permission));
    }
}
