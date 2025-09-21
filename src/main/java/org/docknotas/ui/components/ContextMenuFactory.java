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
        UiTheme.applyGlobalMenuHoverTheme();

        JPopupMenu menu = new JPopupMenu();
        menu.setOpaque(true);
        menu.setBackground(UiTheme.MENU_BG);
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.MENU_BORDER,1,true),
                new EmptyBorder(6,8,6,8)
        ));

        // FILE
        JMenu file = theMenu("File");
        file.add(mi("New (clear)", () -> {
            if (confirm(anchor, "Clear current notes?")) {
                setText.accept("");
                Storage.saveNotes("");
            }
        }));
        file.add(mi("Save", () -> Storage.saveNotes(getText.get())));
        file.add(mi("Save As...", () -> {
            JFileChooser fc = new JFileChooser(); fc.setSelectedFile(new File("DockNotas.txt"));
            if (fc.showSaveDialog(anchor)==JFileChooser.APPROVE_OPTION) {
                try { Storage.exportTo(fc.getSelectedFile()); } catch (Exception ex) { error(anchor, ex.getMessage()); }
            }
        }));
        file.addSeparator();
        file.add(mi("Import .txt...", () -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(anchor)==JFileChooser.APPROVE_OPTION) try {
                Storage.importFrom(fc.getSelectedFile());
                setText.accept(Storage.loadNotes());
            } catch (Exception ex) { error(anchor, ex.getMessage()); }
        }));
        file.add(mi("Open notes folder", () -> { try { Desktop.getDesktop().open(Storage.notesFolder()); } catch (Exception ex) { error(anchor, ex.getMessage()); } }));
        file.add(mi("Backup now", Storage::backupNow));
        file.addSeparator();
        file.add(mi("Exit", () -> {
            try { Storage.saveNotes(getText.get()); } catch (Exception ignore) {}
            System.exit(0);
        }));
        menu.add(file);

        // CONFIG
        JMenu cfg = theMenu("Configurações");

        JCheckBoxMenuItem onTop = themedCheck("Sempre no topo", settings.isAlwaysOnTop());
        onTop.addActionListener(e -> { settings.setAlwaysOnTop(onTop.isSelected()); Storage.saveSettings(settings); SwingUtilities.getWindowAncestor(anchor).setAlwaysOnTop(onTop.isSelected()); });
        cfg.add(onTop);

        JCheckBoxMenuItem startMin = themedCheck("Iniciar minimizado (só barrinha)", settings.isStartMinimized());
        startMin.addActionListener(e -> { settings.setStartMinimized(startMin.isSelected()); Storage.saveSettings(settings); });
        cfg.add(startMin);

        cfg.addSeparator();

        JMenu theme = theMenu("Tema");
        ButtonGroup tg = new ButtonGroup();
        JRadioButtonMenuItem dark = themedRadio("Dark", "dark".equalsIgnoreCase(settings.getTheme()));
        JRadioButtonMenuItem light = themedRadio("Light", "light".equalsIgnoreCase(settings.getTheme()));
        tg.add(dark); tg.add(light); theme.add(dark); theme.add(light);
        dark.addActionListener(e -> { settings.setTheme("dark"); Storage.saveSettings(settings); onThemeApplied.run(); });
        light.addActionListener(e -> { settings.setTheme("light"); Storage.saveSettings(settings); onThemeApplied.run(); });
        cfg.add(theme);

        JMenu fonte = theMenu("Fonte (base)");
        fonte.add(mi("Aumentar", () -> { settings.setFontSize(settings.getFontSize()+1); Storage.saveSettings(settings); onFontOrZoomApplied.run(); } ));
        fonte.add(mi("Diminuir", () -> { settings.setFontSize(Math.max(10, settings.getFontSize()-1)); Storage.saveSettings(settings); onFontOrZoomApplied.run(); } ));
        cfg.add(fonte);

        JMenu zoom = theMenu("Zoom (%)");
        zoom.add(mi("+10%", () -> { settings.setZoomPercent(Math.min(200, settings.getZoomPercent()+10)); Storage.saveSettings(settings); onFontOrZoomApplied.run(); }));
        zoom.add(mi("-10%", () -> { settings.setZoomPercent(Math.max(50,  settings.getZoomPercent()-10)); Storage.saveSettings(settings); onFontOrZoomApplied.run(); }));
        zoom.add(mi("Reset (100%)", () -> { settings.setZoomPercent(100); Storage.saveSettings(settings); onFontOrZoomApplied.run(); }));
        cfg.add(zoom);

        JMenu linhas = theMenu("Espaçamento");
        linhas.add(mi("Mais espaço", () -> { settings.setLineSpacing(settings.getLineSpacing()+2); Storage.saveSettings(settings); onLineSpacingApplied.run(); } ));
        linhas.add(mi("Menos espaço", () -> { settings.setLineSpacing(Math.max(12, settings.getLineSpacing()-2)); Storage.saveSettings(settings); onLineSpacingApplied.run(); } ));
        cfg.add(linhas);

        JMenu prioridade = theMenu("Prioridade/Cor");
        ButtonGroup pg = new ButtonGroup();
        for (String opt : new String[]{"Vermelho","Laranja","Amarelo","Verde","Azul","Roxo","Cinza"}) {
            JRadioButtonMenuItem it = themedRadio(opt, opt.equalsIgnoreCase(settings.getPriorityColor()));
            it.addActionListener(e -> { settings.setPriorityColor(opt.toLowerCase()); Storage.saveSettings(settings); onPriorityApplied.run(); });
            pg.add(it); prioridade.add(it);
        }
        cfg.add(prioridade);

        JMenu intensidade = theMenu("Intensidade da cor (%)");
        intensidade.add(mi("+10%", () -> { settings.setColorStrengthPercent(Math.min(100, settings.getColorStrengthPercent()+10)); Storage.saveSettings(settings); }));
        intensidade.add(mi("-10%", () -> { settings.setColorStrengthPercent(Math.max(40,  settings.getColorStrengthPercent()-10)); Storage.saveSettings(settings); }));
        intensidade.add(mi("Reset (100%)", () -> { settings.setColorStrengthPercent(100); Storage.saveSettings(settings); }));
        cfg.add(intensidade);

        menu.add(cfg);

        // AJUDA
        JMenu help = theMenu("Ajuda");
        help.add(mi("Atalhos", () -> info(anchor, """
                • Clique na barrinha: abre menu / arrasta a janela
                • ×: sair do programa
                • Clique direito na barrinha: menu
                • Ctrl+S: salvar
                """)));
        help.add(mi("Sobre", () -> info(anchor, "DockNotas — barrinha + bloco com linhas (janela única)")));
        menu.add(help);

        return menu;
    }

    /* ===== helpers ===== */

    private static JMenu theMenu(String t){
        JMenu m = new JMenu(t);
        m.setForeground(UiTheme.MENU_FG);
        m.setOpaque(true);
        m.setBackground(UiTheme.MENU_BG);
        m.getPopupMenu().setOpaque(true);
        m.getPopupMenu().setBackground(UiTheme.MENU_BG);
        return m;
    }

    private static JMenuItem mi(String text, Runnable r){
        JMenuItem i = new JMenuItem(text);
        i.setForeground(UiTheme.MENU_FG);
        i.setBackground(UiTheme.MENU_BG);
        i.setOpaque(true);
        i.setBorderPainted(false);
        i.setFocusPainted(false);
        i.addActionListener(e->r.run());
        return i;
    }

    private static JRadioButtonMenuItem themedRadio(String t, boolean sel){
        JRadioButtonMenuItem r=new JRadioButtonMenuItem(t,sel);
        r.setForeground(UiTheme.MENU_FG);
        r.setBackground(UiTheme.MENU_BG);
        r.setOpaque(true);
        r.setFocusPainted(false);
        return r;
    }

    private static JCheckBoxMenuItem themedCheck(String t, boolean sel){
        JCheckBoxMenuItem c=new JCheckBoxMenuItem(t,sel);
        c.setForeground(UiTheme.MENU_FG);
        c.setBackground(UiTheme.MENU_BG);
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
