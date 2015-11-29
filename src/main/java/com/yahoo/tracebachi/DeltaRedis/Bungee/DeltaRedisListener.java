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

import com.yahoo.tracebachi.DeltaRedis.Shared.IDeltaRedisPlugin;
import com.yahoo.tracebachi.DeltaRedis.Shared.Redis.DRCommandSender;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 11/29/15.
 */
public class DeltaRedisListener implements Listener
{
    private IDeltaRedisPlugin plugin;
    private DRCommandSender commandSender;

    public DeltaRedisListener(IDeltaRedisPlugin plugin, DRCommandSender commandSender)
    {
        this.plugin = plugin;
        this.commandSender = commandSender;
    }

    public void shutdown()
    {
        plugin = null;
        commandSender = null;
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
        commandSender.setPlayerAsOffline(playerName);
    }

    @EventHandler
    public void onDeltaRedisMessage(DeltaRedisMessageEvent event)
    {
        if(!event.getChannel().equals("RunCmd")) { return; }

        String command = event.getMessage();
        plugin.info("[RunCmd] Sender = " + event.getSender() + ", Command = " + command);

        BungeeCord instance = BungeeCord.getInstance();
        instance.getPluginManager().dispatchCommand(instance.getConsole(), command);
    }
}
