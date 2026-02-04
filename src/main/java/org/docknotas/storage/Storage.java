package org.docknotas.storage;

import org.docknotas.settings.AppSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Persistência simples via arquivos na pasta ~/.docknotas
 * Gerencia configurações, notas e backups do aplicativo.
 */
public class Storage {

    private static final Logger logger = LoggerFactory.getLogger(Storage.class);
    private static final String APP_DIR_NAME = ".docknotas";
    private static final String SETTINGS_FILENAME = "settings.properties";
    private static final String NOTES_FILENAME = "notes.txt";
    private static final String BACKUP_DIR_NAME = "backups";

    private static final String APP_DIR = System.getProperty("user.home")
            + File.separator + APP_DIR_NAME;

    private static final Path SETTINGS_FILE = Path.of(APP_DIR, SETTINGS_FILENAME);
    private static final Path NOTES_FILE    = Path.of(APP_DIR, NOTES_FILENAME);
    private static final Path BACKUP_DIR    = Path.of(APP_DIR, BACKUP_DIR_NAME);

    /* ---------------------------- infra ---------------------------- */

    /**
     * Garante que os diretórios necessários existam.
     * Cria ~/.docknotas e ~/.docknotas/backups se não existirem.
     */
    public static void ensureDirs() {
        try {
            File appDir = new File(APP_DIR);
            if (!appDir.exists()) {
                boolean created = appDir.mkdirs();
                if (created) {
                    logger.info("Diretório da aplicação criado: {}", APP_DIR);
                }
            }
            
            File backupDir = new File(BACKUP_DIR.toString());
            if (!backupDir.exists()) {
                boolean created = backupDir.mkdirs();
                if (created) {
                    logger.info("Diretório de backups criado: {}", BACKUP_DIR);
                }
            }
        } catch (SecurityException e) {
            logger.error("Erro ao criar diretórios da aplicação", e);
        }
    }

    /* ------------------------ settings (load/save) ------------------------ */

    /**
     * Carrega as configurações do arquivo settings.properties.
     * @return AppSettings com as configurações carregadas ou valores padrão
     */
    public static AppSettings loadSettings() {
        ensureDirs();
        AppSettings s = new AppSettings();
        Properties p = new Properties();

        if (Files.exists(SETTINGS_FILE)) {
            try (InputStream in = Files.newInputStream(SETTINGS_FILE)) {
                p.load(in);
                logger.debug("Configurações carregadas de: {}", SETTINGS_FILE);
            } catch (IOException e) {
                logger.warn("Erro ao carregar configurações, usando valores padrão", e);
            }
        } else {
            logger.info("Arquivo de configurações não encontrado, usando valores padrão");
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

    /**
     * Salva as configurações no arquivo settings.properties.
     * @param s AppSettings a serem salvas
     */
    public static void saveSettings(AppSettings s) {
        if (s == null) {
            logger.warn("Tentativa de salvar configurações nulas");
            return;
        }
        
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
            logger.debug("Configurações salvas em: {}", SETTINGS_FILE);
        } catch (IOException e) {
            logger.error("Erro ao salvar configurações", e);
        }
    }

    /* --------------------------- notas (txt) --------------------------- */

    /**
     * Carrega o conteúdo das notas do arquivo notes.txt.
     * @return String com o conteúdo das notas ou string vazia se não existir
     */
    public static String loadNotes() {
        ensureDirs();
        try {
            if (!Files.exists(NOTES_FILE)) {
                Files.writeString(NOTES_FILE, "", StandardCharsets.UTF_8);
                logger.info("Arquivo de notas criado: {}", NOTES_FILE);
            }
            String content = Files.readString(NOTES_FILE, StandardCharsets.UTF_8);
            logger.debug("Notas carregadas: {} caracteres", content.length());
            return content;
        } catch (IOException e) {
            logger.error("Erro ao carregar notas", e);
            return "";
        }
    }

    /**
     * Salva o conteúdo das notas no arquivo notes.txt.
     * @param text String com o conteúdo a ser salvo (null será tratado como string vazia)
     */
    public static void saveNotes(String text) {
        ensureDirs();
        String content = (text == null) ? "" : text;
        try {
            Files.writeString(NOTES_FILE, content, StandardCharsets.UTF_8);
            logger.debug("Notas salvas: {} caracteres", content.length());
        } catch (IOException e) {
            logger.error("Erro ao salvar notas", e);
        }
    }

    /**
     * Exporta as notas para um arquivo especificado.
     * @param file Arquivo de destino
     * @throws IOException se ocorrer erro na exportação
     */
    public static void exportTo(File file) throws IOException {
        ensureDirs();
        if (file == null) {
            throw new IllegalArgumentException("Arquivo de destino não pode ser null");
        }
        Files.copy(NOTES_FILE, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        logger.info("Notas exportadas para: {}", file.getAbsolutePath());
    }

    /**
     * Importa notas de um arquivo especificado.
     * @param file Arquivo de origem
     * @throws IOException se ocorrer erro na importação
     */
    public static void importFrom(File file) throws IOException {
        ensureDirs();
        if (file == null) {
            throw new IllegalArgumentException("Arquivo de origem não pode ser null");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("Arquivo não encontrado: " + file.getAbsolutePath());
        }
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        saveNotes(content);
        logger.info("Notas importadas de: {}", file.getAbsolutePath());
    }

    /**
     * Retorna o diretório onde as notas são armazenadas.
     * @return File representando o diretório ~/.docknotas
     */
    public static File notesFolder() {
        return new File(APP_DIR);
    }

    /**
     * Cria um backup das notas atuais com timestamp.
     * O backup é salvo em ~/.docknotas/backups/notes-{timestamp}.txt
     */
    public static void backupNow() {
        ensureDirs();
        String fname = "notes-" + System.currentTimeMillis() + ".txt";
        try {
            if (Files.exists(NOTES_FILE)) {
                Files.copy(NOTES_FILE, BACKUP_DIR.resolve(fname));
                logger.info("Backup criado: {}", fname);
            } else {
                logger.warn("Arquivo de notas não existe, backup não criado");
            }
        } catch (IOException e) {
            logger.error("Erro ao criar backup", e);
        }
    }

    /* ----------------------------- util ----------------------------- */

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
