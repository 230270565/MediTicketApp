/*
 ImageManager.java

 Loads and caches the images used in the app

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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// Loads the app images once and keeps scaled copies so they aren't resized on every paint.
public class ImageManager {

    private static final Logger LOGGER = Logger.getLogger(ImageManager.class.getName());

    private static final String IMAGES_PATH = "/images/";

    // Known resource paths
    public static final String HERO_BACKGROUND = "hero/herobg.jpg";

    public static final String ICON_SHIELD       = "icons/Shield.png";
    public static final String ICON_CALENDAR     = "icons/calendar.png";
    public static final String ICON_CLOCK        = "icons/clock.png";
    public static final String ICON_DOCTOR       = "icons/doctor.png";
    public static final String ICON_HEARTBEAT    = "icons/heartbeat.png";
    public static final String ICON_NOTIFICATION = "icons/notification.png";
    public static final String ICON_PATIENT      = "icons/patient.png";
    public static final String ICON_SECURITY     = "icons/security.png";

    public static final String LOGO_ICON      = "logos/LogoWithoutText.png";
    public static final String LOGO_PRIMARY   = "logos/Logo.png";
    public static final String LOGO_PRIMARY_2 = "logos/MediTicketLogo2.png";
    public static final String LOGO_WHITE     = "logos/MediTicketLogoW.png";

    /** Legacy logo paths — kept so nothing breaks if still referenced elsewhere. */
    public static final String LOGO_ICON_LEGACY    = "logos/MediTicketIcon1.png";
    public static final String LOGO_PRIMARY_LEGACY = "logos/MediTicketLogo.png";

    public static final String PLACEHOLDER_AVATAR = "placeholders/Avatar.png";

    // Caches
    private static final Map<String, BufferedImage> RAW_CACHE = new HashMap<>();
    private static final Map<String, ImageIcon> SCALED_CACHE = new HashMap<>();

    private ImageManager() {}

    // Public API
    public static ImageIcon getIcon(String resourcePath) {
        return getIcon(resourcePath, -1, -1);
    }

    // Pass -1 for width or height to keep the shape.
    public static ImageIcon getIcon(String resourcePath, int width, int height) {
        String cacheKey = resourcePath + ":" + width + "x" + height;
        ImageIcon cached = SCALED_CACHE.get(cacheKey);
        if (cached != null) return cached;

        BufferedImage raw = loadRaw(resourcePath);
        if (raw == null) {
            ImageIcon placeholder = buildPlaceholderIcon(width, height);
            SCALED_CACHE.put(cacheKey, placeholder);
            return placeholder;
        }

        Image scaled = scale(raw, width, height);
        ImageIcon icon = new ImageIcon(scaled);
        SCALED_CACHE.put(cacheKey, icon);
        return icon;
    }

    // The image itself, for custom painting.
    public static BufferedImage getImage(String resourcePath) {
        return loadRaw(resourcePath);
    }

    // Round avatar. Uses the placeholder if there is no photo.
    public static ImageIcon getCircularAvatar(String imagePath, int diameter) {
        String path = (imagePath != null) ? imagePath : PLACEHOLDER_AVATAR;
        BufferedImage raw = loadRaw(path);
        if (raw == null) raw = loadRaw(PLACEHOLDER_AVATAR);
        if (raw == null) return buildPlaceholderIcon(diameter, diameter);

        BufferedImage square = cropToSquare(raw);
        Image scaled = square.getScaledInstance(diameter, diameter, Image.SCALE_SMOOTH);

        BufferedImage circular = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circular.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, diameter, diameter));
        g2.drawImage(scaled, 0, 0, null);
        g2.dispose();

        return new ImageIcon(circular);
    }

    // Loads all images at startup so the first screen opens faster.
    public static void preloadAll() {
        String[] all = {
                HERO_BACKGROUND,
                ICON_SHIELD, ICON_CALENDAR, ICON_CLOCK, ICON_DOCTOR,
                ICON_HEARTBEAT, ICON_NOTIFICATION, ICON_PATIENT, ICON_SECURITY,
                LOGO_ICON, LOGO_PRIMARY, LOGO_PRIMARY_2, LOGO_WHITE,
                LOGO_ICON_LEGACY, LOGO_PRIMARY_LEGACY,
                PLACEHOLDER_AVATAR
        };
        for (String path : all) {
            loadRaw(path);
        }
    }

    // Internal loading / scaling
    private static BufferedImage loadRaw(String resourcePath) {
        BufferedImage cached = RAW_CACHE.get(resourcePath);
        if (cached != null) return cached;

        try (InputStream is = ImageManager.class.getResourceAsStream(IMAGES_PATH + resourcePath)) {
            if (is == null) {
                LOGGER.log(Level.WARNING, "Image resource not found on classpath: {0}{1}",
                        new Object[]{IMAGES_PATH, resourcePath});
                return null;
            }
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                LOGGER.log(Level.WARNING, "Could not decode image: {0}{1}", new Object[]{IMAGES_PATH, resourcePath});
                return null;
            }
            RAW_CACHE.put(resourcePath, image);
            return image;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load image: " + resourcePath, e);
            return null;
        }
    }

    private static Image scale(BufferedImage raw, int width, int height) {
        if (width <= 0 && height <= 0) {
            return raw;
        }

        int srcW = raw.getWidth();
        int srcH = raw.getHeight();

        int targetW = width;
        int targetH = height;

        // Preserve aspect ratio when only one dimension is given
        if (width <= 0) {
            targetW = Math.round(srcW * (height / (float) srcH));
        } else if (height <= 0) {
            targetH = Math.round(srcH * (width / (float) srcW));
        }

        return raw.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
    }

    private static BufferedImage cropToSquare(BufferedImage src) {
        int size = Math.min(src.getWidth(), src.getHeight());
        int x = (src.getWidth() - size) / 2;
        int y = (src.getHeight() - size) / 2;
        return src.getSubimage(x, y, size, size);
    }

    private static ImageIcon buildPlaceholderIcon(int width, int height) {
        int w = width > 0 ? width : 48;
        int h = height > 0 ? height : 48;

        BufferedImage placeholder = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = placeholder.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(AppTheme.SURFACE_ALT);
        g2.fillRoundRect(0, 0, w, h, 8, 8);
        g2.setColor(AppTheme.BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
        g2.dispose();

        return new ImageIcon(placeholder);
    }
}
