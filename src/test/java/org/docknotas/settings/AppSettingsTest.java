package org.docknotas.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para AppSettings.
 */
class AppSettingsTest {

    private AppSettings settings;

    @BeforeEach
    void setUp() {
        settings = new AppSettings();
    }

    @Test
    void testDefaultValues() {
        assertTrue(settings.isAlwaysOnTop());
        assertTrue(settings.isStartMinimized());
        assertEquals("dark", settings.getTheme());
        assertEquals(14, settings.getFontSize());
        assertEquals(20, settings.getLineSpacing());
        assertEquals(100, settings.getZoomPercent());
        assertEquals(100, settings.getColorStrengthPercent());
        assertEquals("cinza", settings.getPriorityColor());
    }

    @Test
    void testFontSizeClamping() {
        settings.setFontSize(5);  // muito pequeno
        assertEquals(10, settings.getFontSize());

        settings.setFontSize(50); // muito grande
        assertEquals(36, settings.getFontSize());

        settings.setFontSize(20); // valor válido
        assertEquals(20, settings.getFontSize());
    }

    @Test
    void testLineSpacingClamping() {
        settings.setLineSpacing(5);  // muito pequeno
        assertEquals(12, settings.getLineSpacing());

        settings.setLineSpacing(100); // muito grande
        assertEquals(40, settings.getLineSpacing());

        settings.setLineSpacing(25); // valor válido
        assertEquals(25, settings.getLineSpacing());
    }

    @Test
    void testZoomPercentClamping() {
        settings.setZoomPercent(10);  // muito pequeno
        assertEquals(50, settings.getZoomPercent());

        settings.setZoomPercent(300); // muito grande
        assertEquals(200, settings.getZoomPercent());

        settings.setZoomPercent(150); // valor válido
        assertEquals(150, settings.getZoomPercent());
    }

    @Test
    void testColorStrengthClamping() {
        settings.setColorStrengthPercent(10);  // muito pequeno
        assertEquals(40, settings.getColorStrengthPercent());

        settings.setColorStrengthPercent(150); // muito grande
        assertEquals(100, settings.getColorStrengthPercent());

        settings.setColorStrengthPercent(75); // valor válido
        assertEquals(75, settings.getColorStrengthPercent());
    }

    @Test
    void testThemeNormalization() {
        settings.setTheme("DARK");
        assertEquals("dark", settings.getTheme());

        settings.setTheme("Light");
        assertEquals("light", settings.getTheme());

        settings.setTheme(null);
        assertEquals("dark", settings.getTheme()); // default

        settings.setTheme("");
        assertEquals("dark", settings.getTheme()); // default
    }

    @Test
    void testPriorityColorNormalization() {
        settings.setPriorityColor("VERMELHO");
        assertEquals("vermelho", settings.getPriorityColor());

        settings.setPriorityColor(null);
        assertEquals("cinza", settings.getPriorityColor()); // default
    }

    @Test
    void testBarOrientationNormalization() {
        settings.setBarOrientation("VERTICAL");
        assertEquals("vertical", settings.getBarOrientation());

        settings.setBarOrientation("horizontal");
        assertEquals("horizontal", settings.getBarOrientation());

        settings.setBarOrientation(null);
        assertEquals("horizontal", settings.getBarOrientation()); // default

        settings.setBarOrientation("invalid");
        assertEquals("horizontal", settings.getBarOrientation()); // default
    }

    @Test
    void testWindowSizeValidation() {
        settings.setNotePopupSize(new Dimension(100, 100)); // muito pequeno
        assertEquals(200, settings.getNotePopupSize().width);
        assertEquals(160, settings.getNotePopupSize().height);

        settings.setNotePopupSize(new Dimension(500, 600)); // valor válido
        assertEquals(500, settings.getNotePopupSize().width);
        assertEquals(600, settings.getNotePopupSize().height);

        settings.setNotePopupSize(null); // não deve alterar
        assertEquals(500, settings.getNotePopupSize().width);
        assertEquals(600, settings.getNotePopupSize().height);
    }

    @Test
    void testLocationStorage() {
        Point location = new Point(100, 200);
        settings.setBarLocation(location);
        assertEquals(location, settings.getBarLocation());
    }

    @Test
    void testAlwaysOnTopToggle() {
        settings.setAlwaysOnTop(false);
        assertFalse(settings.isAlwaysOnTop());
        
        settings.setAlwaysOnTop(true);
        assertTrue(settings.isAlwaysOnTop());
    }

    @Test
    void testStartMinimizedToggle() {
        settings.setStartMinimized(false);
        assertFalse(settings.isStartMinimized());
        
        settings.setStartMinimized(true);
        assertTrue(settings.isStartMinimized());
    }
}
