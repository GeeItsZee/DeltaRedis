package com.gmail.tracebachi.DeltaRedis.Spigot;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 1/20/16.
 */
public class WaitingAsyncScheduler
{
    private int counter = 0;
    private JavaPlugin plugin;
    private final Object COUNT_LOCK = new Object();

    public WaitingAsyncScheduler(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void scheduleTask(Runnable runnable)
    {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
        {
            synchronized(COUNT_LOCK) { counter++; }

            try
            {
                runnable.run();
            }
            catch(Throwable throwable)
            {
                throwable.printStackTrace();
            }
            finally
            {
                synchronized(COUNT_LOCK)
                {
                    counter--;
                    COUNT_LOCK.notifyAll();
                }
            }
        });
    }

    public void waitForTasks()
    {
        try
        {
            synchronized(COUNT_LOCK)
            {
                while(counter > 0)
                {
                    COUNT_LOCK.wait();
                }
            }
        }
        catch(InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }
}
