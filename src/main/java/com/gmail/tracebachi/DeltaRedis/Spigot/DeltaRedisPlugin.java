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

import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.IDeltaRedisPlugin;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DRCommandSender;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DRPubSubListener;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.Servers;
import com.gmail.tracebachi.DeltaRedis.Spigot.Commands.RunCmdCommand;
import com.google.common.base.Preconditions;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class DeltaRedisPlugin extends JavaPlugin implements IDeltaRedisPlugin
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
        saveDefaultConfig();
    }

    @Override
    public void onEnable()
    {
        severe("-----------------------------------------------------------------");
        severe("[IMPORTANT] Please make sure that \'ServerName\' is *exactly* the same as your BungeeCord config for this server.");
        severe("[IMPORTANT] DeltaRedis and all plugins that depend on it will not run correctly if the name is not correct.");
        severe("[IMPORTANT] \'World\' is not the same as \'world\'");
        severe("-----------------------------------------------------------------");

        reloadConfig();
        debugEnabled = getConfig().getBoolean("DebugMode", false);

        Preconditions.checkArgument(getConfig().contains("BungeeName"),
            "BungeeName not specified.");
        Preconditions.checkArgument(getConfig().contains("ServerNameInBungeeCord"),
            "ServerNameInBungeeCord not specified.");

        client = RedisClient.create(getRedisUri());
        pubSubConn = client.connectPubSub();
        standaloneConn = client.connect();

        pubSubListener = new DRPubSubListener(this);
        pubSubConn.addListener(pubSubListener);
        pubSubConn.sync().subscribe(
            getBungeeName() + ':' + getServerName(),
            getBungeeName() + ':' + Servers.SPIGOT);

        commandSender = new DRCommandSender(standaloneConn, this);
        commandSender.setup();

        deltaRedisApi = new DeltaRedisApi(commandSender, this);

        mainListener = new DeltaRedisListener(this);
        getServer().getPluginManager().registerEvents(mainListener, this);

        runCmdCommand = new RunCmdCommand(deltaRedisApi);
        getCommand("runcmd").setExecutor(runCmdCommand);

        int updatePeriod = getConfig().getInt("OnlineUpdatePeriod", 300);
        getServer().getScheduler().runTaskTimerAsynchronously(this, () ->
        {
            commandSender.getServers();
            commandSender.getPlayers();
        }, 20, updatePeriod);
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
    }

    public DeltaRedisApi getDeltaRedisApi()
    {
        return deltaRedisApi;
    }

    @Override
    public void onRedisMessageEvent(String source, String channel, String message)
    {
        DeltaRedisMessageEvent event = new DeltaRedisMessageEvent(source, channel, message);
        getServer().getScheduler().runTask(this,
            () -> getServer().getPluginManager().callEvent(event));

    }

    @Override
    public String getBungeeName()
    {
        return getConfig().getString("BungeeName");
    }

    @Override
    public String getServerName()
    {
        return getConfig().getString("ServerNameInBungeeCord");
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

    private RedisURI getRedisUri()
    {
        String redisUrl = getConfig().getString("RedisServer.URL");
        String redisPort = getConfig().getString("RedisServer.Port");
        String redisPass = getConfig().getString("RedisServer.Password");
        boolean hasPassword = getConfig().getBoolean("RedisServer.HasPassword");

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
