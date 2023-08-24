package com.github.Emcc13.TicketsProxy.Events;


import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;

public class LoginHandler{
    public LoginHandler(){
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event){

    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event){

    }
}