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
package com.yahoo.tracebachi.DeltaRedis.Shared;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 11/27/15.
 */
public interface IDeltaRedisPlugin
{
    DeltaRedisApi getDeltaRedisApi();

    void callDeltaRedisMessageEvent(String source, String channel, String message);

    void info(String message);

    void severe(String message);

    void debug(String message);
}
