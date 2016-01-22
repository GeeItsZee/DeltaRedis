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

import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisChannels;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DRCommandSender;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.Servers;
import com.google.common.base.Preconditions;
import net.md_5.bungee.BungeeCord;

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
     * Sends a message to a player in the specified server.
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
     * Sends a message to a player in the specified server.
     *
     * @param destServer Name of the server to send the message to.
     * @param announcement Announcement to send.
     */
    public void sendAnnouncementToServer(String destServer, String announcement)
    {
        Preconditions.checkNotNull(destServer, "Destination server cannot be null.");
        Preconditions.checkNotNull(announcement, "Announcement cannot be null.");
        Preconditions.checkArgument(!destServer.equals(Servers.BUNGEECORD),
            "Server cannot be BungeeCord.");

        BungeeCord.getInstance().getScheduler().runAsync(plugin,
            () -> deltaSender.publish(destServer,
                DeltaRedisChannels.SEND_ANNOUNCEMENT,
                announcement));
    }
}
