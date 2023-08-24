package com.github.Emcc13.TicketsProxy.ServerMessages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerMessage {
    public enum MessageTopic {
        ticketsNum,
        ticketsClaim,
        ticketsUnclaim,
        ticketsClose,
        ticketsTP,
        ticketsPage,
        ticketsPlayerPage,

        ticketList,
        ticketNew,
        ticketRead,

        tpPos,

        ticketsOpen,
        ticketsUnread,

        ticketNotify,
    }

    private MessageTopic topic;
    private String player;

    private String server;
    private String world;
    private Double posX, posY, posZ;
    private Float pitch, yaw;
    private String text;

    private Integer number;

    private String ticketType = "";

    private String messageString;

    public ServerMessage(){
    }

    public static ServerMessage fromBytes(byte[] bytes){
        java.io.ByteArrayInputStream stream = new java.io.ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(stream);
        ServerMessage result = new ServerMessage();
        try {
            result.topic = MessageTopic.valueOf(in.readUTF());
            result.player = in.readUTF();

            switch (result.topic) {
                case ticketsPage:
                    result.number = in.readInt();
                    result.ticketType = in.readUTF();
                    break;

                case ticketsNum:
                case ticketList:
                case ticketsClaim:
                case ticketsUnclaim:
                case ticketsTP:
                case ticketRead:
                    result.number = in.readInt();
                    break;

                case ticketsPlayerPage:
                    result.text = in.readUTF();
                    result.number = in.readInt();
                    result.ticketType = in.readUTF();
                    break;

                case ticketsClose:
                    result.number = in.readInt();
                    result.text = in.readUTF();
                    break;

                case ticketNew:
                    result.text = in.readUTF();
                    result.server = in.readUTF();
                    result.world = in.readUTF();
                    result.posX = in.readDouble();
                    result.posY = in.readDouble();
                    result.posZ = in.readDouble();
                    result.pitch = in.readFloat();
                    result.yaw = in.readFloat();
                    result.ticketType = in.readUTF();
                    break;
                case tpPos:
                    result.world = in.readUTF();
                    result.posX = in.readDouble();
                    result.posY = in.readDouble();
                    result.posZ = in.readDouble();
                    result.pitch = in.readFloat();
                    result.yaw = in.readFloat();
                case ticketNotify:
                    result.messageString = in.readUTF();
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
        }
        return result;
    }

    public static ServerMessage forTicketsNum(String playerName, int number){
        ServerMessage result = new ServerMessage();
        result.topic = MessageTopic.ticketsNum;
        result.player = playerName;
        result.number = number;
        return result;
    }

    public static ServerMessage forTicketsClaim(String playerName, int ticketID){
        ServerMessage result = new ServerMessage();
        result.topic = MessageTopic.ticketsClaim;
        result.player = playerName;
        result.number = ticketID;
        return result;
    }

    public static ServerMessage forTicketsUnclaim(String playerName, int ticketID){
        ServerMessage result = new ServerMessage();
        result.topic = MessageTopic.ticketsUnclaim;
        result.player = playerName;
        result.number = ticketID;
        return result;
    }

    public static ServerMessage forTicketsClose(String playerName, int ticketsID, String... closeText){
        ServerMessage result = new ServerMessage();
        result.topic = MessageTopic.ticketsClose;
        result.player = playerName;
        result.number = ticketsID;
        result.text = String.join(" ", closeText);
        return result;
    }

    public static ServerMessage forTicketsTP(String playerName,int ticketID){
        ServerMessage result = new ServerMessage();
        result.topic = MessageTopic.ticketsTP;
        result.player = playerName;
        result.number = ticketID;
        return result;
    }

    public static ServerMessage forTicketsPage(String playerName, String ticketType, int pageIdx){
        ServerMessage result = new ServerMessage();
        result.topic = MessageTopic.ticketsPage;
        result.player = playerName;
        result.ticketType = ticketType;
        result.number = pageIdx;
        return result;
    }

    public static ServerMessage forTicketsPlayerPage(String playerName, int ticketID, String ticketType, String... text){
        ServerMessage result = new ServerMessage();
        result.topic = MessageTopic.ticketsPlayerPage;
        result.number = ticketID;
        result.player = playerName;
        result.ticketType = ticketType;
        result.text = String.join(" ", text);
        return result;
    }

    public static ServerMessage forTicketList(String playerName, int ticketID){
        ServerMessage result = new ServerMessage();
        result.topic = MessageTopic.ticketList;
        result.player = playerName;
        result.number = ticketID;
        return result;
    }

    public static ServerMessage forTicketNew(String player, String server, String world, Double posX, Double posY, Double posZ,
                                             Float pitch, Float yaw, String ticketType, String... text) {
        ServerMessage result = new ServerMessage();
        result.topic = MessageTopic.ticketNew;
        result.player = player;
        result.server = server;
        result.world = world;
        result.posX = posX;
        result.posY = posY;
        result.posZ = posZ;
        result.pitch = pitch;
        result.yaw = yaw;
        result.text = String.join(" ", text);
        result.ticketType = ticketType;
        return result;
    }

    public static ServerMessage forTicketRead(String playerName, int ticketID){
        ServerMessage result = new ServerMessage();
        result.topic = MessageTopic.ticketRead;
        result.player = playerName;
        result.number = ticketID;
        return result;
    }

    public static ServerMessage forTpPos(String playerName, String world,
                                         Double posX, Double posY, Double posZ,
                                         Float pitch, Float yaw){
        ServerMessage result = new ServerMessage();
        result.topic = MessageTopic.tpPos;
        result.player = playerName;
        result.world = world;
        result.posX = posX;
        result.posY = posY;
        result.posZ = posZ;
        result.pitch = pitch;
        result.yaw = yaw;
        return result;
    }

    public static ServerMessage forTicketsOpen(String playerName){
        ServerMessage result = new ServerMessage();
        result.topic = MessageTopic.ticketsOpen;
        result.player = playerName;
        return result;
    }

    public static ServerMessage forTicketsUnread(String playerName){
        ServerMessage result = new ServerMessage();
        result.topic = MessageTopic.ticketsUnread;
        result.player = playerName;
        return result;
    }

    public static ServerMessage forTicketNotify(String message){
        ServerMessage result = new ServerMessage();
        result.topic = MessageTopic.ticketNotify;
        result.player = " ";
        result.messageString = message;
        return result;
    }

    public byte[] toMessagae() {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF(this.topic.name());
            out.writeUTF(this.player);

            switch (this.topic) {
                case ticketsPage:
                    out.writeInt(this.number);
                    out.writeUTF(this.ticketType);
                    break;

                case ticketsNum:
                case ticketsClaim:
                case ticketsUnclaim:
                case ticketsTP:
                case ticketList:
                case ticketRead:
                    out.writeInt(this.number);
                    break;

                case ticketsPlayerPage:
                    out.writeUTF(this.text);
                    out.writeInt(this.number);
                    out.writeUTF(this.ticketType);
                    break;

                case ticketsClose:
                    out.writeInt(this.number);
                    out.writeUTF(this.text);
                    break;

                case ticketNew:
                    out.writeUTF(this.text);
                    out.writeUTF(this.server);
                    out.writeUTF(this.world);
                    out.writeDouble(this.posX);
                    out.writeDouble(this.posY);
                    out.writeDouble(this.posZ);
                    out.writeFloat(this.pitch);
                    out.writeFloat(this.yaw);
                    out.writeUTF(this.ticketType);
                    break;
                case tpPos:
                    out.writeUTF(this.world);
                    out.writeDouble(this.posX);
                    out.writeDouble(this.posY);
                    out.writeDouble(this.posZ);
                    out.writeFloat(this.pitch);
                    out.writeFloat(this.yaw);
                    break;
                case ticketNotify:
                    out.writeUTF(this.messageString);
                    break;
                default:
                    break;
            }
            out.flush();
        } catch (IOException e) {
            return new byte[0];
        }

        return b.toByteArray();
    }

    public MessageTopic getTopic() {
        return topic;
    }

    public String getPlayer() {
        return player;
    }

    public String getServer() {
        return server;
    }

    public String getWorld() {
        return world;
    }

    public Double getPosX() {
        return posX;
    }

    public Double getPosY() {
        return posY;
    }

    public Double getPosZ() {
        return posZ;
    }

    public Float getPitch() {
        return pitch;
    }

    public Float getYaw() {
        return yaw;
    }

    public String getText() {
        return text;
    }

    public Integer getNumber() {
        return number;
    }

    public String getTicketType() {
        return ticketType;
    }

    public String getMessage() {
        return messageString;
    }
}

