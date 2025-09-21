package org.docknotas.storage;

import org.docknotas.settings.AppSettings;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Persistência simples via arquivos na pasta ~/.docknotas */
public class Storage {

    private static final String APP_DIR = System.getProperty("user.home")
            + File.separator + ".docknotas";

    private static final Path SETTINGS_FILE = Path.of(APP_DIR, "settings.properties");
    private static final Path NOTES_FILE    = Path.of(APP_DIR, "notes.txt");
    private static final Path BACKUP_DIR    = Path.of(APP_DIR, "backups");

    /* ---------------------------- infra ---------------------------- */

    public static void ensureDirs() {
        new File(APP_DIR).mkdirs();
        new File(BACKUP_DIR.toString()).mkdirs();
    }

    /* ------------------------ settings (load/save) ------------------------ */

    public static AppSettings loadSettings() {
        ensureDirs();
        AppSettings s = new AppSettings();
        Properties p = new Properties();

        if (Files.exists(SETTINGS_FILE)) {
            try (InputStream in = Files.newInputStream(SETTINGS_FILE)) {
                p.load(in);
            } catch (IOException ignored) {}
        }

        // Gerais
        s.setAlwaysOnTop(Boolean.parseBoolean(p.getProperty("alwaysOnTop", "true")));
        s.setStartMinimized(Boolean.parseBoolean(p.getProperty("startMinimized", "true")));
        s.setTheme(p.getProperty("theme", "dark"));
        s.setFontSize(parseInt(p.getProperty("fontSize"), 14));
        s.setLineSpacing(parseInt(p.getProperty("lineSpacing"), 20));
        s.setZoomPercent(parseInt(p.getProperty("zoomPercent"), 100));
        s.setColorStrengthPercent(parseInt(p.getProperty("colorStrengthPercent"), 100));

        // Local/Dimensão da janela principal (usamos os mesmos nomes já existentes)
        String barLoc = p.getProperty("barLocation", null);
        if (barLoc != null && barLoc.contains(",")) {
            String[] a = barLoc.split(",");
            s.setBarLocation(new Point(parseInt(a[0], 0), parseInt(a[1], 0)));
        }

        String notePopupSize = p.getProperty("notePopupSize", null);
        if (notePopupSize != null && notePopupSize.contains(",")) {
            String[] a = notePopupSize.split(",");
            s.setNotePopupSize(new Dimension(parseInt(a[0], 420), parseInt(a[1], 520)));
        }

        // Orientação antiga da “barrinha” (mantemos por compat)
        s.setBarOrientation(p.getProperty("barOrientation", "horizontal"));

        // (Legado) campos da janela antiga — ainda lidos para compatibilidade, mas não usados
        String locLegacy = p.getProperty("noteWindowLocation", null);
        if (locLegacy != null && locLegacy.contains(",")) {
            String[] a = locLegacy.split(",");
            s.setNoteWindowLocation(new Point(parseInt(a[0], 0), parseInt(a[1], 0)));
        }
        String sizeLegacy = p.getProperty("noteWindowSize", null);
        if (sizeLegacy != null && sizeLegacy.contains(",")) {
            String[] a = sizeLegacy.split(",");
            s.setNoteWindowSize(new Dimension(parseInt(a[0], 420), parseInt(a[1], 520)));
        }

        return s;
    }

    public static void saveSettings(AppSettings s) {
        ensureDirs();
        Properties p = new Properties();

        // Gerais
        p.setProperty("alwaysOnTop", String.valueOf(s.isAlwaysOnTop()));
        p.setProperty("startMinimized", String.valueOf(s.isStartMinimized()));
        p.setProperty("theme", s.getTheme());
        p.setProperty("fontSize", String.valueOf(s.getFontSize()));
        p.setProperty("lineSpacing", String.valueOf(s.getLineSpacing()));
        p.setProperty("zoomPercent", String.valueOf(s.getZoomPercent()));
        p.setProperty("colorStrengthPercent", String.valueOf(s.getColorStrengthPercent()));

        // Posição/tamanho da janela principal
        if (s.getBarLocation() != null) {
            p.setProperty("barLocation", s.getBarLocation().x + "," + s.getBarLocation().y);
        }
        if (s.getNotePopupSize() != null) {
            p.setProperty("notePopupSize", s.getNotePopupSize().width + "," + s.getNotePopupSize().height);
        }

        // Compat
        p.setProperty("barOrientation", s.getBarOrientation());
        if (s.getNoteWindowLocation() != null) {
            p.setProperty("noteWindowLocation",
                    s.getNoteWindowLocation().x + "," + s.getNoteWindowLocation().y);
        }
        if (s.getNoteWindowSize() != null) {
            p.setProperty("noteWindowSize",
                    s.getNoteWindowSize().width + "," + s.getNoteWindowSize().height);
        }

        try (OutputStream out = Files.newOutputStream(SETTINGS_FILE)) {
            p.store(out, "DockNotas settings");
        } catch (IOException ignored) {}
    }

    /* --------------------------- notas (txt) --------------------------- */

    public static String loadNotes() {
        ensureDirs();
        try {
            if (!Files.exists(NOTES_FILE)) {
                Files.writeString(NOTES_FILE, "", StandardCharsets.UTF_8);
            }
            return Files.readString(NOTES_FILE, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    public static void saveNotes(String text) {
        ensureDirs();
        try {
            Files.writeString(NOTES_FILE, text == null ? "" : text, StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    public static void exportTo(File file) throws IOException {
        ensureDirs();
        if (file == null) return;
        Files.copy(NOTES_FILE, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    public static void importFrom(File file) throws IOException {
        ensureDirs();
        if (file == null) return;
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        saveNotes(content);
    }

    public static File notesFolder() {
        return new File(APP_DIR);
    }

    public static void backupNow() {
        ensureDirs();
        String fname = "notes-" + System.currentTimeMillis() + ".txt";
        try {
            Files.copy(NOTES_FILE, BACKUP_DIR.resolve(fname));
        } catch (IOException ignored) {}
    }

    /* ----------------------------- util ----------------------------- */

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
