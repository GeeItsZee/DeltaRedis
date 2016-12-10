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

import com.gmail.tracebachi.DeltaRedis.Shared.ChatMessageHelper;
import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisInterface;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DeltaRedisCommandSender;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DeltaRedisPubSubListener;
import com.gmail.tracebachi.DeltaRedis.Shared.Servers;
import com.gmail.tracebachi.DeltaRedis.Spigot.Commands.DebugCategoryCommand;
import com.gmail.tracebachi.DeltaRedis.Spigot.Commands.IsOnlineCommand;
import com.gmail.tracebachi.DeltaRedis.Spigot.Commands.RunCmdCommand;
import com.gmail.tracebachi.DeltaRedis.Spigot.Events.DeltaRedisMessageEvent;
import com.gmail.tracebachi.DeltaRedis.Spigot.Listeners.DeltaRedisChatMessageListener;
import com.google.common.base.Preconditions;
import com.lambdaworks.redis.ClientOptions;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.lambdaworks.redis.resource.ClientResources;
import com.lambdaworks.redis.resource.DefaultClientResources;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class DeltaRedis extends JavaPlugin implements DeltaRedisInterface
{
    private boolean debugEnabled;
    private int updatePeriod;
    private String bungeeName;
    private String serverName;
    private DeltaRedisCommandSender commandSender;
    private DeltaRedisPubSubListener pubSubListener;
    private DeltaRedisChatMessageListener deltaRedisChatMessageListener;
    private DebugCategoryCommand debugCategoryCommand;
    private IsOnlineCommand isOnlineCommand;
    private RunCmdCommand runCmdCommand;

    private ClientResources resources;
    private RedisClient client;
    private StatefulRedisPubSubConnection<String, String> pubSubConn;
    private StatefulRedisConnection<String, String> commandConn;

    @Override
    public void onLoad()
    {
        saveDefaultConfig();
    }

    @Override
    public void onEnable()
    {
        info("-----------------------------------------------------------------");
        info("[IMPORTANT] Please make sure that \'ServerName\' is *exactly* the");
        info("[IMPORTANT] same as your BungeeCord config for this server.");
        info("[IMPORTANT] DeltaRedis and all plugins that depend on it may not");
        info("[IMPORTANT] run correctly if the name is not correct.");
        info("[IMPORTANT] \'World\' is not the same as \'world\'");
        info("-----------------------------------------------------------------");

        reloadConfig();
        readConfig(getConfig());
        setupRedis(getConfig());

        debugCategoryCommand = new DebugCategoryCommand(this);
        debugCategoryCommand.register();

        runCmdCommand = new RunCmdCommand(this);
        runCmdCommand.register();

        isOnlineCommand = new IsOnlineCommand(this);
        isOnlineCommand.register();

        deltaRedisChatMessageListener = new DeltaRedisChatMessageListener(this);
        deltaRedisChatMessageListener.register();

        DeltaRedisApi.setup(commandSender, this);

        getServer().getScheduler().runTaskTimerAsynchronously(
            this,
            () ->
            {
                if(commandSender != null)
                {
                    commandSender.getServers();
                    commandSender.getPlayers();
                }
            }, 20, updatePeriod);
    }

    @Override
    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);

        DeltaRedisApi.shutdown();

        deltaRedisChatMessageListener.shutdown();
        deltaRedisChatMessageListener = null;

        runCmdCommand.shutdown();
        runCmdCommand = null;

        isOnlineCommand.shutdown();
        isOnlineCommand = null;

        debugCategoryCommand.shutdown();
        debugCategoryCommand = null;

        commandSender.shutdown();
        commandSender = null;

        commandConn.close();
        commandConn = null;

        pubSubConn.close();
        pubSubConn = null;

        pubSubListener.shutdown();
        pubSubListener = null;

        client.shutdown();
        client = null;

        resources.shutdown();
        resources = null;
    }

    public void setDebugEnabled(boolean debugEnabled)
    {
        this.debugEnabled = debugEnabled;
    }

    @Override
    public void onRedisMessageEvent(List<String> allMessageParts)
    {
        Preconditions.checkNotNull(allMessageParts, "allMessageParts");
        Preconditions.checkArgument(
            allMessageParts.size() >= 2,
            "Less than expected number of parts in message");

        String sendingServer = allMessageParts.get(0);
        String channel = allMessageParts.get(1);
        List<String> eventMessageParts = new ArrayList<>(allMessageParts.size() - 2);

        for(int i = 2; i < allMessageParts.size(); i++)
        {
            eventMessageParts.add(allMessageParts.get(i));
        }

        onRedisMessageEvent(sendingServer, channel, eventMessageParts);
    }

    @Override
    public void onRedisMessageEvent(String sendingServer,
                                    String channel,
                                    List<String> messageParts)
    {
        Preconditions.checkNotNull(sendingServer, "sendingServer");
        Preconditions.checkNotNull(channel, "channel");
        Preconditions.checkNotNull(messageParts, "messageParts");

        DeltaRedisMessageEvent event = new DeltaRedisMessageEvent(
            sendingServer,
            channel,
            messageParts);

        getServer().getPluginManager().callEvent(event);
    }

    @Override
    public String getBungeeName()
    {
        return bungeeName;
    }

    @Override
    public String getServerName()
    {
        return serverName;
    }

    @Override
    public void info(String message)
    {
        getLogger().info(message);
    }

    @Override
    public void severe(String message)
    {
        getLogger().severe(message);
    }

    @Override
    public void debug(String message)
    {
        if(debugEnabled)
        {
            getLogger().info("[Debug] " + message);
        }
    }

    private void readConfig(ConfigurationSection configuration)
    {
        Preconditions.checkNotNull(configuration, "configuration");

        debugEnabled = configuration.getBoolean("Debug", false);
        bungeeName = configuration.getString("BungeeName");
        serverName = configuration.getString("ServerNameInBungeeCord");
        updatePeriod = getConfig().getInt("OnlineUpdatePeriod", 300);

        Preconditions.checkNotNull(bungeeName, "bungeeName");
        Preconditions.checkNotNull(serverName, "serverName");

        ConfigurationSection formatsSection = configuration.getConfigurationSection("Formats");
        for(String key : formatsSection.getKeys(false))
        {
            String value = formatsSection.getString(key);
            if(value != null)
            {
                String translatedFormat = ChatColor
                    .translateAlternateColorCodes('&', value);
                ChatMessageHelper.instance().updateFormat("DeltaRedis." + key, translatedFormat);
            }
        }
    }

    private String getRedisUri(ConfigurationSection config)
    {
        String url = config.getString("RedisServer.URL");
        String port = config.getString("RedisServer.Port");
        String password = config.getString("RedisServer.Password");

        Preconditions.checkNotNull(url, "RedisServer.URL");
        Preconditions.checkNotNull(port, "RedisServer.Port");

        if(password != null)
        {
            return "redis://" + password + '@' + url + ':' + port;
        }
        else
        {
            return "redis://" + url + ':' + port;
        }
    }

    private void setupRedis(ConfigurationSection configuration)
    {
        resources = new DefaultClientResources
            .Builder()
            .ioThreadPoolSize(3)
            .computationThreadPoolSize(3)
            .build();

        client = RedisClient.create(resources, RedisURI.create(getRedisUri(configuration)));
        client.setOptions(new ClientOptions.Builder().autoReconnect(true).build());

        pubSubConn = client.connectPubSub();
        commandConn = client.connect();

        pubSubListener = new DeltaRedisPubSubListener(this);
        pubSubConn.addListener(pubSubListener);
        pubSubConn.sync().subscribe(
            getBungeeName() + ':' + getServerName(),
            getBungeeName() + ':' + Servers.SPIGOT);

        commandSender = new DeltaRedisCommandSender(commandConn, this);
        commandSender.setup();
    }
}
