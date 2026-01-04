package org.docknotas;

import org.docknotas.settings.AppSettings;
import org.docknotas.storage.Storage;
import org.docknotas.ui.components.DockBar;
import org.docknotas.ui.util.UiTheme;
import org.docknotas.ui.windows.NotesWindow;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Storage.ensureDirs();
            AppSettings settings = Storage.loadSettings();

            UiTheme.applyLookAndFeel(settings.getTheme());

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
