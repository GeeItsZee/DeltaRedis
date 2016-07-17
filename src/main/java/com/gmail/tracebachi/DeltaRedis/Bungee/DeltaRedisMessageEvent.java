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

import net.md_5.bungee.api.plugin.Event;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class DeltaRedisMessageEvent extends Event
{
    private final String sendingServer;
    private final String channel;
    private final String message;

    public DeltaRedisMessageEvent(String sendingServer, String channel, String message)
    {
        this.sendingServer = sendingServer;
        this.channel = channel;
        this.message = message;
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
     * @return The message/data received.
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @return Comma separated string of the sendingServer, channel, and message.
     */
    @Override
    public String toString()
    {
        return "{" + sendingServer + " , " + channel + " , " + message + "}";
    }
}
