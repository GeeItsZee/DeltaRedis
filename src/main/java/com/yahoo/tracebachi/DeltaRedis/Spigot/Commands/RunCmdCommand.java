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

import com.yahoo.tracebachi.DeltaRedis.Shared.DeltaRedisApi;
import com.yahoo.tracebachi.DeltaRedis.Shared.DeltaRedisChannels;
import com.yahoo.tracebachi.DeltaRedis.Spigot.Prefixes;
import org.bukkit.Bukkit;
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
    private DeltaRedisApi deltaRedisApi;

    public RunCmdCommand(DeltaRedisApi deltaRedisApi)
    {
        this.deltaRedisApi = deltaRedisApi;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args)
    {
        if(!commandSender.hasPermission("DeltaRedis.runcmd"))
        {
            commandSender.sendMessage(Prefixes.FAILURE + "You do not have permission to run this command.");
            return true;
        }

        if(args.length <= 1)
        {
            commandSender.sendMessage(Prefixes.INFO + "/runcmd server[,server,...] command");
            return true;
        }

        Set<String> destServers = new HashSet<>(Arrays.asList(args[0].split(",")));
        Set<String> servers = deltaRedisApi.getServers();
        String commandStr = joinArgsForCommand(args);

        if(destServers.contains(DeltaRedisChannels.BUNGEECORD))
        {
            commandSender.sendMessage(Prefixes.FAILURE + "DeltaRedis is designed to disable sending commands to " +
                "BungeeCord. If you just need to sent to all Spigot servers, use \"SPIGOT\".");
        }
        else if(destServers.contains(DeltaRedisChannels.SPIGOT) || destServers.contains("ALL"))
        {
            deltaRedisApi.publish(DeltaRedisChannels.SPIGOT, "DeltaRedis-RunCmd", commandStr);
        }
        else
        {
            for(String dest : destServers)
            {
                if(destServers.contains(deltaRedisApi.getServerName()))
                {
                    Bukkit.dispatchCommand(commandSender, commandStr);
                }
                else if(!servers.contains(dest))
                {
                    commandSender.sendMessage(Prefixes.FAILURE + ChatColor.WHITE + dest + ChatColor.GRAY +
                        " is offline or non-existent.");
                }
                else
                {
                    deltaRedisApi.publish(dest, "RunCmd", commandStr);
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
