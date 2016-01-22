package com.gmail.tracebachi.DeltaRedis.Shared;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 1/22/16.
 */
public interface Shutdownable
{
    /**
     * Clears all internal, owned data structures, nullifies references, and
     * performs all functions necessary to cleanup the object.
     *
     * After this method is called, the object will be in an unusable state.
     *
     * This method should only be called once.
     */
    void shutdown();
}
