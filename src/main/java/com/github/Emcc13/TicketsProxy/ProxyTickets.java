package com.github.Emcc13.TicketsProxy;

import com.github.Emcc13.TicketsProxy.Commands.CleanDB;
import com.github.Emcc13.TicketsProxy.Commands.ReloadConfig;
import com.github.Emcc13.TicketsProxy.Config.ConfigManager;
import com.github.Emcc13.TicketsProxy.Database.DBInterface;
import com.github.Emcc13.TicketsProxy.Database.MySQL;
import com.github.Emcc13.TicketsProxy.ServerMessages.MessageReceiver;
import com.github.Emcc13.TicketsProxy.ServerMessages.ServerMessage;
import com.github.Emcc13.TicketsProxy.ServerMessages.TicketChangeNotifier;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;


@Plugin(id = "tickets", name = "TicketsProxy",
        description = "A ticket plugin", version = "0.7", authors = "Emcc13")
public class ProxyTickets {
    private DBInterface dbInterface;
    private Map<String, Object> cachedConfig;
    private static ProxyTickets instance;
    private ScheduledTask dbTask;
    private TicketChangeNotifier ticketChangeNotifier;
    public static Pattern urlPattern;
    private MinecraftChannelIdentifier channelIdentifier;

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public ProxyTickets(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory){
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        ProxyTickets.instance = this;
        this.logger.info("TicketsProxy");

//        https://docs.papermc.io/velocity/dev/command-api
//        https://github.com/Mojang/brigadier
//        https://gist.github.com/Xernium/95c9262c5f70b8791557861bbc09be1b

        File queries = new File(this.dataDirectory.toAbsolutePath().toFile(), "queries");
        if (!queries.exists()) {
            queries.mkdirs();
        }

        server.getEventManager().register(this, new MessageReceiver(this));

        this.cachedConfig = ConfigManager.getConfig(this);
        this.channelIdentifier = MinecraftChannelIdentifier.create(
                "tickets",
                (String) this.cachedConfig.get(ConfigManager.CHANNEL_KEY)
        );
        this.getServer().getChannelRegistrar().register(this.channelIdentifier);

        this.dbInterface = new MySQL(this,
                (String) this.cachedConfig.get(ConfigManager.DATABASE_HOST_KEY),
                (String) this.cachedConfig.get(ConfigManager.DATABASE_PORT_KEY),
                (String) this.cachedConfig.get(ConfigManager.DATABASE_DATABASE_KEY),
                (String) this.cachedConfig.get(ConfigManager.DATABASE_TABLE_KEY),
                (String) this.cachedConfig.get(ConfigManager.DATABASE_USERNAME_KEY),
                (String) this.cachedConfig.get(ConfigManager.DATABASE_PASSWORD_KEY),
                queries.toPath());

        CommandManager commandManager = this.server.getCommandManager();

        CommandMeta cleanDB = commandManager.metaBuilder("tickets.cleandb").plugin(this).build();
        commandManager.register(cleanDB, new CleanDB());

        CommandMeta reload = commandManager.metaBuilder("tickets.reload").plugin(this).build();
        commandManager.register(reload, new ReloadConfig());

        urlPattern = Pattern.compile((String) this.cachedConfig.get(ConfigManager.TICKET_FORMAT_LINK_KEY),
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

        this.ticketChangeNotifier = new TicketChangeNotifier(this);

        this.dbTask = this.getServer().getScheduler().buildTask(this, this.dbInterface).schedule();
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
        this.dbTask.cancel();
    }

    public void reloadConfig() {
        this.getServer().getChannelRegistrar().unregister(this.channelIdentifier);
        this.cachedConfig = ConfigManager.getConfig(this);
        this.channelIdentifier = MinecraftChannelIdentifier.create(
                "tickets",
                (String) this.cachedConfig.get(ConfigManager.CHANNEL_KEY)
        );
        this.getServer().getChannelRegistrar().register(this.channelIdentifier);
        this.dbInterface.updateSettings(
                (String) this.cachedConfig.get(ConfigManager.DATABASE_HOST_KEY),
                (String) this.cachedConfig.get(ConfigManager.DATABASE_PORT_KEY),
                (String) this.cachedConfig.get(ConfigManager.DATABASE_DATABASE_KEY),
                (String) this.cachedConfig.get(ConfigManager.DATABASE_TABLE_KEY),
                (String) this.cachedConfig.get(ConfigManager.DATABASE_USERNAME_KEY),
                (String) this.cachedConfig.get(ConfigManager.DATABASE_PASSWORD_KEY));
    }

    public void sendNotification(String text) {
        byte[] message = ServerMessage.forTicketNotify(text).toMessagae();
        for (RegisteredServer server : getServer().getAllServers()){
            server.sendPluginMessage(this.channelIdentifier, message);
        }
    }

    public static ProxyTickets getInstance() {
        return instance;
    }

    public DBInterface getDbInterface() {
        return dbInterface;
    }

    public Map<String, Object> getCachedConfig() {
        return cachedConfig;
    }

    public TicketChangeNotifier getNotifier() {
        return ticketChangeNotifier;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Path getDirectory(){
        return dataDirectory;
    }

    public ChannelIdentifier getChannelIdentifier(){
        return channelIdentifier;
    }

    public Logger getLogger(){
        return this.logger;
    }
}
