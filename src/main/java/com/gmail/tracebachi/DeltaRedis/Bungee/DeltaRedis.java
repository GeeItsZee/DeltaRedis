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
package com.gmail.tracebachi.DeltaRedis.Bungee;

import com.gmail.tracebachi.DeltaRedis.Bungee.Commands.DebugCategoryCommand;
import com.gmail.tracebachi.DeltaRedis.Bungee.Commands.RunCmdCommand;
import com.gmail.tracebachi.DeltaRedis.Bungee.Events.DeltaRedisMessageEvent;
import com.gmail.tracebachi.DeltaRedis.Bungee.Listeners.ProxiedPlayerListener;
import com.gmail.tracebachi.DeltaRedis.Bungee.Utils.ConfigUtil;
import com.gmail.tracebachi.DeltaRedis.Shared.ChatMessageHelper;
import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisInterface;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DeltaRedisCommandSender;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DeltaRedisPubSubListener;
import com.gmail.tracebachi.DeltaRedis.Shared.Servers;
import com.google.common.base.Preconditions;
import com.lambdaworks.redis.ClientOptions;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.lambdaworks.redis.resource.ClientResources;
import com.lambdaworks.redis.resource.DefaultClientResources;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class DeltaRedis extends Plugin implements DeltaRedisInterface
{
    private boolean debugEnabled;
    private String bungeeName;
    private DeltaRedisCommandSender commandSender;
    private DeltaRedisPubSubListener pubSubListener;
    private ProxiedPlayerListener proxiedPlayerListener;
    private RunCmdCommand runCmdCommand;
    private DebugCategoryCommand debugCategoryCommand;

    private ClientResources resources;
    private RedisClient client;
    private StatefulRedisPubSubConnection<String, String> pubSubConn;
    private StatefulRedisConnection<String, String> commandConn;

    @Override
    public void onEnable()
    {
        info("-----------------------------------------------------------------");
        info("[IMPORTANT] Please verify that all Spigot servers are configured");
        info("[IMPORTANT] with their correct cased name. For example: ");
        info("[IMPORTANT] \'World\' is not the same as \'world\'");
        for(Map.Entry<String, ServerInfo> entry : getProxy().getServers().entrySet())
        {
            info("[IMPORTANT] Case-sensitive server name: " + entry.getValue().getName());
        }
        info("-----------------------------------------------------------------");

        Configuration configuration = loadConfig();
        readConfig(configuration);
        setupRedis(configuration);

        debugCategoryCommand = new DebugCategoryCommand(this);
        debugCategoryCommand.register();

        runCmdCommand = new RunCmdCommand(this);
        runCmdCommand.register();

        proxiedPlayerListener = new ProxiedPlayerListener(commandSender, this);
        proxiedPlayerListener.register();

        DeltaRedisApi.setup(commandSender, this);

        getProxy().getScheduler().schedule(this, () ->
        {
            if(commandSender != null)
            {
                commandSender.getServers();
                commandSender.getPlayers();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable()
    {
        getProxy().getScheduler().cancel(this);

        DeltaRedisApi.shutdown();

        proxiedPlayerListener.shutdown();
        proxiedPlayerListener = null;

        runCmdCommand.shutdown();
        runCmdCommand = null;

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

        getProxy().getPluginManager().callEvent(event);
    }

    @Override
    public String getBungeeName()
    {
        return bungeeName;
    }

    @Override
    public String getServerName()
    {
        return Servers.BUNGEECORD;
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

    private Configuration loadConfig()
    {
        try
        {
            File file = ConfigUtil.saveResource(
                this,
                "bungee-config.yml",
                "config.yml");
            Configuration config = ConfigurationProvider
                .getProvider(YamlConfiguration.class)
                .load(file);

            if(config != null) { return config; }

            ConfigUtil.saveResource(
                this,
                "bungee-config.yml",
                "config-example.yml",
                true);

            throw new RuntimeException("Failed to load configuration file");
        }
        catch(IOException e)
        {
            throw new RuntimeException("Failed to load configuration file", e);
        }
    }

    private void readConfig(Configuration configuration)
    {
        Preconditions.checkNotNull(configuration, "configuration");

        debugEnabled = configuration.getBoolean("Debug", false);
        bungeeName = configuration.getString("BungeeName");

        Preconditions.checkNotNull(bungeeName, "bungeeName");

        Configuration formatsSection = configuration.getSection("Formats");
        for(String key : formatsSection.getKeys())
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

    private String getRedisUri(Configuration config)
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

    private void setupRedis(Configuration configuration)
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
        pubSubConn.sync().subscribe(getBungeeName() + ':' + Servers.BUNGEECORD);

        commandSender = new DeltaRedisCommandSender(commandConn, this);
        commandSender.setup();
    }
}
