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
package com.yahoo.tracebachi.DeltaRedis.Shared.Interfaces;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 11/27/15.
 */
public interface IDeltaRedisPlugin
{
    /**
     * Handles a DeltaRedis message to the server.
     *
     * @param source Server that sent the message.
     * @param channel Custom channel that the message should be delivered to.
     * @param message Message to deliver.
     */
    void onRedisMessageEvent(String source, String channel, String message);

    /**
     * Logs the message as INFO.
     *
     * @param message Message to log.
     */
    void info(String message);

    /**
     * Logs the message as SEVERE.
     *
     * @param message Message to log.
     */
    void severe(String message);

    /**
     * Logs the message as DEBUG if debug is enabled.
     *
     * @param message Message to log.
     */
    void debug(String message);
}
