package org.docknotas;

import com.formdev.flatlaf.FlatDarkLaf;   // << tema moderno
import org.docknotas.settings.AppSettings;
import org.docknotas.storage.Storage;
import org.docknotas.ui.components.DockBar;
import org.docknotas.ui.windows.NotesWindow;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // Look & Feel moderno (FlatLaf – dark). Troque por FlatLightLaf se quiser claro.
        try { FlatDarkLaf.setup(); } catch (Exception ignore) {}

        SwingUtilities.invokeLater(() -> {
            Storage.ensureDirs();
            AppSettings settings = Storage.loadSettings();

            NotesWindow notes = new NotesWindow(settings);
            DockBar dock = new DockBar(notes, settings);

            // fechar = salvar
            notes.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            notes.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) {
                    try {
                        Storage.saveNotes(notes.getEditorText());
                        Storage.saveSettings(settings);
                    } catch (Exception ignored) {}
                }
            });

            // A barrinha decide como iniciar (minimizado/aberto)
            dock.startAccordingToSettings();
        });
    }
}
