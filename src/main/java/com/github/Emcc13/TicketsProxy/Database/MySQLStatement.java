package com.github.Emcc13.TicketsProxy.Database;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MySQLStatement {
    public enum StatementType {
        addTicket,
        claimTicket,
        unclaimTicket,
        readTicket,
        closeTicket,
    }

    private StatementType type;
    private String player;
    private long timeStamp;
    private int id;
    private String text;
    private String ticketType;
    private String server;
    private String world;
    private Double locX;
    private Double locY;
    private Double locZ;
    private Float locAzimuth;
    private Float locElevation;
    private boolean catchSpam;

    public MySQLStatement() {
    }

    public MySQLStatement(byte[] bytes) {
        java.io.ByteArrayInputStream stream = new java.io.ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(stream);
        try {
            type = StatementType.valueOf(in.readUTF());
            player = in.readUTF();
            switch (type) {
                case addTicket:
                    timeStamp = in.readLong();
                    text = in.readUTF();
                    ticketType = in.readUTF();
                    server = in.readUTF();
                    world = in.readUTF();
                    locX = in.readDouble();
                    locY = in.readDouble();
                    locZ = in.readDouble();
                    locAzimuth = in.readFloat();
                    locElevation = in.readFloat();
                    catchSpam = in.readBoolean();
                    break;
                case claimTicket:
                case readTicket:
                    timeStamp = in.readLong();
                    id = in.readInt();
                    break;
                case unclaimTicket:
                    id = in.readInt();
                    break;
                case closeTicket:
                    timeStamp = in.readLong();
                    id = in.readInt();
                    text = in.readUTF();
                    break;
            }
        } catch (IOException e) {
        }
    }

    public byte[] serialize() {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF(type.name());
            out.writeUTF(player);
            switch (type) {
                case addTicket:
                    out.writeLong(timeStamp);
                    out.writeUTF(text);
                    out.writeUTF(ticketType);
                    out.writeUTF(server);
                    out.writeUTF(world);
                    out.writeDouble(locX);
                    out.writeDouble(locY);
                    out.writeDouble(locZ);
                    out.writeFloat(locAzimuth);
                    out.writeFloat(locElevation);
                    out.writeBoolean(catchSpam);
                    break;
                case claimTicket:
                case readTicket:
                    out.writeLong(timeStamp);
                    out.writeInt(id);
                    break;
                case unclaimTicket:
                    out.writeInt(id);
                    break;
                case closeTicket:
                    out.writeLong(timeStamp);
                    out.writeInt(id);
                    out.writeUTF(text);
                    break;
                default:
                    return new byte[0];
            }
            out.flush();
        } catch (IOException e) {
            return new byte[0];
        }
        return b.toByteArray();
    }

    public static MySQLStatement addTicketStatement(String player, String ticketType, String text, String server,
                                                    String world, Double locX, Double locY, Double locZ,
                                                    Float locAzimuth, Float locElevation, long timeStamp,
                                                    boolean catchSpam) {
        MySQLStatement result = new MySQLStatement();
        result.type = StatementType.addTicket;
        result.player = player;
        result.ticketType = ticketType;
        result.server = server;
        result.world = world;
        result.text = text;
        result.locX = locX;
        result.locY = locY;
        result.locZ = locZ;
        result.locAzimuth = locAzimuth;
        result.locElevation = locElevation;
        result.timeStamp = timeStamp;
        result.catchSpam = catchSpam;
        return result;
    }

    public static MySQLStatement claimTicketStatement(int id, String player, long timeStamp) {
        MySQLStatement result = new MySQLStatement();
        result.type = StatementType.claimTicket;
        result.id = id;
        result.player = player;
        result.timeStamp = timeStamp;
        return result;
    }

    public static MySQLStatement unclaimTicketStatement(int id, String player) {
        MySQLStatement result = new MySQLStatement();
        result.type = StatementType.unclaimTicket;
        result.id = id;
        result.player = player;
        return result;
    }

    public static MySQLStatement readTicketStatement(int id, String player, long timeStamp) {
        MySQLStatement result = new MySQLStatement();
        result.type = StatementType.readTicket;
        result.id = id;
        result.player = player;
        result.timeStamp = timeStamp;
        return result;
    }

    public static MySQLStatement closeTicketStatement(int id, String player, String text, long timeStamp) {
        MySQLStatement result = new MySQLStatement();
        result.type = StatementType.closeTicket;
        result.id = id;
        result.player = player;
        result.text = text;
        result.timeStamp = timeStamp;
        return result;
    }

    public void execute(DBInterface database) {
        try {
            switch (type) {
                case addTicket:
                    database.addTicket(player, ticketType, text, server, world,
                            locX, locY, locZ, locAzimuth, locElevation, timeStamp, catchSpam);
                    break;
                case claimTicket:
                    database.claimTicket(id, player, timeStamp);
                    break;
                case unclaimTicket:
                    database.unclaimTicket(id, player);
                    break;
                case readTicket:
                    database.readTicket(id, player, timeStamp);
                    break;
                case closeTicket:
                    database.closeTicket(id, player, text, timeStamp);
                    break;
                default:
                    return;
            }
        } catch (NotConnectedException e) {
        }
    }
}
