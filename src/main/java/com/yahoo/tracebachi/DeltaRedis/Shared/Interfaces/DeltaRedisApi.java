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
package com.yahoo.tracebachi.DeltaRedis.Shared.Interfaces;

import com.yahoo.tracebachi.DeltaRedis.Shared.Cache.CachedPlayer;
import com.yahoo.tracebachi.DeltaRedis.Shared.Channels;

import java.util.Set;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com) on 10/18/15.
 */
public interface DeltaRedisApi
{
    /**
     * @return Name of the server (String). If the server is BungeeCord, the
     * server name will be {@link Channels#BUNGEECORD}.
     */
    String getServerName();

    /**
     * @return Name of the BungeeCord instance to which the server belongs.
     * This value is set in the configuration file for each server.
     */
    String getBungeeName();

    /**
     * @return An unmodifiable set of servers that are part of the
     * same BungeeCord.
     */
    Set<String> getServers();

    /**
     * Returns a cached player from the local cache or Redis. This method
     * does not check players that are online on the current server. The
     * programmer must perform that check before calling this method.
     *
     * @param playerName Name of the player to find.
     *
     * @return CachedPlayer if found and null if not.
     */
    CachedPlayer getPlayer(String playerName);

    /**
     * Publishes a string message using Redis PubSub. The destination can
     * also be one of the special values {@link Channels#BUNGEECORD} or
     * {@link Channels#SPIGOT}.
     *
     * @param dest Server name that message should go to.
     * @param channel Custom channel name for the message.
     * @param message Message to send.
     *
     * @return The number of servers that received the message.
     */
    long publish(String dest, String channel, String message);
}
