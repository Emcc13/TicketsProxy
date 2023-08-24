package com.github.Emcc13.TicketsProxy.Config;

import com.github.Emcc13.TicketsProxy.ProxyTickets;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public abstract class ConfigManager {
    public static final String CHANNEL_KEY = "channel";
    private static final String CHANNEK_DEFAULT = "channel_name";

    public static final String DATABASE_HOST_KEY = "db.host";
    private static final String DATABASE_HOST_DEFAULT = "127.0.0.1";

    public static final String DATABASE_PORT_KEY = "db.port";
    private static final String DATABASE_PORT_DEFAULT = "3306";

    public static final String DATABASE_DATABASE_KEY = "db.database";
    private static final String DATABASE_DATABASE_DEFAULT = "minecraft";

    public static final String DATABASE_TABLE_KEY = "db.table";
    private static final String DATABASE_TABLE_DEFAULT = "tickets";

    public static final String DATABASE_USERNAME_KEY = "db.username";
    private static final String DATABASE_USERNAME_DEFAULT = "root";

    public static final String DATABASE_PASSWORD_KEY = "db.password";
    private static final String DATABASE_PASSWORD_DEFAULT = "123456";

    public static final String TICKET_NOTIFY_KEY = "ticket_type_notify";
    private static final String TICKET_NOTIFY_DEFAULT = "p";

    public static final String TICKET_IGNORE_KEY = "ticket_count_ignore";
    private static final String TICKET_IGNORE_DEFAULT = "h";

    public static final String DB_FAILURE_MESSAGE_KEY = "db.failure_message";
    private static final String DB_FAILURE_MESSAGE_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("There is currently no connection to the database please try again later or ask a team member.").color(TextColor.color(0xaa, 0x00, 0x00))
    );

    public static final String COORDINATE_FORMAT_KEY = "coordinate_format";
    private static final String COORDINATE_FORMAT_DEFAULT = "%.1f";

    public static final String TICKETS_OPEN_FORMAT_KEY = "tickets.format.open";
    private static final String TICKETS_OPEN_FORMAT_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("[").color(TextColor.color(0x55, 0xff, 0x55))
                    .append(Component.text("%ID%").color(TextColor.color(0xff, 0x55, 0x55)).clickEvent(ClickEvent.runCommand("/tickets tp %ID%")))
                    .append(Component.text("]").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text(" [%TICKETTYPE%] ").color(TextColor.color(0x55, 0x55, 0x55)))
                    .append(Component.text("OPEN")
                            .color(TextColor.color(0x55, 0xff, 0x55))
                            .style(Style.style().decorate(TextDecoration.BOLD)))
                    .append(Component.text(" - ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%PLAYER% (%SERVER%, %WORLD%, %X%, %Y%, %Z%)").color(TextColor.color(0xff, 0xff, 0x55)))
                    .append(Component.text(": ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%REQUEST% - ").color(TextColor.color(0xaa, 0xaa, 0xaa)))
                    .append(Component.text("claim").color(TextColor.color(0xaa, 0x00, 0xaa))
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to claim ticket").color(TextColor.color(0xff, 0xaa, 0x00))))
                                    .clickEvent(ClickEvent.runCommand("/tickets claim %ID%"))
                    )
    );
    public static final String TICKETS_CLAIMED_FORMAT_KEY = "tickets.format.claimed";
    private static final String TICKETS_CLAIMED_FORMAT_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("[").color(TextColor.color(0x55, 0xff, 0x55))
                    .append(Component.text("ID").color(TextColor.color(0xff, 0x55, 0x55)).clickEvent(ClickEvent.runCommand("/tickets tp %ID%")))
                    .append(Component.text("]").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text(" [%TICKETTYPE%] ").color(TextColor.color(0x55, 0x55, 0x55)))
                    .append(Component.text("CLAIMED by: %CLAIMER%").color(TextColor.color(0xff, 0xaa, 0x00)).style(Style.style().decorate(TextDecoration.BOLD)))
                    .append(Component.text(" - ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%PLAYER% (%SERVER%, %WORLD%, %X%, %Y%, %Z%)").color(TextColor.color(0xff, 0xff, 0x55)))
                    .append(Component.text(": ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%REQUEST%").color(TextColor.color(0xaa, 0xaa, 0xaa)))
    );
    public static final String TICKETS_CLOSED_FORMAT_KEY = "tickets.format.closed";
    private static final String TICKETS_CLOSED_FORMAT_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("[").color(TextColor.color(0x55, 0xff, 0x55))
                    .append(Component.text("ID").color(TextColor.color(0xff, 0x55, 0x55)).clickEvent(ClickEvent.runCommand("/tickets tp %ID%")))
                    .append(Component.text("]").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text(" [%TICKETTYPE%] ").color(TextColor.color(0x55, 0x55, 0x55)))
                    .append(Component.text("CLOSED").color(TextColor.color(0xff, 0x55, 0x55)).style(Style.style().decorate(TextDecoration.BOLD)))
                    .append(Component.text(" - ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%PLAYER% (%SERVER%, %WORLD%, %X%, %Y%, %Z%)").color(TextColor.color(0xff, 0xff, 0x55)))
                    .append(Component.text(": ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%REQUEST%").color(TextColor.color(0xaa, 0xaa, 0xaa)))
                    .append(Component.text(" | ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%RESPONDER%").color(TextColor.color(0xff, 0xaa, 0x00)))
                    .append(Component.text(": ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%ANSWER%").color(TextColor.color(0xaa, 0xaa, 0xaa)))
    );
    public static final String TICKETS_READ_FORMAT_KEY = "tickets.format.read";
    private static final String TICKETS_READ_FORMAT_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("[").color(TextColor.color(0x55, 0xff, 0x55))
                    .append(Component.text("%ID%").color(TextColor.color(0xff, 0x55, 0x55)).clickEvent(ClickEvent.runCommand("/tickets tp %ID%")))
                    .append(Component.text("]").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text(" [%TICKETTYPE%] ").color(TextColor.color(0x55, 0x55, 0x55)))
                    .append(Component.text("READ").color(TextColor.color(0x00, 0xaa, 0x00)).style(Style.style().decorate(TextDecoration.BOLD)))
                    .append(Component.text(" - ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%PLAYER% (%SERVER%, %WORLD%, %X%, %Y%, %Z%)").color(TextColor.color(0xff, 0xff, 0x55)))
                    .append(Component.text(": ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%REQUEST%").color(TextColor.color(0xaa, 0xaa, 0xaa)))
                    .append(Component.text(" | ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%RESPONDER%").color(TextColor.color(0xff, 0xaa, 0x00)))
                    .append(Component.text(": ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%ANSWER%").color(TextColor.color(0xaa, 0xaa, 0xaa)))
    );
    public static final String TICKET_OPEN_FORMAT_KEY = "ticket.format.open";
    private static final String TICKET_OPEN_FORMAT_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("[").color(TextColor.color(0x55, 0xff, 0x55))
                    .append(Component.text("%ID%").color(TextColor.color(0xff, 0x55, 0x55)))
                    .append(Component.text("]").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text(" [%TICKETTYPE%] ").color(TextColor.color(0x55, 0x55, 0x55)))
                    .append(Component.text("OPEN").color(TextColor.color(0x55, 0xff, 0x55)).style(Style.style().decorate(TextDecoration.BOLD)))
                    .append(Component.text(" - ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%PLAYER% (%SERVER%, %WORLD%, %X%, %Y%, %Z%)").color(TextColor.color(0xff, 0xff, 0x55)))
                    .append(Component.text(": ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%REQUEST%").color(TextColor.color(0xaa, 0xaa, 0xaa)))
    );
    public static final String TICKET_UNREAD_FORMAT_KEY = "ticket.format.unread";
    private static final String TICKET_UNREAD_FORMAT_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("[").color(TextColor.color(0x55, 0xff, 0x55))
                    .append(Component.text("%ID%").color(TextColor.color(0xff, 0x55, 0x55)))
                    .append(Component.text("]").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text(" [%TICKETTYPE%] ").color(TextColor.color(0x55, 0x55, 0x55)))
                    .append(Component.text("UNREAD").color(TextColor.color(0xff, 0x55, 0x55)))
                    .append(Component.text(" - ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%PLAYER% (%SERVER%, %WORLD%, %X%, %Y%, %Z%)").color(TextColor.color(0xff, 0xff, 0x55)))
                    .append(Component.text(": ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%REQUEST%").color(TextColor.color(0xaa, 0xaa, 0xaa)))
                    .append(Component.text(" | ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%RESPONDER%").color(TextColor.color(0xff, 0xaa, 0x00)))
                    .append(Component.text(": ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%ANSWER%; ").color(TextColor.color(0xaa, 0xaa, 0xaa)))
                    .append(Component.text("READ")
                            .color(TextColor.color(0xaa, 0x00, 0x00))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to mark as read").color(TextColor.color(0x55, 0xff, 0x55))))
                            .clickEvent(ClickEvent.runCommand("/ticket read %ID%")))
    );

    public static final String TICKET_FORMAT_TICKETTYPE_KEY = "ticket.format.tickettype";
    private static final Map<String, String> TICKET_FORMAT_TICKETTYPE_DEFAULT = new HashMap<String, String>() {{
        put("p", "p");
        put("c", "c");
        put("h", "h");
    }};

    public static final String TICKETS_PER_PAGE_KEY = "tickets.page.limit";
    private static final Integer TICKETS_PER_PAGE_DEFAULT = 5;
    public static final String TICKETS_PAGE_INFO_KEY = "tickets.page.header";
    private static final String TICKETS_PAGE_INFO_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("Page %PAGE%/%PAGES% | ").append(
                    Component.text("previous").color(TextColor.color(0xaa, 0x00, 0x00))
                            .hoverEvent(HoverEvent.showText(Component.text("Click for previous page").color(TextColor.color(0x55, 0xff, 0x55))))
                            .clickEvent(ClickEvent.runCommand("/tickets page %TICKETTYPE% %PREVPAGE%"))
            ).append(
                    Component.text(" - ")
            ).append(
                    Component.text("next").color(TextColor.color(0x00, 0xaa, 0x00))
                            .hoverEvent(HoverEvent.showText(Component.text("Click for next page").color(TextColor.color(0x55, 0xff, 0x55))))
                            .clickEvent(ClickEvent.runCommand("/tickets page %TICKETTYPE% %NEXTPAGE%"))
            ));
    public static final String TICKETS_PLAYER_PAGE_INFO_KEY = "tickets.PlayerPage.header";
    private static final String TICKETS_PLAYER_PAGE_INFO_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("Page %PAGE%/%PAGES% | ").append(
                    Component.text("previous").color(TextColor.color(0xaa, 0x00, 0x00))
                            .hoverEvent(HoverEvent.showText(Component.text("Click for previous page").color(TextColor.color(0x55, 0xff, 0x55))))
                            .clickEvent(ClickEvent.runCommand("/tickets %PLAYER% %TICKETTYPE% %PREVPAGE%"))
            ).append(
                    Component.text(" - ")
            ).append(
                    Component.text("next").color(TextColor.color(0x00, 0xaa, 0x00))
                            .hoverEvent(HoverEvent.showText(Component.text("Click for next page").color(TextColor.color(0x55, 0xff, 0x55))))
                            .clickEvent(ClickEvent.runCommand("/tickets %PLAYER% %TICKETTYPE% %NEXTPAGE%"))
            ));
    public static final String TICKET_PAGE_INFO_KEY = "ticket.page.info";
    private static final String TICKET_PAGE_INFO_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("Page %PAGE%/%PAGES% | ").append(
                    Component.text("previous").color(TextColor.color(0xaa, 0x00, 0x00))
                            .hoverEvent(HoverEvent.showText(Component.text("Click for previous page").color(TextColor.color(0x55, 0xff, 0x55))))
                            .clickEvent(ClickEvent.runCommand("/ticket list %PREVPAGE%"))
            ).append(
                    Component.text(" - ")
            ).append(
                    Component.text("next").color(TextColor.color(0x00, 0xaa, 0x00))
                            .hoverEvent(HoverEvent.showText(Component.text("Click for next page").color(TextColor.color(0x55, 0xff, 0x55))))
                            .clickEvent(ClickEvent.runCommand("/ticket list %NEXTPAGE%"))
            ));

    public static final String TICKETS_INFO_CREATE_KEY = "tickets.info.create";
    private static final String TICKET_CREATE_INFO_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("%PLAYER%").color(TextColor.color(0xff, 0x55, 0x55))
                    .hoverEvent(HoverEvent.showText(Component.text("Show all tickets by this player.").color(TextColor.color(0x55, 0xff, 0x55))))
                    .clickEvent(ClickEvent.runCommand("/tickets %PLAYER%")).append(
                            Component.text(" has created ticket ").color(TextColor.color(0x55, 0xff, 0x55))
                    ).append(
                            Component.text("#%ID%").color(TextColor.color(0xff, 0x55, 0x55))
                                    .append(Component.text(": ").color(TextColor.color(0x55, 0xff, 0x55))
                                    .append(Component.text("%REQUEST%").color(TextColor.color(0x77, 0x77, 0x77)))
                                    .hoverEvent(HoverEvent.showText(Component.text("Show this ticket.").color(TextColor.color(0x55, 0xff, 0x55))))
                                    .clickEvent(ClickEvent.runCommand("/tickets %ID%")))
                    )
    );
    public static final String TICKET_INFO_CREATE_KEY = "ticket.info.create";
    private static final String TICKET_CREATE_THINK_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("Thank you for your ticket ").color(TextColor.color(0x55, 0xff, 0x55))
                    .append(Component.text("%ID%").color(TextColor.color(0xff, 0x55, 0x55)))
                    .append(Component.text(". A team member will have a look at your request soon.").color(TextColor.color(0x55, 0xff, 0x55)))
    );
    public static final String TICKETS_INFO_CLAIM_KEY = "tickets.info.claim";
    private static final String TICKETS_CLAIM_INFO_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("%PLAYER%").color(TextColor.color(0xff, 0xaa, 0x00))
                    .append(Component.text(" has ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("claimed").color(TextColor.color(0xff, 0x55, 0x55)))
                    .append(Component.text(" ticket ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%ID%").color(TextColor.color(0xff, 0x55, 0x55)))
                    .append(Component.text(".").color(TextColor.color(0x55, 0xff, 0x55)))
    );
    public static final String TICKETS_INFO_UNCLAIM_KEY = "tickets.info.unclaim";
    private static final String TICKETS_UNCLAIM_INFO_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("%PLAYER%").color(TextColor.color(0xff, 0xaa, 0x00))
                    .append(Component.text(" has ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("unclaimed").color(TextColor.color(0xff, 0x55, 0x55)))
                    .append(Component.text(" ticket ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%ID%").color(TextColor.color(0xff, 0x55, 0x55)))
                    .append(Component.text(".").color(TextColor.color(0x55, 0xff, 0x55)))
    );
    public static final String TICKETS_INFO_CLOSE_KEY = "tickets.info.close";
    private static final String TICKETS_CLOSE_INFO_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("%PLAYER%").color(TextColor.color(0xff, 0xaa, 0x00))
                    .append(Component.text(" has ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("closed").color(TextColor.color(0xff, 0x55, 0x55)))
                    .append(Component.text(" ticket ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%ID%").color(TextColor.color(0xff, 0x55, 0x55)))
                    .append(Component.text(": ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%ANSWER%").color(TextColor.color(0xaa, 0xaa, 0xaa)))
    );
    public static final String TICKETS_INFO_CLOSE_FAIL_KEY = "tickets.info.closeFail";
    private static final String TICKETS_INFO_CLOSE_FAIL_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("The ticket ").color(TextColor.color(0xff, 0xaa, 0x00))
                    .append(Component.text("%ID%").color(TextColor.color(0xff, 0x55, 0x55)))
                    .append(Component.text(" does not exist or has already been closed.").color(TextColor.color(0xff, 0xaa, 0x00)))
    );
    public static final String TICKET_INFO_CLOSE_KEY = "ticket.info.close";
    private static final String TICKET_CLOSE_PLAYER_INFO_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("Your ticket ").color(TextColor.color(0x55, 0xff, 0x55))
                    .append(Component.text("%ID%").color(TextColor.color(0xff, 0x55, 0x55)))
                    .append(Component.text(" has been closed from ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%PLAYER%").color(TextColor.color(0xff, 0xff, 0x55)))
                    .append(Component.text(": ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%ANSWER%").color(TextColor.color(0xaa, 0xaa, 0xaa)))
    );
    public static final String TICKETS_INFO_SPAM_KEY = "tickets.info.spam";
    private static final String TICKETS_SPAM_INFO_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("%PLAYER%").color(TextColor.color(0xff, 0xff, 0x55))
                    .append(Component.text(" tried to issue redundant ticket (").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%ID%").color(TextColor.color(0xff, 0xff, 0x55)))
                    .append(Component.text("): ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%REQUEST%").color(TextColor.color(0xaa, 0xaa, 0xaa)))
    );
    public static final String TICKET_SPAM_DISTANCE_KEY = "ticket.spam_distance";
    private static final String TICKET_SPAM_DISTANCE_DEFAULT = "5";
    public static final String TICKETS_INFO_READ = "tickets.info.read";
    private static final String TICKETS_READ_INFO_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("%PLAYER%").color(TextColor.color(0xff, 0xff, 0x55))
                    .append(Component.text(" has red the response to ticket ").color(TextColor.color(0x55, 0xff, 0x55)))
                    .append(Component.text("%ID%").color(TextColor.color(0xff, 0xff, 0x55)))
                    .append(Component.text(".").color(TextColor.color(0x55, 0xff, 0x55)))
//            Component.text("&e%PLAYER%<#55ff55> has read the response to ticket &e%ID%<#55ff55>.")
    );
    public static final String TICKET_INFO_READ_KEY = "ticket.info.read";
    private static final String TICKET_READ_THANK_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("Thank you for reading the teams response.").color(TextColor.color(0x55, 0xff, 0x55))
    );

    public static final String NO_TICKET_WITH_ID_KEY = "no_ticket_with_id";
    private static final String NO_TICKET_WITH_ID_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("There is no ticket with ID: ").color(TextColor.color(0x55, 0xff, 0x55))
                    .append(Component.text("%ID%").color(TextColor.color(0xff, 0xff, 0x55)))
                    .append(Component.text(".").color(TextColor.color(0x55, 0xff, 0x55)))
    );

    public static final String JOIN_OPEN_TICKET_KEY = "join.ticket.open";
    private static final String JOIN_OPEN_TICKET_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("There are currently ").color(TextColor.color(0x55, 0xff, 0x55))
                    .append(Component.text("%NUM%").color(TextColor.color(0xff, 0xff, 0x55)))
                    .append(Component.text(" open tickets.").color(TextColor.color(0x55, 0xff, 0x55)))
                            .hoverEvent(HoverEvent.showText(Component.text("<#55ff55>Show open tickets.")))
                                    .clickEvent(ClickEvent.runCommand("/tickets page 1"))
    );
    public static final String JOIN_UNRAED_TICKET_KEY = "join.ticket.unread";
    private static final String JOIN_UNREAD_TICKET_DEFAULT = MiniMessage.miniMessage().serialize(
            Component.text("You have ").color(TextColor.color(0x55, 0xff, 0x55))
                    .append(Component.text("%NUM%").color(TextColor.color(0xff, 0xff, 0x55)))
                    .append(Component.text(" unread tickets.").color(TextColor.color(0x55, 0xff, 0x55)))
            .append(Component.text("/ticket list").color(TextColor.color(0xff, 0x55, 0x55))
                            .hoverEvent(HoverEvent.showText(Component.text("See your current and unread tickets.").color(TextColor.color(0x55, 0xff, 0x55))))
                            .clickEvent(ClickEvent.suggestCommand("/ticket list"))
            ));

    public static final String TICKET_FORMAT_LINK_KEY = "ticket.format.link_regex";
    private static final String TICKET_FORMAT_LINK_DEFAULT =
            "(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})";

    public static ConfigurationNode loadFromFile(ProxyTickets plugin) {
        Path directory = plugin.getDirectory();
        YAMLConfigurationLoader config_loader = YAMLConfigurationLoader.builder()
                .setPath((new File(directory.toFile(), "config.yml")).toPath())
                .build();
        ConfigurationNode root = null;
        try {
            root = config_loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    public static void saveToFile(ProxyTickets plugin, ConfigurationNode root) {
        Path directory = plugin.getDirectory();
        YAMLConfigurationLoader config_loader = YAMLConfigurationLoader.builder()
                .setPath((new File(directory.toFile(), "config.yml")).toPath())
                .build();
        try {
            config_loader.save(root);
        } catch (IOException e) {
            plugin.getLogger().error("Failed to save config!");
            e.printStackTrace();
        }
    }

    public static ConfigurationNode getSubNode(ConfigurationNode parent, String path){
        ConfigurationNode result = parent;
        for (String subNodeName : path.split("\\.")){
            result = result.getNode(subNodeName);
        }
        return result;
    }

    public static Map<String, Object> getConfig(ProxyTickets plugin) {
        Map<String, Object> cachedConfig = new HashMap<>();
        ConfigurationNode config = ConfigManager.loadFromFile(plugin);

        cachedConfig.put(CHANNEL_KEY,
                getSubNode(config, CHANNEL_KEY).getString(CHANNEK_DEFAULT));

        cachedConfig.put(DATABASE_HOST_KEY,
                getSubNode(config, DATABASE_HOST_KEY).getString(DATABASE_HOST_DEFAULT));
        cachedConfig.put(DATABASE_PORT_KEY,
                getSubNode(config, DATABASE_PORT_KEY).getString(DATABASE_PORT_DEFAULT));
        cachedConfig.put(DATABASE_DATABASE_KEY,
                getSubNode(config, DATABASE_DATABASE_KEY).getString(DATABASE_DATABASE_DEFAULT));
        cachedConfig.put(DATABASE_TABLE_KEY,
                getSubNode(config, DATABASE_TABLE_KEY).getString(DATABASE_TABLE_DEFAULT));
        cachedConfig.put(DATABASE_USERNAME_KEY,
                getSubNode(config, DATABASE_USERNAME_KEY).getString(DATABASE_USERNAME_DEFAULT));
        cachedConfig.put(DATABASE_PASSWORD_KEY,
                getSubNode(config, DATABASE_PASSWORD_KEY).getString(DATABASE_PASSWORD_DEFAULT));

        cachedConfig.put(TICKET_NOTIFY_KEY,
                getSubNode(config, TICKET_NOTIFY_KEY).getString(TICKET_NOTIFY_DEFAULT));
        cachedConfig.put(TICKET_IGNORE_KEY,
                getSubNode(config, TICKET_IGNORE_KEY).getString(TICKET_IGNORE_DEFAULT));

        cachedConfig.put(DB_FAILURE_MESSAGE_KEY,
                getSubNode(config, DB_FAILURE_MESSAGE_KEY).getString(DB_FAILURE_MESSAGE_DEFAULT));

        cachedConfig.put(COORDINATE_FORMAT_KEY,
                getSubNode(config, COORDINATE_FORMAT_KEY).getString(COORDINATE_FORMAT_DEFAULT));

        cachedConfig.put(TICKETS_OPEN_FORMAT_KEY,
                getSubNode(config, TICKETS_OPEN_FORMAT_KEY).getString(TICKETS_OPEN_FORMAT_DEFAULT));
        cachedConfig.put(TICKETS_CLAIMED_FORMAT_KEY,
                getSubNode(config, TICKETS_CLAIMED_FORMAT_KEY).getString(TICKETS_CLAIMED_FORMAT_DEFAULT));
        cachedConfig.put(TICKETS_CLOSED_FORMAT_KEY,
                getSubNode(config, TICKETS_CLOSED_FORMAT_KEY).getString(TICKETS_CLOSED_FORMAT_DEFAULT));
        cachedConfig.put(TICKETS_READ_FORMAT_KEY,
                getSubNode(config, TICKETS_READ_FORMAT_KEY).getString(TICKETS_READ_FORMAT_DEFAULT));
        cachedConfig.put(TICKET_OPEN_FORMAT_KEY,
                getSubNode(config, TICKET_OPEN_FORMAT_KEY).getString(TICKET_OPEN_FORMAT_DEFAULT));
        cachedConfig.put(TICKET_UNREAD_FORMAT_KEY,
                getSubNode(config, TICKET_UNREAD_FORMAT_KEY).getString(TICKET_UNREAD_FORMAT_DEFAULT));

        cachedConfig.put(TICKET_FORMAT_TICKETTYPE_KEY,
                getSubNode(config, TICKET_FORMAT_TICKETTYPE_KEY).getValue(TICKET_FORMAT_TICKETTYPE_DEFAULT));

        cachedConfig.put(TICKETS_PER_PAGE_KEY,
                getSubNode(config, TICKETS_PER_PAGE_KEY).getInt(TICKETS_PER_PAGE_DEFAULT));
        cachedConfig.put(TICKETS_PAGE_INFO_KEY,
                getSubNode(config, TICKETS_PAGE_INFO_KEY).getString(TICKETS_PAGE_INFO_DEFAULT));
        cachedConfig.put(TICKETS_PLAYER_PAGE_INFO_KEY,
                getSubNode(config, TICKETS_PLAYER_PAGE_INFO_KEY).getString(TICKETS_PLAYER_PAGE_INFO_DEFAULT));
        cachedConfig.put(TICKET_PAGE_INFO_KEY,
                getSubNode(config, TICKET_PAGE_INFO_KEY).getString(TICKET_PAGE_INFO_DEFAULT));

        cachedConfig.put(TICKETS_INFO_CREATE_KEY,
                getSubNode(config, TICKETS_INFO_CREATE_KEY).getString(TICKET_CREATE_INFO_DEFAULT));
        cachedConfig.put(TICKET_INFO_CREATE_KEY,
                getSubNode(config, TICKET_INFO_CREATE_KEY).getString(TICKET_CREATE_THINK_DEFAULT));
        cachedConfig.put(TICKETS_INFO_CLAIM_KEY,
                getSubNode(config, TICKETS_INFO_CLAIM_KEY).getString(TICKETS_CLAIM_INFO_DEFAULT));
        cachedConfig.put(TICKETS_INFO_UNCLAIM_KEY,
                getSubNode(config, TICKETS_INFO_UNCLAIM_KEY).getString(TICKETS_UNCLAIM_INFO_DEFAULT));
        cachedConfig.put(TICKETS_INFO_CLOSE_KEY,
                getSubNode(config, TICKETS_INFO_CLOSE_KEY).getString(TICKETS_CLOSE_INFO_DEFAULT));
        cachedConfig.put(TICKETS_INFO_CLOSE_FAIL_KEY,
                getSubNode(config, TICKETS_INFO_CLOSE_FAIL_KEY).getString(TICKETS_INFO_CLOSE_FAIL_DEFAULT));
        cachedConfig.put(TICKET_INFO_CLOSE_KEY,
                getSubNode(config, TICKET_INFO_CLOSE_KEY).getString(TICKET_CLOSE_PLAYER_INFO_DEFAULT));
        cachedConfig.put(TICKETS_INFO_SPAM_KEY,
                getSubNode(config, TICKETS_INFO_SPAM_KEY).getString(TICKETS_SPAM_INFO_DEFAULT));
        cachedConfig.put(TICKET_SPAM_DISTANCE_KEY,
                getSubNode(config, TICKET_SPAM_DISTANCE_KEY).getString(TICKET_SPAM_DISTANCE_DEFAULT));
        cachedConfig.put(TICKETS_INFO_READ,
                getSubNode(config, TICKETS_INFO_READ).getString(TICKETS_READ_INFO_DEFAULT));
        cachedConfig.put(TICKET_INFO_READ_KEY,
                getSubNode(config, TICKET_INFO_READ_KEY).getString(TICKET_READ_THANK_DEFAULT));
        cachedConfig.put(NO_TICKET_WITH_ID_KEY,
                getSubNode(config, NO_TICKET_WITH_ID_KEY).getString(NO_TICKET_WITH_ID_DEFAULT));

        cachedConfig.put(JOIN_OPEN_TICKET_KEY,
                getSubNode(config, JOIN_OPEN_TICKET_KEY).getString(JOIN_OPEN_TICKET_DEFAULT));
        cachedConfig.put(JOIN_UNRAED_TICKET_KEY,
                getSubNode(config, JOIN_UNRAED_TICKET_KEY).getString(JOIN_UNREAD_TICKET_DEFAULT));

        cachedConfig.put(TICKET_FORMAT_LINK_KEY,
                getSubNode(config, TICKET_FORMAT_LINK_KEY).getString(TICKET_FORMAT_LINK_DEFAULT));

        for (Map.Entry<String, Object> entry : cachedConfig.entrySet()) {
            getSubNode(config, entry.getKey()).setValue(entry.getValue());
        }

        ConfigManager.saveToFile(plugin, config);
        Map<String, String> ticketformats;
        Object o = cachedConfig.get(TICKET_FORMAT_TICKETTYPE_KEY);
        if (!(o instanceof Map)) {
            plugin.getLogger().warn(TICKET_FORMAT_TICKETTYPE_KEY+" is not a mapping! Using default!");
            ticketformats = TICKET_FORMAT_TICKETTYPE_DEFAULT;
        } else
            ticketformats = (Map<String, String>) cachedConfig.get(TICKET_FORMAT_TICKETTYPE_KEY);
        cachedConfig.put(TICKET_FORMAT_TICKETTYPE_KEY, ticketformats);
        return cachedConfig;
    }
}
