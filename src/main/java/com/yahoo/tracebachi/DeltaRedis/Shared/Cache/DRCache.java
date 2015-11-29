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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com) on 10/18/15.
 */
public class DRCache<K, V extends Cacheable>
{
    private final long invalidValueTime;
    private final HashMap<K, V> map = new HashMap<>();

    public DRCache(long invalidValueTime)
    {
        this.invalidValueTime = invalidValueTime;
    }

    public void put(K key, V value)
    {
        if(key != null && value != null)
        {
            map.put(key, value);
        }
    }

    public V get(K key)
    {
        V value = null;
        long currentTime = System.currentTimeMillis();

        if(key != null)
        {
            value = map.get(key);
        }

        if(value != null)
        {
            long timeDiff = currentTime - value.getTimeCreatedAt();
            return (timeDiff < invalidValueTime) ? value : null;
        }
        else return null;
    }

    public V remove(K key)
    {
        if(key == null) { return null; }

        return map.remove(key);
    }

    public void removeAll()
    {
        map.clear();
    }

    public void cleanup()
    {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<K,V>> iter = map.entrySet().iterator();

        while(iter.hasNext())
        {
            Map.Entry<K,V> entry = iter.next();
            long timeDiff = currentTime - entry.getValue().getTimeCreatedAt();
            if(timeDiff >= invalidValueTime)
            {
                iter.remove();
            }
        }
    }
}
