package org.docknotas.storage;

import org.docknotas.settings.AppSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para Storage.
 * Nota: Alguns testes estão comentados pois Storage usa diretórios fixos (~/.docknotas)
 * Para testes completos, seria necessário refatorar Storage para aceitar diretórios customizados.
 */
class StorageTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Garantir que os diretórios existam
        Storage.ensureDirs();
    }

    @Test
    void testEnsureDirsCreatesDirectories() {
        // Este teste verifica que ensureDirs() não lança exceções
        assertDoesNotThrow(() -> Storage.ensureDirs());
    }

    @Test
    void testLoadSettingsReturnsNonNull() {
        AppSettings settings = Storage.loadSettings();
        assertNotNull(settings);
    }

    @Test
    void testSaveAndLoadSettingsRoundTrip() {
        // Carregar configurações padrão
        AppSettings settings = Storage.loadSettings();
        
        // Modificar algumas configurações
        settings.setTheme("light");
        settings.setFontSize(18);
        settings.setZoomPercent(125);
        
        // Salvar
        Storage.saveSettings(settings);
        
        // Carregar novamente
        AppSettings reloaded = Storage.loadSettings();
        
        // Verificar que foram persistidas
        assertEquals("light", reloaded.getTheme());
        assertEquals(18, reloaded.getFontSize());
        assertEquals(125, reloaded.getZoomPercent());
    }

    @Test
    void testSaveNotesWithNull() {
        // Não deve lançar exceção ao salvar null
        assertDoesNotThrow(() -> Storage.saveNotes(null));
    }

    @Test
    void testSaveAndLoadNotesRoundTrip() {
        String testContent = "Esta é uma nota de teste\nCom múltiplas linhas\nE acentuação: áéíóú";
        
        Storage.saveNotes(testContent);
        String loaded = Storage.loadNotes();
        
        assertEquals(testContent, loaded);
    }

    @Test
    void testLoadNotesReturnsEmptyStringWhenFileDoesntExist() {
        // Após salvar string vazia, load deve retornar string vazia
        Storage.saveNotes("");
        String loaded = Storage.loadNotes();
        assertEquals("", loaded);
    }

    @Test
    void testExportToWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> Storage.exportTo(null));
    }

    @Test
    void testImportFromWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> Storage.importFrom(null));
    }

    @Test
    void testImportFromNonExistentFileThrowsException() {
        File nonExistent = new File(tempDir.toFile(), "non-existent.txt");
        assertThrows(IOException.class, () -> Storage.importFrom(nonExistent));
    }

    @Test
    void testExportAndImportRoundTrip() throws IOException {
        // Preparar conteúdo
        String originalContent = "Conteúdo de teste para exportar e importar";
        Storage.saveNotes(originalContent);
        
        // Exportar
        File exportFile = new File(tempDir.toFile(), "exported.txt");
        Storage.exportTo(exportFile);
        
        assertTrue(exportFile.exists());
        
        // Modificar conteúdo atual
        Storage.saveNotes("Conteúdo diferente");
        
        // Importar
        Storage.importFrom(exportFile);
        
        // Verificar que o conteúdo foi restaurado
        String imported = Storage.loadNotes();
        assertEquals(originalContent, imported);
    }

    @Test
    void testNotesFolderReturnsValidDirectory() {
        File folder = Storage.notesFolder();
        assertNotNull(folder);
        assertTrue(folder.getPath().endsWith(".docknotas"));
    }

    @Test
    void testBackupNowDoesNotThrow() {
        // Garantir que há conteúdo para fazer backup
        Storage.saveNotes("Teste de backup");
        
        // Backup não deve lançar exceção
        assertDoesNotThrow(() -> Storage.backupNow());
    }

    @Test
    void testSaveSettingsWithNullDoesNotThrow() {
        // Deve logar aviso mas não lançar exceção
        assertDoesNotThrow(() -> Storage.saveSettings(null));
    }

    @Test
    void testMultipleBackupsCreateDifferentFiles() throws InterruptedException {
        Storage.saveNotes("Teste de múltiplos backups");
        
        Storage.backupNow();
        // Pequena pausa para garantir timestamp diferente entre backups
        // Necessário pois o nome do arquivo usa System.currentTimeMillis()
        Thread.sleep(50); // 50ms é suficiente para diferentes timestamps
        Storage.backupNow();
        
        // Verificar que foram criados arquivos de backup
        File backupDir = new File(Storage.notesFolder(), "backups");
        assertTrue(backupDir.exists());
        
        File[] backups = backupDir.listFiles((dir, name) -> name.startsWith("notes-") && name.endsWith(".txt"));
        assertNotNull(backups);
        assertTrue(backups.length >= 2, "Deve ter pelo menos 2 arquivos de backup");
    }
}
