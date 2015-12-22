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
package com.yahoo.tracebachi.DeltaRedis.Bungee;

import com.google.common.base.Preconditions;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.yahoo.tracebachi.DeltaRedis.Shared.Interfaces.LoggablePlugin;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 11/29/15.
 */
public class DeltaRedisListener implements Listener
{
    private final String bungeeName;
    private StatefulRedisConnection<String, String> connection;
    private LoggablePlugin plugin;

    public DeltaRedisListener(String bungeeName, StatefulRedisConnection<String, String> connection,
        LoggablePlugin plugin)
    {
        this.bungeeName = bungeeName;
        this.connection = connection;
        this.plugin = plugin;
    }

    public void shutdown()
    {
        plugin = null;
        connection = null;
    }

    /**
     * Not to be confused with ServerConnectEvent, this event is called once
     * a connection to a server is fully operational, and is about to hand over
     * control of the session to the player. It is useful if you wish to send
     * information to the server before the player logs in.
     *
     * (Doc Source: https://github.com/SpigotMC/BungeeCord/blob/master/api/src/
     * main/java/net/md_5/bungee/api/event/ServerConnectedEvent.java
     */
    @EventHandler
    public void onServerConnectedEvent(ServerConnectedEvent event)
    {
        String playerName = event.getPlayer().getName();
        String serverName = event.getServer().getInfo().getName();
        String ip = event.getPlayer().getAddress().toString();

        setPlayerAsOnline(playerName, serverName, ip);
    }

    /**
     * Called when a player has left the proxy, it is not safe to call any methods
     * that perform an action on the passed player instance.
     *
     * (Doc Source: https://github.com/SpigotMC/BungeeCord/blob/master/api/src/
     * main/java/net/md_5/bungee/api/event/ServerConnectEvent.java)
     */
    @EventHandler
    public void onPlayerLeaveProxy(PlayerDisconnectEvent event)
    {
        String playerName = event.getPlayer().getName();
        setPlayerAsOffline(playerName);
    }

    /**
     * Handles events on the "DR-RunCmd" channel which allows players to execute
     * BungeeCord commands from their servers.
     *
     * @param event The DeltaRedisMessage event to handle.
     */
    @EventHandler
    public void onDeltaRedisMessage(DeltaRedisMessageEvent event)
    {
        if(!event.getChannel().equals("DR-RunCmd")) { return; }

        String command = event.getMessage();
        plugin.info("[RunCmd] Sender: " + event.getSender() + ", Command: /" + command);

        BungeeCord instance = BungeeCord.getInstance();
        instance.getPluginManager().dispatchCommand(instance.getConsole(), command);
    }

    /**
     * Adds the player to Redis with the key "bungeename:players:name" and into
     * the online player set with the key "bungeename:players".
     *
     * @param playerName Name of the player to add.
     * @param serverName Server of the player.
     * @param ip IP address of the player.
     */
    public void setPlayerAsOnline(String playerName, String serverName, String ip)
    {
        Preconditions.checkNotNull(playerName, "Player name cannot be null.");
        Preconditions.checkNotNull(serverName, "Server name cannot be null.");
        Preconditions.checkNotNull(ip, "IP cannot be null.");

        playerName = playerName.toLowerCase();
        serverName = serverName.toLowerCase();

        HashMap<String, String> map = new HashMap<>();
        map.put("server", serverName);
        map.put("ip", ip);

        connection.sync().hmset(bungeeName + ":players:" + playerName, map);
        connection.sync().sadd(bungeeName + ":players", playerName);
        plugin.debug("DeltaRedisListener.setPlayerAsOnline(" + playerName + ")");
    }

    /**
     * Removes the player from Redis with the key "bungeename:players:name" and
     * from the online player set with the key "bungeename:players".
     *
     * @param playerName Name of the player to remove.
     */
    public void setPlayerAsOffline(String playerName)
    {
        Preconditions.checkNotNull(playerName, "Player name cannot be null.");

        playerName = playerName.toLowerCase();

        connection.sync().srem(bungeeName + ":players", playerName);
        connection.sync().del(bungeeName + ":players:" + playerName);
        plugin.debug("DeltaRedisListener.setPlayerAsOffline(" + playerName + ")");
    }
}
