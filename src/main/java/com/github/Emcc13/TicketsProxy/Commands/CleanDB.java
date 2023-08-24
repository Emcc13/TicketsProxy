package com.github.Emcc13.TicketsProxy.Commands;

import com.github.Emcc13.TicketsProxy.Database.NotConnectedException;
import com.github.Emcc13.TicketsProxy.ProxyTickets;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CleanDB implements SimpleCommand {
    public CleanDB(){
        super();
    }

    @Override
    public boolean hasPermission(final Invocation invocation){
        return true || invocation.source().hasPermission("tickets.cleandb");
    }

    @Override
    public void execute(Invocation invocation) {
        try {
            ProxyTickets.getInstance().getDbInterface().cleanDB();
        }catch (NotConnectedException e){
        }
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        return List.of();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
        return CompletableFuture.completedFuture(List.of());
    }
}
