package com.github.Emcc13.TicketsProxy.Util;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.LinkedList;
import java.util.List;

public class CustomTextComponentSerializer {
    public static JsonObject serialize(TextComponent src) {
        JsonObject object = new JsonObject();
        object.addProperty("text", src.getText());
        HoverEvent hover = src.getHoverEvent();
        if (hover != null) {
            JsonObject JOhover = new JsonObject();
            object.add("hover", JOhover);
            JsonObject next_c, contents = new JsonObject();
            JOhover.addProperty("event", hover.getAction().toString());
            for (Content content : hover.getContents()) {
                JOhover.add("contents", contents);
                contents.addProperty("text", (String) ((Text) content).getValue());
                next_c = new JsonObject();
                contents = next_c;
            }
        }
        ClickEvent click = src.getClickEvent();
        if (click != null) {
            JsonObject JOclick = new JsonObject();
            object.add("click", JOclick);
            JOclick.addProperty("event", click.getAction().toString());
            JOclick.addProperty("value", click.getValue());
        }
        JsonObject next_e, JOextra = object;
        List<BaseComponent> components = src.getExtra();
        if (components != null) {
            for (BaseComponent extra : components) {
                next_e = serialize((TextComponent) extra);
                JOextra.add("extra", next_e);
                JOextra = next_e;
            }
        }
        return object;
    }

    public static TextComponent deserialize(JsonObject object) {
        TextComponent result = new TextComponent(object.get("text").getAsString());
        JsonObject JOhover = (JsonObject) object.get("hover");
        if (JOhover != null) {
            List<Content> contents = new LinkedList<>();
            JsonObject JOcontent = (JsonObject) JOhover.get("contents");
            while (JOcontent != null) {
                contents.add(new Text(JOcontent.get("text").getAsString()));
                JOcontent = (JsonObject) JOcontent.get("contents");
            }
            result.setHoverEvent(
                    new HoverEvent(HoverEvent.Action.valueOf(JOhover.get("event").getAsString()),
                            contents));
        }
        JsonObject JOclick = (JsonObject) object.get("click");
        if (JOclick != null) {
            result.setClickEvent(
                    new ClickEvent(
                            ClickEvent.Action.valueOf(JOclick.get("event").getAsString()),
                            JOclick.get("value").getAsString()
                    ));
        }
        JsonObject JOextra = (JsonObject) object.get("extra");
        if (JOextra != null) {
            result.addExtra(deserialize(JOextra));
        }
        return result;
    }
}
