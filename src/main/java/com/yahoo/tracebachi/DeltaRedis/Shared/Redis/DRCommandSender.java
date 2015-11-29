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

import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.yahoo.tracebachi.DeltaRedis.Shared.Cache.CachedPlayer;
import com.yahoo.tracebachi.DeltaRedis.Shared.Cache.DRCache;
import com.yahoo.tracebachi.DeltaRedis.Shared.DeltaRedisApi;
import com.yahoo.tracebachi.DeltaRedis.Shared.IDeltaRedisPlugin;

import java.util.*;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com) on 10/18/15.
 */
public class DRCommandSender implements DeltaRedisApi
{
    private final String serverName;
    private final String bungeeName;

    private StatefulRedisConnection<String, String> connection;
    private IDeltaRedisPlugin plugin;
    private DRCache<String, CachedPlayer> playerCache;

    private long timeLastServerCheck = 0;
    private HashSet<String> serverSet = new HashSet<>();

    public DRCommandSender(StatefulRedisConnection<String, String> connection, String bungeeName,
        String serverName, long playerCacheTime, IDeltaRedisPlugin plugin)
    {
        this.connection = connection;
        this.bungeeName = bungeeName;
        this.serverName = serverName;
        this.playerCache = new DRCache<>(playerCacheTime);
        this.plugin = plugin;
    }

    public void setup()
    {
        long result = connection.sync().sadd(bungeeName + ":servers", serverName);
        plugin.debug("DRCommandSender.setup() : result = " + result);
    }

    public void shutdown()
    {
        serverSet.clear();
        serverSet = null;

        playerCache.removeAll();
        playerCache = null;

        connection.sync().srem(bungeeName + ":servers", serverName);
        connection.close();
        connection = null;

        plugin.debug("DRCommandSender.shutdown()");
    }

    public void setPlayerAsOnline(String playerName, String ip)
    {
        if(playerName == null || ip == null) { return; }
        playerName = playerName.toLowerCase();

        HashMap<String, String> map = new HashMap<>();
        map.put("server", serverName);
        map.put("ip", ip);

        connection.sync().hmset(bungeeName + ":players:" + playerName, map);
        plugin.debug("DRCommandSender.setPlayerAsOnline(" + playerName + ")");
    }

    public void setPlayerAsOffline(String playerName)
    {
        if(playerName == null) { return; }
        playerName = playerName.toLowerCase();

        playerCache.remove(playerName);

        connection.sync().del(bungeeName + ":players:" + playerName);
        plugin.debug("DRCommandSender.setPlayerAsOffline(" + playerName + ")");
    }

    @Override
    public String getBungeeName()
    {
        return bungeeName;
    }

    @Override
    public String getServerName()
    {
        return serverName;
    }

    @Override
    public long publish(String dest, String channel, String message)
    {
        if(dest.equals(serverName))
        {
            throw new IllegalArgumentException("Target channel cannot be " +
                "the same as the server's own channel.");
        }

        plugin.debug("DRCommandSender.publish(" +
            dest + ", " + channel + ", " + message + ")");

        return connection.sync().publish(bungeeName + ':' + dest,
            serverName + "/\\" + channel + "/\\" + message);
    }

    @Override
    public Set<String> getServers()
    {
        if((System.currentTimeMillis() - timeLastServerCheck) > 200)
        {
            serverSet.clear();
            Set<String> result = connection.sync().smembers(bungeeName + ":servers");
            serverSet.addAll(result);
            timeLastServerCheck = System.currentTimeMillis();

            plugin.debug("DRCommandSender.getServers() : Updated server cache.");
        }

        return Collections.unmodifiableSet(serverSet);
    }

    @Override
    public CachedPlayer getPlayer(String playerName)
    {
        if(playerName == null) { return null; }
        playerName = playerName.toLowerCase();

        // Check the cache
        CachedPlayer cachedPlayer = playerCache.get(playerName);
        if(cachedPlayer != null)
        {
            plugin.debug("DRCommandSender.getPlayer() : Found player in cache.");
            return cachedPlayer;
        }

        // Check Redis
        Map<String, String> result = connection.sync()
            .hgetall(bungeeName + ":players:" + playerName);

        if(result == null) { return null; }

        // If the map has the correct data
        String server = result.get("server");
        String ip = result.get("ip");

        if(server == null || ip == null) { return null; }

        // Add to the cache
        cachedPlayer = new CachedPlayer(server, ip);
        playerCache.put(playerName, cachedPlayer);

        // Return
        plugin.debug("DRCommandSender.getPlayer() : Updated/Added player in cache.");
        return cachedPlayer;
    }
}
