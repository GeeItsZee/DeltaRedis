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
package com.yahoo.tracebachi.DeltaRedis.Spigot.Commands;

import com.yahoo.tracebachi.DeltaRedis.Shared.Redis.Channels;
import com.yahoo.tracebachi.DeltaRedis.Spigot.DeltaRedisApi;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 11/28/15.
 */
public class RunCmdCommand implements CommandExecutor
{
    private DeltaRedisApi deltaApi;

    public RunCmdCommand(DeltaRedisApi deltaApi)
    {
        this.deltaApi = deltaApi;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if(!sender.hasPermission("DeltaRedis.RunCmd"))
        {
            sender.sendMessage(Prefixes.FAILURE + "You do not have permission to run this command.");
            return true;
        }

        if(args.length <= 1)
        {
            sender.sendMessage(Prefixes.INFO + "/runcmd server[,server,...] command");
            sender.sendMessage(Prefixes.INFO + "/runcmd ALL command");
            sender.sendMessage(Prefixes.INFO + "/runcmd BUNGEECORD command");
            return true;
        }

        Set<String> destServers = new HashSet<>(Arrays.asList(args[0].split(",")));
        Set<String> servers = deltaApi.getCachedServers();
        String commandStr = joinArgsForCommand(args);

        if(destServers.contains(Channels.BUNGEECORD))
        {
            deltaApi.sendCommandToServer(Channels.BUNGEECORD, commandStr);
            sender.sendMessage(Prefixes.INFO + "Sent command to " +
                ChatColor.WHITE + Channels.BUNGEECORD);
        }
        else if(destServers.contains(Channels.SPIGOT) || destServers.contains("ALL"))
        {
            deltaApi.sendCommandToServer(Channels.SPIGOT, commandStr);
            sender.sendMessage(Prefixes.INFO + "Sent command to " +
                ChatColor.WHITE + "ALL");
        }
        else
        {
            for(String dest : destServers)
            {
                if(!servers.contains(dest))
                {
                    sender.sendMessage(Prefixes.FAILURE + ChatColor.WHITE + dest +
                        ChatColor.GRAY + " is offline or non-existent.");
                }
                else
                {
                    deltaApi.sendCommandToServer(dest, commandStr);
                    sender.sendMessage(Prefixes.INFO + "Sent command to " +
                        ChatColor.WHITE + dest);
                }
            }
        }
        return true;
    }

    private String joinArgsForCommand(String[] args)
    {
        return String.join(" ", Arrays.copyOfRange(args, 1, args.length));
    }
}
