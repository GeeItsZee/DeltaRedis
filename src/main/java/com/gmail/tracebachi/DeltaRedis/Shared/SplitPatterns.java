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
package com.gmail.tracebachi.DeltaRedis.Shared;

import java.util.regex.Pattern;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 7/17/16.
 */
public interface SplitPatterns
{
    /**
     * Split pattern for "\n"
     */
    Pattern NEWLINE = Pattern.compile("\\\\n");

    /**
     * Split pattern for ","
     */
    Pattern COMMA = Pattern.compile(",");
}
