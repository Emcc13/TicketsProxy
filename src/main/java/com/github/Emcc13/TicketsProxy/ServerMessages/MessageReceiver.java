package com.github.Emcc13.TicketsProxy.ServerMessages;

import com.github.Emcc13.TicketsProxy.Config.ConfigManager;
import com.github.Emcc13.TicketsProxy.Database.DBTicket;
import com.github.Emcc13.TicketsProxy.Database.NotConnectedException;
import com.github.Emcc13.TicketsProxy.ProxyTickets;
import com.github.Emcc13.TicketsProxy.Util.Formatter;
import com.github.Emcc13.TicketsProxy.Util.Tuple;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageReceiver implements Listener {
    private ProxyTickets main;

    public MessageReceiver(ProxyTickets pt) {
        this.main = pt;
    }

    @net.md_5.bungee.event.EventHandler
    public void onPluginMessage(PluginMessageEvent ev) {
        if (ev.getTag().equals(main.getCachedConfig().get(ConfigManager.CHANNEL_KEY))) {
            ServerMessage sm = new ServerMessage(ev.getData());
            switch (sm.getTopic()) {
                case ticketsNum:
                    ticketsNum(sm);
                    break;
                case ticketsClaim:
                    ticketsClaim(sm);
                    break;
                case ticketsUnclaim:
                    ticketsUnclaim(sm);
                    break;
                case ticketsClose:
                    ticketsClose(sm);
                    break;
                case ticketsTP:
                    ticketsTP(sm);
                    break;
                case ticketsPage:
                    ticketsPage(sm);
                    break;

                case ticketList:
                    ticketList(sm);
                    break;
                case ticketNew:
                    ticketNew(sm);
                    break;
                case ticketRead:
                    ticketRead(sm);
                    break;
                case ticketsOpen:
                    ticketsOpen(sm);
                    break;
                case ticketsUnread:
                    ticketsUnread(sm);
                    break;
                case ticketsPlayerPage:
                    ticketsPlayerPage(sm);
                    break;
                case ticketNotify:
                    notifyPlayer(sm);
                    break;
                case tpPos:
                default:
                    break;
            }
        }
    }

    private void sendTickets(ProxiedPlayer p, List<DBTicket> tickets){
        List<TextComponent> openTicket = (List<TextComponent>) this.main.getCachedConfig()
                .get(ConfigManager.TICKETS_OPEN_FORMAT_KEY);
        List<TextComponent> claimedTicket = (List<TextComponent>) this.main.getCachedConfig()
                .get(ConfigManager.TICKETS_CLAIMED_FORMAT_KEY);
        List<TextComponent> closedTicket = (List<TextComponent>) this.main.getCachedConfig()
                .get(ConfigManager.TICKETS_CLOSED_FORMAT_KEY);
        List<TextComponent> readTicket = (List<TextComponent>) this.main.getCachedConfig()
                .get(ConfigManager.TICKETS_READ_FORMAT_KEY);
        Map<String, String> ticketTypes = (Map<String, String>) this.main.getCachedConfig().getOrDefault(ConfigManager.TICKET_FORMAT_TICKETTYPE_KEY, new HashMap<>());
        for (DBTicket dbTicket:tickets) {
            p.sendMessage(dbTicket.format(dbTicket.getAnswerDate() != null ?
                    dbTicket.getReadDate() != null ? readTicket : closedTicket :
                    dbTicket.getClaimingDate() != null ? claimedTicket : openTicket, ticketTypes));
        }
    }

    private void ticketsPage(ServerMessage sm) {
        ProxiedPlayer p = this.main.getProxy().getPlayer(sm.getPlayer());
        if (p == null) {
            return;
        }
        Integer entriesPerPage = (Integer) this.main.getCachedConfig().get(ConfigManager.TICKETS_PER_PAGE_KEY);
        Tuple<String, String> white_black = getWhitelistBlacklist(sm.getTicketType());
        try {
            Integer pages = (int) java.lang.Math.ceil(
                    Double.valueOf(this.main.getDbInterface().getOpenClaimedTickets(white_black.first,
                            white_black.second)) / entriesPerPage);
            List<TextComponent> page_info = ((List<TextComponent>) main.getCachedConfig()
                    .get(ConfigManager.TICKETS_PAGE_INFO_KEY));
            int page = Math.max(Math.min(sm.getNumber(), pages - 1), 0);
            p.sendMessage(Formatter.formatComponents(page_info,
                    new Tuple<String, String>("%PAGE%", String.valueOf(page + 1)),
                    new Tuple<String, String>("%PAGES%", String.valueOf(pages)),
                    new Tuple<String, String>("%PREVPAGE%", String.valueOf(Math.max(Math.min(page, pages), 1))),
                    new Tuple<String, String>("%NEXTPAGE%", String.valueOf(Math.max(Math.min(page + 2, pages), 1))),
                    new Tuple<String, String>("%TICKETTYPE%", sm.getTicketType())
            ));
            sendTickets(p, this.main.getDbInterface().getOpenTicketsPage(page, entriesPerPage,
                    white_black.first, white_black.second));
        } catch (NotConnectedException e) {
            p.sendMessage(Formatter.formatComponents((List<TextComponent>) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY)));
        }
    }

    private void ticketsPlayerPage(ServerMessage sm) {
        ProxiedPlayer p = this.main.getProxy().getPlayer(sm.getPlayer());
        if (p == null) {
            return;
        }
        Integer entriesPerPage = (Integer) this.main.getCachedConfig().get(ConfigManager.TICKETS_PER_PAGE_KEY);
        Tuple<String, String> white_black = getWhitelistBlacklist(sm.getTicketType());
        try {
            int pages = (int) java.lang.Math.ceil(
                    Double.valueOf(this.main.getDbInterface().getPlayerTicketsTotal(sm.getText(), white_black.first,
                            white_black.second)) / entriesPerPage);
            List<TextComponent> page_info = ((List<TextComponent>) main.getCachedConfig()
                    .get(ConfigManager.TICKETS_PLAYER_PAGE_INFO_KEY));
            int page = Math.max(Math.min(sm.getNumber() - 1, pages - 1), 0);
            p.sendMessage(Formatter.formatComponents(page_info,
                    new Tuple<String, String>("%PAGE%", String.valueOf(page + 1)),
                    new Tuple<String, String>("%PAGES%", String.valueOf(pages)),
                    new Tuple<String, String>("%PREVPAGE%", String.valueOf(Math.max(Math.min(page, pages), 1))),
                    new Tuple<String, String>("%NEXTPAGE%", String.valueOf(Math.max(Math.min(page + 2, pages), 1))),
                    new Tuple<String, String>("%PLAYER%", sm.getText()),
                    new Tuple<String, String>("%TICKETTYPE%", sm.getTicketType())
            ));
            sendTickets(p, this.main.getDbInterface()
                    .getPlayerTicketsPage(page, sm.getText(), entriesPerPage, white_black.first, white_black.second));
        } catch (NotConnectedException e) {
            p.sendMessage(Formatter.formatComponents((List<TextComponent>) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY)));
        }
    }

    private void ticketsNum(ServerMessage sm) {
        ProxiedPlayer p = this.main.getProxy().getPlayer(sm.getPlayer());
        if (p == null) {
            System.out.println(String.format("Failed to find player with: %s", sm.getPlayer()));
            return;
        }
        try {
            DBTicket dbTicket = this.main.getDbInterface().getTicketByID(sm.getNumber()); // format
            if (dbTicket != null) {
                sendTickets(p, new ArrayList<DBTicket>(){{add(dbTicket);}});
            } else {
                p.sendMessage(Formatter.formatComponents(
                        (List<TextComponent>) main.getCachedConfig().get(ConfigManager.NO_TICKET_WITH_ID_KEY),
                        new Tuple<>("%ID%", String.valueOf(sm.getNumber()))));
            }
        } catch (NotConnectedException e) {
            p.sendMessage(Formatter.formatComponents((List<TextComponent>) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY)));
        }
    }

    private void ticketsClaim(ServerMessage sm) {
        ProxiedPlayer p = this.main.getProxy().getPlayer(sm.getPlayer());
        if (p == null) {
            System.out.println(String.format("Failed to find player with: %s", sm.getPlayer()));
            return;
        }
        try {
            this.main.getDbInterface().claimTicket(sm.getNumber(), p.getName());
        } catch (NotConnectedException e) {
            p.sendMessage(Formatter.formatComponents((List<TextComponent>) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY)));
        }
    }

    private void ticketsUnclaim(ServerMessage sm) {
        ProxiedPlayer p = this.main.getProxy().getPlayer(sm.getPlayer());
        if (p == null) {
            System.out.println(String.format("Failed to find player with: %s", sm.getPlayer()));
            return;
        }
        try {
            this.main.getDbInterface().unclaimTicket(sm.getNumber(), p.getName());
        } catch (NotConnectedException e) {
            p.sendMessage(Formatter.formatComponents((List<TextComponent>) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY)));
        }
    }

    private void ticketsClose(ServerMessage sm) {
        ProxiedPlayer p = this.main.getProxy().getPlayer(sm.getPlayer());
        try {
            this.main.getDbInterface().closeTicket(sm.getNumber(), p.getName(), sm.getText());
        } catch (NotConnectedException e) {
            p.sendMessage(Formatter.formatComponents((List<TextComponent>) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY)));
        }
    }

    private void ticketsTP(ServerMessage sm) {
        ProxiedPlayer p = this.main.getProxy().getPlayer(sm.getPlayer());
        if (p == null) {
            System.out.println(String.format("Failed to find player with: %s", sm.getPlayer()));
            return;
        }
        try {
            DBTicket ticket = this.main.getDbInterface().getTicketByID(sm.getNumber());
            ServerMessage tpMessage = new ServerMessage(sm.getPlayer(),
                    ticket.getWorld(),
                    ticket.getX_pos(),
                    ticket.getY_pos(),
                    ticket.getZ_pos(),
                    ticket.getAzimuth(),
                    ticket.getElevation());
            String server = ticket.getServer();
            ServerInfo si = this.main.getProxy().getServerInfo(server);
            if (!p.getServer().getInfo().getName().equals(server)) {
                p.connect(si);
            }
            si.sendData((String) main.getCachedConfig().get(ConfigManager.CHANNEL_KEY),
                    tpMessage.toMessagae());
        } catch (NotConnectedException e) {
            p.sendMessage(Formatter.formatComponents((List<TextComponent>) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY)));
        }
    }


    private void ticketList(ServerMessage sm) {
        ProxiedPlayer p = this.main.getProxy().getPlayer(sm.getPlayer());
        if (p == null) {
            System.out.println(String.format("Failed to find player with: %s", sm.getPlayer()));
            return;
        }
        Integer entriesPerPage = (Integer) this.main.getCachedConfig().get(ConfigManager.TICKETS_PER_PAGE_KEY);
        try {
            Integer pages = (int) java.lang.Math.ceil(
                    Double.valueOf(this.main.getDbInterface().getUnreadOpenTickets(sm.getPlayer())) / entriesPerPage);
            int page = Math.max(Math.min(sm.getNumber(), pages - 1), 0);
            p.sendMessage(Formatter.formatComponents((List<TextComponent>) main.getCachedConfig()
                            .get(ConfigManager.TICKET_PAGE_INFO_KEY),
                    new Tuple<String, String>("%PAGE%", String.valueOf(page + 1)),
                    new Tuple<String, String>("%PAGES%", String.valueOf(pages)),
                    new Tuple<String, String>("%PREVPAGE%", String.valueOf(Math.max(Math.min(page, pages), 1))),
                    new Tuple<String, String>("%NEXTPAGE%", String.valueOf(Math.max(Math.min(page + 2, pages), 1)))
            ));
            List<TextComponent> unread_ticket = (List<TextComponent>) this.main.getCachedConfig()
                    .get(ConfigManager.TICKET_UNREAD_FORMAT_KEY);
            List<TextComponent> open_ticket = (List<TextComponent>) this.main.getCachedConfig()
                    .get(ConfigManager.TICKET_OPEN_FORMAT_KEY);
            Map<String, String> ticketTypes = (Map<String, String>) this.main.getCachedConfig().getOrDefault(ConfigManager.TICKET_FORMAT_TICKETTYPE_KEY, new HashMap<>());
            for (DBTicket dbticket :
                    this.main.getDbInterface().getPlayerTicketPage(page, sm.getPlayer(), entriesPerPage)) {
                if (dbticket.getAnswerDate() == null) {
                    p.sendMessage(dbticket.format(open_ticket, ticketTypes));
                } else {
                    p.sendMessage(dbticket.format(unread_ticket, ticketTypes));
                }
            }
        } catch (NotConnectedException e) {
            p.sendMessage(Formatter.formatComponents((List<TextComponent>) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY)));
        }
    }

    private void ticketNew(ServerMessage sm) {
        boolean catch_spam = true;
        if (!((String)main.getCachedConfig().get(ConfigManager.TICKET_NOTIFY_KEY)).contains(sm.getTicketType())){
            catch_spam = false;
        }
        String server;
        ProxiedPlayer p = this.main.getProxy().getPlayer(sm.getPlayer());
        if (p==null) {
            server = sm.getServer();
            for (Map.Entry<String, ServerInfo> entry : this.main.getProxy().getServers().entrySet()) {
                if (entry.getValue().getSocketAddress().toString().split(":")[1].equals(server)) {
                    server = entry.getKey();
                }
            }
        }else{
            server = p.getServer().getInfo().getName();
        }
        try {
            this.main.getDbInterface().addTicket(sm.getPlayer(), sm.getTicketType(), sm.getText(),
                    server, sm.getWorld(), sm.getPosX(), sm.getPosY(), sm.getPosZ(),
                    sm.getPitch(), sm.getYaw(), catch_spam);
        } catch (NotConnectedException e) {
            if (p!=null){
                p.sendMessage(Formatter.formatComponents((List<TextComponent>) main.getCachedConfig()
                        .get(ConfigManager.DB_FAILURE_MESSAGE_KEY)));
            }
        }
    }

    private void ticketRead(ServerMessage sm) {
        ProxiedPlayer p = this.main.getProxy().getPlayer(sm.getPlayer());
        if (p == null) {
            System.out.println(String.format("Failed to find player with: %s", sm.getPlayer()));
            return;
        }
        try {
            this.main.getDbInterface().readTicket(sm.getNumber(), sm.getPlayer());
        } catch (NotConnectedException e) {
            p.sendMessage(Formatter.formatComponents((List<TextComponent>) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY)));
        }
    }

    private void ticketsOpen(ServerMessage sm) {
        try {
            int openTickets = main.getDbInterface().getOpenTickets();
            if (openTickets > 0) {
                List<TextComponent> join_open = (List<TextComponent>) main.getCachedConfig()
                        .get(ConfigManager.JOIN_OPEN_TICKET_KEY);
                TextComponent info = Formatter.formatComponents(join_open, new Tuple<>("%NUM%", String.valueOf(openTickets)));
                for (String player : sm.getPlayer().split(" ")) {
                    ProxiedPlayer p = this.main.getProxy().getPlayer(player);
                    p.sendMessage(info);
                }
            }
        } catch (NotConnectedException e) {
        }
    }

    private void ticketsUnread(ServerMessage sm) {
        try {
            List<TextComponent> unreadTicket = (List<TextComponent>) main.getCachedConfig().
                    get(ConfigManager.JOIN_UNRAED_TICKET_KEY);
            for (String player : sm.getPlayer().split(" ")) {
                ProxiedPlayer p = this.main.getProxy().getPlayer(player);
                int unreadTickets = main.getDbInterface().
                        getUnreadTickets(player);
                if (unreadTickets > 0) {
                    p.sendMessage(Formatter.formatComponents(unreadTicket,
                            new Tuple<>("%NUM%", String.valueOf(unreadTickets))));
                }
            }
        } catch (NotConnectedException e) {
        }
    }

    private void notifyPlayer(ServerMessage sm) {
        ProxiedPlayer p;
        ProxyServer ps = this.main.getProxy();
        for (String player : sm.getPlayer().split(" ")) {
            p = ps.getPlayer(player);
            if (p != null) {
                p.sendMessage(sm.getTextComponent());
            }
        }
    }

    private Tuple<String, String> getWhitelistBlacklist(String tickettypes){
        String whitelist = "";
        String blacklist = "";
        boolean white = true;
        for (char c : tickettypes.toCharArray()){
            switch (c){
                case '-':
                    white = false;
                    break;
                case '+':
                    white = true;
                    break;
                default:
                    if (white)
                        whitelist += c;
                    else
                        blacklist += c;
            }
        }
        return new Tuple<>(whitelist, blacklist);
    }
}
