package org.docknotas.ui.windows;

import org.docknotas.settings.AppSettings;
import org.docknotas.storage.Storage;
import org.docknotas.ui.components.LineRuledTextArea;
import org.docknotas.ui.util.UiTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.List;

/**
 * Janela única com menu + editor de notas.
 * - Tema dark/light
 * - Fonte base + Zoom (%)
 * - Espaçamento de linhas
 * - Cor de prioridade (borda/faixa) + intensidade
 * - Persistência de conteúdo e tamanho
 * - Atalho Ctrl+S para salvar
 */
public class NotesWindow extends JFrame {

    private final AppSettings settings;
    private final LineRuledTextArea editor = new LineRuledTextArea();
    private Runnable onPriorityOrThemeChanged = null;
    private JSlider zoomSlider;
    private JLabel  zoomLabel;

    // Cores da moldura/faixa baseadas na prioridade (na view)
    private static class PriColor {
        final Color border, tag;
        PriColor(Color border, Color tag) { this.border = border; this.tag = tag; }
    }

    private PriColor pickPri(String name) {
        if (name == null) name = "cinza";
        return switch (name.toLowerCase()) {
            case "vermelho" -> new PriColor(new Color(220,80,80),   new Color(232,72,72));
            case "laranja"  -> new PriColor(new Color(235,160,70),  new Color(242,178,102));
            case "amarelo"  -> new PriColor(new Color(220,200,60),  new Color(236,220,114));
            case "verde"    -> new PriColor(new Color(70,185,120),  new Color(108,208,154));
            case "azul"     -> new PriColor(new Color(84,140,210),  new Color(122,168,222));
            case "roxo"     -> new PriColor(new Color(148,98,200),  new Color(174,132,214));
            default         -> new PriColor(new Color(88,88,96),    new Color(128,128,136));
        };
    }

    private float effectiveFont() {
        return (float)(settings.getFontSize() * settings.getZoomPercent() / 100.0);
    }

    public NotesWindow(AppSettings settings) {
        super("DockNotas");
        this.settings = settings;
        setUndecorated(true);
        setBackground(new Color(0,0,0,0));

        // >>> único acréscimo: ícone da janela <<<
        setIconImages(loadAppIcons());

        // comportamento básico
        setAlwaysOnTop(settings.isAlwaysOnTop());
        setMinimumSize(new Dimension(340, 320));
        Dimension saved = settings.getNotePopupSize();
        if (saved != null) setSize(saved); else setSize(360, 360);
        updateWindowShape();

        // MENU
        setJMenuBar(buildMenuBar());

        // EDITOR
        editor.setEditable(true);
        editor.setFocusable(true);
        editor.setRequestFocusEnabled(true);
        editor.setLineWrap(true);
        editor.setWrapStyleWord(true);
        editor.setText(Storage.loadNotes());
        editor.setLineHeight(settings.getLineSpacing());
        editor.setFont(editor.getFont().deriveFont(effectiveFont()));

        // Salva automaticamente ao digitar
        editor.addCaretListener(e -> Storage.saveNotes(editor.getText()));

        // Atalho Ctrl+S
        bindSaveAccelerator(editor);

        // Card visual (fundo+bordas+faixa)
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // corpo
                Color body = isDarkTheme() ? new Color(17,20,24,230) : new Color(250,250,250,245);
                g2.setColor(body);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);

                // borda + faixa
                PriColor pc = pickPri(settings.getPriorityColor());
                int alpha = (int)(Math.max(40, Math.min(100, settings.getColorStrengthPercent())) * 2.3); // ~0..230
                Color border = new Color(pc.border.getRed(), pc.border.getGreen(), pc.border.getBlue(), alpha);
                Color tag    = new Color(pc.tag.getRed(), pc.tag.getGreen(), pc.tag.getBlue(), alpha);

