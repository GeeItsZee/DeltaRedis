package com.gmail.tracebachi.DeltaRedis.Shared;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 1/21/16.
 */
public interface DeltaRedisChannels
{
    /**
     * Channel for sending messages to a single player on a different server.
     */
    String SEND_MESSAGE = "DR-Message";

    /**
     * Channel for sending messages to all players on a different server.
     */
    String SEND_ANNOUNCEMENT = "DR-Announce";

    /**
     * Channel for the /runcmd command.
     */
    String RUN_CMD = "DR-RunCmd";
}
