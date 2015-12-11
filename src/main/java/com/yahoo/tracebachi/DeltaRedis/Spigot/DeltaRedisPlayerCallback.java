package com.yahoo.tracebachi.DeltaRedis.Spigot;

import com.yahoo.tracebachi.DeltaRedis.Shared.Cache.CachedPlayer;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/11/15.
 */
@FunctionalInterface
public interface DeltaRedisPlayerCallback
{
    void call(CachedPlayer player);
}
