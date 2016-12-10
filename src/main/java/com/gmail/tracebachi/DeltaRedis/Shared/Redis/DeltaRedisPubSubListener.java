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
package com.gmail.tracebachi.DeltaRedis.Shared.Redis;

import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisInterface;
import com.gmail.tracebachi.DeltaRedis.Shared.EscapeAndDelimiterUtil;
import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Shutdownable;
import com.lambdaworks.redis.pubsub.RedisPubSubListener;

import java.util.List;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class DeltaRedisPubSubListener implements RedisPubSubListener<String, String>, Shutdownable
{
    private DeltaRedisInterface plugin;

    public DeltaRedisPubSubListener(DeltaRedisInterface plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void shutdown()
    {
        this.plugin = null;
    }

    /**
     * Called when a message is received by the RedisPubSub listener
     *
     * @param channel         Ignored as the listener is only registered to explicit channels
     * @param completeMessage Complete received message
     */
    public void message(String channel, String completeMessage)
    {
        try
        {
            List<String> publishedMessageParts = EscapeAndDelimiterUtil
                .DELTA_SEPARATED
                .unescapeAndUndelimit(completeMessage);

            plugin.debug("Received message: " + completeMessage);

            plugin.onRedisMessageEvent(publishedMessageParts);
        }
        catch(IllegalArgumentException e)
        {
            plugin.severe("Received badly formatted message: " + completeMessage);

            e.printStackTrace();
        }
    }

    /**
     * @param channel Channel that the listener was registered to
     * @param count   Number of other listeners (on that Redis instance) on the channel
     */
    public void subscribed(String channel, long count)
    {
        plugin.debug("Listener subscribed to channel: " + channel);
    }

    /**
     * @param channel Channel that the listener was unregistered from
     * @param count   Number of other listeners (on that Redis instance) on the channel
     */
    public void unsubscribed(String channel, long count)
    {
        plugin.debug("Listener unsubscribed from channel: " + channel);
    }

    /**
     * This method handles pattern matched messages, but is unused.
     */
    public void message(String pattern, String channel, String message) {}

    /**
     * This method handles pattern subscription, but is unused.
     */
    public void psubscribed(String pattern, long count) {}

    /**
     * This method handles pattern unsubscription, but is unused.
     */
    public void punsubscribed(String pattern, long count) {}
}
