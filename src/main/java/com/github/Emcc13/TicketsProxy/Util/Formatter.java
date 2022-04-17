package com.github.Emcc13.TicketsProxy.Util;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.LinkedList;
import java.util.List;

public class Formatter {
    public static TextComponent formatComponents(List<TextComponent> template, Tuple<String, String>... replacements){
        TextComponent result = new TextComponent();
        for (TextComponent tc: template){
            TextComponent copy = new TextComponent(formatString(tc.getText(), replacements));
            HoverEvent hover = tc.getHoverEvent();
            if (hover!=null){
                List<Content> contents = new LinkedList<>();
                for (Content content: hover.getContents()){
                    contents.add(new Text(formatString(
                            ((String)((Text) content).getValue()),
                            replacements
                    )));
                }
                copy.setHoverEvent(new HoverEvent(hover.getAction(),contents));
            }
            ClickEvent click = tc.getClickEvent();
            if (click!=null){
                copy.setClickEvent(new ClickEvent(click.getAction(),
                        formatString(click.getValue(), replacements)));
            }
            result.addExtra(copy);
        }
        return result;
    }

    public static String formatString(String template, Tuple<String, String>... replacements){
        String result = template;
        for (Tuple<String, String> replacement: replacements){
            result = result.replace(replacement.first, replacement.second);
        }
        return result;
    }
}
