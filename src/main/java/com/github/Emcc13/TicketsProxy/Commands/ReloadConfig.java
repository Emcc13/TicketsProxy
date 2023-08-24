package com.github.Emcc13.TicketsProxy.Commands;

import com.github.Emcc13.TicketsProxy.ProxyTickets;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReloadConfig implements SimpleCommand {
    public ReloadConfig(){
        super();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true || invocation.source().hasPermission("tickets.reload");
    }

    @Override
    public void execute(Invocation invocation) {
        ProxyTickets.getInstance().reloadConfig();
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(List.of());
    }


}
