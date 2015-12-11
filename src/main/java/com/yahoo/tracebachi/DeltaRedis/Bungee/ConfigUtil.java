package com.yahoo.tracebachi.DeltaRedis.Bungee;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.*;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/10/15.
 */
public interface ConfigUtil
{
    /***
     * Source for this method found at:
     * https://www.spigotmc.org/threads/bungeecords-configuration-api.11214/#post-119017
     * <p>
     * Originally authored by: vemacs, Feb 15, 2014
     */
    static File loadResource(Plugin plugin, String resourceName, String destinationName)
    {
        File folder = plugin.getDataFolder();
        if(!folder.exists())
        {
            folder.mkdir();
        }

        File destinationFile = new File(folder, destinationName);
        try
        {
            if(!destinationFile.exists())
            {
                destinationFile.createNewFile();
                try(InputStream in = plugin.getResourceAsStream(resourceName);
                    OutputStream out = new FileOutputStream(destinationFile))
                {
                    ByteStreams.copy(in, out);
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return destinationFile;
    }
}
