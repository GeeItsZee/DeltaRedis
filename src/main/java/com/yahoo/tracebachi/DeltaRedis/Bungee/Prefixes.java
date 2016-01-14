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
package com.yahoo.tracebachi.DeltaRedis.Bungee;

import net.md_5.bungee.api.ChatColor;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 11/28/15.
 */
public interface Prefixes
{
    String INFO = ChatColor.translateAlternateColorCodes('&',
        "&8[&9!&8] &9Info &8[&9!&8]&7 ");

    String SUCCESS = ChatColor.translateAlternateColorCodes('&',
        "&8[&a!&8] &aSuccess &8[&a!&8]&7 ");

    String FAILURE = ChatColor.translateAlternateColorCodes('&',
        "&8[&c!&8] &cFailure &8[&c!&8]&7 ");

    /**
     * Utility method to avoid formatting messages as
     * <p>
     * <code>... + ChatColor.WHITE + input + ChatColor.GRAY + ...</code>.
     * </p>
     *
     * @param object Object to call {@link Object#toString()} on.
     *
     * @return A string representation of the object colored in
     * white, but a gray color appended.
     */
    static String input(Object object)
    {
        return ChatColor.WHITE + object.toString() + ChatColor.GRAY;
    }
}
