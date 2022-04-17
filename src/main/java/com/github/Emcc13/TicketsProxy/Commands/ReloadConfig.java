package com.github.Emcc13.TicketsProxy.Commands;

import com.github.Emcc13.TicketsProxy.Config.ConfigManager;
import com.github.Emcc13.TicketsProxy.ProxyTickets;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;

public class ReloadConfig extends Command {
    public ReloadConfig(){
        super("ticketsreload");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings){
        if (commandSender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) commandSender;
            List<String> reload_ops = (List<String>)ProxyTickets.getInstance().getCachedConfig().
                    get(ConfigManager.CONFIG_RELOAD_OP_KEY);
            if (reload_ops.contains(p.getUniqueId().toString())){
                ProxyTickets.getInstance().reloadConfig();
            }
        }else {
            ProxyTickets.getInstance().reloadConfig();
        }
    }
}
