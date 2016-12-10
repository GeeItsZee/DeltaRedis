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
import com.gmail.tracebachi.DeltaRedis.Bungee.Events.DebugCategoryChangeEvent;
import com.gmail.tracebachi.DeltaRedis.Bungee.Events.DeltaRedisMessageEvent;
import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisChannels;
import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Registerable;
import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Shutdownable;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.event.EventHandler;

import java.util.List;

import static com.gmail.tracebachi.DeltaRedis.Shared.ChatMessageHelper.format;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/10/16.
 */
public class DebugCategoryCommand extends Command implements Listener, Registerable, Shutdownable
{
    private DeltaRedis plugin;

    public DebugCategoryCommand(DeltaRedis deltaRedis)
    {
        super("setdebugbungee");
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
        if(!sender.hasPermission("DeltaRedis.DebugCategory"))
        {
            sendMessage(sender, format(
                "NoPerm",
                "DeltaRedis.DebugCategory"));
            return;
        }

        if(args.length < 2)
        {
            sendMessage(sender, format(
                "Usage",
                "/setdebugbungee <category> <on|off>"));
            return;
        }

        if(args[1].equalsIgnoreCase("on"))
        {
            DebugCategoryChangeEvent event = new DebugCategoryChangeEvent(args[0], true);
            plugin.getProxy().getPluginManager().callEvent(event);

            sendMessage(sender, format(
                "DeltaRedis.DebugCategoryChange",
                args[0],
                "on"));
        }
        else if(args[1].equalsIgnoreCase("off"))
        {
            DebugCategoryChangeEvent event = new DebugCategoryChangeEvent(args[0], false);
            plugin.getProxy().getPluginManager().callEvent(event);

            sendMessage(sender, format(
                "DeltaRedis.DebugCategoryChange",
                args[0],
                "off"));
        }
        else
        {
            sendMessage(sender, format(
                "Usage",
                "/setdebugbungee <category> <on|off>"));
        }
    }

    @EventHandler
    public void onDeltaRedisMessage(DeltaRedisMessageEvent event)
    {
        String channel = event.getChannel();
        List<String> messageParts = event.getMessageParts();

        if(channel.equals(DeltaRedisChannels.DEBUG_CATEGORY_CHANGE))
        {
            String sender = messageParts.get(0);
            String category = messageParts.get(1);
            boolean enabled = messageParts.get(2).equals("1");
            DebugCategoryChangeEvent changeEvent = new DebugCategoryChangeEvent(category, enabled);

            plugin.info("[DebugCategoryChange] sendingServer: " + event.getSendingServer() +
                ", sender: " + sender +
                ", category: " + category +
                ", enabled: " + enabled);

            plugin.getProxy().getPluginManager().callEvent(changeEvent);
        }
    }

    @EventHandler
    public void onDebugCategoryChange(DebugCategoryChangeEvent event)
    {
        if(event.getDebugCategory().equalsIgnoreCase("DeltaRedisBungee"))
        {
            plugin.setDebugEnabled(event.shouldEnable());
        }
    }

    private void sendMessage(CommandSender receiver, String message)
    {
        receiver.sendMessage(TextComponent.fromLegacyText(message));
    }
}
