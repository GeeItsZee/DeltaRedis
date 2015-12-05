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
package com.yahoo.tracebachi.DeltaRedis.Bungee;

import com.google.common.io.ByteStreams;
import com.lambdaworks.redis.ClientOptions;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.yahoo.tracebachi.DeltaRedis.Shared.Channels;
import com.yahoo.tracebachi.DeltaRedis.Shared.Interfaces.DeltaRedisApi;
import com.yahoo.tracebachi.DeltaRedis.Shared.Interfaces.IDeltaRedisPlugin;
import com.yahoo.tracebachi.DeltaRedis.Shared.Redis.DRCommandSender;
import com.yahoo.tracebachi.DeltaRedis.Shared.Redis.DRPubSubListener;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com) on 10/18/15.
 */
public class DeltaRedisPlugin extends Plugin implements IDeltaRedisPlugin, Listener
{
    private boolean debugEnabled;
    private Configuration config;
    private RedisClient client;
    private DRPubSubListener pubSubListener;
    private DRCommandSender commandSender;
    private StatefulRedisPubSubConnection<String, String> pubSubConn;
    private StatefulRedisConnection<String, String> standaloneConn;
    private DeltaRedisListener mainListener;

    @Override
    public void onLoad()
    {
        try
        {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class)
                .load(loadResource(this, "config.yml"));
        }
        catch(IOException e)
        {
            getLogger().severe("Failed to load configuration file. " +
                "Report this error and the following stacktrace.");
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable()
    {
        if(config == null || !isConfigValid(config))
        {
            getLogger().severe("Invalid configuration file!");
            getLogger().severe("Backup the configuration that currently exists.");
            getLogger().severe("Remove the configuration (but not the backup).");
            getLogger().severe("Restart the server to create a default configuration.");
            getLogger().severe("An older config that works with this version of DeltaRedis can also be used.");
            return;
        }

        debugEnabled = config.getBoolean("DebugMode", false);
        String bungeeName = config.getString("BungeeName");
        int playerCacheTime = config.getInt("PlayerCacheTime", 1000);

        ClientOptions.Builder optionBuilder = new ClientOptions.Builder();
        optionBuilder.autoReconnect(true);
        client = RedisClient.create(getRedisUri(config));
        client.setOptions(optionBuilder.build());
        pubSubConn = client.connectPubSub();
        standaloneConn = client.connect();

        pubSubListener = new DRPubSubListener(Channels.BUNGEECORD, this);
        pubSubConn.addListener(pubSubListener);
        pubSubConn.sync().subscribe(bungeeName + ':' + Channels.BUNGEECORD);

        commandSender = new DRCommandSender(standaloneConn, bungeeName,
            Channels.BUNGEECORD, playerCacheTime, this);
        commandSender.setup();

        mainListener = new DeltaRedisListener(this, commandSender);
        getProxy().getPluginManager().registerListener(this, mainListener);

        // Schedule a task every two minutes to cleanup the cache
        getProxy().getScheduler().schedule(this, () ->
        {
            if(commandSender != null)
            {
                commandSender.cleanupCache();
            }
        }, 2, 2, TimeUnit.MINUTES);
    }

    @Override
    public void onDisable()
    {
        if(mainListener != null)
        {
            getProxy().getPluginManager().unregisterListener(mainListener);
            mainListener.shutdown();
            mainListener = null;
        }

        // Remove all online players from Redis
        for(ProxiedPlayer player : getProxy().getPlayers())
        {
            commandSender.setPlayerAsOffline(player.getName());
        }

        if(commandSender != null)
        {
            commandSender.shutdown();
            commandSender = null;
        }

        if(standaloneConn != null)
        {
            standaloneConn.close();
            standaloneConn = null;
        }

        if(pubSubConn != null)
        {
            pubSubConn.removeListener(pubSubListener);
            pubSubConn.close();
            pubSubConn = null;
            pubSubListener = null;
        }

        if(client != null)
        {
            client.shutdown();
            client = null;
        }

        debugEnabled = false;
    }

    @Override
    public DeltaRedisApi getDeltaRedisApi()
    {
        return commandSender;
    }

    @Override
    public void onDeltaRedisMessageEvent(String source, String channel, String message)
    {
        DeltaRedisMessageEvent event = new DeltaRedisMessageEvent(source, channel, message);
        getProxy().getPluginManager().callEvent(event);
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

    private boolean isConfigValid(Configuration config)
    {
        return config.get("ServerName") != null &&
            config.get("BungeeName") != null &&
            config.get("PlayerCacheTime") != null &&
            config.get("RedisServer.URL") != null &&
            config.get("RedisServer.Port") != null &&
            config.get("RedisServer.Password") != null &&
            config.get("RedisServer.HasPassword") != null;
    }

    private RedisURI getRedisUri(Configuration config)
    {
        String redisUrl = config.getString("RedisServer.URL");
        String redisPort = config.getString("RedisServer.Port");
        String redisPass = config.getString("RedisServer.Password");
        boolean hasPassword = config.getBoolean("RedisServer.HasPassword");

        if(hasPassword)
        {
            return RedisURI.create("redis://" + redisPass + '@' + redisUrl + ':' + redisPort);
        }
        else
        {
            return RedisURI.create("redis://" + redisUrl + ':' + redisPort);
        }
    }

    /***
     * Source for this method found at:
     * https://www.spigotmc.org/threads/bungeecords-configuration-api.11214/#post-119017
     *
     * Originally authored by: vemacs, Feb 15, 2014
     */
    public static File loadResource(Plugin plugin, String resource)
    {
        File folder = plugin.getDataFolder();
        if (!folder.exists())
        {
            folder.mkdir();
        }

        File destinationFile = new File(folder, resource);
        try
        {
            if(!destinationFile.exists())
            {
                destinationFile.createNewFile();
                try (InputStream in = plugin.getResourceAsStream(resource);
                     OutputStream out = new FileOutputStream(destinationFile))
                {
                    ByteStreams.copy(in, out);
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return destinationFile;
    }
}
