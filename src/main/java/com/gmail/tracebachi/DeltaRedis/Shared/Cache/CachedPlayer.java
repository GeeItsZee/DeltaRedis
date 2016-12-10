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
package com.gmail.tracebachi.DeltaRedis.Shared.Cache;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class CachedPlayer implements Cacheable
{
    private final String ip;
    private final String server;
    private final long timeCreatedAt;

    public CachedPlayer(String ip, String server)
    {
        this.ip = ip;
        this.server = server;
        this.timeCreatedAt = System.currentTimeMillis();
    }

    /**
     * @return Time in milliseconds when this {@link CachedPlayer} was created
     */
    @Override
    public long getTimeCreatedAt()
    {
        return timeCreatedAt;
    }

    /**
     * @return IP address of the player recorded by BungeeCord
     */
    public String getIp()
    {
        return ip;
    }

    /**
     * @return The last known server the player was on
     */
    public String getServer()
    {
        return server;
    }
}
