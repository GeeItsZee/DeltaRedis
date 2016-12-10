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
package com.gmail.tracebachi.DeltaRedis.Shared;

import java.util.List;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 11/27/15.
 */
public interface DeltaRedisInterface
{
    /**
     * Handles a DeltaRedis message sent to the server
     *
     * @param publishedMessageParts Complete message (as parts) being received
     * <p>
     * This message parts include the sending server an channel
     * as the first 2 elements.
     * </p>
     */
    void onRedisMessageEvent(List<String> publishedMessageParts);

    /**
     * Handles a DeltaRedis message sent to the server
     *
     * @param server Server the message is coming from
     * @param channel Channel the message is going to
     * @param messageParts Channel message (in parts) being received
     */
    void onRedisMessageEvent(String server, String channel, List<String> messageParts);

    /**
     * @return Name of the BungeeCord instance to which the server belongs
     * <p>This value is set in the configuration file for each server.</p>
     */
    String getBungeeName();

    /**
     * @return {@link Servers#BUNGEECORD} or the name of the Spigot server
     * <p>This value is set in the configuration file for each server.</p>
     */
    String getServerName();

    /**
     * Logs the message as INFO
     *
     * @param message Message to log
     */
    void info(String message);

    /**
     * Logs the message as SEVERE
     *
     * @param message Message to log
     */
    void severe(String message);

    /**
     * Logs the message as DEBUG if debug is enabled
     *
     * @param message Message to log
     */
    void debug(String message);
}
