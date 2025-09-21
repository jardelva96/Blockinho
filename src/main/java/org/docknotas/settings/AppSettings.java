package org.docknotas.settings;

import java.awt.*;

/**
 * Preferências do DockNotas.
 * Persistidas via Storage (settings.properties).
 */
public class AppSettings {

    /* ---------- Gerais ---------- */
    private boolean alwaysOnTop    = true;
    private boolean startMinimized = true;      // iniciar só com a janela visível? (UI decide)
    private String  theme          = "dark";    // "dark" | "light"

    /** Tamanho de fonte base (antes do zoom). */
    private int fontSize    = 14;               // px (10..36)
    /** Espaçamento entre linhas do bloco (LineRuledTextArea). */
    private int lineSpacing = 20;               // px (12..40)

    /** Zoom percentual aplicado sobre a fonte base. */
    private int zoomPercent = 100;              // 50..200

    /** Intensidade/força das cores (para UI que use prioridade/cor). */
    private int colorStrengthPercent = 100;     // 40..100

    /** Cor de prioridade (compat) — "vermelho|laranja|amarelo|verde|azul|roxo|cinza". */
    private String priorityColor = "cinza";

    /* ---------- (Legado) Janela antiga - compat ---------- */
    private Point     noteWindowLocation = null;
    private Dimension noteWindowSize     = new Dimension(200, 520);

    /* ---------- Posição/tamanho atuais ---------- */
    /** Local onde a janela principal foi posicionada pela última vez. */
    private Point barLocation = null;

    /** Mantido por compat (algumas UIs podem ignorar). */
    private String barOrientation = "horizontal"; // "horizontal" | "vertical"

    /** Tamanho do painel/editor de notas. */
    private Dimension notePopupSize = new Dimension(200, 300);

    /* ===================== Getters / Setters ===================== */

    public boolean isAlwaysOnTop() { return alwaysOnTop; }
    public void setAlwaysOnTop(boolean v) { alwaysOnTop = v; }

    public boolean isStartMinimized() { return startMinimized; }
    public void setStartMinimized(boolean v) { startMinimized = v; }

    public String getTheme() { return theme; }
    public void setTheme(String t) {
        theme = (t == null || t.isBlank()) ? "dark" : t.toLowerCase();
    }

    public int getFontSize() { return fontSize; }
    public void setFontSize(int v) { fontSize = clamp(v, 10, 36); }

    public int getLineSpacing() { return lineSpacing; }
    public void setLineSpacing(int v) { lineSpacing = clamp(v, 12, 40); }

    /** Zoom em % (50..200). */
    public int getZoomPercent() { return zoomPercent; }
    public void setZoomPercent(int v) { zoomPercent = clamp(v, 50, 200); }

    /** Intensidade da cor em % (40..100). */
    public int getColorStrengthPercent() { return colorStrengthPercent; }
    public void setColorStrengthPercent(int v) { colorStrengthPercent = clamp(v, 40, 100); }

    /** Prioridade/cor (compat). */
    public String getPriorityColor() { return priorityColor; }
    public void setPriorityColor(String c) {
        priorityColor = (c == null || c.isBlank()) ? "cinza" : c.toLowerCase();
    }

    /* --------- legado (compat) --------- */
    public Point getNoteWindowLocation() { return noteWindowLocation; }
    public void setNoteWindowLocation(Point p) { noteWindowLocation = p; }

    public Dimension getNoteWindowSize() { return noteWindowSize; }
    public void setNoteWindowSize(Dimension d) {
        if (d != null) {
            int w = Math.max(240, d.width);
            int h = Math.max(160, d.height);
            noteWindowSize = new Dimension(w, h);
        }
    }

    /* --------- posição/tamanho atuais --------- */
    public Point getBarLocation() { return barLocation; }
    public void setBarLocation(Point p) { barLocation = p; }

    public String getBarOrientation() { return barOrientation; }
    public void setBarOrientation(String o) {
        if (o == null) o = "horizontal";
        String v = o.toLowerCase();
        barOrientation = "vertical".equals(v) ? "vertical" : "horizontal";
    }

    public Dimension getNotePopupSize() { return notePopupSize; }
    public void setNotePopupSize(Dimension d) {
        if (d != null) {
            int w = Math.max(200, d.width);
            int h = Math.max(160, d.height);
            notePopupSize = new Dimension(w, h);
        }
    }

    /* ===================== Util ===================== */
    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
