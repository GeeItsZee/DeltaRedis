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

import com.gmail.tracebachi.DeltaRedis.Shared.Structures.CaseInsensitiveHashMap;
import com.google.common.base.Preconditions;

import java.text.MessageFormat;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/11/16.
 */
public class ChatMessageHelper
{
    private static ChatMessageHelper instance;

    private CaseInsensitiveHashMap<MessageFormat> formatMap = new CaseInsensitiveHashMap<>();

    /**
     * @return Singleton instance of the ChatMessageHelper
     */
    public static ChatMessageHelper instance()
    {
        if(instance == null)
        {
            instance = new ChatMessageHelper();
        }

        return instance;
    }

    /**
     * Uses a stored format with the provided arguments to return a formatted
     * string
     *
     * @param formatName Name of the format to use
     * @param arguments List of arguments
     * @return Formatted string or string indicating that the format was not
     * found
     */
    public static String format(String formatName, String... arguments)
    {
        Preconditions.checkNotNull(formatName, "formatName");
        Preconditions.checkNotNull(arguments, "arguments");

        if(instance == null)
        {
            instance = new ChatMessageHelper();
        }

        MessageFormat format = instance.formatMap.get(formatName);

        if(format == null)
        {
            return "Format not found. formatName: " + formatName;
        }
        else
        {
            return format.format(arguments);
        }
    }

    /**
     * Formats the permission using the "DeltaRedis.NoPerm" format
     *
     * @param permission String to format
     * @return Formatted string
     */
    public static String formatNoPerm(String permission)
    {
        return format("DeltaRedis.NoPerm", permission);
    }

    /**
     * Formats the usage string using the "DeltaRedis.Usage" format
     *
     * @param usageString String to format
     * @return Formatted string
     */
    public static String formatUsage(String usageString)
    {
        return format("DeltaRedis.Usage", usageString);
    }

    /**
     * Formats the name using the "DeltaRedis.PlayerOnlyCommand" format
     *
     * @param command String to format
     * @return Formatted string
     */
    public static String formatPlayerOnlyCommand(String command)
    {
        return format("DeltaRedis.PlayerOnlyCommand", command);
    }

    /**
     * Formats the name using the "DeltaRedis.PlayerOffline" format
     *
     * @param name String to format
     * @return Formatted string
     */
    public static String formatPlayerOffline(String name)
    {
        return format("DeltaRedis.PlayerOffline", name);
    }

    /**
     * Adds or updates a new format
     *
     * @param formatName Name of the format to update
     * @param format String representation of the format like the one
     * that would be used in {@link MessageFormat}
     */
    public void updateFormat(String formatName, String format)
    {
        Preconditions.checkNotNull(formatName, "formatName");
        Preconditions.checkNotNull(format, "format");
        formatMap.put(formatName, new MessageFormat(format));
    }

    /**
     * Removes a format
     *
     * @param formatName Name of the format to update
     * @return True if removed or false
     */
    public boolean removeFormat(String formatName)
    {
        Preconditions.checkNotNull(formatName, "formatName");
        return formatMap.remove(formatName) != null;
    }

    private ChatMessageHelper()
    {

    }
}
