package com.github.Emcc13.TicketsProxy.Config;

import com.github.Emcc13.TicketsProxy.ProxyTickets;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class ConfigManager {
    public static final String CHANNEL_KEY = "channel";
    private static final String CHANNEK_DEFAULT = "channel:name";

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

    public static final String DB_UPDATED_KEY = "db.updated";
    private static final boolean DB_UPDATED_DEFAULT = true;

    public static final String TICKET_NOTIFY_KEY = "ticket_type_notify";
    private static final String TICKET_NOTIFY_DEFAULT = "p";

    public static final String TICKET_IGNORE_KEY = "ticket_count_ignore";
    private static final String TICKET_IGNORE_DEFAULT = "h";

    public static final String DB_FAILURE_MESSAGE_KEY = "db.failure_message";
    private static final List<Map<String, String>> DB_FAILURE_MESSAGE_DEFAULT =
            new ArrayList<Map<String, String>>(){{
                add(new HashMap<String, String>(){{
                    put("text", "&4There is currently no connection to the database please try again later or ask a team member.");
                }});
            }};

    public static final String COORDINATE_FORMAT_KEY = "coordinate_format";
    private static final String COORDINATE_FORMAT_DEFAULT = "%.1f";

    public static final String TICKETS_OPEN_FORMAT_KEY = "tickets.format.open";
    private static final List<Map<String, String>> TICKETS_OPEN_FORMAT_DEFAULT =
            new ArrayList<Map<String, String>>() {{
                add(new HashMap<String, String>() {{
                    put("text", "&a[&c%ID%&a] &8[%TICKETTYPE%] &a&lOPEN&r&a - &e%PLAYER% (%SERVER%, %WORLD%, %X%, %Y%, %Z%)&a: &7%REQUEST% - ");
                }});
                add(new HashMap<String, String>() {{
                    put("text", "&5claim");
                    put("runcommand", "/tickets claim %ID%");
                    put("showtext", "&6Click to claim ticket");
                }});
            }};
    public static final String TICKETS_CLAIMED_FORMAT_KEY = "tickets.format.claimed";
    private static final List<Map<String, String>> TICKETS_CLAIMED_FORMAT_DEFAULT =
            new ArrayList<Map<String, String>>() {{
                add(new HashMap<String, String>() {{
                    put("text", "&a[&c%ID%&a] &8[%TICKETTYPE%] &6&lCLAIMED by: %CLAIMER%&r&a - " +
                            "&e%PLAYER% (%SERVER%, %WORLD%, %X%, %Y%, %Z%)&a: &7%REQUEST%");
                }});
            }};
    public static final String TICKETS_CLOSED_FORMAT_KEY = "tickets.format.closed";
    private static final List<Map<String, String>> TICKETS_CLOSED_FORMAT_DEFAULT =
            new ArrayList<Map<String, String>>() {{
                add(new HashMap<String, String>() {{
                    put("text", "&a[&c%ID%&a] &8[%TICKETTYPE%] &c&lCLOSED&r&a - " +
                            "&e%PLAYER% (%SERVER%, %WORLD%, %X%, %Y%, %Z%)&a: &7%REQUEST% &a| &6%RESPONDER%&a: &7%ANSWER%");
                }});
            }};
    public static final String TICKETS_READ_FORMAT_KEY = "tickets.format.read";
    private static final List<Map<String, String>> TICKETS_READ_FORMAT_DEFAULT =
            new ArrayList<Map<String, String>>() {{
                add(new HashMap<String, String>() {{
                    put("text", "&a[&c%ID%&a] &8[%TICKETTYPE%] &2&lREAD&r&a - " +
                            "&e%PLAYER% (%SERVER%, %WORLD%, %X%, %Y%, %Z%)&a: &7%REQUEST% &a| &6%RESPONDER%&a: &7%ANSWER%");
                }});
            }};
    public static final String TICKET_OPEN_FORMAT_KEY = "ticket.format.open";
    private static final List<Map<String, String>> TICKET_OPEN_FORMAT_DEFAULT =
            new ArrayList<Map<String, String>>() {{
                add(new HashMap<String, String>() {{
                    put("text", "&a[&c%ID%&a] &8[%TICKETTYPE%] &a&lOPEN&r&a - &e%PLAYER% (%SERVER%, %WORLD%, %X%, %Y%, %Z%)&a: &7%REQUEST%");
                }});
            }};
    public static final String TICKET_UNREAD_FORMAT_KEY = "ticket.format.unread";
    private static final List<Map<String, String>> TICKET_UNREAD_FORMAT_DEFAULT =
            new ArrayList<Map<String, String>>() {{
                add(new HashMap<String, String>() {{
                    put("text", "&a[&c%ID%&a] &8[%TICKETTYPE%] &c&lUNREAD&r&a - &e%PLAYER% (%SERVER%, %WORLD%, %X%, %Y%, %Z%)&a: &7%REQUEST% &a| " +
                            "&6%RESPONDER%&a: &7%ANSWER%; ");
                }});
                add(new HashMap<String, String>() {{
                    put("text", "&4READ");
                    put("showtext", "&aClick to mark as read");
                    put("runcommand", "/ticket read %ID%");
                }});
            }};

    public static final String TICKET_COLOR_CHAR_KEY = "ticket.color.char";
    private static final char TICKET_COLOR_CHAR_DEFAULT = '&';

    public static final String TICKET_FORMAT_TICKETTYPE_KEY = "ticket.format.tickettype";
    private static final Map<String, String> TICKET_FORMAT_TICKETTYPE_DEFAULT = new HashMap<String, String>(){{
        put("p", "p");
        put("c", "c");
        put("h", "h");
    }};

    public static final String CONFIG_RELOAD_OP_KEY = "config.reload.op";
    private static final List<String> CONFIG_RELOAD_OP_DEFAULT = new LinkedList<>();
    public static final String CONFIG_DBCLEAN_OP_KEY = "config.cleandb.op";
    private static final List<String> CONFIG_DBCLEAN_OP_DEFAULT = new LinkedList<>();

    public static final String TICKETS_PER_PAGE_KEY = "tickets.page.limit";
    private static final Integer TICKETS_PER_PAGE_DEFAULT = 5;
    public static final String TICKETS_PAGE_INFO_KEY = "tickets.page.header";
    private static final List<Map<String, String>> TICKETS_PAGE_INFO_DEFAULT =
            new ArrayList<Map<String, String>>() {{
                add(new HashMap<String, String>() {{
                    put("text", "Page %PAGE%/%PAGES% | ");
                }});
                add(new HashMap<String, String>() {{
                    put("text", "&4previous");
                    put("showtext", "&aClick for previous page");
                    put("runcommand", "/tickets page %TICKETTYPE% %PREVPAGE%");
                }});
                add(new HashMap<String, String>() {{
                    put("text", " - ");
                }});
                add(new HashMap<String, String>() {{
                    put("text", "&2next");
                    put("showtext", "&aClick for next page");
                    put("runcommand", "/tickets page %TICKETTYPE% %NEXTPAGE%");
                }});
            }};
    public static final String TICKETS_PLAYER_PAGE_INFO_KEY = "tickets.PlayerPage.header";
    private static final List<Map<String, String>> TICKETS_PLAYER_PAGE_INFO_DEFAULT =
            new ArrayList<Map<String, String>>() {{
                add(new HashMap<String, String>() {{
                    put("text", "Page %PAGE%/%PAGES% | ");
                }});
                add(new HashMap<String, String>() {{
                    put("text", "&4previous");
                    put("showtext", "&aClick for previous page");
                    put("runcommand", "/tickets %PLAYER% %TICKETTYPE% %PREVPAGE%");
                }});
                add(new HashMap<String, String>() {{
                    put("text", " - ");
                }});
                add(new HashMap<String, String>() {{
                    put("text", "&2next");
                    put("showtext", "&aClick for next page");
                    put("runcommand", "/tickets %PLAYER% %TICKETTYPE% %NEXTPAGE%");
                }});
            }};
    public static final String TICKET_PAGE_INFO_KEY = "ticket.page.info";
    private static final List<Map<String, String>> TICKET_PAGE_INFO_DEFAULT =
            new ArrayList<Map<String, String>>() {{
                add(new HashMap<String, String>() {{
                    put("text", "Page %PAGE%/%PAGES% | ");
                }});
                add(new HashMap<String, String>() {{
                    put("text", "&4previous");
                    put("showtext", "&aClick for previous page");
                    put("runcommand", "/ticket list %PREVPAGE%");
                }});
                add(new HashMap<String, String>() {{
                    put("text", " - ");
                }});
                add(new HashMap<String, String>() {{
                    put("text", "&2next");
                    put("showtext", "&aClick for next page");
                    put("runcommand", "/ticket list %NEXTPAGE%");
                }});
            }};

    public static final String TICKETS_INFO_CREATE_KEY = "tickets.info.create";
    private static final List<Map<String,String>> TICKET_CREATE_INFO_DEFAULT =
            new ArrayList<Map<String,String>>(){{
                add(new HashMap<String, String>(){{
                    put("text", "&c%PLAYER%");
                    put("runcommand", "/tickets %PLAYER%");
                    put("showtext", "&aShow all tickets by this player.");
                }});
                add(new HashMap<String, String>(){{
                    put("text", "&a has created ticket ");
                }});
                add(new HashMap<String, String>(){{
                    put("text", "&c#%ID%&a: &7%REQUEST%");
                    put("runcommand", "/tickets %ID%");
                    put("showtext", "&aShow this ticket.");
                }});
            }};
    public static final String TICKET_INFO_CREATE_KEY = "ticket.info.create";
    private static final List<Map<String, String>> TICKET_CREATE_THINK_DEFAULT =
            new ArrayList<Map<String, String>>(){{
                add(new HashMap<String, String>(){{
                    put("text", "&aThank you for your ticket &c%ID%&a. A team member will have a look at your request soon.");
                }});
            }};
    public static final String TICKETS_INFO_CLAIM_KEY = "tickets.info.claim";
    private static final List<Map<String, String>> TICKETS_CLAIM_INFO_DEFAULT =
            new ArrayList<Map<String, String>>(){{
                add(new HashMap<String, String>(){{
                    put("text", "&6%PLAYER%&a has &cclaimed&a ticket &c%ID%&a.");
                }});
            }};
    public static final String TICKETS_INFO_UNCLAIM_KEY = "tickets.info.unclaim";
    private static final List<Map<String, String>> TICKETS_UNCLAIM_INFO_DEFAULT =
            new ArrayList<Map<String, String>>(){{
                add(new HashMap<String, String>(){{
                    put("text", "&6%PLAYER%&a has &cunclaimed&a ticket &c%ID%&a.");
                }});
            }};
    public static final String TICKETS_INFO_CLOSE_KEY = "tickets.info.close";
    private static final List<Map<String, String>> TICKETS_CLOSE_INFO_DEFAULT =
            new ArrayList<Map<String, String>>(){{
                add(new HashMap<String, String>(){{
                    put("text", "&6%PLAYER% has &cclosed&a ticket &c%ID%&a: &7%ANSWER%");
                }});
            }};
    public static final String TICKETS_INFO_CLOSE_FAIL_KEY = "tickets.info.closeFail";
    private static final List<Map<String, String>> TICKETS_INFO_CLOSE_FAIL_DEFAULT =
            new ArrayList<Map<String, String>>(){{
                add(new HashMap<String, String>(){{
                    put("text", "&6A ticket &c%ID%&6 does not exist or has already been closed.");
                }});
            }};
    public static final String TICKET_INFO_CLOSE_KEY = "ticket.info.close";
    private static final List<Map<String, String>> TICKET_CLOSE_PLAYER_INFO_DEFAULT =
            new ArrayList<Map<String, String>>(){{
                add(new HashMap<String, String>(){{
                    put("text", "&aYour ticket &c%ID%&a has been closed from &e%PLAYER%&a: &7%ANSWER%");
                }});
            }};
    public static final String TICKETS_INFO_SPAM_KEY = "tickets.info.spam";
    private static final List<Map<String, String>> TICKETS_SPAM_INFO_DEFAULT =
            new ArrayList<Map<String, String>>(){{
                add(new HashMap<String, String>(){{
                    put("text", "&e%PLAYER%&a tried to issue redundant ticket (&e%ID%&a): &7%REQUEST%");
                }});
            }};
    public static final String TICKET_SPAM_DISTANCE_KEY = "ticket.spam_distance";
    private static final String TICKET_SPAM_DISTANCE_DEFAULT = "5";
    public static final String TICKETS_INFO_READ = "tickets.info.read";
    private static final List<Map<String, String>> TICKETS_READ_INFO_DEFAULT =
            new ArrayList<Map<String, String>>(){{
                add(new HashMap<String, String>(){{
                    put("text", "&e%PLAYER%&a has read the response to ticket &e%ID%&a.");
                }});
            }};
    public static final String TICKET_INFO_READ_KEY = "ticket.info.read";
    private static final List<Map<String, String>> TICKET_READ_THANK_DEFAULT =
            new ArrayList<Map<String, String>>(){{
                add(new HashMap<String, String>(){{
                    put("text", "&aThank you for reading the teams response.");
                }});
            }};

    public static final String NO_TICKET_WITH_ID_KEY = "no_ticket_with_id";
    private static final List<Map<String, String>> NO_TICKET_WITH_ID_DEFAULT =
            new ArrayList<Map<String, String>>(){{
                add(new HashMap<String, String>(){{
                    put("text", "&aThere is no ticket with ID: &e%ID%&a.");
                }});
            }};

    public static final String JOIN_OPEN_TICKET_KEY = "join.ticket.open";
    private static final List<Map<String, String>> JOIN_OPEN_TICKET_DEFAULT =
            new ArrayList<Map<String, String>>(){{
                add(new HashMap<String, String>(){{
                    put("text", "&aThere are currently &e%NUM%&a open tickets.");
                    put("showtext", "&aShow open tickets.");
                    put("runcommand", "/tickets page 1");
                }});
            }};
    public static final String JOIN_UNRAED_TICKET_KEY = "join.ticket.unread";
    private static final List<Map<String, String>> JOIN_UNREAD_TICKET_DEFAULT =
            new ArrayList<Map<String, String>>(){{
                add(new HashMap<String, String>(){{
                    put("text", "&aYou have &c%NUM%&a unread tickets. ");
                }});
                add(new HashMap<String, String>(){{
                    put("text", "&c/ticket list");
                    put("showtext", "&aSee your current and unread tickets.");
                    put("suggestcommand", "/ticket list");
                }});
            }};

    public static final String TICKET_FORMAT_LINK_KEY = "ticket.format.link_regex";
    private static final String TICKET_FORMAT_LINK_DEFAULT =
            "(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})";

    public static Configuration loadFromFile(ProxyTickets plugin) {
        try {
            File file = new File(plugin.getDataFolder(), "config.yml");
            Configuration config;
            if (file.exists()) {
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            } else {
                config = new Configuration();
            }
            return config;
        } catch (IOException e) {
            return new Configuration();
        }
    }

    public static void saveToFile(ProxyTickets plugin, Configuration config) {
        try {
            File file = new File(plugin.getDataFolder(), "config.yml");
            File directory = new File(plugin.getDataFolder(), "");
            if (!directory.exists()) {
                directory.mkdir();
            }
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
        } catch (IOException e) {
            System.out.println("Failed to save config!");
            e.printStackTrace();
        }
    }

    public static Map<String, Object> getConfig(ProxyTickets plugin) {
        Map<String, Object> cachedConfig = new HashMap<>();
        Configuration config = ConfigManager.loadFromFile(plugin);
        Yaml yaml = new Yaml();

        cachedConfig.put(CHANNEL_KEY,
                config.getString(CHANNEL_KEY, CHANNEK_DEFAULT));

        cachedConfig.put(DATABASE_HOST_KEY,
                config.getString(DATABASE_HOST_KEY, DATABASE_HOST_DEFAULT));
        cachedConfig.put(DATABASE_PORT_KEY,
                config.getString(DATABASE_PORT_KEY, DATABASE_PORT_DEFAULT));
        cachedConfig.put(DATABASE_DATABASE_KEY,
                config.getString(DATABASE_DATABASE_KEY, DATABASE_DATABASE_DEFAULT));
        cachedConfig.put(DATABASE_TABLE_KEY,
                config.getString(DATABASE_TABLE_KEY, DATABASE_TABLE_DEFAULT));
        cachedConfig.put(DATABASE_USERNAME_KEY,
                config.getString(DATABASE_USERNAME_KEY, DATABASE_USERNAME_DEFAULT));
        cachedConfig.put(DATABASE_PASSWORD_KEY,
                config.getString(DATABASE_PASSWORD_KEY, DATABASE_PASSWORD_DEFAULT));
        cachedConfig.put(DB_UPDATED_KEY,
                config.getBoolean(DB_UPDATED_KEY, DB_UPDATED_DEFAULT));

        cachedConfig.put(TICKET_NOTIFY_KEY,
                 config.getString(TICKET_NOTIFY_KEY, TICKET_NOTIFY_DEFAULT));
        cachedConfig.put(TICKET_IGNORE_KEY,
                config.getString(TICKET_IGNORE_KEY, TICKET_IGNORE_DEFAULT));

        cachedConfig.put(DB_FAILURE_MESSAGE_KEY,
                config.getString(DB_FAILURE_MESSAGE_KEY, yaml.dump(DB_FAILURE_MESSAGE_DEFAULT)));

        cachedConfig.put(COORDINATE_FORMAT_KEY,
                config.getString(COORDINATE_FORMAT_KEY, COORDINATE_FORMAT_DEFAULT));

        cachedConfig.put(TICKETS_OPEN_FORMAT_KEY,
                config.getString(TICKETS_OPEN_FORMAT_KEY, yaml.dump(TICKETS_OPEN_FORMAT_DEFAULT)));
        cachedConfig.put(TICKETS_CLAIMED_FORMAT_KEY,
                config.getString(TICKETS_CLAIMED_FORMAT_KEY, yaml.dump(TICKETS_CLAIMED_FORMAT_DEFAULT)));
        cachedConfig.put(TICKETS_CLOSED_FORMAT_KEY,
                config.getString(TICKETS_CLOSED_FORMAT_KEY, yaml.dump(TICKETS_CLOSED_FORMAT_DEFAULT)));
        cachedConfig.put(TICKETS_READ_FORMAT_KEY,
                config.getString(TICKETS_READ_FORMAT_KEY, yaml.dump(TICKETS_READ_FORMAT_DEFAULT)));
        cachedConfig.put(TICKET_OPEN_FORMAT_KEY,
                config.getString(TICKET_OPEN_FORMAT_KEY, yaml.dump(TICKET_OPEN_FORMAT_DEFAULT)));
        cachedConfig.put(TICKET_UNREAD_FORMAT_KEY,
                config.getString(TICKET_UNREAD_FORMAT_KEY, yaml.dump(TICKET_UNREAD_FORMAT_DEFAULT)));

        cachedConfig.put(TICKET_FORMAT_TICKETTYPE_KEY,
                config.get(TICKET_FORMAT_TICKETTYPE_KEY, TICKET_FORMAT_TICKETTYPE_DEFAULT));

        cachedConfig.put(TICKET_COLOR_CHAR_KEY,
                config.getChar(TICKET_COLOR_CHAR_KEY, TICKET_COLOR_CHAR_DEFAULT));

        List<String> config_reload_op = config.getStringList(CONFIG_RELOAD_OP_KEY);
        cachedConfig.put(CONFIG_RELOAD_OP_KEY,
                config_reload_op.size() > 0 ? config_reload_op : CONFIG_RELOAD_OP_DEFAULT);
        List<String> config_db_clean = config.getStringList(CONFIG_DBCLEAN_OP_KEY);
        cachedConfig.put(CONFIG_DBCLEAN_OP_KEY,
                config_db_clean.size() > 0 ? config_db_clean : CONFIG_DBCLEAN_OP_DEFAULT);

        cachedConfig.put(TICKETS_PER_PAGE_KEY,
                config.getInt(TICKETS_PER_PAGE_KEY, TICKETS_PER_PAGE_DEFAULT));
        cachedConfig.put(TICKETS_PAGE_INFO_KEY,
                config.getString(TICKETS_PAGE_INFO_KEY, yaml.dump(TICKETS_PAGE_INFO_DEFAULT)));
        cachedConfig.put(TICKETS_PLAYER_PAGE_INFO_KEY,
                config.getString(TICKETS_PLAYER_PAGE_INFO_KEY, yaml.dump(TICKETS_PLAYER_PAGE_INFO_DEFAULT)));
        cachedConfig.put(TICKET_PAGE_INFO_KEY,
                config.getString(TICKET_PAGE_INFO_KEY, yaml.dump(TICKET_PAGE_INFO_DEFAULT)));

        cachedConfig.put(TICKETS_INFO_CREATE_KEY,
                config.getString(TICKETS_INFO_CREATE_KEY, yaml.dump(TICKET_CREATE_INFO_DEFAULT)));
        cachedConfig.put(TICKET_INFO_CREATE_KEY,
                config.getString(TICKET_INFO_CREATE_KEY, yaml.dump(TICKET_CREATE_THINK_DEFAULT)));
        cachedConfig.put(TICKETS_INFO_CLAIM_KEY,
                config.getString(TICKETS_INFO_CLAIM_KEY, yaml.dump(TICKETS_CLAIM_INFO_DEFAULT)));
        cachedConfig.put(TICKETS_INFO_UNCLAIM_KEY,
                config.getString(TICKETS_INFO_UNCLAIM_KEY, yaml.dump(TICKETS_UNCLAIM_INFO_DEFAULT)));
        cachedConfig.put(TICKETS_INFO_CLOSE_KEY,
                config.getString(TICKETS_INFO_CLOSE_KEY, yaml.dump(TICKETS_CLOSE_INFO_DEFAULT)));
        cachedConfig.put(TICKETS_INFO_CLOSE_FAIL_KEY,
                config.getString(TICKETS_INFO_CLOSE_FAIL_KEY, yaml.dump(TICKETS_INFO_CLOSE_FAIL_DEFAULT)));
        cachedConfig.put(TICKET_INFO_CLOSE_KEY,
                config.getString(TICKET_INFO_CLOSE_KEY, yaml.dump(TICKET_CLOSE_PLAYER_INFO_DEFAULT)));
        cachedConfig.put(TICKETS_INFO_SPAM_KEY,
                config.getString(TICKETS_INFO_SPAM_KEY, yaml.dump(TICKETS_SPAM_INFO_DEFAULT)));
        cachedConfig.put(TICKET_SPAM_DISTANCE_KEY,
                config.getString(TICKET_SPAM_DISTANCE_KEY, TICKET_SPAM_DISTANCE_DEFAULT));
        cachedConfig.put(TICKETS_INFO_READ,
                config.getString(TICKETS_INFO_READ, yaml.dump(TICKETS_READ_INFO_DEFAULT)));
        cachedConfig.put(TICKET_INFO_READ_KEY,
                config.getString(TICKET_INFO_READ_KEY, yaml.dump(TICKET_READ_THANK_DEFAULT)));
        cachedConfig.put(NO_TICKET_WITH_ID_KEY,
                config.getString(NO_TICKET_WITH_ID_KEY, yaml.dump(NO_TICKET_WITH_ID_DEFAULT)));

        cachedConfig.put(JOIN_OPEN_TICKET_KEY,
                config.getString(JOIN_OPEN_TICKET_KEY, yaml.dump(JOIN_OPEN_TICKET_DEFAULT)));
        cachedConfig.put(JOIN_UNRAED_TICKET_KEY,
                config.getString(JOIN_UNRAED_TICKET_KEY, yaml.dump(JOIN_UNREAD_TICKET_DEFAULT)));

        cachedConfig.put(TICKET_FORMAT_LINK_KEY,
                config.getString(TICKET_FORMAT_LINK_KEY, TICKET_FORMAT_LINK_DEFAULT));

        for (Map.Entry<String, Object> entry : cachedConfig.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        ConfigManager.saveToFile(plugin, config);
        char altColor = (char) cachedConfig.get(TICKET_COLOR_CHAR_KEY);
        Map<String, String> ticketformats;
        Object o = cachedConfig.get(TICKET_FORMAT_TICKETTYPE_KEY);
        if (!(o instanceof Map)){
            ticketformats = new HashMap<String, String>();
            Configuration conf = (Configuration) o;
            for (String key: conf.getKeys()){
                ticketformats.put(key, conf.getString(key, ""));
            }
        }else
            ticketformats = (Map<String, String>)cachedConfig.get(TICKET_FORMAT_TICKETTYPE_KEY);
        for (Map.Entry<String, String> entry: ticketformats.entrySet()){
            ticketformats.put(entry.getKey(),
                    ChatColor.translateAlternateColorCodes(altColor, entry.getValue()));
        }
        cachedConfig.put(TICKET_FORMAT_TICKETTYPE_KEY, ticketformats);

        for (String key : new String[]{
                DB_FAILURE_MESSAGE_KEY,
                TICKETS_OPEN_FORMAT_KEY,
                TICKETS_CLAIMED_FORMAT_KEY,
                TICKETS_CLOSED_FORMAT_KEY,
                TICKETS_READ_FORMAT_KEY,
                TICKET_OPEN_FORMAT_KEY,
                TICKET_UNREAD_FORMAT_KEY,

                TICKETS_PAGE_INFO_KEY,
                TICKETS_PLAYER_PAGE_INFO_KEY,
                TICKET_PAGE_INFO_KEY,
                TICKETS_INFO_CREATE_KEY,
                TICKET_INFO_CREATE_KEY,
                TICKETS_INFO_CLAIM_KEY,
                TICKETS_INFO_UNCLAIM_KEY,
                TICKETS_INFO_CLOSE_KEY,
                TICKETS_INFO_CLOSE_FAIL_KEY,
                TICKET_INFO_CLOSE_KEY,
                TICKETS_INFO_SPAM_KEY,
                TICKETS_INFO_READ,
                TICKET_INFO_READ_KEY,
                NO_TICKET_WITH_ID_KEY,
                JOIN_OPEN_TICKET_KEY,
                JOIN_UNRAED_TICKET_KEY,
        }) {
            List<Object> components = (List) yaml.load((String) cachedConfig.get(key));
            List<TextComponent> tcs = new LinkedList<TextComponent>();
            for (Object obj_ : components) {
                Map<String, String> comp = (Map<String, String>) obj_;
                TextComponent ntc = new TextComponent(
                        ChatColor.translateAlternateColorCodes(altColor, comp.get("text")));
                if (comp.containsKey("showtext")) {
                    ntc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new Text(ChatColor.translateAlternateColorCodes(altColor, comp.get("showtext")))));
                }
                if (comp.containsKey("runcommand")) {
                    ntc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            comp.get("runcommand")));
                }
                if (comp.containsKey("suggestcommand")) {
                    ntc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                            comp.get("suggestcommand")));
                }
                if (comp.containsKey("openurl")) {
                    ntc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                            comp.get("openurl")));
                }
                tcs.add(ntc);
            }
            cachedConfig.put(key, tcs);
        }
        return cachedConfig;
    }
}
