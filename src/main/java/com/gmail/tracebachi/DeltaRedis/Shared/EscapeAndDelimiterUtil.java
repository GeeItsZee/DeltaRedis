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

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/10/16.
 */
public class EscapeAndDelimiterUtil
{
    public static final EscapeAndDelimiterUtil DELTA_SEPARATED =
        new EscapeAndDelimiterUtil('\u0394', '\\');
    public static final EscapeAndDelimiterUtil COMMA_SEPARATED =
        new EscapeAndDelimiterUtil(',', '\\');

    private final char delimiter;
    private final char escapeChar;

    /**
     * Constructs a new utility with the specified delimiter and escape character
     *
     * @param delimiter Character for delimiting strings
     * @param escapeChar Character for escaping special characters
     */
    public EscapeAndDelimiterUtil(char delimiter, char escapeChar)
    {
        Preconditions.checkArgument(
            delimiter != escapeChar,
            "Delimiter and escape character cannot be the same");
        Preconditions.checkArgument(
            delimiter != '\0',
            "Delimiter cannot be the null character");
        Preconditions.checkArgument(
            escapeChar != '\0',
            "Escape character cannot be the null character");

        this.delimiter = delimiter;
        this.escapeChar = escapeChar;
    }

    /**
     * Escapes every string in the given list and then joins the strings
     * using the configured delimiter
     *
     * @param input List of strings to escape and delimit
     * @return Escaped and delimited string
     */
    public String escapeAndDelimit(List<String> input)
    {
        Preconditions.checkNotNull(input, "input");

        StringBuilder builder = new StringBuilder(256);

        for(int i = 0; i < input.size(); i++)
        {
            String inputStr = input.get(i);
            Preconditions.checkNotNull(inputStr, "inputStr #" + i);

            for(int charIndex = 0; charIndex < inputStr.length(); charIndex++)
            {
                char argumentChar = inputStr.charAt(charIndex);

                if(argumentChar == delimiter)
                {
                    builder.append(escapeChar);
                    builder.append(delimiter);
                }
                else if(argumentChar == escapeChar)
                {
                    builder.append(escapeChar);
                    builder.append(escapeChar);
                }
                else
                {
                    builder.append(argumentChar);
                }
            }

            // Avoid putting an extra delimiter after escaping the last string
            if(i != input.size() - 1)
            {
                builder.append(delimiter);
            }
        }

        return builder.toString();
    }

    /**
     * Unescapes and undelimits the input string into a list of strings
     *
     * @param input Properly escaped and delimited string
     * @return Unescaped and undelimited strings in a list
     */
    public List<String> unescapeAndUndelimit(String input)
    {
        Preconditions.checkNotNull(input, "input");

        List<String> result = new ArrayList<>(4);
        StringBuilder builder = new StringBuilder();
        int inputLen = input.length();

        // If the input is empty
        if(inputLen < 1)
        {
            result.add("");
            return result;
        }
        // If the input only has 1 character
        else if(inputLen == 1)
        {
            char firstChar = input.charAt(0);
            if(firstChar == delimiter)
            {
                result.add("");
                result.add("");
                return result;
            }
            else if(firstChar != escapeChar)
            {
                result.add(String.valueOf(firstChar));
                return result;
            }
            else
            {
                throw new IllegalArgumentException("Unexpected escape char at index: 0");
            }
        }

        for(int i = 0; i < inputLen; i++)
        {
            // Handle ending with <?>
            if(i == inputLen - 1)
            {
                char a = input.charAt(i);

                // Handle ending with <delimiter>
                if(a == delimiter)
                {
                    result.add(getStringAndClear(builder));
                    result.add("");
                }
                // Handle ending with <escape>
                else if(a == escapeChar)
                {
                    throw new IllegalArgumentException("Unexpected escape char" +
                        " at index: " + (i));
                }
                // Handle ending with <char>
                else
                {
                    builder.append(a);
                    result.add(getStringAndClear(builder));
                }
            }
            // Handle ending with <?><?>
            else if(i == inputLen - 2)
            {
                char a = input.charAt(i);
                char b = input.charAt(i + 1);

                // Handle ending with <delimiter><?>
                if(a == delimiter)
                {
                    result.add(getStringAndClear(builder));
                }
                // Handle ending with <escape><?>
                else if(a == escapeChar)
                {
                    // Handle ending with <escape><delimiter>
                    if(b == delimiter)
                    {
                        builder.append(delimiter);
                        result.add(getStringAndClear(builder));
                        i++;
                    }
                    // Handle ending with <escape><escape>
                    else if(b == escapeChar)
                    {
                        builder.append(escapeChar);
                        result.add(getStringAndClear(builder));
                        i++;
                    }
                    // Handle ending with <escape><char>
                    else
                    {
                        throw new IllegalArgumentException("Unexpected escape char" +
                            " at index: " + (i));
                    }
                }
                // Handle ending with <char><?>
                else
                {
                    builder.append(a);
                }
            }
            // Handle <?><?>...
            else
            {
                char a = input.charAt(i);
                char b = input.charAt(i + 1);

                // Handle <delimiter><?>...
                if(a == delimiter)
                {
                    result.add(getStringAndClear(builder));
                }
                // Handle <escape><?>
                else if(a == escapeChar)
                {
                    // Handle <escape><delimiter>... and <escape><escape>...
                    if(b == delimiter || b == escapeChar)
                    {
                        builder.append(b);
                        i++;
                    }
                    // Handle <escape><char>...
                    else
                    {
                        throw new IllegalArgumentException("Unexpected escape char" +
                            " at index: " + (i));
                    }
                }
                // Handle <char><?>
                else
                {
                    builder.append(a);
                }
            }
        }

        return result;
    }

    private String getStringAndClear(StringBuilder builder)
    {
        String result = builder.toString();
        builder.setLength(0);
        return result;
    }
}
