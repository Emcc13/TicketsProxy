package com.github.Emcc13.TicketsProxy.Database;

import com.github.Emcc13.TicketsProxy.Config.ConfigManager;
import com.github.Emcc13.TicketsProxy.ProxyTickets;
//import com.mysql.cj.jdbc.Driver;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

public class MySQL implements DBInterface {
    private ProxyTickets main;
    private Connection con;
    private String host;
    private String port;
    private String database;
    private String table;
    private String username;
    private String password;

    private Path path;

    private PreparedStatement addTicket_statement;
    private PreparedStatement ticketSpam_statement;
    private PreparedStatement claimTicket_statement;
    private PreparedStatement unclaimTicket_statement;
    private PreparedStatement closeTicket_statement;
    private PreparedStatement readTicket_statement;
    private PreparedStatement ticketbyID_statement;
    private PreparedStatement ticketsPage_statement;
    private PreparedStatement ticketsPlayerPage_statement;
    private PreparedStatement ticketsPlayerTotal_statement;
    private PreparedStatement ticketPage_statement;
    private PreparedStatement openTicketsPerPlayer_statement;
    private PreparedStatement openTicketsCount_statement;
    private PreparedStatement openClaimedTicketsCount_statement;
    private PreparedStatement unreadTicketsPerPlayer_statement;
    private PreparedStatement unreadTicketsCount_statement;
    private PreparedStatement unreadOpenTicketsCount_statement;


    private final String IDC = "id";
    private final String enquirerC = "enquirer";
    private final String ticketTypeC = "ticketType";
    private final String requestC = "request";
    private final String requestDateC = "requestDate";

    private final String locServerC = "server";
    private final String locWorldC = "world";
    private final String locXC = "X";
    private final String locYC = "Y";
    private final String locZC = "Z";
    private final String locAzimuthC = "yaw";
    private final String locElevationC = "pitch";

    private final String claimedByC = "claimedBy";
    private final String claimingDateC = "claimingD";

    private final String responderC = "responder";
    private final String answerC = "answer";
    private final String answerDateC = "answerD";

    private final String readC = "readD"; // read is mysql keyword

    public MySQL(ProxyTickets main, String host, String port, String database, String table,
                 String username, String password, Path path) {
        this.main = main;
        this.host = host;
        this.port = port;
        this.database = database;
        this.table = table;
        this.username = username;
        this.password = password;

        this.path = path;

        if (this.connect()) {
            this.setupTables();
        }
    }

    public boolean disconnect() {
        try {
            this.con.close();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }


    @Override
    public void run() {
        try {
            Boolean isFolder = (Boolean) Files.getAttribute(this.path,
                    "basic:isDirectory", NOFOLLOW_LINKS);
            if (!isFolder) {
                throw new IllegalArgumentException("Path: " + path
                        + " is not a folder");
            }
        } catch (IOException ioe) {
            // Folder does not exists
            ioe.printStackTrace();
        }

        // obtain the file system of the Path
        FileSystem fs = path.getFileSystem();

        // create the new WatchService using the new try() block
        try (WatchService service = fs.newWatchService()) {
            // register the path to the service
            // watch for creation events
            path.register(service, ENTRY_CREATE);
            path.register(service, ENTRY_MODIFY);
//            path.register(service, ENTRY_DELETE);
            // Start the infinite polling loop
            WatchKey key = null;
            boolean new_files = true;
            while (true) {
                if (!new_files) {
                    key = service.take();
                    // Dequeueing events
                    WatchEvent.Kind<?> kind = null;
                    for (WatchEvent<?> watchEvent : key.pollEvents()) {
                        // Get the type of the event
                        kind = watchEvent.kind();
                        //                    Path newPath;
                        if (OVERFLOW == kind) {
                            continue;
                        } else if (ENTRY_CREATE == kind) {
                            //                         A new Path was created
                            new_files = true;
                        } else if (ENTRY_MODIFY == kind) {
                            //                         modified
                            new_files = true;
                        } else {
                            continue;
                        }
                    }
                }
                if (new_files) {
                    if (!this.isConnected()) {
                        if (!this.connect()) {
                            Thread.sleep(15 * 1000);
                            continue;
                        }
                    }
                    new_files = !insertCachedTickets();
                }
                if (key != null && !key.reset()) {
                    break;
                }
            }
        } catch (IOException | InterruptedException ioe) {
        }
    }

    private boolean insertCachedTickets(){
        File[] queries = path.toFile().listFiles();
        boolean all_deleted = true;
        for (File f : queries) {
            MySQLStatement statement = null;
            try {
                statement = new MySQLStatement(Files.readAllBytes(
                        Paths.get(f.toString())));
            } catch (IOException e) {
            }
            if (statement.execute(this)) {
                f.delete();
            }else{
                all_deleted = false;
            }
        }
        return all_deleted;
    }

    public void updateSettings(String host, String port, String database, String table,
                               String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.table = table;
        this.username = username;
        this.password = password;
        this.connect();
    }

    private boolean connect() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
            con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" +
                    database + "?autoReconnect=false&useSSL=false", username, password);

