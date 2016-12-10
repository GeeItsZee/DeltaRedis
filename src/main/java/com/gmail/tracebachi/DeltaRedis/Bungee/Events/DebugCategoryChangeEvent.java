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
package com.gmail.tracebachi.DeltaRedis.Bungee.Events;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.plugin.Event;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/14/16.
 */
public class DebugCategoryChangeEvent extends Event
{
    private final String debugCategory;
    private final boolean enable;

    /**
     * Constructs a new DebugCategoryChangeEvent
     *
     * @param debugCategory Name of the debug type
     * @param enable True if the user requested the debug category
     * to be enable or false
     */
    public DebugCategoryChangeEvent(String debugCategory, boolean enable)
    {
        Preconditions.checkNotNull(debugCategory, "debugCategory");
        Preconditions.checkArgument(!debugCategory.isEmpty(), "Empty debugCategory");

        this.debugCategory = debugCategory;
        this.enable = enable;
    }

    /**
     * @return A (hopefully) unique category string used by a plugin
     * <p>Shared category strings may mean multiple will assume a change is
     * for them when it is not.</p>
     */
    public String getDebugCategory()
    {
        return debugCategory;
    }

    /**
     * @return True if the user requested the debug category to be
     * enable or disabled
     */
    public boolean shouldEnable()
    {
        return enable;
    }
}
