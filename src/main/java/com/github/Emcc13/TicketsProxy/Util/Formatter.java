package com.github.Emcc13.TicketsProxy.Util;

import net.kyori.adventure.text.Component;

import java.util.LinkedList;
import java.util.List;

public class Formatter {

    public static String formatString(String template, Tuple<String, String>... replacements){
        String result = template;
        for (Tuple<String, String> replacement: replacements){
            result = result.replace(replacement.first, replacement.second);
        }
        return result;
    }
}
