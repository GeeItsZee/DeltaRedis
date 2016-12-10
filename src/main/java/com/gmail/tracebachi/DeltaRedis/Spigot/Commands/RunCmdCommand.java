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
package com.gmail.tracebachi.DeltaRedis.Spigot.Commands;

import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisChannels;
import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Registerable;
import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Shutdownable;
import com.gmail.tracebachi.DeltaRedis.Shared.Servers;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedis;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedisApi;
import com.gmail.tracebachi.DeltaRedis.Spigot.Events.DeltaRedisMessageEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.gmail.tracebachi.DeltaRedis.Shared.ChatMessageHelper.format;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 11/28/15.
 */
public class RunCmdCommand implements CommandExecutor, Listener, Registerable, Shutdownable
{
    private DeltaRedis plugin;

    public RunCmdCommand(DeltaRedis plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void register()
    {
        plugin.getCommand("runcmd").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unregister()
    {
        plugin.getCommand("runcmd").setExecutor(null);

        HandlerList.unregisterAll(this);
    }

    @Override
    public void shutdown()
    {
        unregister();
        plugin = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if(!sender.hasPermission("DeltaRedis.RunCmd"))
        {
            sender.sendMessage(format(
                "NoPerm",
                "DeltaRedis.RunCmd"));
            return true;
        }

        if(args.length <= 1)
        {
            sender.sendMessage(format(
                "Usage",
                "/runcmd server[,server,...] command"));
            sender.sendMessage(format(
                "Usage",
                "/runcmd ALL command"));
            return true;
        }

        DeltaRedisApi deltaApi = DeltaRedisApi.instance();
        Set<String> argServers = new HashSet<>(Arrays.asList(args[0].split(",")));
        Set<String> servers = deltaApi.getCachedServers();
        String senderName = sender.getName();
        String commandStr = joinArgsForCommand(args);

        if(doesSetContain(argServers, "BUNGEE"))
        {
            if(deltaApi.isBungeeCordOnline())
            {
                deltaApi.sendServerCommand(Servers.BUNGEECORD, commandStr, senderName);

                sender.sendMessage(format(
                    "DeltaRedis.CommandSent",
                    "BUNGEE"));
            }
            else
            {
                sender.sendMessage(format("DeltaRedis.BungeeNotAvailable"));
            }
        }
        else if(doesSetContain(argServers, "ALL"))
        {
            deltaApi.sendServerCommand(Servers.SPIGOT, commandStr, senderName);

            sender.sendMessage(format(
                "DeltaRedis.CommandSent",
                "ALL"));
        }
        else
        {
            for(String dest : argServers)
            {
                String correctedDest = getMatchInSet(servers, dest);

                if(correctedDest != null)
                {
                    deltaApi.sendServerCommand(correctedDest, commandStr, senderName);

                    sender.sendMessage(format(
                        "DeltaRedis.CommandSent",
                        dest));
                }
                else
                {
                    sender.sendMessage(format(
                        "DeltaRedis.ServerNotFound",
                        dest));
                }
            }
        }

        return true;
    }

    @EventHandler
    public void onDeltaRedisMessage(DeltaRedisMessageEvent event)
    {
        String channel = event.getChannel();
        List<String> messageParts = event.getMessageParts();

        if(channel.equals(DeltaRedisChannels.RUN_CMD))
        {
            String sender = messageParts.get(0);
            String command = messageParts.get(1);

            plugin.info("[RunCmd] sendingServer: " + event.getSendingServer() +
                ", sender: " + sender +
                ", command: /" + command);

            plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    private String joinArgsForCommand(String[] args)
    {
        return String.join(" ", (CharSequence[]) Arrays.copyOfRange(args, 1, args.length));
    }

    private boolean doesSetContain(Set<String> set, String source)
    {
        for(String item : set)
        {
            if(item.equalsIgnoreCase(source))
            {
                return true;
            }
        }
        return false;
    }

    private String getMatchInSet(Set<String> set, String source)
    {
        for(String item : set)
        {
            if(item.equalsIgnoreCase(source))
            {
                return item;
            }
        }
        return null;
    }
}
