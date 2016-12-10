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
package com.gmail.tracebachi.DeltaRedis.Spigot.Events;

import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Callback;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/17/16.
 */
public abstract class DelayedHandingEvent<T> extends Event
{
    private final Set<Plugin> intents = new HashSet<>();
    private final Callback<T> callback;
    private final Object LOCK = new Object();

    private boolean fired = false;

    /**
     * @param callback Callback to call when there are no intents left
     */
    public DelayedHandingEvent(Callback<T> callback)
    {
        Preconditions.checkNotNull(callback, "callback");
        this.callback = callback;
    }

    /**
     * Registers the intent to do other work before completing the event
     *
     * @param plugin Plugin to register intent with
     */
    public void registerIntent(Plugin plugin)
    {
        Preconditions.checkNotNull(plugin, "plugin");

        synchronized(LOCK)
        {
            Preconditions.checkArgument(!fired, "Event has finished firing");
            Preconditions.checkArgument(
                !intents.contains(plugin),
                plugin.getName() + " already registered intent");

            intents.add(plugin);
        }
    }

    /**
     * Completes the intent to do other work before completing the event
     *
     * @param plugin Plugin to complete intent with
     */
    public void completeIntent(Plugin plugin)
    {
        Preconditions.checkNotNull(plugin, "plugin");

        synchronized(LOCK)
        {
            Preconditions.checkArgument(
                intents.remove(plugin),
                plugin.getName() + " has not registered intent");

            if(fired && intents.size() == 0)
            {
                callback.call((T) this);
            }
        }
    }

    /**
     * Should be called by the method calling the event when the event
     * has finished firing
     */
    public void postEventCall()
    {
        synchronized(LOCK)
        {
            Preconditions.checkArgument(fired, "postEventCall() already called");

            fired = true;

            if(intents.size() == 0)
            {
                callback.call((T) this);
            }
        }
    }
}