            this.setupTables();
            return true;
        } catch (SQLException e) {
            con = null;
            return false;
        }
    }

    private boolean setupTables() {
        String ticketnotify = (String) this.main.getCachedConfig().get(ConfigManager.TICKET_NOTIFY_KEY);
        String ticketignore = (String) this.main.getCachedConfig().get(ConfigManager.TICKET_IGNORE_KEY);
        try {
            Statement st = this.con.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS " +
                    table + " (" +
                    this.IDC + " INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    this.enquirerC + " CHAR(36) NOT NULL, " +
                    this.ticketTypeC + " CHAR(1) NOT NULL, " +
                    this.requestC + " TEXT NOT NULL, " +
                    this.requestDateC + " TIMESTAMP, " +

                    this.locServerC + " CHAR(255) NOT NULL, " +
                    this.locWorldC + " CHAR(255) NOT NULL,  " +
                    this.locXC + " DOUBLE NOT NULL, " +
                    this.locYC + " DOUBLE NOT NULL, " +
                    this.locZC + " DOUBLE NOT NULL, " +
                    this.locAzimuthC + " FLOAT NOT NULL, " +
                    this.locElevationC + " FLOAT NOT NULL, " +

                    this.claimedByC + " CHAR(36), " +
                    this.claimingDateC + " TIMESTAMP, " +

                    this.responderC + " CHAR(36), " +
                    this.answerC + " TEXT, " +
                    this.answerDateC + " TIMESTAMP, " +

                    this.readC + " TIMESTAMP" +
                    ");");
            this.ticketSpam_statement = this.con.prepareStatement(
                    "SELECT " + this.IDC + " FROM " +
                            this.table + " WHERE " +
                            this.enquirerC + " = ? AND " +
                            this.requestC + " = ? AND " +
                            this.answerDateC + " IS NULL AND " +
                            "SQRT(POW(" + this.locXC + " - ?, 2)+" +
                            "POW(" + this.locYC + " - ?, 2)+" +
                            "POW(" + this.locZC + " - ?, 2)) <= " +
                            this.main.getCachedConfig().get(ConfigManager.TICKET_SPAM_DISTANCE_KEY) + ";"
            );
            this.addTicket_statement = this.con.prepareStatement(
                    "INSERT INTO " +
                            this.table + " (" +
                            this.enquirerC + "," +
                            this.ticketTypeC + "," +
                            this.requestC + "," +
                            this.locServerC + "," +
                            this.locWorldC + "," +
                            this.locXC + "," +
                            this.locYC + "," +
                            this.locZC + "," +
                            this.locAzimuthC + "," +
                            this.locElevationC + "," +
                            this.requestDateC + ") " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?);",
                    Statement.RETURN_GENERATED_KEYS
            );
            this.claimTicket_statement = this.con.prepareStatement(
                    "UPDATE " +
                            this.table + " SET " +
                            this.claimedByC + " = ?," +
                            this.claimingDateC + " = ? WHERE " +
                            this.IDC + " = ? AND " +
                            this.claimedByC + " IS NULL;"
            );
            this.unclaimTicket_statement = this.con.prepareStatement(
                    "UPDATE " +
                            this.table + " SET " +
                            this.claimedByC + " = NULL, " +
                            this.claimingDateC + " = NULL WHERE " +
                            this.IDC + " = ?;"
            );
            this.closeTicket_statement = this.con.prepareStatement(
                    "UPDATE " +
                            this.table + " SET " +
                            this.responderC + " = ?, " +
                            this.answerDateC + " = ?, " +
                            this.answerC + " = ?, " +
                            this.readC + " = CASE WHEN " +
                            "\"" + ticketnotify + "\"" + " LIKE CONCAT(\"%\", " +
                            ticketTypeC + ", \"%\") " +
                            "THEN null ELSE ? END WHERE " +
                            this.IDC + " = ? AND " +
                            this.answerC + " IS NULL;"
            );
            this.readTicket_statement = this.con.prepareStatement(
                    "UPDATE " +
                            this.table + " SET " +
                            this.readC + " = ? WHERE " +
                            this.IDC + " = ? AND " +
                            this.readC + " IS NULL AND " +
                            this.enquirerC + " = ?;"
            );
            this.ticketbyID_statement = this.con.prepareStatement(
                    "SELECT * FROM " +
                            table + " WHERE " +
                            this.IDC + " = ?;"
            );
            this.ticketsPage_statement = this.con.prepareStatement(
                    "SELECT * FROM " +
                            this.table + " WHERE " +
                            this.answerC + " IS NULL " +
                            "AND (CHAR_LENGTH(?)<1 OR ? LIKE CONCAT(\"%\", " +
                            this.ticketTypeC + ", \"%\")) " +
                            "AND (CHAR_LENGTH(?)<1 OR ? NOT LIKE CONCAT(\"%\", " +
                            this.ticketTypeC + ", \"%\")) " +
                            "ORDER BY " +
                            this.IDC + " ASC LIMIT ?, ?;"
            );
            this.ticketPage_statement = this.con.prepareStatement(
                    "(SELECT * FROM " +
                            this.table + " WHERE " +
                            this.enquirerC + " = ? AND " +
                            this.answerC + " IS NOT NULL AND " +
                            this.readC + " IS NULL AND " +
                            "\"" + ticketignore + "\"" + " NOT LIKE CONCAT(\"%\", " +
                            ticketTypeC + ", \"%\") ORDER BY " +
                            this.IDC + " ASC) UNION (SELECT * FROM " +
                            this.table + " WHERE " +
                            this.enquirerC + " = ? AND " +
                            this.answerC + " IS NULL AND " +
                            this.readC + " IS NULL AND " +
                            "\"" + ticketignore + "\"" + " NOT LIKE CONCAT(\"%\", " +
                            ticketTypeC + ", \"%\") ORDER BY " +
                            this.IDC + " DESC) LIMIT ?, ?;"
            );
            this.ticketsPlayerPage_statement = this.con.prepareStatement(
                    "SELECT * FROM " +
                            this.table + " WHERE " +
                            this.enquirerC + " = ? " +
                            "AND (CHAR_LENGTH(?)<1 OR ? LIKE CONCAT(\"%\", " +
                            this.ticketTypeC + ", \"%\")) " +
                            "AND (CHAR_LENGTH(?)<1 OR ? NOT LIKE CONCAT(\"%\", " +
                            this.ticketTypeC + ", \"%\")) ORDER BY " +
                            this.IDC + " DESC LIMIT ?, ?;"
            );
            this.ticketsPlayerTotal_statement = this.con.prepareStatement(
                    "SELECT COUNT(ID) FROM " +
                            this.table + " WHERE " +
                            this.enquirerC + " = ? " +
                            "AND (CHAR_LENGTH(?)<1 OR ? LIKE CONCAT(\"%\", " +
                            this.ticketTypeC + ", \"%\")) " +
                            "AND (CHAR_LENGTH(?)<1 OR ? NOT LIKE CONCAT(\"%\", " +
                            this.ticketTypeC + ", \"%\"));"
            );

            this.openTicketsPerPlayer_statement = this.con.prepareStatement(
                    "SELECT COUNT(ID), " + enquirerC +
                            " FROM " + this.table + " WHERE " +
                            this.answerDateC + " IS NULL " +
                            " GROUP BY " + this.enquirerC
            );
            this.openTicketsCount_statement = this.con.prepareStatement(
                    "SELECT COUNT(ID) FROM " + this.table + " WHERE " +
                            this.answerDateC + " IS NULL AND " +
                            this.claimingDateC + " IS NULL AND " +
                            "\"" + ticketignore + "\"" + " NOT LIKE CONCAT(\"%\", " +
                            ticketTypeC + ", \"%\");"
            );
            this.openClaimedTicketsCount_statement = this.con.prepareStatement(
                    "SELECT COUNT(ID) FROM " + this.table + " WHERE " +
                            this.answerDateC + " IS NULL " +
                            "AND (CHAR_LENGTH(?)<1 OR ? LIKE CONCAT(\"%\", " +
                            this.ticketTypeC + ", \"%\")) " +
                            "AND (CHAR_LENGTH(?)<1 OR ? NOT LIKE CONCAT(\"%\", " +
                            this.ticketTypeC + ", \"%\"));"
            );
            this.unreadTicketsPerPlayer_statement = this.con.prepareStatement(
                    "SELECT COUNT(ID), " + enquirerC +
                            " FROM " + this.table + " WHERE " +
                            this.answerDateC + " IS NOT NULL AND " +
                            "\"" + ticketignore + "\"" + " NOT LIKE CONCAT(\"%\", " +
                            ticketTypeC + ", \"%\") AND " +
                            this.readC + " IS NULL" +
                            " GROUP BY " + this.enquirerC
            );
            this.unreadTicketsCount_statement = this.con.prepareStatement(
                    "SELECT COUNT(ID) FROM " + this.table + " WHERE " +
                            this.answerDateC + " IS NOT NULL AND " +
                            this.readC + " IS NULL AND " +
                            "\"" + ticketignore + "\"" + " NOT LIKE CONCAT(\"%\", " +
                            ticketTypeC + ", \"%\") AND " +
                            this.enquirerC + " = ?;"
            );
            this.unreadOpenTicketsCount_statement = this.con.prepareStatement(
                    "SELECT COUNT(ID) FROM " + this.table + " WHERE " +
                            this.readC + " IS NULL AND " +
                            "\"" + ticketignore + "\"" + " NOT LIKE CONCAT(\"%\", " +
                            ticketTypeC + ", \"%\") AND " +
                            this.enquirerC + " = ?;"
            );
            return true;
        } catch (SQLException | NullPointerException e) {
            return false;
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return con != null && !con.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }


    public void saveStatement(long timeStamp, byte[] serialized) {
        try {
            Files.write(new File(this.path.toFile(), String.valueOf(timeStamp) + ".txt").toPath(), serialized);
        } catch (IOException e) {
            this.main.getLogger().error("Failed to save sql statement!");
//            e.printStackTrace();
        }
    }

    @Override
    public void addTicket(String player, String ticketType, String text, String server, String world,
                          Double locX, Double locY, Double locZ, Float locAzimuth, Float locElevation,
                          boolean catchSpam)
            throws NotConnectedException {
        this.addTicket(player, ticketType, text, server, world, locX, locY, locZ, locAzimuth, locElevation,
                System.currentTimeMillis(), catchSpam);
    }

    @Override
    public void addTicket(String player, String ticketType, String text, String server, String world,
                          Double locX, Double locY, Double locZ, Float locAzimuth, Float locElevation, long timeStamp,
                          boolean catchSpam)
            throws NotConnectedException {
        try {
            if (catchSpam) {
                this.ticketSpam_statement.clearParameters();
                this.ticketSpam_statement.setString(1, player);
                this.ticketSpam_statement.setString(2, text);
                this.ticketSpam_statement.setDouble(3, locX);
                this.ticketSpam_statement.setDouble(4, locY);
                this.ticketSpam_statement.setDouble(5, locZ);
                ResultSet rs = this.ticketSpam_statement.executeQuery();
                while (rs.next()) {
                    this.main.getNotifier().spamInfo(player, rs.getInt(1), text);
                    return;
                }
            }
            this.addTicket_statement.clearParameters();
            this.addTicket_statement.setString(1, player);
            this.addTicket_statement.setString(2, ticketType);
            this.addTicket_statement.setString(3, text);
            this.addTicket_statement.setString(4, server);
            this.addTicket_statement.setString(5, world);
            this.addTicket_statement.setDouble(6, locX);
            this.addTicket_statement.setDouble(7, locY);
            this.addTicket_statement.setDouble(8, locZ);
            this.addTicket_statement.setFloat(9, locAzimuth);
            this.addTicket_statement.setFloat(10, locElevation);
            this.addTicket_statement.setTimestamp(11, new Timestamp(timeStamp));
            this.addTicket_statement.executeUpdate();
            try {
                ResultSet keys = this.addTicket_statement.getGeneratedKeys();
                keys.next();
                this.main.getNotifier().createInfo(player, keys.getInt(1), text, ((String) main.getCachedConfig().get(ConfigManager.TICKET_NOTIFY_KEY)).contains(ticketType));
            } catch (SQLException | NullPointerException e) {
                this.main.getNotifier().createInfo(player, -1, text, false);
            }
        } catch (SQLException | NullPointerException e) {
//            e.printStackTrace();
            this.saveStatement(timeStamp, MySQLStatement.addTicketStatement(player, ticketType, text, server, world,
                    locX, locY, locZ, locAzimuth, locElevation, timeStamp, catchSpam).serialize());
            this.con = null;
            throw new NotConnectedException();
        }
    }

    private DBTicket dbticketFromResultSet(ResultSet rs) {
        DBTicket result = null;
        try {
            Integer id = rs.getInt(1);
            String enquirer = rs.getString(2);
            String ticketType = rs.getString(3);
            String request = rs.getString(4);
            Timestamp requestD = rs.getTimestamp(5);
            String server = rs.getString(6);
            String world = rs.getString(7);
            Double locX = rs.getDouble(8);
            Double locY = rs.getDouble(9);
            Double locZ = rs.getDouble(10);
            Float azimuth = rs.getFloat(11);
            Float elevation = rs.getFloat(12);
            String claimedBy = rs.getString(13);
            Timestamp claimingD = rs.getTimestamp(14);

            String responder = rs.getString(15);
            String answer = rs.getString(16);
            Timestamp answerD = rs.getTimestamp(17);
            Timestamp read = rs.getTimestamp(18);
            result = new DBTicket(id, enquirer, ticketType, request, requestD,
                    server, world, locX, locY, locZ, azimuth, elevation,
                    claimedBy, claimingD,
                    responder, answer, answerD,
                    read);
        } catch (SQLException | NullPointerException e) {
        }
        return result;
    }

    @Override
    public DBTicket getTicketByID(int id) throws NotConnectedException {
        DBTicket result = null;
        try {
            this.ticketbyID_statement.clearParameters();
            this.ticketbyID_statement.setInt(1, id);
            ResultSet rs = this.ticketbyID_statement.executeQuery();
            if (rs.next()) {
                result = this.dbticketFromResultSet(rs);
            }
        } catch (SQLException | NullPointerException e) {
            this.con = null;
            this.connect();
            throw new NotConnectedException();
        }
        return result;
    }

    @Override
    public List<DBTicket> getOpenTicketsPage(int page, int entriesPerPage, String whitelist, String blacklist)
            throws NotConnectedException {
        LinkedList<DBTicket> result = new LinkedList<>();
        try {
            this.ticketsPage_statement.clearParameters();
            this.ticketsPage_statement.setString(1, whitelist);
            this.ticketsPage_statement.setString(2, whitelist);
            this.ticketsPage_statement.setString(3, blacklist);
            this.ticketsPage_statement.setString(4, blacklist);
            this.ticketsPage_statement.setInt(5, page * entriesPerPage);
            this.ticketsPage_statement.setInt(6, entriesPerPage);
            ResultSet rs = this.ticketsPage_statement.executeQuery();
            while (rs.next()) {
                result.add(this.dbticketFromResultSet(rs));
            }
        } catch (SQLException | NullPointerException e) {
//            e.printStackTrace();
            this.con = null;
            this.connect();
            throw new NotConnectedException();
        }
        return result;
    }

    @Override
    public void claimTicket(int id, String player) throws NotConnectedException {
        this.claimTicket(id, player, System.currentTimeMillis());
    }


    @Override
    public void claimTicket(int id, String player, long timeStamp) throws NotConnectedException {
        try {
            this.claimTicket_statement.clearParameters();
            this.claimTicket_statement.setString(1, player);
            this.claimTicket_statement.setTimestamp(2, new Timestamp(timeStamp));
            this.claimTicket_statement.setInt(3, id);
            this.claimTicket_statement.executeUpdate();
            this.main.getNotifier().claimInfo(player, id);
        } catch (SQLException | NullPointerException e) {
            this.saveStatement(timeStamp, MySQLStatement.claimTicketStatement(id, player, timeStamp).serialize());
            this.con = null;
            throw new NotConnectedException();
        }
    }

    @Override
    public void unclaimTicket(int id, String player) throws NotConnectedException {
        try {
            this.unclaimTicket_statement.clearParameters();
            this.unclaimTicket_statement.setInt(1, id);
            this.unclaimTicket_statement.executeUpdate();
            this.main.getNotifier().unclaimInfo(player, id);
        } catch (SQLException | NullPointerException e) {
            this.saveStatement(System.currentTimeMillis(),
                    MySQLStatement.unclaimTicketStatement(id, player).serialize());
            this.con = null;
            throw new NotConnectedException();
        }
    }

    @Override
    public void closeTicket(int id, String player, String text) throws NotConnectedException {
        this.closeTicket(id, player, text, System.currentTimeMillis());
    }

    @Override
    public void closeTicket(int id, String player, String text, long timeStamp) throws NotConnectedException {
        try {
            this.closeTicket_statement.clearParameters();
            this.closeTicket_statement.setString(1, player);
            this.closeTicket_statement.setTimestamp(2, new Timestamp(timeStamp));
            this.closeTicket_statement.setString(3, text);
            this.closeTicket_statement.setTimestamp(4, new Timestamp(timeStamp));
            this.closeTicket_statement.setInt(5, id);
            int updatedRows = this.closeTicket_statement.executeUpdate();
            if (updatedRows<1){
                this.main.getNotifier().closeFailedInfo(player, id);
                return;
            }
            try {
                DBTicket ticket = this.getTicketByID(id);
                this.main.getNotifier().closeInfo(player, id, text, ticket.getEnquirer(),
                        ((String) this.main.getCachedConfig().getOrDefault(ConfigManager.TICKET_NOTIFY_KEY, ""))
                                .contains(ticket.getTicketType()));
            } catch (NotConnectedException | NullPointerException e) {
//                this.main.getNotifier().closeInfo(player, id, text, "", false);
                this.main.getNotifier().closeFailedInfo(player, id);
            }
        } catch (SQLException | NullPointerException e) {
            this.saveStatement(timeStamp, MySQLStatement.closeTicketStatement(id, player, text, timeStamp).serialize());
            this.con = null;
            throw new NotConnectedException();
        }
    }

    @Override
    public void readTicket(int id, String player) throws NotConnectedException {
        this.readTicket(id, player, System.currentTimeMillis());
    }

    @Override
    public void readTicket(int id, String player, long timeStamp) throws NotConnectedException {
        try {
            this.readTicket_statement.clearParameters();
            this.readTicket_statement.setTimestamp(1, new Timestamp(timeStamp));
            this.readTicket_statement.setInt(2, id);
            this.readTicket_statement.setString(3, player);
            if (this.readTicket_statement.executeUpdate() > 0) {
                this.main.getNotifier().readInfo(player, id);
            }
        } catch (SQLException | NullPointerException e) {
            this.saveStatement(timeStamp, MySQLStatement.readTicketStatement(id, player, timeStamp).serialize());
            this.con = null;
            throw new NotConnectedException();
        }
    }

    @Override
    public List<DBTicket> getPlayerTicketPage(int page, String player, int entriesPerPage) throws NotConnectedException {
        List<DBTicket> result = new LinkedList<>();
        try {
            this.ticketPage_statement.clearParameters();
            this.ticketPage_statement.setString(1, player);
            this.ticketPage_statement.setString(2, player);
            this.ticketPage_statement.setInt(3, page * entriesPerPage);
            this.ticketPage_statement.setInt(4, entriesPerPage);
            ResultSet rs = this.ticketPage_statement.executeQuery();
            while (rs.next()) {
                result.add(this.dbticketFromResultSet(rs));
            }
        } catch (SQLException | NullPointerException e) {
            this.con = null;
            throw new NotConnectedException();
        }
        return result;
    }

    @Override
    public List<DBTicket> getPlayerTicketsPage(int page, String player, int entriesPerPage, String whitelist,
                                               String blacklist) throws NotConnectedException {
        List<DBTicket> result = new LinkedList<>();
        try {
            this.ticketsPlayerPage_statement.clearParameters();
            this.ticketsPlayerPage_statement.setString(1, player);
            this.ticketsPlayerPage_statement.setString(2, whitelist);
            this.ticketsPlayerPage_statement.setString(3, whitelist);
            this.ticketsPlayerPage_statement.setString(4, blacklist);
            this.ticketsPlayerPage_statement.setString(5, blacklist);
            this.ticketsPlayerPage_statement.setInt(6, page * entriesPerPage);
            this.ticketsPlayerPage_statement.setInt(7, entriesPerPage);
            ResultSet rs = this.ticketsPlayerPage_statement.executeQuery();
            while (rs.next()) {
                result.add(this.dbticketFromResultSet(rs));
            }
        } catch (SQLException | NullPointerException e) {
            this.con = null;
            this.connect();
            throw new NotConnectedException();
        }
        return result;
    }

    @Override
    public Integer getPlayerTicketsTotal(String player, String whitelist, String blacklist) throws NotConnectedException {
        try {
            this.ticketsPlayerTotal_statement.clearParameters();
            this.ticketsPlayerTotal_statement.setString(1, player);
            this.ticketsPlayerTotal_statement.setString(2, whitelist);
            this.ticketsPlayerTotal_statement.setString(3, whitelist);
            this.ticketsPlayerTotal_statement.setString(4, blacklist);
            this.ticketsPlayerTotal_statement.setString(5, blacklist);
            ResultSet rs = this.ticketsPlayerTotal_statement.executeQuery();
            while (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException | NullPointerException e) {
//            e.printStackTrace();
            this.con = null;
            this.connect();
            throw new NotConnectedException();
        }
        return 0;
    }

    public Map<String, Integer> getUnreadTickets() throws NotConnectedException {
        Map<String, Integer> result = new HashMap<>();
        try {
            ResultSet rs = this.unreadTicketsPerPlayer_statement.executeQuery();
            while (rs.next()) {
                result.put(rs.getString(2), rs.getInt(1));
            }
        } catch (SQLException | NullPointerException e) {
            this.con = null;
            this.connect();
            throw new NotConnectedException();
        }
        return result;
    }

    public Integer getUnreadTickets(String player) throws NotConnectedException {
        try {
            this.unreadTicketsCount_statement.clearParameters();
            this.unreadTicketsCount_statement.setString(1, player);
            ResultSet rs = this.unreadTicketsCount_statement.executeQuery();
            while (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException | NullPointerException e) {
            this.con = null;
            throw new NotConnectedException();
        }
        return 0;
    }

    public Integer getUnreadOpenTickets(String player) throws NotConnectedException {
        try {
            this.unreadOpenTicketsCount_statement.clearParameters();
            this.unreadOpenTicketsCount_statement.setString(1, player);
            ResultSet rs = this.unreadOpenTicketsCount_statement.executeQuery();
            while (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException | NullPointerException e) {
            this.con = null;
            throw new NotConnectedException();
        }
        return 0;
    }

    //    Unused
    public Map<String, Integer> getOpenTicketsPerPlayer() throws NotConnectedException {
        Map<String, Integer> result = new HashMap<>();
        try {
            ResultSet rs = this.openTicketsPerPlayer_statement.executeQuery();
            while (rs.next()) {
                result.put(rs.getString(2), rs.getInt(1));
            }
        } catch (SQLException | NullPointerException e) {
            this.con = null;
            this.connect();
            throw new NotConnectedException();
        }
        return result;
    }

    public Integer getOpenTickets() throws NotConnectedException {
        try {
            ResultSet rs = this.openTicketsCount_statement.executeQuery();
            while (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException | NullPointerException e) {
            this.con = null;
            this.connect();
            throw new NotConnectedException();
        }
        return 0;
    }

    public Integer getOpenClaimedTickets(String whitelist, String blacklist) throws NotConnectedException {
        try {
            this.openClaimedTicketsCount_statement.clearParameters();
            this.openClaimedTicketsCount_statement.setString(1, whitelist);
            this.openClaimedTicketsCount_statement.setString(2, whitelist);
            this.openClaimedTicketsCount_statement.setString(3, blacklist);
            this.openClaimedTicketsCount_statement.setString(4, blacklist);
            ResultSet rs = this.openClaimedTicketsCount_statement.executeQuery();
            while (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException | NullPointerException e) {
//            e.printStackTrace();
            this.con = null;
            this.connect();
            throw new NotConnectedException();
        }
        return 0;
    }

    public void cleanDB() throws NotConnectedException {
        try {
            Statement statement = this.con.createStatement();
            statement.execute("DELETE from " +
                    this.table + " WHERE " +
                    this.answerC + " IS NOT NULL AND " +
                    this.readC + " IS NOT NULL;");
            statement.execute("SET @num := 0;");
            statement.execute("UPDATE " +
                    this.table + " SET " +
                    this.IDC + " = @num := (@num+1);");
            statement.execute("ALTER TABLE " +
                    this.table + " AUTO_INCREMENT=1;");
            statement.close();
        } catch (SQLException | NullPointerException e) {
            this.con = null;
            this.connect();
            throw new NotConnectedException();
        }
    }
}
