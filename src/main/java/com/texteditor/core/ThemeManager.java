package com.texteditor.core;

import com.texteditor.model.ColorScheme;

import java.awt.Color;

public class ThemeManager {

    public static final ColorScheme LIGHT = new ColorScheme(
        "Light",
        Color.WHITE,                    // background
        new Color(0x1A1A1A),            // foreground
        new Color(0x0000CC),            // keyword   – blue
        new Color(0x008000),            // string    – green
        new Color(0x808080),            // comment   – gray
        new Color(0x0000FF),            // number    – blue
        new Color(0x808000),            // annotation – olive
        new Color(0xF2F2F2),            // linenum bg
        new Color(0x888888),            // linenum fg
        new Color(0xB5D5FF),            // selection
        Color.BLACK                     // caret
    );

    public static final ColorScheme DARK = new ColorScheme(
        "Dark",
        new Color(0x2B2B2B),
        new Color(0xA9B7C6),
        new Color(0xCC7832),            // keyword   – orange
        new Color(0x6A8759),            // string    – muted green
        new Color(0x808080),            // comment   – gray
        new Color(0x6897BB),            // number    – steel blue
        new Color(0xBBB529),            // annotation – yellow
        new Color(0x313335),
        new Color(0x606366),
        new Color(0x214283),
        new Color(0xBBBBBB)
    );

    public static final ColorScheme SOLARIZED = new ColorScheme(
        "Solarized",
        new Color(0xFDF6E3),            // solarized base3
        new Color(0x657B83),            // base00
        new Color(0x268BD2),            // keyword   – blue
        new Color(0x2AA198),            // string    – cyan
        new Color(0x93A1A1),            // comment
        new Color(0xD33682),            // number    – magenta
        new Color(0xCB4B16),            // annotation – orange
        new Color(0xEEE8D5),
        new Color(0x93A1A1),
        new Color(0xEEE8D5),
        new Color(0x586E75)
    );

    private ColorScheme current = LIGHT;

    public ColorScheme getCurrent() { return current; }

    public void setTheme(ColorScheme scheme) { this.current = scheme; }

    public ColorScheme byName(String name) {
        switch (name) {
            case "Dark":      return DARK;
            case "Solarized": return SOLARIZED;
            default:          return LIGHT;
        }
    }
}
