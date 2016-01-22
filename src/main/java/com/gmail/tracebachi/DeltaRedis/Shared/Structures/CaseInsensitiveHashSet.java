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
