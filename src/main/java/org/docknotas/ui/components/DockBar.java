package org.docknotas.ui.components;

import org.docknotas.settings.AppSettings;
import org.docknotas.storage.Storage;
import org.docknotas.ui.windows.NotesWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Barrinha flutuante que cola nas bordas e abre/fecha o NotesWindow.
 * – Clique esquerdo: abre/fecha
 * – “×”: sair salvando
 * – Arrastar: move; com bloco aberto mantém ancorado ao lado/abaixo
 * – Orientação muda conforme borda mais próxima (H/V)
 */
public class DockBar {

    private final JWindow bar = new JWindow();
    private final Handle handle = new Handle();

    private final NotesWindow notes;
    private final AppSettings settings;

    private final Dimension sizeH = new Dimension(90, 28);
    private final Dimension sizeV = new Dimension(28, 90);

    /* ===== paleta pela prioridade (opacidade = intensidade %) ===== */
    private static class Pri { final Color header; Pri(Color c){ header=c; } }
    private Pri pri(String name){
        if (name == null) name = "cinza";
        int a = (int)(Math.max(40, Math.min(100, settings.getColorStrengthPercent()))/100.0 * 230);
        return switch (name.toLowerCase()) {
            case "vermelho" -> new Pri(new Color(196,44,44,a));
            case "laranja"  -> new Pri(new Color(210,120,30,a));
            case "amarelo"  -> new Pri(new Color(180,160,20,a));
            case "verde"    -> new Pri(new Color(32,140,72,a));
            case "azul"     -> new Pri(new Color(34,93,170,a));
            case "roxo"     -> new Pri(new Color(108,54,160,a));
            default         -> new Pri(new Color(30,30,36,a));
        };
    }

    public DockBar(NotesWindow notes, AppSettings settings) {
        this.notes = notes;
        this.settings = settings;

        bar.setBackground(new Color(0,0,0,0));
        bar.setAlwaysOnTop(settings.isAlwaysOnTop());
        bar.getContentPane().add(handle);
        handle.setPreferredSize(isVertical() ? sizeV : sizeH);
        handle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        handle.setToolTipText("Clique: abrir/fechar • Arraste: mover • ×: sair");
        bar.pack();

        // posição inicial
        if (settings.getBarLocation() != null) {
            bar.setLocation(settings.getBarLocation());
        } else {
            positionTopCenter();
        }

        setOrientation(settings.getBarOrientation());
        hookMouse();

        bar.setVisible(false); // App decide quando mostrar
    }

    /** Repinta cores de prioridade/intensidade (quando alteradas via menu). */
    public void refreshColors() {
        handle.repaint();
        bar.repaint();
        if (notes != null && notes.isVisible()) {
            anchorStackedLayout();
        }
    }

    /* ===================== API ===================== */
    public void startAccordingToSettings() {
        bar.setVisible(true);
        bar.toFront();
        if (settings.isStartMinimized()) {
            snapCollapsedToEdge();
        } else {
            anchorStackedLayout();   // barrinha “em cima” e bloco colado na borda
            notes.showAndFocus();
        }
    }

