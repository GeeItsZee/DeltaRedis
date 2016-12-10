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
package com.gmail.tracebachi.DeltaRedis.Spigot.Listeners;

import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisChannels;
import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Registerable;
import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Shutdownable;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedis;
import com.gmail.tracebachi.DeltaRedis.Spigot.Events.DeltaRedisMessageEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.List;

import static com.gmail.tracebachi.DeltaRedis.Shared.SplitPatterns.NEWLINE;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class DeltaRedisChatMessageListener implements Listener, Registerable, Shutdownable
{
    private DeltaRedis plugin;

    public DeltaRedisChatMessageListener(DeltaRedis plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void register()
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unregister()
    {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void shutdown()
    {
        unregister();
        plugin = null;
    }

    @EventHandler
    public void onDeltaRedisMessage(DeltaRedisMessageEvent event)
    {
        String channel = event.getChannel();
        List<String> messageParts = event.getMessageParts();

        if(channel.equals(DeltaRedisChannels.SEND_ANNOUNCEMENT))
        {
            String permission = messageParts.get(0);
            String[] lines = NEWLINE.split(messageParts.get(1));

            if(permission.equals(""))
            {
                for(String line : lines)
                {
                    Bukkit.broadcastMessage(line);
                }
            }
            else
            {
                for(String line : lines)
                {
                    Bukkit.broadcast(line, permission);
                }
            }
        }
        else if(channel.equals(DeltaRedisChannels.SEND_MESSAGE))
        {
            String receiverName = messageParts.get(0);
            String[] lines = NEWLINE.split(messageParts.get(1));

            if(receiverName.equalsIgnoreCase("console"))
            {
                for(String line : lines)
                {
                    Bukkit.getConsoleSender().sendMessage(line);
                }
            }
            else
            {
                Player receiver = Bukkit.getPlayerExact(receiverName);

                if(receiver != null)
                {
                    for(String line : lines)
                    {
                        receiver.sendMessage(line);
                    }
                }
            }
        }
    }
}
