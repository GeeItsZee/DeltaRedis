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
package com.gmail.tracebachi.DeltaRedis.Bungee.Utils;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.*;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/10/15.
 */
public interface ConfigUtil
{
    /**
     * Loads the resource from the JAR and saves it to the destination under the plugin's
     * data folder. By default, the destination file will not be replaced if it exists.
     *
     * @param plugin          Plugin that contains the resource in its JAR
     * @param resourceName    Filename of the resource
     * @param destinationName Filename of the destination
     * @return Destination File
     */
    static File saveResource(Plugin plugin, String resourceName, String destinationName)
    {
        return saveResource(plugin, resourceName, destinationName, false);
    }

    /***
     * Source for the majority of this method can be found at:
     * https://www.spigotmc.org/threads/bungeecords-configuration-api.11214/#post-119017
     * <p>
     * Originally authored by: vemacs, Feb 15, 2014
     */
    static File saveResource(Plugin plugin, String resourceName, String destinationName,
                             boolean replace)
    {
        File folder = plugin.getDataFolder();

        if(!folder.exists() && !folder.mkdir())
        {
            return null;
        }

        File destinationFile = new File(folder, destinationName);

        try
        {
            if(!destinationFile.exists() || replace)
            {
                if(destinationFile.createNewFile())
                {
                    try(InputStream in = plugin.getResourceAsStream(resourceName);
                        OutputStream out = new FileOutputStream(destinationFile))
                    {
                        ByteStreams.copy(in, out);
                    }
                }
                else { return null; }
            }

            return destinationFile;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
