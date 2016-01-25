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

import com.gmail.tracebachi.DeltaRedis.Shared.Prefixes;
import com.gmail.tracebachi.DeltaRedis.Shared.Servers;
import com.gmail.tracebachi.DeltaRedis.Shared.Shutdownable;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedisApi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 11/28/15.
 */
public class RunCmdCommand implements CommandExecutor, Shutdownable
{
    private DeltaRedisApi deltaApi;

    public RunCmdCommand(DeltaRedisApi deltaApi)
    {
        this.deltaApi = deltaApi;
    }

    @Override
    public void shutdown()
    {
        deltaApi = null;
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
            sender.sendMessage(Prefixes.INFO + "/runcmd BUNGEE command");
            return true;
        }

        Set<String> argServers = new HashSet<>(Arrays.asList(args[0].split(",")));
        Set<String> servers = deltaApi.getCachedServers();
        String commandStr = joinArgsForCommand(args);

        if(doesSetContain(argServers, "BUNGEE"))
        {
            if(deltaApi.isBungeeCordOnline())
            {
                deltaApi.sendCommandToServer(Servers.BUNGEECORD, commandStr);
                sender.sendMessage(Prefixes.SUCCESS +
                    "Sent command to " + Prefixes.input(Servers.BUNGEECORD));
            }
            else
            {
                sender.sendMessage(Prefixes.FAILURE +
                    "BungeeCord is currently down.");
            }
        }
        else if(doesSetContain(argServers, "ALL"))
        {
            deltaApi.sendCommandToServer(Servers.SPIGOT, commandStr);
            sender.sendMessage(Prefixes.SUCCESS +
                "Sent command to " + Prefixes.input("ALL"));
        }
        else
        {
            for(String dest : argServers)
            {
                if(!servers.contains(dest))
                {
                    sender.sendMessage(Prefixes.FAILURE + Prefixes.input(dest) +
                        " is offline or non-existent.");
                }
                else
                {
                    deltaApi.sendCommandToServer(dest, commandStr);
                    sender.sendMessage(Prefixes.SUCCESS + "Sent command to " +
                        Prefixes.input(dest));
                }
            }
        }
        return true;
    }

    private String joinArgsForCommand(String[] args)
    {
        return String.join(" ", Arrays.copyOfRange(args, 1, args.length));
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
}
