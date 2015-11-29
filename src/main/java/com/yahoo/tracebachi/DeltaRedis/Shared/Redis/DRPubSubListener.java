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
package com.yahoo.tracebachi.DeltaRedis.Shared.Redis;

import com.lambdaworks.redis.pubsub.RedisPubSubListener;
import com.yahoo.tracebachi.DeltaRedis.Shared.DeltaRedisChannels;
import com.yahoo.tracebachi.DeltaRedis.Shared.IDeltaRedisPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com) on 10/18/15.
 */
public class DRPubSubListener implements RedisPubSubListener<String, String>
{
    private static Pattern messageSplitPattern = Pattern.compile("/\\\\");

    private final String serverName;
    private final IDeltaRedisPlugin plugin;
    private final Set<String> validChannels;

    public DRPubSubListener(String bungeeName, String serverName, IDeltaRedisPlugin plugin)
    {
        this.serverName = serverName;
        this.plugin = plugin;
        this.validChannels = new HashSet<>();

        this.validChannels.add(bungeeName + ':' + serverName);
        this.validChannels.add(bungeeName + ':' + DeltaRedisChannels.SPIGOT);
        this.validChannels.add(bungeeName + ':' + DeltaRedisChannels.BUNGEECORD);
    }

    public void message(String channel, String message)
    {
        if(validChannels.contains(channel))
        {
            String[] splitMessage = messageSplitPattern.split(message, 3);

            if(splitMessage.length == 3)
            {
                plugin.debug(message);

                // Ignore messages sent to self
                if(!splitMessage[0].equals(serverName))
                {
                    plugin.callDeltaRedisMessageEvent(splitMessage[0], splitMessage[1], splitMessage[2]);
                }
                else
                {
                    plugin.debug("Ignored message received from self.");
                }
            }
            else
            {
                plugin.info("Received badly formatted message in DRPubSubListener.");
            }
        }
    }

    public void subscribed(String channel, long count)
    {
        plugin.debug("Subscribed to channel: " + channel);
    }

    public void unsubscribed(String channel, long count)
    {
        plugin.debug("Unsubscribed from channel: " + channel);
    }

    public void message(String pattern, String channel, String message)
    {

    }

    public void psubscribed(String pattern, long count)
    {

    }

    public void punsubscribed(String pattern, long count)
    {

    }
}
