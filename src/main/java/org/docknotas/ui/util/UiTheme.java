package org.docknotas.ui.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/** Centraliza cores/estilos e corrige hover de menu. */
public class UiTheme {

    /** Paleta completa para menus. */
    public record MenuColors(Color background, Color foreground, Color border,
                             Color selectionBackground, Color selectionForeground) {}

    private static MenuColors lastMenuColors = null;

    public static boolean isDark(String theme) {
        return theme == null || !"light".equalsIgnoreCase(theme);
    }

    public static MenuColors menuColors(String theme) {
        boolean dark = isDark(theme);
        Color bg = dark ? new Color(20, 23, 28, 235) : new Color(250, 250, 252, 240);
        Color fg = dark ? new Color(235, 235, 240) : new Color(24, 24, 28);
        Color border = dark ? new Color(70, 70, 80) : new Color(210, 210, 220);
        Color selBg = dark ? new Color(45, 50, 58) : new Color(225, 230, 240);
        Color selFg = dark ? new Color(240, 240, 245) : new Color(24, 24, 28);
        return new MenuColors(bg, fg, border, selBg, selFg);
    }

    /** Ajusta o L&F e recolore hovers de menus conforme o tema. */
    public static void applyLookAndFeel(String theme) {
        if (isDark(theme)) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        applyGlobalMenuHoverTheme(theme);
        FlatLaf.updateUI();
    }

    public static void applyGlobalMenuHoverTheme(String theme) {
        MenuColors colors = menuColors(theme);
        if (colors.equals(lastMenuColors)) return;
        lastMenuColors = colors;

        UIManager.put("Menu.selectionBackground", colors.selectionBackground());
        UIManager.put("Menu.selectionForeground", colors.selectionForeground());
        UIManager.put("MenuItem.selectionBackground", colors.selectionBackground());
        UIManager.put("MenuItem.selectionForeground", colors.selectionForeground());
        UIManager.put("CheckBoxMenuItem.selectionBackground", colors.selectionBackground());
        UIManager.put("CheckBoxMenuItem.selectionForeground", colors.selectionForeground());
        UIManager.put("RadioButtonMenuItem.selectionBackground", colors.selectionBackground());
        UIManager.put("RadioButtonMenuItem.selectionForeground", colors.selectionForeground());
    }

    public static Color headerColor() {
        // header translúcido levemente colorido (neutro)
        return new Color(30,30,36,220);
    }

    // prioridade: tons fixos
    public static Color priorityBorder(String name) {
        if (name == null) name = "cinza";
        return switch (name.toLowerCase()) {
            case "vermelho" -> new Color(220,80,80);
            case "laranja"  -> new Color(235,160,70);
            case "amarelo"  -> new Color(220,200,60);
            case "verde"    -> new Color(70,185,120);
            case "azul"     -> new Color(84,140,210);
            case "roxo"     -> new Color(148,98,200);
            default         -> new Color(88,88,96);
        };
    }
    public static Color priorityTag(String name) {
        if (name == null) name = "cinza";
        return switch (name.toLowerCase()) {
            case "vermelho" -> new Color(232,72,72);
            case "laranja"  -> new Color(242,178,102);
            case "amarelo"  -> new Color(236,220,114);
            case "verde"    -> new Color(108,208,154);
            case "azul"     -> new Color(122,168,222);
            case "roxo"     -> new Color(174,132,214);
            default         -> new Color(128,128,136);
        };
    }
}
