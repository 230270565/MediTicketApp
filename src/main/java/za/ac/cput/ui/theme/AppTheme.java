package za.ac.cput.ui.theme;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.SystemInfo;

import javax.swing.*;
import java.awt.*;

/**
 * Central theme definition for MediTicket.
 * Sets up FlatLaf as the base L&F, then layers on custom colors, fonts,
 * and component defaults so every screen pulls from one source of truth
 * instead of hardcoding hex values / font sizes locally.
 */
public class AppTheme {

    // ── Brand palette ──────────────────────────────────────────
    // Clinical-but-warm: deep teal as the primary action color,
    // soft blue-greys for structure, a clear status set for
    // ConfirmationStatus / StatusType / PaymentStatus badges.

    public static final Color PRIMARY        = new Color(0x0E7C86); // deep teal — primary buttons, active nav
    public static final Color PRIMARY_DARK   = new Color(0x0A5C63);
    public static final Color PRIMARY_LIGHT  = new Color(0xE3F2F3);

    public static final Color ACCENT         = new Color(0xF2A65A); // warm amber — secondary CTAs, highlights
    public static final Color ACCENT_DARK    = new Color(0xD98C3D);

    public static final Color BACKGROUND     = new Color(0xF7F9FA); // app background
    public static final Color SURFACE        = Color.WHITE;         // cards / panels
    public static final Color SURFACE_ALT    = new Color(0xF0F3F4); // subtle alt rows/panels

    public static final Color BORDER         = new Color(0xDDE3E5);
    public static final Color DIVIDER        = new Color(0xE9EDEE);

    public static final Color TEXT_PRIMARY   = new Color(0x1E2A2E);
    public static final Color TEXT_SECONDARY = new Color(0x5C6C70);
    public static final Color TEXT_MUTED     = new Color(0x93A2A5);
    public static final Color TEXT_ON_PRIMARY = Color.WHITE;

    // Status colors — reused across ConfirmationStatus, StatusType,
    // PaymentStatus, NotificationStatus badges/pills throughout the UI
    public static final Color STATUS_SUCCESS      = new Color(0x2E9E5B); // CONFIRMED, PAID, RESOLVED, SENT
    public static final Color STATUS_SUCCESS_BG   = new Color(0xE5F5EA);

    public static final Color STATUS_WARNING       = new Color(0xD98C3D); // PENDING
    public static final Color STATUS_WARNING_BG    = new Color(0xFBF0E1);

    public static final Color STATUS_DANGER        = new Color(0xD64545); // REJECTED, FAILED, CANCELLED, ESCALATED
    public static final Color STATUS_DANGER_BG     = new Color(0xFAE6E6);

    public static final Color STATUS_INFO          = new Color(0x3B7DD8); // IN_PROGRESS, RESCHEDULED
    public static final Color STATUS_INFO_BG       = new Color(0xE7EFFB);

    public static final Color STATUS_NEUTRAL        = new Color(0x6C7A7D); // CLOSED, REFUNDED
    public static final Color STATUS_NEUTRAL_BG     = new Color(0xEAECEC);

    // ── Spacing scale ──────────────────────────────────────────
    public static final int SPACE_XS  = 4;
    public static final int SPACE_SM  = 8;
    public static final int SPACE_MD  = 16;
    public static final int SPACE_LG  = 24;
    public static final int SPACE_XL  = 32;
    public static final int SPACE_XXL = 48;

    // ── Corner radii (used with FlatLaf's arc-based rounding) ──
    public static final int RADIUS_SM = 6;
    public static final int RADIUS_MD = 10;
    public static final int RADIUS_LG = 16;

    private static boolean initialized = false;

