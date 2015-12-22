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
package com.yahoo.tracebachi.DeltaRedis.Shared.Redis;

import com.google.common.base.Preconditions;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.yahoo.tracebachi.DeltaRedis.Shared.Cache.CachedPlayer;
import com.yahoo.tracebachi.DeltaRedis.Shared.Interfaces.LoggablePlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com) on 10/18/15.
 */
public class DRCommandSender
{
    private final String serverName;
    private final String bungeeName;

    private StatefulRedisConnection<String, String> connection;
    private LoggablePlugin plugin;
    private Set<String> cachedServers;
    private Set<String> cachedPlayers;

    public DRCommandSender(StatefulRedisConnection<String, String> connection,
        String bungeeName, String serverName, LoggablePlugin plugin)
    {
        this.connection = connection;
        this.bungeeName = bungeeName;
        this.serverName = serverName;
        this.plugin = plugin;
    }

    /**
     * Adds server to Redis, making it visible to other servers/
     */
    public synchronized void setup()
    {
        connection.sync().sadd(bungeeName + ":servers", serverName);
        plugin.debug("DRPublisher.setup()");
    }

    /**
     * Removes server from Redis and nullifies all internet resources.
     */
    public synchronized void shutdown()
    {
        connection.sync().srem(bungeeName + ":servers", serverName);
        connection.close();
        connection = null;

        plugin.debug("DRPublisher.shutdown()");
        plugin = null;
    }

    /**
     * @return Name of the BungeeCord instance to which the server belongs.
     * This value is set in the configuration file for each server.
     */
    public String getBungeeName()
    {
        return bungeeName;
    }

    /**
     * @return Name of the server (String). If the server is BungeeCord, the
     * server name will be {@link Channels#BUNGEECORD}.
     */
    public String getServerName()
    {
        return serverName;
    }

    /**
     * @return An unmodifiable set of servers that are part of the
     * same BungeeCord. This method will retrieve the servers from Redis.
     */
    public synchronized Set<String> getServers()
    {
        Set<String> result = connection.sync().smembers(bungeeName + ":servers");
        plugin.debug("DRCommandSender.getServers() : Updated cache.");

        cachedServers = Collections.unmodifiableSet(new HashSet<>(result));
        return cachedServers;
    }

    /**
     * @return An unmodifiable set of servers that are part of the
     * same BungeeCord. This method will retrieve the servers from the last
     * call to {@link DRCommandSender#getServers()}.
     */
    public Set<String> getCachedServers()
    {
        return cachedServers;
    }

    /**
     * @return An unmodifiable set of players (names) that are part of the
     * same BungeeCord. This method will retrieve the players from Redis.
     */
    public synchronized Set<String> getPlayers()
    {
        Set<String> result = connection.sync().smembers(bungeeName + ":players");
        plugin.debug("DRCommandSender.getPlayers() : Updated cache.");

        cachedPlayers = Collections.unmodifiableSet(new HashSet<>(result));
        return cachedPlayers;
    }

    /**
     * @return An unmodifiable set of players (names) that are part of the
     * same BungeeCord. This method will retrieve the players from the last
     * call to {@link DRCommandSender#getPlayers()}.
     */
    public Set<String> getCachedPlayers()
    {
        return cachedPlayers;
    }

    /**
     * Publishes a string message using Redis PubSub. The destination can
     * also be one of the special values {@link Channels#BUNGEECORD}
     * or {@link Channels#SPIGOT}.
     *
     * @param dest Server name that message should go to.
     * @param channel Custom channel name for the message.
     * @param message Message to send.
     *
     * @return The number of servers that received the message.
     */
    public synchronized long publish(String dest, String channel, String message)
    {
        plugin.debug("DRCommandSender.publish(" +
            dest + ", " + channel + ", " + message + ")");

        return connection.sync().publish(
            bungeeName + ':' + dest,
            serverName + "/\\" + channel + "/\\" + message);
    }

    /**
     * Returns a cached player from the local cache or Redis. This method
     * does not check players that are online on the current server. The
     * programmer must perform that check before calling this method.
     *
     * @param playerName Name of the player to find.
     *
     * @return CachedPlayer if found and null if not.
     */
    public synchronized CachedPlayer getPlayer(String playerName)
    {
        Preconditions.checkNotNull(playerName, "Player name cannot be null.");
        playerName = playerName.toLowerCase();

        Map<String, String> result = connection.sync()
            .hgetall(bungeeName + ":players:" + playerName);
        plugin.debug("DRCommandSender.getPlayer()");

        if(result != null)
        {
            String server = result.get("server");
            String ip = result.get("ip");

            if(server == null || ip == null)
            {
                return null;
            }

            return new CachedPlayer(server, ip);
        }
        else
        {
            return null;
        }
    }
}
