package com.gmail.tracebachi.DeltaRedis.Shared.Structures;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 1/22/16.
 */
public class CaseInsensitiveHashMap<V> extends HashMap<String, V>
{
    public CaseInsensitiveHashMap()
    {
        super();
    }

    public CaseInsensitiveHashMap(int initialCapacity)
    {
        super(initialCapacity);
    }

    public CaseInsensitiveHashMap(int initialCapacity, float loadFactor)
    {
        super(initialCapacity, loadFactor);
    }

    public CaseInsensitiveHashMap(Map<? extends String, ? extends V> m)
    {
        super(m);
    }

    @Override
    public V get(Object key)
    {
        Preconditions.checkArgument(key instanceof String);
        return super.get(((String) key).toLowerCase());
    }

    @Override
    public V getOrDefault(Object key, V defaultValue)
    {
        Preconditions.checkArgument(key instanceof String);
        return super.getOrDefault(((String) key).toLowerCase(), defaultValue);
    }

    @Override
    public V put(String key, V value)
    {
        return super.put(key.toLowerCase(), value);
    }

    @Override
    public V putIfAbsent(String key, V value)
    {
        return super.putIfAbsent(key.toLowerCase(), value);
    }

    @Override
    public V remove(Object key)
    {
        Preconditions.checkArgument(key instanceof String);
        return super.remove(((String) key).toLowerCase());
    }

    @Override
    public boolean remove(Object key, Object value)
    {
        Preconditions.checkArgument(key instanceof String);
        return super.remove(((String) key).toLowerCase(), value);
    }

    @Override
    public V replace(String key, V value)
    {
        return super.replace(key.toLowerCase(), value);
    }

    @Override
    public boolean replace(String key, V oldValue, V newValue)
    {
        return super.replace(key.toLowerCase(), oldValue, newValue);
    }

    @Override
    public boolean containsKey(Object key)
    {
        Preconditions.checkArgument(key instanceof String);
        return super.containsKey(((String) key).toLowerCase());
    }

    @Override
    public V computeIfAbsent(String key, Function<? super String, ? extends V> mappingFunction)
    {
        return super.computeIfAbsent(key.toLowerCase(), mappingFunction);
    }

    @Override
    public V computeIfPresent(String key, BiFunction<? super String, ? super V, ? extends V> remappingFunction)
    {
        return super.computeIfPresent(key.toLowerCase(), remappingFunction);
    }

    @Override
    public V compute(String key, BiFunction<? super String, ? super V, ? extends V> remappingFunction)
    {
        return super.compute(key.toLowerCase(), remappingFunction);
    }

    @Override
    public V merge(String key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
    {
        return super.merge(key.toLowerCase(), value, remappingFunction);
    }
}
