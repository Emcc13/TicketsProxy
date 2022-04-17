package com.github.Emcc13.TicketsProxy.ServerMessages;

import com.github.Emcc13.TicketsProxy.Util.CustomTextComponentSerializer;
import com.google.gson.*;
import net.md_5.bungee.api.chat.TextComponent;

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

    private String ticketType;

    private TextComponent textComponent;

    public ServerMessage(byte[] bytes) {
        java.io.ByteArrayInputStream stream = new java.io.ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(stream);
        try {
            topic = MessageTopic.valueOf(in.readUTF());
            this.player = in.readUTF();

            switch (this.topic) {
                case ticketsPage:
                    this.number = in.readInt();
                    this.ticketType = in.readUTF();
                    break;

                case ticketsNum:
                case ticketsClaim:
                case ticketsUnclaim:
                case ticketsTP:
                case ticketList:
                case ticketRead:
                    this.number = in.readInt();
                    break;

                case ticketsPlayerPage:
                    this.text = in.readUTF();
                    this.number = in.readInt();
                    this.ticketType = in.readUTF();
                    break;

                case ticketsClose:
                    this.number = in.readInt();
                    this.text = in.readUTF();
                    break;

                case ticketNew:
                    this.text = in.readUTF();
                    this.server = in.readUTF();
                    this.world = in.readUTF();
                    this.posX = in.readDouble();
                    this.posY = in.readDouble();
                    this.posZ = in.readDouble();
                    this.pitch = in.readFloat();
                    this.yaw = in.readFloat();
                    this.ticketType = in.readUTF();
                    break;
                case tpPos:
                    this.world = in.readUTF();
                    this.posX = in.readDouble();
                    this.posY = in.readDouble();
                    this.posZ = in.readDouble();
                    this.pitch = in.readFloat();
                    this.yaw = in.readFloat();
                case ticketNotify:
                    JsonParser parser = new JsonParser();
                    this.textComponent = CustomTextComponentSerializer.
                            deserialize((JsonObject) parser.parse(in.readUTF()));
                    break;
                default:
                    break;
            }
        } catch (IOException e) {

        }
    }

    public ServerMessage(MessageTopic mt, String player, int ticketID, String... text) {
        this(mt, player, ticketID);
        this.text = String.join(" ", text);
    }

    public ServerMessage(String player, String server, String world, Double posX, Double posY, Double posZ,
                         Float pitch, Float yaw, String ticketType, String... text) {
        this.topic = MessageTopic.ticketNew;
        this.player = player;
        this.server = server;
        this.world = world;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.pitch = pitch;
        this.yaw = yaw;
        this.text = String.join(" ", text);
        this.ticketType = ticketType;
    }

    public ServerMessage(MessageTopic mt, ServerMessage sm) {
        this.topic = mt;
        this.player = sm.player;
        this.server = sm.server;
        this.world = sm.world;
        this.posX = sm.posX;
        this.posY = sm.posY;
        this.posZ = sm.posZ;
        this.pitch = sm.pitch;
        this.yaw = sm.yaw;
        this.text = sm.text;
        this.number = sm.number;
    }

    public ServerMessage(String player, String world, Double posX, Double posY, Double posZ,
                         Float pitch, Float yaw) {
        this(MessageTopic.tpPos, player);
        this.world = world;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public ServerMessage(MessageTopic mt, String player, int ticketID) {
        this(mt, player);
        this.number = ticketID;
    }

    public ServerMessage(MessageTopic mt, String player) {
        this.topic = mt;
        this.player = player;
    }

    public ServerMessage(MessageTopic mt, TextComponent textComponent) {
        this.topic = mt;
        this.player = " ";
        this.textComponent = textComponent;
    }

    public ServerMessage(MessageTopic mt, String player, TextComponent textComponent) {
        this.topic = mt;
        this.player = player;
        this.textComponent = textComponent;
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
                    Gson gson = new Gson();
                    out.writeUTF(gson.toJson(CustomTextComponentSerializer.serialize(this.textComponent)));
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

    public TextComponent getTextComponent() {
        return textComponent;
    }
}
