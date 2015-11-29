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
package com.yahoo.tracebachi.DeltaRedis.Shared.Cache;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com) on 10/18/15.
 */
public class CachedPlayer implements Cacheable
{
    private final String server;
    private final String ip;
    private final long timeCreatedAt;

    public CachedPlayer(String server, String ip)
    {
        this.server = server;
        this.ip = ip;
        this.timeCreatedAt = System.currentTimeMillis();
    }

    @Override
    public long getTimeCreatedAt()
    {
        return timeCreatedAt;
    }

    public String getServer()
    {
        return server;
    }

    public String getIp()
    {
        return ip;
    }
}
