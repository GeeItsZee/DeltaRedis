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

import com.gmail.tracebachi.DeltaRedis.Bungee.Commands.RunCmdBungeeCommand;
import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisInterface;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DRCommandSender;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DRPubSubListener;
import com.gmail.tracebachi.DeltaRedis.Shared.Servers;
import com.google.common.base.Preconditions;
import com.lambdaworks.redis.ClientOptions;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.lambdaworks.redis.resource.ClientResources;
import com.lambdaworks.redis.resource.DefaultClientResources;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class DeltaRedis extends Plugin implements DeltaRedisInterface
{
    private boolean debugEnabled;
    private Configuration config;
    private ClientResources resources;
    private RedisClient client;
    private DRPubSubListener pubSubListener;
    private DRCommandSender commandSender;
    private StatefulRedisPubSubConnection<String, String> pubSubConn;
    private StatefulRedisConnection<String, String> commandConn;

    private DeltaRedisListener mainListener;
    private RunCmdBungeeCommand runCmdBungeeCommand;
    private DeltaRedisApi deltaRedisApi;

    @Override
    public void onEnable()
    {
        info("-----------------------------------------------------------------");
        info("[IMPORTANT] Please verify that all Spigot servers are configured with their correct cased name.");
        info("[IMPORTANT] \'World\' is not the same as \'world\'");
        for(Map.Entry<String, ServerInfo> entry : getProxy().getServers().entrySet())
        {
            info("[IMPORTANT] Case-sensitive server name: " + entry.getValue().getName());
        }
        info("-----------------------------------------------------------------");

        reloadConfig();
        if(config == null) { return; }
        debugEnabled = config.getBoolean("DebugMode", false);

        Preconditions.checkArgument(config.get("BungeeName") != null,
            "BungeeName not specified.");

        ClientOptions.Builder optionBuilder = new ClientOptions.Builder();
        optionBuilder.autoReconnect(true);

        resources = new DefaultClientResources.Builder()
            .ioThreadPoolSize(4)
            .computationThreadPoolSize(4)
            .build();

        client = RedisClient.create(resources, getRedisUri(config));
        client.setOptions(optionBuilder.build());
        pubSubConn = client.connectPubSub();
        commandConn = client.connect();

        pubSubListener = new DRPubSubListener(this);
        pubSubConn.addListener(pubSubListener);
        pubSubConn.sync().subscribe(getBungeeName() + ':' + Servers.BUNGEECORD);

        commandSender = new DRCommandSender(commandConn, this);
        commandSender.setup();

        deltaRedisApi = new DeltaRedisApi(commandSender, this);

        mainListener = new DeltaRedisListener(commandConn, this);
        mainListener.register();

        runCmdBungeeCommand = new RunCmdBungeeCommand(deltaRedisApi, this);
        runCmdBungeeCommand.register();

        getProxy().getScheduler().schedule(this, () ->
        {
            commandSender.getServers();
            commandSender.getPlayers();
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable()
    {
        runCmdBungeeCommand.shutdown();
        runCmdBungeeCommand = null;

        mainListener.shutdown();
        mainListener = null;

        deltaRedisApi.shutdown();
        deltaRedisApi = null;

        commandSender.shutdown();
        commandSender = null;

        commandConn.close();
        commandConn = null;

        pubSubConn.removeListener(pubSubListener);
        pubSubConn.close();
        pubSubConn = null;

        pubSubListener.shutdown();
        pubSubListener = null;

        client.shutdown();
        client = null;

        resources.shutdown();
        resources = null;

        debugEnabled = false;
    }

    public DeltaRedisApi getDeltaRedisApi()
    {
        return deltaRedisApi;
    }

    @Override
    public void onRedisMessageEvent(String source, String channel, String message)
    {
        DeltaRedisMessageEvent event = new DeltaRedisMessageEvent(source, channel, message);
        getProxy().getPluginManager().callEvent(event);
    }

    @Override
    public String getBungeeName()
    {
        return config.getString("BungeeName");
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

    private void reloadConfig()
    {
        try
        {
            File file = ConfigUtil.saveResource(this, "bungee-config.yml", "config.yml");
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

            if(config == null)
            {
                ConfigUtil.saveResource(this, "bungee-config.yml", "config-example.yml", true);
                getLogger().severe("Invalid configuration file! An example configuration" +
                    " has been saved to the DeltaRedis folder.");
            }
        }
        catch(IOException e)
        {
            getLogger().severe("Failed to load configuration file.");
            e.printStackTrace();
        }
    }

    private RedisURI getRedisUri(Configuration config)
    {
        String redisUrl = config.getString("RedisServer.URL");
        String redisPort = config.getString("RedisServer.Port");
        String redisPass = config.getString("RedisServer.Password");
        boolean hasPassword = config.getBoolean("RedisServer.HasPassword");

        Preconditions.checkNotNull(redisUrl, "Redis URL cannot be null.");
        Preconditions.checkNotNull(redisPort, "Redis Port cannot be null.");

        if(hasPassword)
        {
            return RedisURI.create("redis://" + redisPass + '@' + redisUrl + ':' + redisPort);
        }
        else
        {
            return RedisURI.create("redis://" + redisUrl + ':' + redisPort);
        }
    }
}
