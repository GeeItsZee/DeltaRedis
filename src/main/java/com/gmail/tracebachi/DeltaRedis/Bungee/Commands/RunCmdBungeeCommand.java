package com.gmail.tracebachi.DeltaRedis.Bungee.Commands;

import com.gmail.tracebachi.DeltaRedis.Bungee.DeltaRedis;
import com.gmail.tracebachi.DeltaRedis.Bungee.DeltaRedisApi;
import com.gmail.tracebachi.DeltaRedis.Shared.Registerable;
import com.gmail.tracebachi.DeltaRedis.Shared.Servers;
import com.gmail.tracebachi.DeltaRedis.Shared.Shutdownable;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.gmail.tracebachi.DeltaRedis.Shared.Prefixes.*;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 4/28/16.
 */
public class RunCmdBungeeCommand extends Command implements Registerable, Shutdownable
{
    private DeltaRedis plugin;

    public RunCmdBungeeCommand(DeltaRedis deltaRedis)
    {
        super("runcmdbungee", null, "rcbungee");

        this.plugin = deltaRedis;
    }

    @Override
    public void register()
    {
        plugin.getProxy().getPluginManager().registerCommand(plugin, this);
    }

    @Override
    public void unregister()
    {
        plugin.getProxy().getPluginManager().unregisterCommand(this);
    }

    @Override
    public void shutdown()
    {
        unregister();
        plugin = null;
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("DeltaRedis.RunCmd"))
        {
            sender.sendMessage(FAILURE + "You do not have permission to run this command.");
            return;
        }

        if(args.length <= 1)
        {
            sender.sendMessage(INFO + "/runcmd server[,server,...] command");
            sender.sendMessage(INFO + "/runcmd ALL command");
            return;
        }

        DeltaRedisApi deltaApi = DeltaRedisApi.instance();
        Set<String> argServers = new HashSet<>(Arrays.asList(args[0].split(",")));
        Set<String> servers = deltaApi.getCachedServers();
        String commandStr = joinArgsForCommand(args);

        if(doesSetContain(argServers, "ALL"))
        {
            deltaApi.sendCommandToServer(Servers.SPIGOT, commandStr);

            sendMessage(sender, SUCCESS + "Sent command to " + input("ALL"));
        }
        else
        {
            for(String dest : argServers)
            {
                String correctedDest = getMatchInSet(servers, dest);

                if(correctedDest != null)
                {
                    deltaApi.sendCommandToServer(correctedDest, commandStr);

                    sendMessage(sender, SUCCESS + "Sent command to " + input(dest));
                }
                else
                {
                    sendMessage(sender, FAILURE + input(dest) + " is offline or non-existent.");
                }
            }
        }
    }

    private String joinArgsForCommand(String[] args)
    {
        return String.join(" ", (CharSequence[]) Arrays.copyOfRange(args, 1, args.length));
    }

    private boolean doesSetContain(Set<String> set, String source)
    {
        for(String item : set)
        {
            if(item.equalsIgnoreCase(source))
            {
                return true;
            }
        }
        return false;
    }

    private String getMatchInSet(Set<String> set, String source)
    {
        for(String item : set)
        {
            if(item.equalsIgnoreCase(source))
            {
                return item;
            }
        }
        return null;
    }

    private void sendMessage(CommandSender receiver, String message)
    {
        receiver.sendMessage(TextComponent.fromLegacyText(message));
    }
}
