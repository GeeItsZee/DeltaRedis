package com.gmail.tracebachi.DeltaRedis.Shared;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 1/25/16.
 */
public interface Registerable
{
    /**
     * Registers a command or listener.
     */
    void register();

    /**
     * Unregisters a command or listener.
     */
    void unregister();
}
