package org.docknotas.ui.components;

import org.docknotas.ui.util.UiTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Barrinha interna da janela (draggable + botão × + abre menu com direito) */
public class HeaderBar extends JComponent {
    private final Runnable onClose;
    private final java.util.function.Consumer<MouseEvent> onShowMenu;

    private Point dragStart = null;

    public HeaderBar(Runnable onClose, java.util.function.Consumer<MouseEvent> onShowMenu) {
        this.onClose = onClose;
        this.onShowMenu = onShowMenu;
        setPreferredSize(new Dimension(120, 28));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Clique e arraste para mover • Direito: menu • ×: sair");

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                // clique direito = menu
                if (SwingUtilities.isRightMouseButton(e) ||
                        (e.isControlDown() && SwingUtilities.isLeftMouseButton(e))) {
                    if (onShowMenu != null) onShowMenu.accept(e);
                    return;
                }
                // começa arrastar
                dragStart = e.getPoint();

                // clicou no × ?
                if (closeRect().contains(e.getPoint())) {
                    if (onClose != null) onClose.run();
                }
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (dragStart == null) return;
                Window w = SwingUtilities.getWindowAncestor(HeaderBar.this);
                if (w != null) {
                    Point loc = w.getLocation();
                    int nx = loc.x + e.getX() - dragStart.x;
                    int ny = loc.y + e.getY() - dragStart.y;
                    w.setLocation(nx, ny);
                }
            }
            @Override public void mouseReleased(MouseEvent e) { dragStart = null; }
            @Override public void mouseClicked(MouseEvent e) {
                // clique simples esquerdo abre menu também (toque de atalho)
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    if (onShowMenu != null) onShowMenu.accept(e);
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    private Rectangle closeRect() {
        int pad = 6, d = 14;
        return new Rectangle(getWidth()-d-pad, pad, d, d);
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        int radius = 12;

        g2.setColor(UiTheme.headerColor());
        g2.fillRoundRect(0,0,w,h,radius,radius);

        // listras
        g2.setColor(new Color(255,255,255,190));
        int midY = h/2, lineW = w/3, x = (w-lineW)/2;
        g2.fillRoundRect(x, midY-7, lineW, 3, 6, 6);
        g2.fillRoundRect(x, midY+4, lineW, 3, 6, 6);

        // botão ×
        Rectangle xr = closeRect();
        g2.setColor(new Color(0,0,0,110));
        g2.fillOval(xr.x, xr.y, xr.width, xr.height);
        g2.setColor(new Color(255,255,255,220));
        int cx = xr.x + xr.width/2, cy = xr.y + xr.height/2;
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(cx-3, cy-3, cx+3, cy+3);
        g2.drawLine(cx+3, cy-3, cx-3, cy+3);

        g2.dispose();
    }
}
