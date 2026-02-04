package org.docknotas;

import com.formdev.flatlaf.FlatDarkLaf;
import org.docknotas.settings.AppSettings;
import org.docknotas.storage.Storage;
import org.docknotas.ui.components.DockBar;
import org.docknotas.ui.windows.NotesWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Classe principal do aplicativo DockNotas.
 * Inicializa o Look & Feel, carrega configurações e cria a interface.
 */
public class App {
    
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("Iniciando DockNotas...");
        
        // Look & Feel moderno (FlatLaf – dark). Troque por FlatLightLaf se quiser claro.
        try {
            FlatDarkLaf.setup();
            logger.debug("FlatLaf Dark theme aplicado");
        } catch (Exception e) {
            logger.warn("Erro ao configurar FlatLaf, usando Look & Feel padrão", e);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                Storage.ensureDirs();
                AppSettings settings = Storage.loadSettings();

                NotesWindow notes = new NotesWindow(settings);
                DockBar dock = new DockBar(notes, settings);

                // fechar = salvar
                notes.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                notes.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override 
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        try {
                            Storage.saveNotes(notes.getEditorText());
                            Storage.saveSettings(settings);
                            logger.info("Configurações e notas salvas ao fechar");
                        } catch (Exception ex) {
                            logger.error("Erro ao salvar dados ao fechar aplicação", ex);
                        }
                    }
                });

                // A barrinha decide como iniciar (minimizado/aberto)
                dock.startAccordingToSettings();
                logger.info("DockNotas iniciado com sucesso");
                
            } catch (Exception e) {
                logger.error("Erro fatal ao iniciar aplicação", e);
                JOptionPane.showMessageDialog(null, 
                    "Erro ao iniciar o aplicativo: " + e.getMessage(),
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
