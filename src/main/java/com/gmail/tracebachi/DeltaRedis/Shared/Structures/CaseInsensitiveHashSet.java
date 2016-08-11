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
package com.gmail.tracebachi.DeltaRedis.Shared.Structures;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 1/22/16.
 */
public class CaseInsensitiveHashSet extends HashSet<String>
{
    public CaseInsensitiveHashSet()
    {
        super();
    }

    public CaseInsensitiveHashSet(Collection<? extends String> c)
    {
        super(c);
    }

    public CaseInsensitiveHashSet(int initialCapacity)
    {
        super(initialCapacity);
    }

    public CaseInsensitiveHashSet(int initialCapacity, float loadFactor)
    {
        super(initialCapacity, loadFactor);
    }

    @Override
    public boolean contains(Object o)
    {
        Preconditions.checkArgument(o instanceof String);
        return super.contains(((String) o).toLowerCase());
    }

    @Override
    public boolean add(String s)
    {
        return super.add(s.toLowerCase());
    }

    @Override
    public boolean remove(Object o)
    {
        Preconditions.checkArgument(o instanceof String);
        return super.remove(((String) o).toLowerCase());
    }
}
