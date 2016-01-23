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
package com.gmail.tracebachi.DeltaRedis.Shared.Interfaces;

import com.gmail.tracebachi.DeltaRedis.Shared.Redis.Servers;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 11/27/15.
 */
public interface IDeltaRedis extends LoggablePlugin
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
     * @return Name of the BungeeCord instance to which the server belongs.
     * This value is set in the configuration file for each server.
     */
    String getBungeeName();

    /**
     * @return Name of the server. If the server is a BungeeCord instance, the
     * server name will be {@link Servers#BUNGEECORD}. This value is set in
     * the configuration file for each server.
     */
    String getServerName();
}
