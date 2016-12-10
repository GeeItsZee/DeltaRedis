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
package com.gmail.tracebachi.DeltaRedis.Spigot.Events;

import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedisApi;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.List;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class DeltaRedisMessageEvent extends Event
{
    private final String sendingServer;
    private final String channel;
    private final List<String> messageParts;

    public DeltaRedisMessageEvent(String sendingServer, String channel, List<String> messageParts)
    {
        Preconditions.checkNotNull(sendingServer, "sendingServer");
        Preconditions.checkNotNull(channel, "channel");
        Preconditions.checkNotNull(messageParts, "messageParts");
        Preconditions.checkArgument(!sendingServer.isEmpty(), "Empty sendingServer");
        Preconditions.checkArgument(!channel.isEmpty(), "Empty channel");

        this.sendingServer = sendingServer;
        this.channel = channel;
        this.messageParts = Collections.unmodifiableList(messageParts);
    }

    /**
     * @return Name of the server that sent the message.
     */
    public String getSendingServer()
    {
        return sendingServer;
    }

    /**
     * @return Name of the channel that the message is targeted at.
     */
    public String getChannel()
    {
        return channel;
    }

    /**
     * @return The message parts/data received.
     */
    public List<String> getMessageParts()
    {
        return messageParts;
    }

    /**
     * @return True if the message was sent by the current server or false
     */
    public boolean isSendingServerSelf()
    {
        return DeltaRedisApi.instance().getServerName().equals(sendingServer);
    }

    /**
     * @return Comma separated string of the sendingServer, channel, and message.
     */
    @Override
    public String toString()
    {
        return "(" + sendingServer + ", " + channel + ", " + String.join(", ", messageParts) + ")";
    }

    private static final HandlerList handlers = new HandlerList();

    /**
     * Used by the Bukkit/Spigot event system
     */
    public HandlerList getHandlers()
    {
        return handlers;
    }

    /**
     * Used by the Bukkit/Spigot event system
     */
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
