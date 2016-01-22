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
import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.LoggablePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.regex.Pattern;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class DeltaRedisListener implements Listener
{
    private static final Pattern DELTA_PATTERN = Pattern.compile("/\\\\");
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\\\\n");

    private LoggablePlugin plugin;

    public DeltaRedisListener(LoggablePlugin plugin)
    {
        this.plugin = plugin;
    }

    public void shutdown()
    {
        this.plugin = null;
    }

    @EventHandler
    public void onDeltaRedisMessage(DeltaRedisMessageEvent event)
    {
        String channel = event.getChannel();
        String eventMessage = event.getMessage();

        if(channel.equals(DeltaRedisChannels.RUN_CMD))
        {
            plugin.info("[RunCmd] {SendingServer: " + event.getSendingServer() +
                " , Command: /" + eventMessage + "}");

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), eventMessage);
        }
        else if(channel.equals(DeltaRedisChannels.SEND_ANNOUNCEMENT))
        {
            String[] lines = NEWLINE_PATTERN.split(eventMessage);

            for(Player player : Bukkit.getOnlinePlayers())
            {
                for(String line : lines)
                {
                    player.sendMessage(line);
                }
            }
        }
        else if(channel.equals(DeltaRedisChannels.SEND_MESSAGE))
        {
            String[] receiverAndMessage = DELTA_PATTERN.split(eventMessage, 2);
            String receiverName = receiverAndMessage[0];
            String[] lines = NEWLINE_PATTERN.split(receiverAndMessage[1]);

            if(receiverName.equalsIgnoreCase("console"))
            {
                for(String line : lines)
                {
                    Bukkit.getConsoleSender().sendMessage(line);
                }
            }
            else
            {
                Player receiver = Bukkit.getPlayer(receiverName);
                if(receiver != null && receiver.isOnline())
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
