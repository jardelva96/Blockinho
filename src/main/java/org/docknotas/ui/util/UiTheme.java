package org.docknotas.ui.util;

import java.awt.*;
import javax.swing.*;

/** Centraliza cores/estilos e corrige hover de menu. */
public class UiTheme {
    public static final Color MENU_BG     = new Color(20,23,28,235);
    public static final Color MENU_FG     = new Color(235,235,240);
    public static final Color MENU_BORDER = new Color(70,70,80);

    private static boolean hoverAppliedOnce = false;

    public static void applyGlobalMenuHoverTheme() {
        if (hoverAppliedOnce) return;
        hoverAppliedOnce = true;

        Color bgSel = new Color(45,50,58);
        Color fgSel = new Color(240,240,245);

        UIManager.put("Menu.selectionBackground", bgSel);
        UIManager.put("Menu.selectionForeground", fgSel);
        UIManager.put("MenuItem.selectionBackground", bgSel);
        UIManager.put("MenuItem.selectionForeground", fgSel);
        UIManager.put("CheckBoxMenuItem.selectionBackground", bgSel);
        UIManager.put("CheckBoxMenuItem.selectionForeground", fgSel);
        UIManager.put("RadioButtonMenuItem.selectionBackground", bgSel);
        UIManager.put("RadioButtonMenuItem.selectionForeground", fgSel);
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
