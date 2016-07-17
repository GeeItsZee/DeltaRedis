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

import com.gmail.tracebachi.DeltaRedis.Shared.Cache.CachedPlayer;
import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisChannels;
import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DRCommandSender;
import com.gmail.tracebachi.DeltaRedis.Shared.Servers;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/11/15.
 */
public class DeltaRedisApi
{
    private static DeltaRedisApi instance;

    public static DeltaRedisApi instance()
    {
        return instance;
    }

    private DRCommandSender deltaSender;
    private DeltaRedis plugin;

    /**
     * Package-private constructor.
     */
    DeltaRedisApi(DRCommandSender deltaSender, DeltaRedis plugin)
    {
        if(instance != null)
        {
            instance.shutdown();
        }

        this.deltaSender = deltaSender;
        this.plugin = plugin;

        instance = this;
    }

    /**
     * Package-private shutdown method.
     */
    void shutdown()
    {
        this.deltaSender = null;
        this.plugin = null;

        instance = null;
    }

    /**
     * @return Name of the BungeeCord instance to which the server belongs.
     * This value is set in the configuration file for each server.
     */
    public String getBungeeName()
    {
        return plugin.getBungeeName();
    }

    /**
     * @return Name of the server (String). If the server is BungeeCord, the
     * server name will be {@link Servers#BUNGEECORD}.
     */
    public String getServerName()
    {
        return plugin.getServerName();
    }

    /**
     * @return An unmodifiable set of servers that are part of the same
     * BungeeCord as the current server. This method will retrieve the
     * servers from the last call to {@link DRCommandSender#getServers()}.
     */
    public Set<String> getCachedServers()
    {
        return deltaSender.getCachedServers();
    }

    /**
     * @return True if the BungeeCord instance was last known to be online.
     * False if it was not.
     */
    public boolean isBungeeCordOnline()
    {
        return deltaSender.isBungeeCordOnline();
    }

    /**
     * @return An unmodifiable set of players (names) that are part of the
     * same BungeeCord. This method will retrieve the players from the last
     * call to {@link DRCommandSender#getPlayers()}.
     */
    public Set<String> getCachedPlayers()
    {
        return deltaSender.getCachedPlayers();
    }

    /**
     * @param partial Non-null string that is the beginning of a name
     *
     * @return A list of player names that begins with the partial
     * sent to this method.
     */
    public List<String> matchStartOfPlayerName(String partial)
    {
        Preconditions.checkNotNull(partial, "Partial was null.");

        List<String> result = new ArrayList<>();
        partial = partial.toLowerCase();

        for(String name : getCachedPlayers())
        {
            if(name.startsWith(partial))
            {
                result.add(name);
            }
        }

        return result;
    }

    /**
     * @param partial Non-null string that is the beginning of a name
     *
     * @return A list of player names that begins with the partial
     * sent to this method.
     */
    public List<String> matchStartOfServerName(String partial)
    {
        Preconditions.checkNotNull(partial, "Partial was null.");

        List<String> result = new ArrayList<>();
        partial = partial.toLowerCase();

        for(String name : getCachedServers())
        {
            if(name.toLowerCase().startsWith(partial))
            {
                result.add(name);
            }
        }

        return result;
    }

    /**
     * Asynchronously looks for the player in Redis. The callback is called synchronously
     * with the {@link CachedPlayer} or null.
     *
     * @param playerName Name of the player to find.
     * @param callback Callback to run when fetch is complete.
     */
    public void findPlayer(String playerName, CachedPlayerCallback callback)
    {
        findPlayer(playerName, callback, true);
    }

