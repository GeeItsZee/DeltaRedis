package com.gmail.tracebachi.DeltaRedis.Shared;

import java.util.regex.Pattern;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 7/17/16.
 */
public interface SplitPatterns
{
    Pattern DELTA = Pattern.compile("/\\\\");

    Pattern NEWLINE = Pattern.compile("\\\\n");

    Pattern COMMA = Pattern.compile(",");
}
