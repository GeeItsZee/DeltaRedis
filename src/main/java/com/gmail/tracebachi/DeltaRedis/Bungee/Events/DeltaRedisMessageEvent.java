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
package com.gmail.tracebachi.DeltaRedis.Bungee.Events;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.plugin.Event;

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
     * @return Name of the server that sent the message
     */
    public String getSendingServer()
    {
        return sendingServer;
    }

    /**
     * @return Name of the channel that the message is targeted at
     */
    public String getChannel()
    {
        return channel;
    }

    /**
     * @return The message parts/data received
     */
    public List<String> getMessageParts()
    {
        return messageParts;
    }
}