    /**
     * Asynchronously looks for the player in Redis. The callback is called
     * with the {@link CachedPlayer} or null.
     *
     * @param playerName Name of the player to find.
     * @param callback Callback to run when fetch is complete.
     * @param syncCallback Set to true to run callback sync else it will run async.
     */
    public void findPlayer(String playerName, CachedPlayerCallback callback, boolean syncCallback)
    {
        Preconditions.checkNotNull(playerName, "PlayerName was null.");
        Preconditions.checkNotNull(callback, "Callback was null.");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {
            CachedPlayer cachedPlayer = deltaSender.getPlayer(playerName);

            if(syncCallback)
            {
                Bukkit.getScheduler().runTask(
                    plugin,
                    () -> callback.call(cachedPlayer));
            }
            else
            {
                callback.call(cachedPlayer);
            }
        });
    }

    /**
     * Publishes a message built from string message pieces joined by
     * the "/\" (forward-slash, back-slash) to Redis.
     *
     * @param destination Server to send message to.
     * @param channel Channel of the message.
     * @param messagePieces The parts of the message.
     */
    public void publish(String destination, String channel, String... messagePieces)
    {
        String joinedMessage = String.join("/\\", (CharSequence[]) messagePieces);

        publish(destination, channel, joinedMessage);
    }

    /**
     * Publishes a message to Redis.
     *
     * @param destServer Server to send message to.
     * @param channel Channel of the message.
     * @param message The actual message.
     */
    public void publish(String destServer, String channel, String message)
    {
        Preconditions.checkNotNull(destServer, "DestServer was null.");
        Preconditions.checkNotNull(channel, "Channel was null.");
        Preconditions.checkNotNull(message, "Message was null.");

        if(plugin.getServerName().equals(destServer))
        {
            plugin.onRedisMessageEvent(destServer, channel, message);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(
            plugin,
            () -> deltaSender.publish(
                destServer,
                channel,
                message));
    }

    /**
     * Sends a command that will run as OP by the receiving server.
     *
     * @param destServer Destination server name.
     * @param command Command to send.
     */
    public void sendCommandToServer(String destServer, String command)
    {
        sendCommandToServer(destServer, command, "UNKNOWN_PLUGIN");
    }

    /**
     * Sends a command that will run as OP by the receiving server.
     *
     * @param destServer Destination server name.
     * @param command Command to send.
     * @param sender Name to record in the logs as having run the command.
     */
    public void sendCommandToServer(String destServer, String command, String sender)
    {
        Preconditions.checkNotNull(destServer, "DestServer was null.");
        Preconditions.checkNotNull(command, "Command was null.");
        Preconditions.checkNotNull(sender, "Sender was null.");

        if(plugin.getServerName().equals(destServer))
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(
            plugin,
            () -> deltaSender.publish(
                destServer,
                DeltaRedisChannels.RUN_CMD,
                sender + "/\\" + command));
    }

    /**
     * Sends a message to a player on a different server. It fails quietly if
     * the player is not online. This method should not be used to send messages
     * to players that are on the same server.
     *
     * @param playerName Name of the player to try and send a message to.
     * @param message Message to send.
     */
    public void sendMessageToPlayer(String playerName, String message)
    {
        Preconditions.checkNotNull(playerName, "PlayerName was null.");
        Preconditions.checkNotNull(message, "Message was null.");

        Bukkit.getScheduler().runTaskAsynchronously(
            plugin,
            () ->
            {
                CachedPlayer cachedPlayer = deltaSender.getPlayer(playerName);

                if(cachedPlayer == null) { return; }

                deltaSender.publish(
                    cachedPlayer.getServer(),
                    DeltaRedisChannels.SEND_MESSAGE,
                    playerName + "/\\" + message);
            });
    }

    /**
     * Sends an announcement to all players on a server. If
     * {@link Servers#SPIGOT} is used, the announcement will also be performed
     * on the current server.
     *
     * @param destServer Destination server name or {@link Servers#SPIGOT}.
     * @param announcement Announcement to send.
     */
    public void sendAnnouncementToServer(String destServer, String announcement)
    {
        sendAnnouncementToServer(destServer, announcement, "");
    }

    /**
     * Sends an announcement to all players on a server. If
     * {@link Servers#SPIGOT} is used, the announcement will also be performed
     * on the current server.
     *
     * @param destServer Destination server name or {@link Servers#SPIGOT}.
     * @param announcement Announcement to send.
     * @param permission Permission that a player must have to receive the
     *                   announcement. The empty string, "", can be used to
     *                   signal that a permission is not required.
     */
    public void sendAnnouncementToServer(String destServer, String announcement, String permission)
    {
        Preconditions.checkNotNull(destServer, "DestServer was null.");
        Preconditions.checkNotNull(announcement, "Announcement was null.");
        Preconditions.checkNotNull(permission, "Permission was null.");

        Bukkit.getScheduler().runTaskAsynchronously(
            plugin,
            () -> deltaSender.publish(
                destServer,
                DeltaRedisChannels.SEND_ANNOUNCEMENT,
                permission + "/\\" + announcement));
    }
}
