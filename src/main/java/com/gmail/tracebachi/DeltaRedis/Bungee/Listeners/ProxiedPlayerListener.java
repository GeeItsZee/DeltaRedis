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
package com.gmail.tracebachi.DeltaRedis.Bungee.Listeners;

import com.gmail.tracebachi.DeltaRedis.Bungee.DeltaRedis;
import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Registerable;
import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Shutdownable;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DeltaRedisCommandSender;
import com.google.common.base.Preconditions;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 11/29/15.
 */
public class ProxiedPlayerListener implements Listener, Registerable, Shutdownable
{
    private final HashSet<String> onlinePlayers = new HashSet<>(64);
    private DeltaRedisCommandSender commandSender;
    private DeltaRedis plugin;

    public ProxiedPlayerListener(DeltaRedisCommandSender commandSender, DeltaRedis plugin)
    {
        this.commandSender = commandSender;
        this.plugin = plugin;
    }

    @Override
    public void register()
    {
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @Override
    public void unregister()
    {
        plugin.getProxy().getPluginManager().unregisterListener(this);
    }

    @Override
    public void shutdown()
    {
        for(ProxiedPlayer player : BungeeCord.getInstance().getPlayers())
        {
            commandSender.removePlayer(player.getName());
        }

        // This handles the case where PlayerDisconnectEvent may not have
        // been called to flush data for players that are no longer online.
        for(String name : onlinePlayers)
        {
            commandSender.removePlayer(name);
        }

        onlinePlayers.clear();
        commandSender.removePlayers();

        commandSender = null;
        plugin = null;
    }

    /**
     * Not to be confused with ServerConnectEvent, this event is called once
     * a connection to a server is fully operational, and is about to hand over
     * control of the session to the player. It is useful if you wish to send
     * information to the server before the player logs in.
     * <p>
     * (Doc Source: https://github.com/SpigotMC/BungeeCord/blob/master/api/src/
     * main/java/net/md_5/bungee/api/event/ServerConnectedEvent.java)
     * </p>
     */
    @EventHandler
    public void onServerConnectedEvent(ServerConnectedEvent event)
    {
        ProxiedPlayer player = event.getPlayer();
        String playerName = player.getName();
        String serverName = event.getServer().getInfo().getName();
        String ip = player.getAddress().toString();

        Preconditions.checkNotNull(playerName, "playerName");
        Preconditions.checkNotNull(serverName, "serverName");
        Preconditions.checkNotNull(ip, "ip");

        HashMap<String, String> map = new HashMap<>();
        map.put("server", serverName);
        map.put("ip", ip);

        commandSender.updatePlayer(playerName, map);
        onlinePlayers.add(playerName);
    }

    /**
     * Called when a player has left the proxy, it is not safe to call any methods
     * that perform an action on the passed player instance.
     * <p>
     * (Doc Source: https://github.com/SpigotMC/BungeeCord/blob/master/api/src/
     * main/java/net/md_5/bungee/api/event/ServerConnectEvent.java)
     * </p>
     */
    @EventHandler
    public void onPlayerLeaveProxy(PlayerDisconnectEvent event)
    {
        String playerName = event.getPlayer().getName();

        commandSender.removePlayer(playerName);
        onlinePlayers.remove(playerName);
    }
}
