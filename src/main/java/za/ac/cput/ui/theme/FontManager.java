/*
 FontManager.java

 Loads the app fonts and hands out font sizes

 Authors:
  Joshua Reid Adams (230317693)
  Abdullahi Raage Farah (230971091)
  Aidan Barends (230255639)
  Jaden Clayton Abrahams (222206721)
  Joshua Peter Bonzet (221312536)
  Matthew Barron (230398863)
  Raul Jaaim Everts (230270565)

 Date: 11th August 2026
*/

package za.ac.cput.ui.theme;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// Loads Inter and Playfair Display and hands out fonts by size.
// Inter bold is made with deriveFont, Playfair has a real bold file.
public class FontManager {

    private static final Logger LOGGER = Logger.getLogger(FontManager.class.getName());

    private static final String FONTS_PATH = "/fonts/";

    private static final String INTER_REGULAR    = "Inter-VariableFont_opsz,wght.ttf";
    private static final String INTER_ITALIC     = "Inter-Italic-VariableFont_opsz,wght.ttf";
    private static final String PLAYFAIR_REGULAR = "PlayfairDisplay-VariableFont_wght.ttf";
    private static final String PLAYFAIR_ITALIC  = "PlayfairDisplay-Italic-VariableFont_wght.ttf";
    private static final String PLAYFAIR_BOLD    = "PlayfairDisplay-Bold.ttf";

    // Logical family names we derive everything from after registration
    public static final String FAMILY_INTER    = "Inter";
    public static final String FAMILY_PLAYFAIR = "Playfair Display";

    private static final Map<String, Font> BASE_FONTS = new HashMap<>();
    private static boolean registered = false;

    // Call once before AppTheme sets the fonts.
    public static void registerFonts() {
        if (registered) return;

        registerFont(INTER_REGULAR, FAMILY_INTER + "-Regular");
        registerFont(INTER_ITALIC, FAMILY_INTER + "-Italic");
        registerFont(PLAYFAIR_REGULAR, FAMILY_PLAYFAIR + "-Regular");
        registerFont(PLAYFAIR_ITALIC, FAMILY_PLAYFAIR + "-Italic");
        registerFont(PLAYFAIR_BOLD, FAMILY_PLAYFAIR + "-Bold");

        registered = true;
    }

    private static void registerFont(String fileName, String cacheKey) {
        try (InputStream is = FontManager.class.getResourceAsStream(FONTS_PATH + fileName)) {
            if (is == null) {
                LOGGER.log(Level.WARNING, "Font resource not found on classpath: {0}{1}", new Object[]{FONTS_PATH, fileName});
                return;
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            BASE_FONTS.put(cacheKey, font);
        } catch (FontFormatException | IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load font: " + fileName, e);
        }
    }

    // Body text (Inter)

    public static Font bodyFont(int style, float size) {
        Font base = pickInterBase(style);
        return base.deriveFont(style & Font.BOLD, size);
    }

    public static Font bodyFont(float size) {
        return bodyFont(Font.PLAIN, size);
    }

    // Headings (Playfair Display)

    public static Font headlineFont(int style, float size) {
        boolean italic = (style & Font.ITALIC) != 0;
        boolean bold = (style & Font.BOLD) != 0;

        if (bold && !italic) {
            // Real bold file
            Font boldBase = fallbackIfMissing(FAMILY_PLAYFAIR + "-Bold", Font.SERIF);
            return boldBase.deriveFont(Font.PLAIN, size);
        }

        if (bold) {
            // No bold italic file, so bold the italic one.
            Font italicBase = fallbackIfMissing(FAMILY_PLAYFAIR + "-Italic", Font.SERIF);
            return italicBase.deriveFont(Font.BOLD, size);
        }

        Font base = pickPlayfairBase(style);
        return base.deriveFont(Font.PLAIN, size);
    }

    public static Font headlineFont(float size) {
        return headlineFont(Font.PLAIN, size);
    }

    // Resolution helpers
    private static Font pickInterBase(int style) {
        boolean italic = (style & Font.ITALIC) != 0;
        String key = italic ? FAMILY_INTER + "-Italic" : FAMILY_INTER + "-Regular";
        return fallbackIfMissing(key, Font.SANS_SERIF);
    }

    private static Font pickPlayfairBase(int style) {
        boolean italic = (style & Font.ITALIC) != 0;
        String key = italic ? FAMILY_PLAYFAIR + "-Italic" : FAMILY_PLAYFAIR + "-Regular";
        return fallbackIfMissing(key, Font.SERIF);
    }

    private static Font fallbackIfMissing(String cacheKey, String logicalFallback) {
        Font base = BASE_FONTS.get(cacheKey);
        if (base != null) return base;
        LOGGER.log(Level.FINE, "Using system fallback for missing font: {0}", cacheKey);
        return new Font(logicalFallback, Font.PLAIN, 12);
    }
}
