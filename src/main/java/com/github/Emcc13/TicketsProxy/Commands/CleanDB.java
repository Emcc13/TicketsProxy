package com.github.Emcc13.TicketsProxy.Commands;

import com.github.Emcc13.TicketsProxy.Config.ConfigManager;
import com.github.Emcc13.TicketsProxy.Database.NotConnectedException;
import com.github.Emcc13.TicketsProxy.ProxyTickets;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;

public class CleanDB extends Command {
    public CleanDB(){
        super("ticketscleandb");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings){
        if (commandSender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) commandSender;
            List<String> db_clean_ops = (List<String>)ProxyTickets.getInstance().getCachedConfig().
                    get(ConfigManager.CONFIG_DBCLEAN_OP_KEY);
            if (db_clean_ops.contains(p.getUniqueId().toString())){
                try {
                    ProxyTickets.getInstance().getDbInterface().cleanDB();
                }catch (NotConnectedException e){
                }
            }
        }else {
            ProxyTickets.getInstance().reloadConfig();
        }
    }
}
