package org.docknotas.ui.components;

import org.docknotas.settings.AppSettings;
import org.docknotas.storage.Storage;
import org.docknotas.ui.util.UiTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class ContextMenuFactory {

    public static JPopupMenu build(
            Component anchor,
            AppSettings settings,
            java.util.function.Supplier<String> getText,
            java.util.function.Consumer<String> setText,
            Runnable onThemeApplied,
            Runnable onFontOrZoomApplied,
            Runnable onLineSpacingApplied,
            Runnable onPriorityApplied
    ) {
        UiTheme.applyGlobalMenuHoverTheme(settings.getTheme());
        UiTheme.MenuColors colors = UiTheme.menuColors(settings.getTheme());

        JPopupMenu menu = new JPopupMenu();
        menu.setOpaque(true);
        menu.setBackground(colors.background());
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colors.border(),1,true),
                new EmptyBorder(6,8,6,8)
        ));

        // FILE
        JMenu file = theMenu("File", colors);
        file.add(mi("New (clear)", colors, () -> {
            if (confirm(anchor, "Clear current notes?")) {
                setText.accept("");
                Storage.saveNotes("");
            }
        }));
        file.add(mi("Save", colors, () -> Storage.saveNotes(getText.get())));
        file.add(mi("Save As...", colors, () -> {
            JFileChooser fc = new JFileChooser(); fc.setSelectedFile(new File("DockNotas.txt"));
            if (fc.showSaveDialog(anchor)==JFileChooser.APPROVE_OPTION) {
                try { Storage.exportTo(fc.getSelectedFile()); } catch (Exception ex) { error(anchor, ex.getMessage()); }
            }
        }));
        file.addSeparator();
        file.add(mi("Import .txt...", colors, () -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(anchor)==JFileChooser.APPROVE_OPTION) try {
                Storage.importFrom(fc.getSelectedFile());
                setText.accept(Storage.loadNotes());
            } catch (Exception ex) { error(anchor, ex.getMessage()); }
        }));
        file.add(mi("Open notes folder", colors, () -> { try { Desktop.getDesktop().open(Storage.notesFolder()); } catch (Exception ex) { error(anchor, ex.getMessage()); } }));
        file.add(mi("Backup now", colors, Storage::backupNow));
        file.addSeparator();
        file.add(mi("Exit", colors, () -> {
            try { Storage.saveNotes(getText.get()); } catch (Exception ignore) {}
            System.exit(0);
        }));
        menu.add(file);

        // CONFIG
        JMenu cfg = theMenu("Configurações", colors);

        JCheckBoxMenuItem onTop = themedCheck("Sempre no topo", colors, settings.isAlwaysOnTop());
        onTop.addActionListener(e -> { settings.setAlwaysOnTop(onTop.isSelected()); Storage.saveSettings(settings); SwingUtilities.getWindowAncestor(anchor).setAlwaysOnTop(onTop.isSelected()); });
        cfg.add(onTop);

        JCheckBoxMenuItem startMin = themedCheck("Iniciar minimizado (só barrinha)", colors, settings.isStartMinimized());
        startMin.addActionListener(e -> { settings.setStartMinimized(startMin.isSelected()); Storage.saveSettings(settings); });
        cfg.add(startMin);

        cfg.addSeparator();

        JMenu theme = theMenu("Tema", colors);
        ButtonGroup tg = new ButtonGroup();
        JRadioButtonMenuItem dark = themedRadio("Dark", colors, "dark".equalsIgnoreCase(settings.getTheme()));
        JRadioButtonMenuItem light = themedRadio("Light", colors, "light".equalsIgnoreCase(settings.getTheme()));
        tg.add(dark); tg.add(light); theme.add(dark); theme.add(light);
        dark.addActionListener(e -> { settings.setTheme("dark"); Storage.saveSettings(settings); onThemeApplied.run(); });
        light.addActionListener(e -> { settings.setTheme("light"); Storage.saveSettings(settings); onThemeApplied.run(); });
        cfg.add(theme);

        JMenu fonte = theMenu("Fonte (base)", colors);
        fonte.add(mi("Aumentar", colors, () -> { settings.setFontSize(settings.getFontSize()+1); Storage.saveSettings(settings); onFontOrZoomApplied.run(); } ));
        fonte.add(mi("Diminuir", colors, () -> { settings.setFontSize(Math.max(10, settings.getFontSize()-1)); Storage.saveSettings(settings); onFontOrZoomApplied.run(); } ));
        cfg.add(fonte);

        JMenu zoom = theMenu("Zoom (%)", colors);
        zoom.add(mi("+10%", colors, () -> { settings.setZoomPercent(Math.min(200, settings.getZoomPercent()+10)); Storage.saveSettings(settings); onFontOrZoomApplied.run(); }));
        zoom.add(mi("-10%", colors, () -> { settings.setZoomPercent(Math.max(50,  settings.getZoomPercent()-10)); Storage.saveSettings(settings); onFontOrZoomApplied.run(); }));
        zoom.add(mi("Reset (100%)", colors, () -> { settings.setZoomPercent(100); Storage.saveSettings(settings); onFontOrZoomApplied.run(); }));
        cfg.add(zoom);

        JMenu linhas = theMenu("Espaçamento", colors);
        linhas.add(mi("Mais espaço", colors, () -> { settings.setLineSpacing(settings.getLineSpacing()+2); Storage.saveSettings(settings); onLineSpacingApplied.run(); } ));
        linhas.add(mi("Menos espaço", colors, () -> { settings.setLineSpacing(Math.max(12, settings.getLineSpacing()-2)); Storage.saveSettings(settings); onLineSpacingApplied.run(); } ));
        cfg.add(linhas);

        JMenu prioridade = theMenu("Prioridade/Cor", colors);
        ButtonGroup pg = new ButtonGroup();
        for (String opt : new String[]{"Vermelho","Laranja","Amarelo","Verde","Azul","Roxo","Cinza"}) {
            JRadioButtonMenuItem it = themedRadio(opt, colors, opt.equalsIgnoreCase(settings.getPriorityColor()));
            it.addActionListener(e -> { settings.setPriorityColor(opt.toLowerCase()); Storage.saveSettings(settings); onPriorityApplied.run(); });
            pg.add(it); prioridade.add(it);
        }
        cfg.add(prioridade);

        JMenu intensidade = theMenu("Intensidade da cor (%)", colors);
        intensidade.add(mi("+10%", colors, () -> { settings.setColorStrengthPercent(Math.min(100, settings.getColorStrengthPercent()+10)); Storage.saveSettings(settings); }));
        intensidade.add(mi("-10%", colors, () -> { settings.setColorStrengthPercent(Math.max(40,  settings.getColorStrengthPercent()-10)); Storage.saveSettings(settings); }));
        intensidade.add(mi("Reset (100%)", colors, () -> { settings.setColorStrengthPercent(100); Storage.saveSettings(settings); }));
        cfg.add(intensidade);

        menu.add(cfg);

        // AJUDA
        JMenu help = theMenu("Ajuda", colors);
        help.add(mi("Atalhos", colors, () -> info(anchor, """
                • Clique na barrinha: abre menu / arrasta a janela
                • ×: sair do programa
                • Clique direito na barrinha: menu
                • Ctrl+S: salvar
                """)));
        help.add(mi("Sobre", colors, () -> info(anchor, "DockNotas — barrinha + bloco com linhas (janela única)")));
        menu.add(help);

        return menu;
    }

    /* ===== helpers ===== */

    private static JMenu theMenu(String t, UiTheme.MenuColors colors){
        JMenu m = new JMenu(t);
        m.setForeground(colors.foreground());
        m.setOpaque(true);
        m.setBackground(colors.background());
        m.getPopupMenu().setOpaque(true);
        m.getPopupMenu().setBackground(colors.background());
        return m;
    }

    private static JMenuItem mi(String text, UiTheme.MenuColors colors, Runnable r){
        JMenuItem i = new JMenuItem(text);
        i.setForeground(colors.foreground());
        i.setBackground(colors.background());
        i.setOpaque(true);
        i.setBorderPainted(false);
        i.setFocusPainted(false);
        i.addActionListener(e->r.run());
        return i;
    }

    private static JRadioButtonMenuItem themedRadio(String t, UiTheme.MenuColors colors, boolean sel){
        JRadioButtonMenuItem r=new JRadioButtonMenuItem(t,sel);
        r.setForeground(colors.foreground());
        r.setBackground(colors.background());
        r.setOpaque(true);
        r.setFocusPainted(false);
        return r;
    }

    private static JCheckBoxMenuItem themedCheck(String t, UiTheme.MenuColors colors, boolean sel){
        JCheckBoxMenuItem c=new JCheckBoxMenuItem(t,sel);
        c.setForeground(colors.foreground());
        c.setBackground(colors.background());
        c.setOpaque(true);
        c.setFocusPainted(false);
        return c;
    }

    private static boolean confirm(Component parent, String msg){
        return JOptionPane.showConfirmDialog(parent,msg,"Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;
    }
    private static void info(Component parent, String msg){ JOptionPane.showMessageDialog(parent,msg,"Info",JOptionPane.INFORMATION_MESSAGE); }
    private static void error(Component parent, String msg){ JOptionPane.showMessageDialog(parent,msg,"Erro",JOptionPane.ERROR_MESSAGE); }
}
