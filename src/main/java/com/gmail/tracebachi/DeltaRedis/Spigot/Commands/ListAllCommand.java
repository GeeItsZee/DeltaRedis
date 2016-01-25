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
import com.gmail.tracebachi.DeltaRedis.Shared.Shutdownable;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedisApi;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 11/28/15.
 */
public class ListAllCommand implements CommandExecutor, Shutdownable
{
    private DeltaRedisApi deltaApi;

    public ListAllCommand(DeltaRedisApi deltaApi)
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
        if(!sender.hasPermission("DeltaRedis.ListAll"))
        {
            sender.sendMessage(Prefixes.FAILURE + "You do not have permission to run this command.");
            return true;
        }

        List<String> playerList = new ArrayList<>(deltaApi.getCachedPlayers());
        Collections.sort(playerList);
        String joined = String.join(ChatColor.GRAY + ", " + ChatColor.WHITE, playerList);

        sender.sendMessage(Prefixes.INFO + "Players Online on Network:");
        sender.sendMessage(ChatColor.WHITE + joined);
        return true;
    }
}