    /**
     * Call once, at application startup, before any window is created.
     */
    public static void initialize() {
        if (initialized) return;

        if (SystemInfo.isMacOS) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "MediTicket");
        }

        FontManager.registerFonts();

        FlatLightLaf.setup();
        applyUiDefaults();

        initialized = true;
    }

    private static void applyUiDefaults() {
        UIManager.put("@background", toHex(BACKGROUND));
        UIManager.put("@foreground", toHex(TEXT_PRIMARY));
        UIManager.put("@accentColor", toHex(PRIMARY));

        // Global arc/rounding — gives buttons, text fields, combo boxes
        // the soft rounded-card look consistent with the FlatLaf ethos
        UIManager.put("Button.arc", RADIUS_MD);
        UIManager.put("Component.arc", RADIUS_SM);
        UIManager.put("ProgressBar.arc", RADIUS_SM);
        UIManager.put("TextComponent.arc", RADIUS_SM);
        UIManager.put("CheckBox.arc", 4);

        // Typography — Inter for UI text, Playfair Display reserved for
        // display/headline moments (see FontManager.headlineFont())
        Font bodyFont = FontManager.bodyFont(Font.PLAIN, 14);
        UIManager.put("defaultFont", bodyFont);
        UIManager.put("Label.font", bodyFont);
        UIManager.put("Button.font", FontManager.bodyFont(Font.BOLD, 14));
        UIManager.put("TextField.font", bodyFont);
        UIManager.put("PasswordField.font", bodyFont);
        UIManager.put("TextArea.font", bodyFont);
        UIManager.put("ComboBox.font", bodyFont);
        UIManager.put("Table.font", bodyFont);
        UIManager.put("TableHeader.font", FontManager.bodyFont(Font.BOLD, 13));
        UIManager.put("TabbedPane.font", FontManager.bodyFont(Font.BOLD, 14));
        UIManager.put("Menu.font", bodyFont);
        UIManager.put("MenuItem.font", bodyFont);

        // Core color mappings
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.borderColor", BORDER);
        UIManager.put("TextField.focusedBorderColor", PRIMARY);
        UIManager.put("PasswordField.background", SURFACE);

        UIManager.put("Button.default.background", PRIMARY);
        UIManager.put("Button.default.foreground", TEXT_ON_PRIMARY);
        UIManager.put("Button.default.focusedBackground", PRIMARY_DARK);
        UIManager.put("Button.default.hoverBackground", PRIMARY_DARK);
        UIManager.put("Button.background", SURFACE);
        UIManager.put("Button.foreground", TEXT_PRIMARY);

        UIManager.put("Component.borderColor", BORDER);
        UIManager.put("Component.focusColor", PRIMARY_LIGHT);
        UIManager.put("Component.linkColor", PRIMARY);

        UIManager.put("ScrollBar.thumbArc", RADIUS_SM);
        UIManager.put("ScrollBar.trackArc", RADIUS_SM);
        UIManager.put("ScrollBar.width", 10);

        UIManager.put("Table.rowHeight", 36);
        UIManager.put("Table.selectionBackground", PRIMARY_LIGHT);
        UIManager.put("Table.selectionForeground", TEXT_PRIMARY);

        UIManager.put("TabbedPane.selectedBackground", SURFACE);
        UIManager.put("TabbedPane.underlineColor", PRIMARY);
    }

    // ── Status → color helpers ─────────────────────────────────
    // Central mapping so any page rendering a ConfirmationStatus /
    // StatusType / PaymentStatus / NotificationStatus string gets a
    // consistent badge color without re-deriving the mapping per screen.

    public static Color statusColor(String status) {
        if (status == null) return STATUS_NEUTRAL;
        return switch (status) {
            case "CONFIRMED", "PAID", "RESOLVED", "SENT", "APPROVED", "ACTIVE", "COMPLETED", "READ" -> STATUS_SUCCESS;
            case "PENDING" -> STATUS_WARNING;
            case "REJECTED", "FAILED", "CANCELLED", "ESCALATED", "SUSPENDED", "INACTIVE" -> STATUS_DANGER;
            case "IN_PROGRESS", "RESCHEDULED" -> STATUS_INFO;
            case "OPEN" -> PRIMARY;
            default -> STATUS_NEUTRAL; // CLOSED, REFUNDED, etc.
        };
    }

    public static Color statusBackground(String status) {
        if (status == null) return STATUS_NEUTRAL_BG;
        return switch (status) {
            case "CONFIRMED", "PAID", "RESOLVED", "SENT", "APPROVED", "ACTIVE", "COMPLETED", "READ" -> STATUS_SUCCESS_BG;
            case "PENDING" -> STATUS_WARNING_BG;
            case "REJECTED", "FAILED", "CANCELLED", "ESCALATED", "SUSPENDED", "INACTIVE" -> STATUS_DANGER_BG;
            case "IN_PROGRESS", "RESCHEDULED" -> STATUS_INFO_BG;
            case "OPEN" -> PRIMARY_LIGHT;
            default -> STATUS_NEUTRAL_BG;
        };
    }

    /**
     * Readable label for an enum value shown to users:
     * IN_PROGRESS -> "In progress", MEDICAL_AID -> "Medical aid", EFT stays "EFT".
     */
    public static String label(String value) {
        if (value == null || value.isBlank()) return "\u2014";
        if (value.equals("EFT") || value.equals("SMS")) return value;
        String spaced = value.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    // ── Shared control styles ──────────────────────────────────
    // One place for the look of filter pills and text links, so every page matches.

    /** Filter pill: teal fill when selected, quiet outline otherwise. */
    public static void styleFilterButton(AbstractButton btn, boolean selected) {
        btn.setFont(FontManager.bodyFont(selected ? Font.BOLD : Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBackground(selected ? PRIMARY : SURFACE);
        btn.setForeground(selected ? TEXT_ON_PRIMARY : TEXT_SECONDARY);
        btn.putClientProperty("FlatLaf.style",
                "arc: 999; borderWidth: 1; focusWidth: 0; innerFocusWidth: 0; margin: 5,14,5,14;"
                        + " borderColor: " + toHex(selected ? PRIMARY : BORDER) + ";"
                        + " hoverBackground: " + toHex(selected ? PRIMARY_DARK : SURFACE_ALT) + ";");
    }

    /** Text link button: teal text, no box, underline on hover. Replaces boxed link buttons. */
    public static void styleLinkButton(AbstractButton btn) {
        btn.setFont(FontManager.bodyFont(Font.BOLD, 13));
        btn.setForeground(PRIMARY);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        String text = btn.getText();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setText("<html><u>" + text + "</u></html>");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setText(text);
            }
        });
    }

    /** Primary (filled teal) or secondary (teal outline) action button with an optional icon. */
    public static void styleActionButton(AbstractButton btn, boolean primary, String iconName) {
        btn.setFont(FontManager.bodyFont(Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(primary ? PRIMARY : SURFACE);
        btn.setForeground(primary ? TEXT_ON_PRIMARY : PRIMARY);
        btn.putClientProperty("FlatLaf.style",
                "arc: " + RADIUS_MD + "; borderWidth: 1; focusWidth: 0; innerFocusWidth: 0; margin: 9,16,9,16;"
                        + " borderColor: " + toHex(PRIMARY) + ";"
                        + " hoverBackground: " + toHex(primary ? PRIMARY_DARK : PRIMARY_LIGHT) + ";");
        if (iconName != null) {
            btn.setIcon(Icons.get(iconName, 16, primary ? TEXT_ON_PRIMARY : PRIMARY));
            btn.setIconTextGap(8);
        }
    }

    private static String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}