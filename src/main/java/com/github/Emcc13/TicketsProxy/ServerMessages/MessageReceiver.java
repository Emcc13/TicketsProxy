package com.github.Emcc13.TicketsProxy.ServerMessages;

import com.github.Emcc13.TicketsProxy.Config.ConfigManager;
import com.github.Emcc13.TicketsProxy.Database.DBTicket;
import com.github.Emcc13.TicketsProxy.Database.NotConnectedException;
import com.github.Emcc13.TicketsProxy.ProxyTickets;
import com.github.Emcc13.TicketsProxy.Util.Formatter;
import com.github.Emcc13.TicketsProxy.Util.Tuple;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;

public class MessageReceiver {
    private ProxyTickets main;

    public MessageReceiver(ProxyTickets pt) {
        this.main = pt;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent ev) {
        if (ev.getIdentifier().equals(main.getChannelIdentifier())) {
            ServerMessage sm = ServerMessage.fromBytes(ev.getData());
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

    private void sendTickets(Player p, List<DBTicket> tickets) {
        String openTicket = (String) this.main.getCachedConfig()
                .get(ConfigManager.TICKETS_OPEN_FORMAT_KEY);
        String claimedTicket = (String) this.main.getCachedConfig()
                .get(ConfigManager.TICKETS_CLAIMED_FORMAT_KEY);
        String closedTicket = (String) this.main.getCachedConfig()
                .get(ConfigManager.TICKETS_CLOSED_FORMAT_KEY);
        String readTicket = (String) this.main.getCachedConfig()
                .get(ConfigManager.TICKETS_READ_FORMAT_KEY);
        Map<String, String> ticketTypes = (Map<String, String>) this.main.getCachedConfig().getOrDefault(ConfigManager.TICKET_FORMAT_TICKETTYPE_KEY, new HashMap<>());
        for (DBTicket dbTicket : tickets) {
            p.sendMessage(dbTicket.format(dbTicket.getAnswerDate() != null ?
                    dbTicket.getReadDate() != null ? readTicket : closedTicket :
                    dbTicket.getClaimingDate() != null ? claimedTicket : openTicket, ticketTypes));
        }
    }

    private void ticketsPage(ServerMessage sm) {
        Player p = this.main.getServer().getPlayer(sm.getPlayer()).orElse(null);
        if (p == null) {
            return;
        }
        Integer entriesPerPage = (Integer) this.main.getCachedConfig().get(ConfigManager.TICKETS_PER_PAGE_KEY);
        Tuple<String, String> white_black = getWhitelistBlacklist(sm.getTicketType());
        try {
            Integer pages = (int) java.lang.Math.ceil(
                    Double.valueOf(this.main.getDbInterface().getOpenClaimedTickets(white_black.first,
                            white_black.second)) / entriesPerPage);
            String page_info = (String) main.getCachedConfig()
                    .get(ConfigManager.TICKETS_PAGE_INFO_KEY);
            int page = Math.max(Math.min(sm.getNumber(), pages - 1), 0);
            sendTickets(p, this.main.getDbInterface().getOpenTicketsPage(page, entriesPerPage,
                    white_black.first, white_black.second));
            p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString(page_info,
                    new Tuple<String, String>("%PAGE%", String.valueOf(page + 1)),
                    new Tuple<String, String>("%PAGES%", String.valueOf(pages)),
                    new Tuple<String, String>("%PREVPAGE%", String.valueOf(Math.max(Math.min(page, pages), 1))),
                    new Tuple<String, String>("%NEXTPAGE%", String.valueOf(Math.max(Math.min(page + 2, pages), 1))),
                    new Tuple<String, String>("%TICKETTYPE%", sm.getTicketType())
            )));
        } catch (NotConnectedException e) {
            p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString((String) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY))));
        }
    }

    private void ticketsPlayerPage(ServerMessage sm) {
        Player p = this.main.getServer().getPlayer(sm.getPlayer()).orElse(null);
        if (p == null) {
            return;
        }
        Integer entriesPerPage = (Integer) this.main.getCachedConfig().get(ConfigManager.TICKETS_PER_PAGE_KEY);
        Tuple<String, String> white_black = getWhitelistBlacklist(sm.getTicketType());
        try {
            int pages = (int) java.lang.Math.ceil(
                    Double.valueOf(this.main.getDbInterface().getPlayerTicketsTotal(sm.getText(), white_black.first,
                            white_black.second)) / entriesPerPage);
            String page_info = ((String) main.getCachedConfig()
                    .get(ConfigManager.TICKETS_PLAYER_PAGE_INFO_KEY));
            int page = Math.max(Math.min(sm.getNumber() - 1, pages - 1), 0);
            sendTickets(p, this.main.getDbInterface()
                    .getPlayerTicketsPage(page, sm.getText(), entriesPerPage, white_black.first, white_black.second));
            p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString(page_info,
                    new Tuple<String, String>("%PAGE%", String.valueOf(page + 1)),
                    new Tuple<String, String>("%PAGES%", String.valueOf(pages)),
                    new Tuple<String, String>("%PREVPAGE%", String.valueOf(Math.max(Math.min(page, pages), 1))),
                    new Tuple<String, String>("%NEXTPAGE%", String.valueOf(Math.max(Math.min(page + 2, pages), 1))),
                    new Tuple<String, String>("%PLAYER%", sm.getText()),
                    new Tuple<String, String>("%TICKETTYPE%", sm.getTicketType())
            )));
        } catch (NotConnectedException e) {
            p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString((String) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY))));
        }
    }

    private void ticketsNum(ServerMessage sm) {
        Player p = this.main.getServer().getPlayer(sm.getPlayer()).orElse(null);
        if (p == null) {
            this.main.getLogger().warn(String.format("Failed to find player with: %s", sm.getPlayer()));
            return;
        }
        try {
            DBTicket dbTicket = this.main.getDbInterface().getTicketByID(sm.getNumber()); // format
            if (dbTicket != null) {
                sendTickets(p, new ArrayList<DBTicket>() {{
                    add(dbTicket);
                }});
            } else {
                p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString(
                        (String) main.getCachedConfig().get(ConfigManager.NO_TICKET_WITH_ID_KEY),
                        new Tuple<>("%ID%", String.valueOf(sm.getNumber())))));
            }
        } catch (NotConnectedException e) {
            p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString((String) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY))));
        }
    }

    private void ticketsClaim(ServerMessage sm) {
        Player p = this.main.getServer().getPlayer(sm.getPlayer()).orElse(null);
        if (p == null) {
            this.main.getLogger().warn(String.format("Failed to find player with: %s", sm.getPlayer()));
            return;
        }
        try {
            this.main.getDbInterface().claimTicket(sm.getNumber(), p.getUsername());
        } catch (NotConnectedException e) {
            p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString((String) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY))));
        }
    }

    private void ticketsUnclaim(ServerMessage sm) {
        Player p = this.main.getServer().getPlayer(sm.getPlayer()).orElse(null);
        if (p == null) {
            this.main.getLogger().warn(String.format("Failed to find player with: %s", sm.getPlayer()));
            return;
        }
        try {
            this.main.getDbInterface().unclaimTicket(sm.getNumber(), p.getUsername());
        } catch (NotConnectedException e) {
            p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString((String) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY))));
        }
    }

    private void ticketsClose(ServerMessage sm) {
        Player p = this.main.getServer().getPlayer(sm.getPlayer()).orElse(null);
        try {
            this.main.getDbInterface().closeTicket(sm.getNumber(), p.getUsername(), sm.getText());
        } catch (NotConnectedException e) {
            p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString((String) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY))));
        }
    }

    private void ticketsTP(ServerMessage sm) {
        Player p = this.main.getServer().getPlayer(sm.getPlayer()).orElse(null);
        if (p == null) {
            this.main.getLogger().warn(String.format("Failed to find player with: %s", sm.getPlayer()));
            return;
        }
        try {
            DBTicket ticket = this.main.getDbInterface().getTicketByID(sm.getNumber());
            ServerMessage tpMessage = ServerMessage.forTpPos(sm.getPlayer(),
                    ticket.getWorld(),
                    ticket.getX_pos(),
                    ticket.getY_pos(),
                    ticket.getZ_pos(),
                    ticket.getAzimuth(),
                    ticket.getElevation());
            String server = ticket.getServer();

            Optional<RegisteredServer> opt_server = this.main.getServer().getServer(server);
            if (opt_server.isPresent()) {
//                When no player is on the requested server, then no pluginMessage is send
                RegisteredServer reg_server = opt_server.get();
                if (!p.getCurrentServer().orElse(null).getServerInfo().getName().equals(server)) {
                    if (reg_server.getPlayersConnected().size() < 1) {
                        this.main.getServer().getScheduler().buildTask(this.main, new Thread() {
                            @Override
                            public void run() {
                                while (true) {
                                    if (p.getCurrentServer().orElse(null).getServerInfo().getName().equals(server)) {
                                        break;
                                    }
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                reg_server.sendPluginMessage(main.getChannelIdentifier(), tpMessage.toMessagae());
                            }
                        }).schedule();
                    }else {
                        reg_server.sendPluginMessage(main.getChannelIdentifier(), tpMessage.toMessagae());
                    }
                    p.createConnectionRequest(reg_server).connect();
                }else {
                    reg_server.sendPluginMessage(main.getChannelIdentifier(), tpMessage.toMessagae());
                }
            }
        } catch (NotConnectedException | NoSuchElementException e) {
            p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString((String) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY))));
        }
    }

    private void ticketList(ServerMessage sm) {
        Player p = this.main.getServer().getPlayer(sm.getPlayer()).orElse(null);
        if (p == null) {
            this.main.getLogger().warn(String.format("Failed to find player with: %s", sm.getPlayer()));
            return;
        }
        Integer entriesPerPage = (Integer) this.main.getCachedConfig().get(ConfigManager.TICKETS_PER_PAGE_KEY);
        try {
            Integer pages = (int) java.lang.Math.ceil(
                    Double.valueOf(this.main.getDbInterface().getUnreadOpenTickets(sm.getPlayer())) / entriesPerPage);
            int page = Math.max(Math.min(sm.getNumber(), pages - 1), 0);
            String unread_ticket = (String) this.main.getCachedConfig()
                    .get(ConfigManager.TICKET_UNREAD_FORMAT_KEY);
            String open_ticket = (String) this.main.getCachedConfig()
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
            p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString((String) main.getCachedConfig()
                            .get(ConfigManager.TICKET_PAGE_INFO_KEY),
                    new Tuple<String, String>("%PAGE%", String.valueOf(page + 1)),
                    new Tuple<String, String>("%PAGES%", String.valueOf(pages)),
                    new Tuple<String, String>("%PREVPAGE%", String.valueOf(Math.max(Math.min(page, pages), 1))),
                    new Tuple<String, String>("%NEXTPAGE%", String.valueOf(Math.max(Math.min(page + 2, pages), 1)))
            )));
        } catch (NotConnectedException e) {
            p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString((String) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY))));
        }
    }

    private void ticketNew(ServerMessage sm) {
        boolean catch_spam = true;
        if (!((String) main.getCachedConfig().get(ConfigManager.TICKET_NOTIFY_KEY)).contains(sm.getTicketType())) {
            catch_spam = false;
        }
        String server;
        Player p = this.main.getServer().getPlayer(sm.getPlayer()).orElse(null);
        if (p == null) {
            server = sm.getServer();
            for (RegisteredServer registeredServer : this.main.getServer().getAllServers()) {
                if (registeredServer.getServerInfo().getAddress().getPort() == Integer.parseInt(server)) {
                    server = registeredServer.getServerInfo().getName();
                }
            }
        } else {
            server = p.getCurrentServer().get().getServer().getServerInfo().getName();
//            server = p.getServer().getInfo().getUsername();
        }
        try {
            this.main.getDbInterface().addTicket(sm.getPlayer(), sm.getTicketType(), sm.getText(),
                    server, sm.getWorld(), sm.getPosX(), sm.getPosY(), sm.getPosZ(),
                    sm.getPitch(), sm.getYaw(), catch_spam);
        } catch (NotConnectedException e) {
            if (p != null) {
                p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString((String) main.getCachedConfig()
                        .get(ConfigManager.DB_FAILURE_MESSAGE_KEY))));
            }
        }
    }

    private void ticketRead(ServerMessage sm) {
        Player p = this.main.getServer().getPlayer(sm.getPlayer()).orElse(null);
        if (p == null) {
            this.main.getLogger().warn(String.format("Failed to find player with: %s", sm.getPlayer()));
            return;
        }
        try {
            this.main.getDbInterface().readTicket(sm.getNumber(), sm.getPlayer());
        } catch (NotConnectedException e) {
            p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString((String) main.getCachedConfig()
                    .get(ConfigManager.DB_FAILURE_MESSAGE_KEY))));
        }
    }

    private void ticketsOpen(ServerMessage sm) {
        try {
            int openTickets = main.getDbInterface().getOpenTickets();
            if (openTickets > 0) {
                String join_open = (String) main.getCachedConfig()
                        .get(ConfigManager.JOIN_OPEN_TICKET_KEY);
                Component info = MiniMessage.miniMessage().deserialize(Formatter.formatString(join_open, new Tuple<>("%NUM%", String.valueOf(openTickets))));
                for (String player : sm.getPlayer().split(" ")) {
                    Player p = this.main.getServer().getPlayer(player).orElse(null);
                    p.sendMessage(info);
                }
            }
        } catch (NotConnectedException e) {
        }
    }

    private void ticketsUnread(ServerMessage sm) {
        try {
            String unreadTicket = (String) main.getCachedConfig().
                    get(ConfigManager.JOIN_UNRAED_TICKET_KEY);
            for (String player : sm.getPlayer().split(" ")) {
                Player p = this.main.getServer().getPlayer(player).orElse(null);
                int unreadTickets = main.getDbInterface().
                        getUnreadTickets(player);
                if (unreadTickets > 0) {
                    p.sendMessage(MiniMessage.miniMessage().deserialize(Formatter.formatString(unreadTicket,
                            new Tuple<>("%NUM%", String.valueOf(unreadTickets)))));
                }
            }
        } catch (NotConnectedException e) {
        }
    }

    private void notifyPlayer(ServerMessage sm) {
        Player p;
        ProxyServer ps = this.main.getServer();
        for (String player : sm.getPlayer().split(" ")) {
            p = ps.getPlayer(player).orElse(null);
            if (p != null) {
                p.sendMessage(MiniMessage.miniMessage().deserialize(sm.getMessage()));
            }
        }
    }

    private Tuple<String, String> getWhitelistBlacklist(String tickettypes) {
        String whitelist = "";
        String blacklist = "";
        boolean white = true;
        for (char c : tickettypes.toCharArray()) {
            switch (c) {
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