                g2.setStroke(new BasicStroke(2f));
                g2.setColor(border);
                g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,14,14);

                g2.setColor(tag);
                g2.fillRoundRect(8, 12, 6, getHeight()-24, 8, 8);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10,10,10,10));

        JScrollPane sc = new JScrollPane(editor);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.setOpaque(false);
        sc.getViewport().setOpaque(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(8,8,8,8));
        content.setOpaque(true);
        setContentPane(content);

        content.add(card, BorderLayout.CENTER);
        card.add(sc, BorderLayout.CENTER);

        // Tema inicial
        applyTheme(settings.getTheme());

        // Persistir tamanho ao redimensionar
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                settings.setNotePopupSize(getSize());
                Storage.saveSettings(settings);
                updateWindowShape();
            }
        });

        // Quando a janela ganhar foco, empurra para o editor
        addWindowFocusListener(new WindowAdapter() {
            @Override public void windowGainedFocus(WindowEvent e) {
                SwingUtilities.invokeLater(() -> editor.requestFocusInWindow());
            }
        });
    }

    // carrega os ícones do resources (múltiplos tamanhos para HiDPI)
    private List<Image> loadAppIcons() {
        String base = "/icons/docknotas-";
        int[] sizes = {16, 32, 48, 64, 128, 256};
        java.util.ArrayList<Image> list = new java.util.ArrayList<>();
        for (int s : sizes) {
            java.net.URL url = getClass().getResource(base + s + ".png");
            if (url != null) list.add(new ImageIcon(url).getImage());
        }
        // fallback: se nada encontrado, retorna lista vazia (Swing usa default)
        return list;
    }

    /* ===================== Menu ===================== */

    private JMenuBar buildMenuBar() {
        UiTheme.MenuColors colors = UiTheme.menuColors(settings.getTheme());
        JMenuBar mb = new JMenuBar();
        mb.setOpaque(true);
        mb.setBackground(colors.background());

        // FILE
        JMenu file = new JMenu("File");
        styleMenu(file, colors);
        file.add(item("New (clear)", () -> {
            if (confirm("Clear current notes?")) {
                editor.setText("");
                Storage.saveNotes("");
            }
        }));
        file.add(item("Save", () -> Storage.saveNotes(editor.getText())));
        file.add(item("Save As...", this::doSaveAs));
        file.addSeparator();
        file.add(item("Import .txt...", this::doImportTxt));
        file.add(item("Open notes folder", this::openNotesFolder));
        file.add(item("Backup now", Storage::backupNow));
        file.addSeparator();
        file.add(item("Exit", () -> {
            try { Storage.saveNotes(editor.getText()); Storage.saveSettings(settings); } catch (Exception ignore) {}
            System.exit(0);
        }));
        mb.add(file);

        // VIEW
        JMenu view = new JMenu("View");
        styleMenu(view, colors);

        JMenu theme = new JMenu("Theme");
        styleMenu(theme, colors);
        ButtonGroup tg = new ButtonGroup();
        JRadioButtonMenuItem dark  = new JRadioButtonMenuItem("Dark",  isDarkTheme());
        JRadioButtonMenuItem light = new JRadioButtonMenuItem("Light", !isDarkTheme());
        styleMenuItem(dark, colors);
        styleMenuItem(light, colors);
        tg.add(dark); tg.add(light);
        theme.add(dark); theme.add(light);
        dark.addActionListener(e -> { settings.setTheme("dark");  applyTheme("dark");  Storage.saveSettings(settings); });
        light.addActionListener(e -> { settings.setTheme("light"); applyTheme("light"); Storage.saveSettings(settings); });
        view.add(theme);

        JMenu fonte = new JMenu("Font (base)");
        styleMenu(fonte, colors);
        fonte.add(item("Increase", () -> { settings.setFontSize(settings.getFontSize()+1); applyFontSize(settings.getFontSize()); Storage.saveSettings(settings);} ));
        fonte.add(item("Decrease", () -> { settings.setFontSize(Math.max(10, settings.getFontSize()-1)); applyFontSize(settings.getFontSize()); Storage.saveSettings(settings);} ));
        view.add(fonte);

        JMenu zoom = new JMenu("Zoom (%)");
        styleMenu(zoom, colors);
        zoom.add(item("+10%", () -> applyZoom(settings.getZoomPercent()+10)));
        zoom.add(item("-10%", () -> applyZoom(settings.getZoomPercent()-10)));
        zoom.add(item("Reset (100%)", () -> applyZoom(100)));
        view.add(zoom);

        JMenu linhas = new JMenu("Line spacing");
        styleMenu(linhas, colors);
        linhas.add(item("More space", () -> { settings.setLineSpacing(settings.getLineSpacing()+2); editor.setLineHeight(settings.getLineSpacing()); editor.repaint(); Storage.saveSettings(settings);} ));
        linhas.add(item("Less space", () -> { settings.setLineSpacing(Math.max(12, settings.getLineSpacing()-2)); editor.setLineHeight(settings.getLineSpacing()); editor.repaint(); Storage.saveSettings(settings);} ));
        view.add(linhas);

        JCheckBoxMenuItem onTop = new JCheckBoxMenuItem("Always on top", settings.isAlwaysOnTop());
        styleMenuItem(onTop, colors);
        onTop.addActionListener(e -> {
            boolean v = onTop.isSelected();
            settings.setAlwaysOnTop(v);
            setAlwaysOnTop(v);
            Storage.saveSettings(settings);
        });
        view.addSeparator();
        view.add(onTop);

        mb.add(view);

        // PRIORITY
        JMenu pri = new JMenu("Priority/Color");
        styleMenu(pri, colors);
        ButtonGroup pg = new ButtonGroup();
        for (String opt : new String[]{"Vermelho","Laranja","Amarelo","Verde","Azul","Roxo","Cinza"}) {
            JRadioButtonMenuItem it = new JRadioButtonMenuItem(opt,
                    opt.equalsIgnoreCase(settings.getPriorityColor()));
            styleMenuItem(it, colors);
            it.addActionListener(e -> {
                settings.setPriorityColor(opt.toLowerCase());
                Storage.saveSettings(settings);
                repaint(); // repinta a faixa/borda
                notifyPriorityOrThemeChanged();
            });
            pg.add(it); pri.add(it);
        }

        JMenu intensidade = new JMenu("Intensity (%)");
        styleMenu(intensidade, colors);
        intensidade.add(item("+10%", () -> { settings.setColorStrengthPercent(Math.min(100, settings.getColorStrengthPercent()+10)); Storage.saveSettings(settings); repaint(); }));
        intensidade.add(item("-10%", () -> { settings.setColorStrengthPercent(Math.max(40, settings.getColorStrengthPercent()-10)); Storage.saveSettings(settings); repaint(); }));
        intensidade.add(item("Reset (100%)", () -> { settings.setColorStrengthPercent(100); Storage.saveSettings(settings); repaint(); }));

        mb.add(pri);
        mb.add(intensidade);

        // HELP
        JMenu help = new JMenu("Help");
        styleMenu(help, colors);
        help.add(item("Shortcuts", () -> info("""
                • Ctrl+S: salvar
                • View → Theme: alterna tema
                • View → Font/Zoom/Line spacing
                • Priority/Color: define faixa/borda
                """)));
        help.add(item("About", () -> info("DockNotas — janela única com menu + editor")));
        mb.add(help);

        return mb;
    }

    private JMenuItem item(String text, Runnable r) {
        JMenuItem i = new JMenuItem(text);
        styleMenuItem(i, UiTheme.menuColors(settings.getTheme()));
        i.addActionListener(e -> r.run());
        return i;
    }

    /* ===================== Ações de arquivo ===================== */

    private void doSaveAs() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("DockNotas.txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try { Storage.exportTo(fc.getSelectedFile()); }
            catch (Exception ex) { error(ex.getMessage()); }
        }
    }

    private void doImportTxt() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Storage.importFrom(fc.getSelectedFile());
                editor.setText(Storage.loadNotes());
            } catch (Exception ex) { error(ex.getMessage()); }
        }
    }

    private void openNotesFolder() {
        try { Desktop.getDesktop().open(Storage.notesFolder()); }
        catch (Exception ex) { error(ex.getMessage()); }
    }

    /* ===================== UI helpers ===================== */

    private void applyTheme(String theme) {
        UiTheme.applyLookAndFeel(theme);
        UiTheme.MenuColors colors = UiTheme.menuColors(theme);
        boolean dark = UiTheme.isDark(theme);
        if (dark) {
            editor.setBackground(new Color(0x111418));
            editor.setForeground(new Color(0xE8E8E8));
        } else {
            editor.setBackground(new Color(0xFAFAFA));
            editor.setForeground(Color.DARK_GRAY);
        }
        editor.repaint();
        getContentPane().setBackground(dark ? new Color(12,14,18) : new Color(240,242,245));
        restyleMenuBar(colors);
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
        notifyPriorityOrThemeChanged();
    }

    private boolean isDarkTheme() {
        return "dark".equalsIgnoreCase(settings.getTheme());
    }

    private void applyFontSize(int base) {
        editor.setFont(editor.getFont().deriveFont((float)(base * settings.getZoomPercent()/100.0)));
        editor.repaint();
    }

    private void applyZoom(int percent) {
        settings.setZoomPercent(Math.max(50, Math.min(200, percent)));
        Storage.saveSettings(settings);
        editor.setFont(editor.getFont().deriveFont(effectiveFont()));
        editor.repaint();
    }

    private void bindSaveAccelerator(JComponent root) {
        // Ctrl+S salva
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "saveNotes");
        root.getActionMap().put("saveNotes", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                Storage.saveNotes(editor.getText());
                // Feedback discreto
                editor.putClientProperty("savedAt", System.currentTimeMillis());
            }
        });
    }

    private boolean confirm(String msg){ return JOptionPane.showConfirmDialog(this,msg,"Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION; }
    private void info(String msg){ JOptionPane.showMessageDialog(this,msg,"Info",JOptionPane.INFORMATION_MESSAGE); }
    private void error(String msg){ JOptionPane.showMessageDialog(this,msg,"Erro",JOptionPane.ERROR_MESSAGE); }

    private void styleMenu(JMenu menu, UiTheme.MenuColors colors) {
        menu.setOpaque(true);
        menu.setBackground(colors.background());
        menu.setForeground(colors.foreground());
        if (menu.getPopupMenu() != null) {
            menu.getPopupMenu().setOpaque(true);
            menu.getPopupMenu().setBackground(colors.background());
        }
    }

    private void styleMenuItem(AbstractButton item, UiTheme.MenuColors colors) {
        item.setOpaque(true);
        item.setBackground(colors.background());
        item.setForeground(colors.foreground());
    }

    private void restyleMenuBar(UiTheme.MenuColors colors) {
        JMenuBar mb = getJMenuBar();
        if (mb == null) return;
        mb.setOpaque(true);
        mb.setBackground(colors.background());
        for (MenuElement el : mb.getSubElements()) {
            if (el instanceof JMenu menu) {
                styleMenu(menu, colors);
                for (MenuElement child : menu.getSubElements()) {
                    if (child instanceof JMenuItem mi) {
                        styleMenuItem(mi, colors);
                    }
                }
            }
        }
    }

    private void updateWindowShape() {
        if (isUndecorated()) {
            Shape shape = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14);
            setShape(shape);
        }
    }

    public void setOnPriorityOrThemeChanged(Runnable r) {
        this.onPriorityOrThemeChanged = r;
    }

    private void notifyPriorityOrThemeChanged() {
        if (onPriorityOrThemeChanged != null) onPriorityOrThemeChanged.run();
    }

    /* ===================== API usada pelo App ===================== */

    /** Retorna o conteúdo atual do editor. */
    public String getEditorText() { return editor.getText(); }

    /** Pede foco ao editor após mostrar a janela. */
    public void focusEditorSoon() {
        SwingUtilities.invokeLater(() -> {
            editor.requestFocusInWindow();
            editor.setCaretPosition(editor.getDocument().getLength());
        });
    }

    /** Dá pack só se fizer sentido (para manter tamanho salvo). */
    public void packIfNeeded() {
        if (getWidth() <= 0 || getHeight() <= 0) pack();
    }

    public void showAndFocus() {
        if (!isVisible()) setVisible(true);
        toFront();
        requestFocus();
        SwingUtilities.invokeLater(() -> {
            editor.requestFocusInWindow();
            try { editor.setCaretPosition(editor.getDocument().getLength()); } catch (Exception ignore) {}
        });
    }
}
