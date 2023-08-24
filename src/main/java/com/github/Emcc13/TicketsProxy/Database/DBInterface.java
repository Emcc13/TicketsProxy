package com.github.Emcc13.TicketsProxy.Database;

import java.util.List;
import java.util.Map;

public interface DBInterface extends Runnable{
    void updateSettings(String host, String port, String database, String table,
                        String username, String password);
    boolean isConnected();
//    boolean disconnect();

    void addTicket(String player, String ticketType, String text, String server, String world,
                          Double locX, Double locY, Double locZ, Float locAzimuth, Float locElevation,
                   boolean catchSpam) throws NotConnectedException;
    void addTicket(String player, String ticketType, String text, String server, String world,
                   Double locX, Double locY, Double locZ, Float locAzimuth, Float locElevation, long timestamp,
                   boolean catchSpam) throws NotConnectedException;
    DBTicket getTicketByID(int id) throws NotConnectedException;
    List<DBTicket> getOpenTicketsPage(int page, int entriesPerPage, String whitelist, String blacklist) throws NotConnectedException;
    void claimTicket(int id, String player) throws NotConnectedException;
    void claimTicket(int id, String player, long timestamp) throws NotConnectedException;
    void unclaimTicket(int id, String player) throws NotConnectedException;
    void readTicket(int id, String player) throws NotConnectedException;
    void readTicket(int id, String player, long timestamp) throws NotConnectedException;
    void closeTicket(int id, String player, String text) throws NotConnectedException;
    void closeTicket(int id, String player, String text, long timestamp) throws NotConnectedException;
    void cleanDB() throws NotConnectedException;
    List<DBTicket> getPlayerTicketPage(int page, String player, int entriesPerPage) throws NotConnectedException;
    List<DBTicket> getPlayerTicketsPage(int page, String player, int entriesPerPage, String whitelist, String blacklist) throws NotConnectedException;
    Integer getPlayerTicketsTotal(String player, String whitelist, String blacklist) throws NotConnectedException;
    Map<String, Integer> getUnreadTickets() throws NotConnectedException;
    Integer getUnreadTickets(String player) throws NotConnectedException;
    Integer getUnreadOpenTickets(String player) throws NotConnectedException;
    Map<String, Integer> getOpenTicketsPerPlayer() throws NotConnectedException;
    Integer getOpenTickets() throws NotConnectedException;
    Integer getOpenClaimedTickets(String whitelist, String blacklist) throws NotConnectedException;
}
