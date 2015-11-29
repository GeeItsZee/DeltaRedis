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
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.yahoo.tracebachi.DeltaRedis.Shared.DeltaRedisApi;
import com.yahoo.tracebachi.DeltaRedis.Shared.DeltaRedisChannels;
import com.yahoo.tracebachi.DeltaRedis.Shared.IDeltaRedisPlugin;
import com.yahoo.tracebachi.DeltaRedis.Shared.Redis.DRCommandSender;
import com.yahoo.tracebachi.DeltaRedis.Shared.Redis.DRPubSubListener;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.*;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com) on 10/18/15.
 */
public class DeltaRedisPlugin extends Plugin implements IDeltaRedisPlugin, Listener
{
    private boolean debugEnabled;
    private RedisClient client;
    private DRPubSubListener pubSubListener;
    private DRCommandSender commandSender;
    private StatefulRedisPubSubConnection<String, String> pubSubConn;
    private StatefulRedisConnection<String, String> standaloneConn;

    @Override
    public void onEnable()
    {
        Configuration config;
        try
        {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(loadResource(this, "config.yml"));
        }
        catch(IOException e)
        {
            getLogger().severe("Failed to load configuration file. Report this error and the following stacktrace.");
            e.printStackTrace();
            return;
        }

        if(!validateConfig(config))
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

        client = RedisClient.create(getRedisUri(config));
        pubSubConn = client.connectPubSub();
        standaloneConn = client.connect();

        pubSubListener = new DRPubSubListener(bungeeName, DeltaRedisChannels.BUNGEECORD, this);
        pubSubConn.addListener(pubSubListener);
        pubSubConn.sync().subscribe(bungeeName + ':' + DeltaRedisChannels.BUNGEECORD);

        commandSender = new DRCommandSender(standaloneConn, bungeeName, DeltaRedisChannels.BUNGEECORD,
            playerCacheTime, this);
        commandSender.setup();

        getProxy().getPluginManager().registerListener(this, this);
    }

    @Override
    public void onDisable()
    {
        getProxy().getPluginManager().unregisterListener(this);

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
    public void callDeltaRedisMessageEvent(String source, String channel, String message)
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

    private boolean validateConfig(Configuration config)
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

    @EventHandler
    public void onDeltaRedisMessage(DeltaRedisMessageEvent event)
    {
        if(!event.getChannel().equals("RunCmd")) { return; }

        String command = event.getMessage();
        getLogger().info("[RunCmd] Sender = " + event.getSender() + ", Command = " + command);
        getProxy().getPluginManager().dispatchCommand(getProxy().getConsole(), command);
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
