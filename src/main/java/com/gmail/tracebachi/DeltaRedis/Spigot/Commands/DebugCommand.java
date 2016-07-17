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

import com.gmail.tracebachi.DeltaRedis.Shared.Registerable;
import com.gmail.tracebachi.DeltaRedis.Shared.Shutdownable;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedis;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static com.gmail.tracebachi.DeltaRedis.Shared.Prefixes.*;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 11/28/15.
 */
public class DebugCommand implements CommandExecutor, Registerable, Shutdownable
{
    private DeltaRedis plugin;

    public DebugCommand(DeltaRedis plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void register()
    {
        plugin.getCommand("deltaredisdebug").setExecutor(this);
    }

    @Override
    public void unregister()
    {
        plugin.getCommand("deltaredisdebug").setExecutor(null);
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
        if(!sender.hasPermission("DeltaRedis.Debug"))
        {
            sender.sendMessage(FAILURE + "You do not have the " +
                input("DeltaRedis.Debug") + " permission.");
            return true;
        }

        if(args.length < 1)
        {
            sender.sendMessage(INFO + "/deltaredisdebug <on|off>");
            return true;
        }

        if(args[0].equalsIgnoreCase("on"))
        {
            plugin.setDebugEnabled(true);
        }
        else
        {
            plugin.setDebugEnabled(false);
        }

        return true;
    }
}
