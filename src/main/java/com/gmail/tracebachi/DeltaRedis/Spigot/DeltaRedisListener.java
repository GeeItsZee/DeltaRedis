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
package com.gmail.tracebachi.DeltaRedis.Spigot;

import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisChannels;
import com.gmail.tracebachi.DeltaRedis.Shared.Registerable;
import com.gmail.tracebachi.DeltaRedis.Shared.Shutdownable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import static com.gmail.tracebachi.DeltaRedis.Shared.SplitPatterns.DELTA;
import static com.gmail.tracebachi.DeltaRedis.Shared.SplitPatterns.NEWLINE;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class DeltaRedisListener implements Listener, Registerable, Shutdownable
{
    private DeltaRedis plugin;

    public DeltaRedisListener(DeltaRedis plugin)
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
        String eventMessage = event.getMessage();

        if(channel.equals(DeltaRedisChannels.RUN_CMD))
        {
            String[] splitMessage = DELTA.split(eventMessage, 2);
            String sender = splitMessage[0];
            String command = splitMessage[1];

            plugin.info("[RunCmd] {SendingServer: " + event.getSendingServer() +
                " , Sender: " + sender +
                " , Command: /" + command + "}");

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), eventMessage);
        }
        else if(channel.equals(DeltaRedisChannels.SEND_ANNOUNCEMENT))
        {
            String[] splitMessage = DELTA.split(eventMessage, 2);
            String permission = splitMessage[0];
            String[] lines = NEWLINE.split(splitMessage[1]);

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
            String[] splitMessage = DELTA.split(eventMessage, 2);
            String receiverName = splitMessage[0];
            String[] lines = NEWLINE.split(splitMessage[1]);

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
