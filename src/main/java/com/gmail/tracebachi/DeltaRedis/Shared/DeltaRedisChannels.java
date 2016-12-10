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

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 1/21/16.
 */
public interface DeltaRedisChannels
{
    /**
     * Channel for sending messages to a single player on a different server
     */
    String SEND_MESSAGE = "DR-Message";

    /**
     * Channel for sending messages to all players on a different server
     */
    String SEND_ANNOUNCEMENT = "DR-Announce";

    /**
     * Channel for the /runcmd command
     */
    String RUN_CMD = "DR-RunCmd";

    /**
     * Change for the /setdebug command to forward changes to Bungee
     */
    String DEBUG_CATEGORY_CHANGE = "DR-DebugCategoryChange";
}
