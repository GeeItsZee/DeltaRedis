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

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.yahoo.tracebachi.DeltaRedis.Shared.Interfaces.IDeltaRedisPlugin;
import com.yahoo.tracebachi.DeltaRedis.Shared.Interfaces.LoggablePlugin;
import com.yahoo.tracebachi.DeltaRedis.Shared.Redis.Channels;
import com.yahoo.tracebachi.DeltaRedis.Shared.Redis.DRCommandSender;
import com.yahoo.tracebachi.DeltaRedis.Shared.Redis.DRPubSubListener;
import com.yahoo.tracebachi.DeltaRedis.Spigot.Commands.RunCmdCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com) on 10/18/15.
 */
public class DeltaRedisPlugin extends JavaPlugin implements IDeltaRedisPlugin, LoggablePlugin
{
    private boolean debugEnabled;
    private DeltaRedisListener mainListener;
    private RunCmdCommand runCmdCommand;

    private RedisClient client;
    private DRPubSubListener pubSubListener;
    private DRCommandSender commandSender;
    private DeltaRedisApi deltaRedisApi;
    private StatefulRedisPubSubConnection<String, String> pubSubConn;
    private StatefulRedisConnection<String, String> standaloneConn;

    @Override
    public void onLoad()
    {
        File config = new File(getDataFolder(), "config.yml");
        if(!config.exists()) { saveDefaultConfig(); }
    }

    @Override
    public void onEnable()
    {
        reloadConfig();
        debugEnabled = getConfig().getBoolean("DebugMode", false);

        if(!isConfigValid())
        {
            getLogger().severe("Invalid configuration file!");
            getLogger().severe("Backup the configuration that currently exists.");
            getLogger().severe("Remove the configuration (but not the backup).");
            getLogger().severe("Restart the server to create a default configuration.");
            getLogger().severe("An older config that works with this version of DeltaRedis can also be used.");
            return;
        }

        String serverName = getConfig().getString("ServerName");
        String bungeeName = getConfig().getString("BungeeName");

        client = RedisClient.create(getRedisUri());
        pubSubConn = client.connectPubSub();
        standaloneConn = client.connect();

        pubSubListener = new DRPubSubListener(serverName, this);
        pubSubConn.addListener(pubSubListener);
        pubSubConn.sync().subscribe(
            bungeeName + ':' + serverName,
            bungeeName + ':' + Channels.SPIGOT);

        commandSender = new DRCommandSender(standaloneConn,
            bungeeName, serverName, this);
        commandSender.setup();

        deltaRedisApi = new DeltaRedisApi(commandSender, this);

        mainListener = new DeltaRedisListener(this);
        getServer().getPluginManager().registerEvents(mainListener, this);

        runCmdCommand = new RunCmdCommand(deltaRedisApi);
        getCommand("runcmd").setExecutor(runCmdCommand);

        getServer().getScheduler().runTaskTimerAsynchronously(this, () ->
        {
            commandSender.getServers();
            commandSender.getPlayers();
        }, 20, 20 * 15);
    }

    @Override
    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);

        if(runCmdCommand != null)
        {
            getCommand("runcmd").setExecutor(null);
            runCmdCommand.shutdown();
            runCmdCommand = null;
        }

        if(mainListener != null)
        {
            mainListener.shutdown();
            mainListener = null;
        }

        if(deltaRedisApi != null)
        {
            deltaRedisApi.shutdown();
            deltaRedisApi = null;
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

        if(pubSubListener != null)
        {
            pubSubListener.shutdown();
            pubSubListener = null;
        }

        if(client != null)
        {
            client.shutdown();
            client = null;
        }

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

        // Call the event synchronously
        getServer().getScheduler().runTask(this, () ->
            getServer().getPluginManager().callEvent(event));

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

    private boolean isConfigValid()
    {
        FileConfiguration config = getConfig();

        return config.get("ServerName") != null &&
            config.get("BungeeName") != null &&
            config.get("RedisServer.URL") != null &&
            config.get("RedisServer.Port") != null &&
            config.get("RedisServer.Password") != null &&
            config.get("RedisServer.HasPassword") != null;
    }

    private RedisURI getRedisUri()
    {
        String redisUrl = getConfig().getString("RedisServer.URL");
        String redisPort = getConfig().getString("RedisServer.Port");
        String redisPass = getConfig().getString("RedisServer.Password");
        boolean hasPassword = getConfig().getBoolean("RedisServer.HasPassword");

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