    /* ===================== interação ===================== */
    private void hookMouse() {
        // Clique normal abre/fecha; clicar no “×” sai
        handle.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (handle.isCloseHit(e.getPoint())) {
                    try { Storage.saveNotes(notes.getEditorText()); } catch (Exception ignore) {}
                    System.exit(0);
                    return;
                }
                if (!notes.isVisible()) {
                    anchorStackedLayout();
                    notes.showAndFocus();
                } else {
                    notes.setVisible(false);
                    snapCollapsedToEdge();
                }
                handle.repaint();
            }
        });

        // Arrastar
        MouseAdapter dragger = new MouseAdapter() {
            Point start;
            @Override public void mousePressed(MouseEvent e) { start = e.getPoint(); }
            @Override public void mouseDragged(MouseEvent e) {
                Point p = bar.getLocation();
                bar.setLocation(p.x + e.getX() - start.x, p.y + e.getY() - start.y);

                if (!notes.isVisible()) {
                    // não “cola” durante o arrasto para evitar tremido; só ajusta ao soltar
                } else {
                    anchorPopup();
                }

                settings.setBarLocation(bar.getLocation());
                Storage.saveSettings(settings);
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (!notes.isVisible()) {
                    snapCollapsedToEdge();
                } else {
                    updateOrientationByPosition();
                    anchorPopup();
                }
                settings.setBarLocation(bar.getLocation());
                Storage.saveSettings(settings);
            }
        };
        handle.addMouseListener(dragger);
        handle.addMouseMotionListener(dragger);
    }

    /* ===================== layout/ancoragem ===================== */

    /** Barrinha “em cima” e bloco colado na borda apropriada. */
    private void anchorStackedLayout() {
        Rectangle s = screenBounds(); Insets in = screenInsets();
        Point b = bar.getLocation();
        boolean vertical = isVertical();

        if (vertical) {
            boolean left = b.x < s.getCenterX();
            int popX = left ? (s.x + in.left + 6) : (s.x + s.width - in.right - notes.getWidth() - 6);
            int popY = clamp(s.y + in.top + 8, b.y - 8, s.y + s.height - in.bottom - notes.getHeight() - 8);
            notes.setLocation(popX, popY);

            int barX = left ? (popX + notes.getWidth() + 6) : (popX - bar.getWidth() - 6);
            int barY = clamp(s.y + in.top + 8, popY - 8, s.y + s.height - in.bottom - bar.getHeight() - 8);
            bar.setLocation(barX, barY);
        } else {
            boolean bottom = b.y > s.getCenterY();
            int popY = bottom ? (s.y + s.height - in.bottom - notes.getHeight() - 6) : (s.y + in.top + 6);
            int popX = clamp(s.x + in.left + 8,
                    b.x - (notes.getWidth()/2) + (bar.getWidth()/2),
                    s.x + s.width - in.right - notes.getWidth() - 8);
            notes.setLocation(popX, popY);

            int barY = bottom ? (popY - bar.getHeight() - 6) : (popY + notes.getHeight() + 6);
            int barX = clamp(s.x + in.left + 8, b.x, s.x + s.width - in.right - bar.getWidth() - 8);
            bar.setLocation(barX, barY);
        }

        settings.setBarLocation(bar.getLocation());
        Storage.saveSettings(settings);
    }

    /** Mantém o bloco ao lado/abaixo enquanto arrasta com bloco aberto. */
    private void anchorPopup() {
        Rectangle s = screenBounds(); Insets in = screenInsets(); Point b = bar.getLocation();
        int px, py;
        if (isVertical()) {
            boolean atRight = b.x + bar.getWidth() > s.x + s.width - 60 - in.right;
            px = atRight ? b.x - notes.getWidth() - 8 : b.x + bar.getWidth() + 8;
            py = clamp(s.y + in.top + 8, b.y, s.y + s.height - notes.getHeight() - in.bottom - 8);
        } else {
            px = clamp(s.x + in.left + 8, b.x, s.x + s.width - notes.getWidth() - in.right - 8);
            py = Math.min(s.y + s.height - in.bottom - notes.getHeight() - 8, b.y + bar.getHeight() + 8);
            py = Math.max(s.y + in.top + 8, py);
        }
        notes.setLocation(px, py);
    }

    /** Cola a barrinha na borda mais próxima quando o bloco está fechado. */
    private void snapCollapsedToEdge() {
        Rectangle s = screenBounds(); Insets in = screenInsets();
        Point p = bar.getLocation();

        int distLeft   = Math.abs(p.x - (s.x + in.left + 4));
        int distRight  = Math.abs((s.x + s.width - in.right - bar.getWidth() - 4) - p.x);
        int distTop    = Math.abs(p.y - (s.y + in.top + 4));
        int distBottom = Math.abs((s.y + s.height - in.bottom - bar.getHeight() - 4) - p.y);

        int min = Math.min(Math.min(distLeft, distRight), Math.min(distTop, distBottom));
        if (min == distLeft || min == distRight) {
            setOrientation("vertical");
            int x = (min == distLeft) ? (s.x + in.left + 4) : (s.x + s.width - in.right - bar.getWidth() - 4);
            int y = clamp(s.y + in.top + 8, p.y, s.y + s.height - in.bottom - bar.getHeight() - 8);
            bar.setLocation(x, y);
        } else {
            setOrientation("horizontal");
            int y = (min == distTop) ? (s.y + in.top + 4) : (s.y + s.height - in.bottom - bar.getHeight() - 4);
            int x = clamp(s.x + in.left + 8, p.x, s.x + s.width - in.right - bar.getWidth() - 8);
            bar.setLocation(x, y);
        }
    }

    private void positionTopCenter() {
        Rectangle s = screenBounds(); Insets in = screenInsets();
        int x = s.x + (s.width - sizeH.width) / 2;
        int y = s.y + in.top + 6;
        bar.setLocation(x, y);
        settings.setBarLocation(new Point(x,y));
        Storage.saveSettings(settings);
    }

    private void setOrientation(String o) {
        String ori = (o != null && o.equalsIgnoreCase("vertical")) ? "vertical" : "horizontal";
        settings.setBarOrientation(ori);
        Storage.saveSettings(settings);
        handle.setOrientation(ori);
        bar.setSize(isVertical() ? sizeV : sizeH);
        bar.validate(); bar.repaint();
    }

    private void updateOrientationByPosition() {
        Point p = bar.getLocation(); Rectangle s = screenBounds(); int margin = 80;
        String o = "horizontal";
        if (p.x <= s.x + margin || p.x + bar.getWidth() >= s.x + s.width - margin) o = "vertical";
        setOrientation(o);
    }

    private boolean isVertical() { return "vertical".equalsIgnoreCase(settings.getBarOrientation()); }

    /* ===================== componente visual ===================== */
    class Handle extends JComponent {
        private String orientation = "horizontal";
        private final int radius = 12;

        void setOrientation(String o) {
            orientation = (o != null && o.equalsIgnoreCase("vertical")) ? "vertical" : "horizontal";
            setPreferredSize("vertical".equals(orientation) ? sizeV : sizeH);
            revalidate(); repaint();
        }

        boolean isCloseHit(Point p){
            Rectangle r = closeRect();
            return r != null && r.contains(p);
        }

        private Rectangle closeRect() {
            int pad = 6, d = 14;
            if ("vertical".equals(orientation)) return new Rectangle((getWidth()-d)/2, pad, d, d);
            return new Rectangle(getWidth()-d-pad, pad, d, d);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            g2.setColor(pri(settings.getPriorityColor()).header);
            g2.fillRoundRect(0,0,w,h,radius,radius);

            // listras (grip)
            g2.setColor(new Color(255,255,255,190));
            if ("vertical".equals(orientation)) {
                int midX = w/2, lineH = h/3;
                g2.fillRoundRect(midX-1, lineH-8, 3, 28, 6, 6);
                g2.fillRoundRect(midX-1, 2*lineH-8, 3, 28, 6, 6);
            } else {
                int midY = h/2, lineW = w/3, x = (w-lineW)/2;
                g2.fillRoundRect(x, midY-7, lineW, 3, 6, 6);
                g2.fillRoundRect(x, midY+4, lineW, 3, 6, 6);
            }

            // botão ×  (trecho igual ao que você referenciou)
            Rectangle xr = closeRect();
            g2.setColor(new Color(0,0,0,110));
            g2.fillOval(xr.x, xr.y, xr.width, xr.height);
            g2.setColor(new Color(255,255,255,220));
            int cx = xr.x + xr.width/2, cy = xr.y + xr.height/2;
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(cx-3, cy-3, cx+3, cy+3);
            g2.drawLine(cx+3, cy-3, cx-3, cy+3);
        }
    }

    /* ===================== util tela ===================== */
    private Rectangle screenBounds() {
        GraphicsConfiguration gc = bar.getGraphicsConfiguration();
        return gc.getBounds();
    }
    private Insets screenInsets() {
        GraphicsConfiguration gc = bar.getGraphicsConfiguration();
        return Toolkit.getDefaultToolkit().getScreenInsets(gc);
    }
    private int clamp(int min, int val, int max){ return Math.max(min, Math.min(max, val)); }
}
