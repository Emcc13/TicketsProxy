package com.github.Emcc13.TicketsProxy.ServerMessages;

import com.github.Emcc13.TicketsProxy.Config.ConfigManager;
import com.github.Emcc13.TicketsProxy.ProxyTickets;
import com.github.Emcc13.TicketsProxy.Util.Formatter;
import com.github.Emcc13.TicketsProxy.Util.Tuple;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class TicketChangeNotifier {
    private ProxyTickets main;
    public TicketChangeNotifier(ProxyTickets main){
        this.main = main;
    }

    public void createInfo(String player, Integer id, String request){
        createInfo(player, id, request, true);
    }

    public void createInfo(String player, Integer id, String request, boolean inform_player){
        main.sendNotification(Formatter.formatComponents(
                (List<TextComponent>) main.getCachedConfig().get(ConfigManager.TICKETS_INFO_CREATE_KEY),
                new Tuple<String, String>("%PLAYER%", player),
                new Tuple<String, String>("%ID%", String.valueOf(id)),
                new Tuple<String, String>("%REQUEST%", request)));
        if (inform_player) {
            ProxiedPlayer p = this.main.getProxy().getPlayer(player);
            if (p == null) {
                return;
            }
            p.sendMessage(Formatter.formatComponents(
                    (List<TextComponent>) main.getCachedConfig().get(ConfigManager.TICKET_INFO_CREATE_KEY),
                    new Tuple<>("%ID%", String.valueOf(id))
            ));
        }
    }

    public void spamInfo(String player, Integer id, String request){
        main.sendNotification(Formatter.formatComponents(
                (List<TextComponent>) main.getCachedConfig().get(ConfigManager.TICKETS_INFO_SPAM_KEY),
                new Tuple<>("%PLAYER%", player),
                new Tuple<>("%ID%", String.valueOf(id)),
                new Tuple<>("%REQUEST%", request)
        ));
    }

    public void claimInfo(String player, Integer id){
        main.sendNotification(Formatter.formatComponents(
                (List<TextComponent>) main.getCachedConfig().get(ConfigManager.TICKETS_INFO_CLAIM_KEY),
                new Tuple<>("%PLAYER%", player),
                new Tuple<>("%ID%", String.valueOf(id))
        ));
    }

    public void unclaimInfo(String player, Integer id){
        main.sendNotification(Formatter.formatComponents(
                (List<TextComponent>) main.getCachedConfig().get(ConfigManager.TICKETS_INFO_UNCLAIM_KEY),
                new Tuple<>("%PLAYER%", player),
                new Tuple<>("%ID%", String.valueOf(id))
        ));
    }

    public void closeInfo(String team, Integer id, String answer, String player){
        closeInfo(team, id, answer, player, true);
    }

    public void closeInfo(String team, Integer id, String answer, String player, boolean inform_player){
        main.sendNotification(Formatter.formatComponents(
                (List<TextComponent>) main.getCachedConfig().get(ConfigManager.TICKETS_INFO_CLOSE_KEY),
                new Tuple<>("%PLAYER%", team),
                new Tuple<>("%ID%", String.valueOf(id)),
                new Tuple<>("%ANSWER%", answer)
        ));
        if (inform_player) {
            ProxiedPlayer p = this.main.getProxy().getPlayer(player);
            if (p == null) {
                return;
            }
            p.sendMessage(Formatter.formatComponents(
                    (List<TextComponent>) main.getCachedConfig().get(ConfigManager.TICKET_INFO_CLOSE_KEY),
                    new Tuple<>("%ID%", String.valueOf(id)),
                    new Tuple<>("%PLAYER%", team),
                    new Tuple<>("%ANSWER%", answer)
            ));
        }
    }

    public void closeFailedInfo(String team, Integer id){
        ProxiedPlayer p = this.main.getProxy().getPlayer(team);
        if (p==null)
            return;
        p.sendMessage(Formatter.formatComponents(
                (List<TextComponent>) main.getCachedConfig().get(ConfigManager.TICKETS_INFO_CLOSE_FAIL_KEY),
                new Tuple<>("%ID%", String.valueOf(id))
        ));
    }

    public void readInfo(String player, Integer id){
        main.sendNotification(Formatter.formatComponents(
                (List<TextComponent>) main.getCachedConfig().get(ConfigManager.TICKETS_INFO_READ),
                new Tuple<>("%PLAYER%", player),
                new Tuple<>("%ID%", String.valueOf(id))
        ));
        ProxiedPlayer p = this.main.getProxy().getPlayer(player);
        if (p==null){
            return;
        }
        p.sendMessage(Formatter.formatComponents(
                (List<TextComponent>) main.getCachedConfig().get(ConfigManager.TICKET_INFO_READ_KEY)
        ));
    }
}
