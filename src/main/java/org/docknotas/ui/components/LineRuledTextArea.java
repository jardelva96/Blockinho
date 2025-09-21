package org.docknotas.ui.components;

import javax.swing.*;
import java.awt.*;

/** JTextArea com “linhas de caderno”. */
public class LineRuledTextArea extends JTextArea {

    private int lineHeight = 20;
    private Color guidelineColor = null; // null => auto

    public LineRuledTextArea() {
        super();
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));
    }

    /** Define o espaçamento entre linhas (mín. 12 px). */
    public void setLineHeight(int h) {
        lineHeight = Math.max(12, h);
        revalidate();
        repaint();
    }

    /** Cor explícita das linhas (null para automático conforme tema). */
    public void setGuidelineColor(Color c) {
        guidelineColor = c;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // cor das linhas
        Color line = guidelineColor;
        if (line == null) {
            Color bg = getBackground();
            double luminance = (0.2126 * bg.getRed() + 0.7152 * bg.getGreen() + 0.0722 * bg.getBlue());
            boolean dark = luminance < 128;
            line = new Color(dark ? 255 : 0, dark ? 255 : 0, dark ? 255 : 0, 38);
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(line);

        Insets ins = getInsets();
        int h = getHeight();
        int w = getWidth();

        FontMetrics fm = g2.getFontMetrics(getFont());
        int baseline = ins.top + fm.getAscent();

        int step = Math.max(lineHeight, fm.getHeight());

        for (int y = baseline; y < h - ins.bottom; y += step) {
            g2.drawLine(ins.left, y + 1, w - ins.right, y + 1);
        }

        g2.dispose();
    }
}
