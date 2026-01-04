package org.docknotas.ui;

import org.docknotas.settings.AppSettings;
import org.docknotas.storage.Storage;
import org.docknotas.ui.components.ContextMenuFactory;
import org.docknotas.ui.components.HeaderBar;
import org.docknotas.ui.components.LineRuledTextArea;
import org.docknotas.ui.util.UiTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Janela única que contém:
 * - HeaderBar (barrinha) com botão ×, arrastar janela e menu (clique direito)
 * - Área de notas (LineRuledTextArea) dentro de um "card" com borda/faixa
 * - Tema/zoom/linha/fonte integrados com AppSettings + Storage
 */
public class BlockinhoFrame extends JFrame {

    private final AppSettings settings;
    private final LineRuledTextArea textArea = new LineRuledTextArea();
    private final JPanel card = new JPanel(new BorderLayout()) {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // fundo do card conforme tema
            if ("light".equalsIgnoreCase(settings.getTheme())) {
                g2.setColor(new Color(0xFAFAFA));
            } else {
                g2.setColor(new Color(0x111418));
            }
            int arc = 14;
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            // borda
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(UiTheme.priorityBorder(settings.getPriorityColor()));
            g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, arc, arc);

            // faixa/tags lateral (cor da prioridade)
            g2.setColor(UiTheme.priorityTag(settings.getPriorityColor()));
            g2.fillRoundRect(6, 10, 6, getHeight()-20, 8, 8);

            g2.dispose();
        }
    };

    private JScrollPane scroll;
    private HeaderBar header;

    public BlockinhoFrame(AppSettings settings) {
        super("DockNotas");
        this.settings = settings;

        // aparência básica
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setAlwaysOnTop(settings.isAlwaysOnTop());
        UiTheme.applyLookAndFeel(settings.getTheme());
        getRootPane().putClientProperty("apple.awt.draggableWindowBackground", Boolean.TRUE);

        // tamanho/pos inicial
        Dimension initial = settings.getNotePopupSize() != null ? settings.getNotePopupSize() : new Dimension(420, 520);
        setSize(initial);
        if (settings.getBarLocation() != null) {
            setLocation(settings.getBarLocation());
        } else {
            setLocationRelativeTo(null);
        }

        // header/barrinha dentro da janela
        header = new HeaderBar(() -> {
            try { Storage.saveNotes(textArea.getText()); } catch (Exception ignore) {}
            System.exit(0);
        }, this::showContextMenu);

        add(header, BorderLayout.NORTH);

        // textArea — DIGITÁVEL
        textArea.setEditable(true);
        textArea.setFocusable(true);
        textArea.setRequestFocusEnabled(true);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setText(Storage.loadNotes());
        textArea.setLineHeight(settings.getLineSpacing());
        applyTheme(settings.getTheme());
        applyFontAndZoom();
        textArea.addCaretListener(e -> Storage.saveNotes(textArea.getText()));

        // A rolagem transparente acompanha o card
        scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        // margem interna do card
        card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        card.setOpaque(true);
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);

        // clique em qualquer lugar do card devolve foco ao editor
        card.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestTextFocus();
            }
        });

        // redimensionamento: salva tamanho
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                Dimension d = getSize();
                settings.setNotePopupSize(d);
                Storage.saveSettings(settings);
            }
            @Override public void componentMoved(java.awt.event.ComponentEvent e) {
                Point p = getLocation();
                settings.setBarLocation(p);
                Storage.saveSettings(settings);
            }
        });

        // tema inicial
        applyTheme(settings.getTheme());
    }

    /* ===================== API pública ===================== */

    public void startAccordingToSettings() {
        setVisible(true);
        if (!settings.isStartMinimized()) {
            requestTextFocus();
            showContextMenu(null); // abre o menu uma vez ao iniciar (comportamento original)
        }
    }

    public void applyTheme(String themeName) {
        UiTheme.applyLookAndFeel(themeName);
        UiTheme.MenuColors colors = UiTheme.menuColors(themeName);
        boolean light = !UiTheme.isDark(themeName);
        if (light) {
            textArea.setBackground(new Color(0xFAFAFA));
            textArea.setForeground(Color.DARK_GRAY);
        } else {
            textArea.setBackground(new Color(0x111418));
            textArea.setForeground(new Color(0xE8E8E8));
        }
        if (getJMenuBar() != null) {
            getJMenuBar().setBackground(colors.background());
        }
        card.repaint();
        textArea.repaint();
        SwingUtilities.updateComponentTreeUI(this);
    }

    public void applyFontAndZoom() {
        float px = (float)(settings.getFontSize() * settings.getZoomPercent() / 100.0);
        textArea.setFont(textArea.getFont().deriveFont(px));
        textArea.repaint();
    }

    public void applyLineSpacing(int spacing) {
        textArea.setLineHeight(spacing);
        textArea.repaint();
    }

    public void refreshPriorityColors() {
        card.repaint();
        header.repaint();
    }

    public void requestTextFocus() {
        SwingUtilities.invokeLater(() -> {
            textArea.requestFocusInWindow();
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    /* ===================== menu ===================== */

    private void showContextMenu(MouseEvent trigger) {
        JPopupMenu menu = ContextMenuFactory.build(
                this,
                settings,
                textArea::getText,
                text -> { textArea.setText(text == null ? "" : text); requestTextFocus(); },
                // callbacks de UI:
                () -> { applyTheme(settings.getTheme()); requestTextFocus(); },
                () -> { applyFontAndZoom(); requestTextFocus(); },
                () -> { applyLineSpacing(settings.getLineSpacing()); requestTextFocus(); },
                this::refreshPriorityColors
        );

        menu.setFocusable(false);
        menu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {}
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { requestTextFocus(); }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { requestTextFocus(); }
        });

        // posição: logo abaixo do header (barrinha)
        int x = header.getWidth() / 2;
        int y = header.getHeight() + 4;
        menu.show(header, x, y);
    }
}
