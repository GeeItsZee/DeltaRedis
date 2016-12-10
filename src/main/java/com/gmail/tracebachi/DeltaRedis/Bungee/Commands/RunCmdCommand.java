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
package com.gmail.tracebachi.DeltaRedis.Bungee.Commands;

import com.gmail.tracebachi.DeltaRedis.Bungee.DeltaRedis;
import com.gmail.tracebachi.DeltaRedis.Bungee.DeltaRedisApi;
import com.gmail.tracebachi.DeltaRedis.Bungee.Events.DeltaRedisMessageEvent;
import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisChannels;
import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Registerable;
import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Shutdownable;
import com.gmail.tracebachi.DeltaRedis.Shared.Servers;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.gmail.tracebachi.DeltaRedis.Shared.ChatMessageHelper.format;
import static com.gmail.tracebachi.DeltaRedis.Shared.SplitPatterns.COMMA;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 4/28/16.
 */
public class RunCmdCommand extends Command implements Listener, Registerable, Shutdownable
{
    private DeltaRedis plugin;

    public RunCmdCommand(DeltaRedis deltaRedis)
    {
        super("runcmdbungee", null, "rcbungee");
        this.plugin = deltaRedis;
    }

    @Override
    public void register()
    {
        PluginManager pluginManager = plugin.getProxy().getPluginManager();
        pluginManager.registerCommand(plugin, this);
        pluginManager.registerListener(plugin, this);
    }

    @Override
    public void unregister()
    {
        PluginManager pluginManager = plugin.getProxy().getPluginManager();
        pluginManager.unregisterCommand(this);
        pluginManager.unregisterListener(this);
    }

    @Override
    public void shutdown()
    {
        unregister();
        plugin = null;
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("DeltaRedis.RunCmd"))
        {
            sendMessage(sender, format(
                "NoPerm",
                "DeltaRedis.RunCmd"));
            return;
        }

        if(args.length <= 1)
        {
            sendMessage(sender, format(
                "Usage",
                "/runcmdbungee server[,server,...] command"));
            sendMessage(sender, format(
                "Usage",
                "/runcmdbungee ALL command"));
            return;
        }

        DeltaRedisApi deltaApi = DeltaRedisApi.instance();
        Set<String> argServers = new HashSet<>(Arrays.asList(COMMA.split(args[0])));
        Set<String> servers = deltaApi.getCachedServers();
        String commandStr = joinArgsForCommand(args);
        String senderName = sender.getName();

        if(doesSetContain(argServers, "ALL"))
        {
            deltaApi.sendServerCommand(Servers.SPIGOT, commandStr, senderName);

            sendMessage(sender, format(
                "DeltaRedis.CommandSent",
                "ALL"));
            return;
        }

        for(String dest : argServers)
        {
            String correctedDest = getMatchInSet(servers, dest);

            if(correctedDest != null)
            {
                deltaApi.sendServerCommand(correctedDest, commandStr, senderName);

                sendMessage(sender, format(
                    "DeltaRedis.CommandSent",
                    dest));
            }
            else
            {
                sendMessage(sender, format(
                    "DeltaRedis.ServerNotFound",
                    dest));
            }
        }
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

            ProxyServer proxy = plugin.getProxy();
            proxy.getPluginManager().dispatchCommand(proxy.getConsole(), command);
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

    private void sendMessage(CommandSender receiver, String message)
    {
        receiver.sendMessage(TextComponent.fromLegacyText(message));
    }
}
