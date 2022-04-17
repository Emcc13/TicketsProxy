package com.github.Emcc13.TicketsProxy;

import com.github.Emcc13.TicketsProxy.Commands.CleanDB;
import com.github.Emcc13.TicketsProxy.Commands.ReloadConfig;
import com.github.Emcc13.TicketsProxy.Config.ConfigManager;
import com.github.Emcc13.TicketsProxy.Database.DBInterface;
import com.github.Emcc13.TicketsProxy.Database.MySQL;
import com.github.Emcc13.TicketsProxy.ServerMessages.MessageReceiver;
import com.github.Emcc13.TicketsProxy.ServerMessages.ServerMessage;
import com.github.Emcc13.TicketsProxy.ServerMessages.TicketChangeNotifier;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

public class ProxyTickets extends Plugin{
    private DBInterface dbInterface;
    private Map<String, Object> cachedConfig;
    private static ProxyTickets instance;
    private ScheduledTask dbTask;
    private TicketChangeNotifier ticketChangeNotifier;
    public static Pattern urlPattern;

    @Override
    public void onEnable(){
        ProxyTickets.instance = this;
        getLogger().info("TicketsProxy");

        getProxy().getPluginManager().registerListener(this, new MessageReceiver(this));

        this.cachedConfig = ConfigManager.getConfig(this);
        getProxy().registerChannel((String)this.cachedConfig.get(ConfigManager.CHANNEL_KEY));
        File querries = new File(this.getDataFolder().getAbsolutePath(),"querries");
        if (!querries.exists()){
            querries.mkdir();
        }

        this.dbInterface = new MySQL(this,
                (String)this.cachedConfig.get(ConfigManager.DATABASE_HOST_KEY),
                (String)this.cachedConfig.get(ConfigManager.DATABASE_PORT_KEY),
                (String)this.cachedConfig.get(ConfigManager.DATABASE_DATABASE_KEY),
                (String)this.cachedConfig.get(ConfigManager.DATABASE_TABLE_KEY),
                (String)this.cachedConfig.get(ConfigManager.DATABASE_USERNAME_KEY),
                (String)this.cachedConfig.get(ConfigManager.DATABASE_PASSWORD_KEY),
                querries.toPath());
        if (!(boolean) this.cachedConfig.get(ConfigManager.DB_UPDATED_KEY)){
            if (this.dbInterface.updateDB()) {
                this.dbInterface.disconnect();
                Configuration config = ConfigManager.loadFromFile(this);
                config.set(ConfigManager.DB_UPDATED_KEY, true);
                ConfigManager.saveToFile(this, config);
                this.cachedConfig.put(ConfigManager.DB_UPDATED_KEY, true);
            }
        }

        getProxy().getPluginManager().registerCommand(this, new ReloadConfig());
        getProxy().getPluginManager().registerCommand(this, new CleanDB());
        urlPattern = Pattern.compile((String)this.cachedConfig.get(ConfigManager.TICKET_FORMAT_LINK_KEY),
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

        this.ticketChangeNotifier = new TicketChangeNotifier(this);
        this.dbTask = ProxyServer.getInstance().getScheduler().runAsync(this, this.dbInterface);
    }

    @Override
    public void onDisable(){
        this.dbTask.cancel();
    }

    public void reloadConfig(){
        this.getProxy().unregisterChannel((String)this.cachedConfig.get(ConfigManager.CHANNEL_KEY));
        this.cachedConfig = ConfigManager.getConfig(this);
        getProxy().registerChannel((String)this.cachedConfig.get(ConfigManager.CHANNEL_KEY));
        this.dbInterface.updateSettings(
                (String)this.cachedConfig.get(ConfigManager.DATABASE_HOST_KEY),
                (String)this.cachedConfig.get(ConfigManager.DATABASE_PORT_KEY),
                (String)this.cachedConfig.get(ConfigManager.DATABASE_DATABASE_KEY),
                (String)this.cachedConfig.get(ConfigManager.DATABASE_TABLE_KEY),
                (String)this.cachedConfig.get(ConfigManager.DATABASE_USERNAME_KEY),
                (String)this.cachedConfig.get(ConfigManager.DATABASE_PASSWORD_KEY));
    }

    public void sendNotification(TextComponent text){
        byte[] message = new ServerMessage(ServerMessage.MessageTopic.ticketNotify, text).toMessagae();
        String channel = (String)cachedConfig.get(ConfigManager.CHANNEL_KEY);
        for (ServerInfo server : getProxy().getServers().values()){
            server.sendData(channel, message);
        }
    }

    public static ProxyTickets getInstance(){
        return instance;
    }

    public DBInterface getDbInterface() {
        return dbInterface;
    }

    public Map<String, Object> getCachedConfig() {
        return cachedConfig;
    }

    public TicketChangeNotifier getNotifier(){
        return ticketChangeNotifier;
    }
}
