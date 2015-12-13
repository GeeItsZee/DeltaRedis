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
package com.yahoo.tracebachi.DeltaRedis.Spigot;

import com.yahoo.tracebachi.DeltaRedis.Shared.Interfaces.LoggablePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.regex.Pattern;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com) on 10/18/15.
 */
public class DeltaRedisListener implements Listener
{
    private static final Pattern pattern = Pattern.compile("/\\\\");

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
        if(event.getChannel().equals(DeltaRedisApi.RUN_CMD_CHANNEL))
        {
            String command = event.getMessage();
            plugin.info("[RunCmd] Sender: " + event.getSender() + ", Command: " + command);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
        else if(event.getChannel().equals(DeltaRedisApi.SEND_MESSAGE_CHANNEL))
        {
            String[] receiverAndMessage = pattern.split(event.getMessage(), 2);

            if(receiverAndMessage[0].equalsIgnoreCase("console"))
            {
                Bukkit.getConsoleSender().sendMessage(receiverAndMessage[1]);
            }
            else
            {
                Player receiver = Bukkit.getPlayer(receiverAndMessage[0]);
                if(receiver != null && receiver.isOnline())
                {
                    receiver.sendMessage(receiverAndMessage[1]);
                }
            }
        }
    }
}
