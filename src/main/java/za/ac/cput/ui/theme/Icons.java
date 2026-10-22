package za.ac.cput.ui.theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Vector line icons drawn with Java2D (Lucide-style: 24x24 grid, 2px round strokes).
 *
 * Why not emoji: Swing on Windows cannot draw colour emoji, so every emoji icon
 * rendered as a black box or a faint glyph. These icons are plain shapes, so they
 * look the same on every machine, scale to any size, and take any colour.
 *
 * Usage:  label.setIcon(Icons.get(Icons.CALENDAR, 18, AppTheme.PRIMARY));
 */
public final class Icons {

    public static final String HOME = "home";
    public static final String CALENDAR = "calendar";
    public static final String TICKET = "ticket";
    public static final String CARD = "card";
    public static final String BELL = "bell";
    public static final String USER = "user";
    public static final String USERS = "users";
    public static final String CLIPBOARD = "clipboard";
    public static final String CHART = "chart";
    public static final String LOGOUT = "logout";
    public static final String REFRESH = "refresh";
    public static final String CLOCK = "clock";
    public static final String FLAG = "flag";
    public static final String MAIL = "mail";
    public static final String CHECK = "check";
    public static final String X = "x";
    public static final String INFO = "info";
    public static final String ALERT = "alert";
    public static final String PLUS = "plus";

    private Icons() {}

    /** Icon by name, drawn at {@code size} px in {@code color}. */
    public static Icon get(String name, int size, Color color) {
        Consumer<Graphics2D> painter = PAINTERS.getOrDefault(name, PAINTERS.get(INFO));
        return new LineIcon(painter, size, color);
    }

    /**
     * Maps the emoji the dashboards used to pass to {@link za.ac.cput.ui.layout.NavItem}
     * onto vector icons, so existing NavItem lists keep working unchanged.
     */
    public static String nameForEmoji(String emoji) {
        if (emoji == null) return INFO;
        return switch (emoji) {
            case "🏠" -> HOME;          // house
            case "📅" -> CALENDAR;      // calendar
            case "🎫" -> TICKET;        // ticket
            case "💳" -> CARD;          // credit card
            case "🔔" -> BELL;          // bell
            case "👤" -> USER;          // person
            case "👥", "🛌" -> USERS; // people, bed
            case "📋" -> CLIPBOARD;     // clipboard
            case "📊" -> CHART;         // bar chart
            case "🚪" -> LOGOUT;        // door
            case "🕒" -> CLOCK;         // clock
            case "🚩" -> FLAG;          // flag
            default -> emoji.length() > 2 ? emoji : INFO; // already an icon name
        };
    }

    /**
     * Splits a leading emoji off a label ("\uD83D\uDCB3 2 payments" -> {CARD, "2 payments"}),
     * so text that used to start with an emoji can show a vector icon instead.
     * Returns {null, text} when there is no leading emoji.
     */
    public static String[] splitLeadingEmoji(String text) {
        if (text == null || text.length() < 2 || !Character.isHighSurrogate(text.charAt(0))) {
            return new String[]{null, text};
        }
        String emoji = text.substring(0, 2);
        String rest = text.substring(2).stripLeading();
        String name = nameForEmoji(emoji);
        return new String[]{INFO.equals(name) ? null : name, rest};
    }

    // ── Painters: each draws on a 24x24 grid with the stroke already set ──

    private static final Map<String, Consumer<Graphics2D>> PAINTERS = Map.ofEntries(
            Map.entry(HOME, g -> {
                g.draw(path(3, 10, 12, 3, 21, 10));
                g.draw(path(5, 9, 5, 20, 19, 20, 19, 9));
                g.draw(path(10, 20, 10, 14, 14, 14, 14, 20));
            }),
            Map.entry(CALENDAR, g -> {
                g.draw(new RoundRectangle2D.Double(3, 5, 18, 16, 4, 4));
                g.draw(new Line2D.Double(3, 10, 21, 10));
                g.draw(new Line2D.Double(8, 3, 8, 7));
                g.draw(new Line2D.Double(16, 3, 16, 7));
            }),
            Map.entry(TICKET, g -> {
                Path2D p = new Path2D.Double();
                p.moveTo(3, 7); p.lineTo(21, 7); p.lineTo(21, 10);
                p.quadTo(18.5, 12, 21, 14); p.lineTo(21, 17); p.lineTo(3, 17); p.lineTo(3, 14);
                p.quadTo(5.5, 12, 3, 10); p.closePath();
                g.draw(p);
                g.draw(new Line2D.Double(14, 7, 14, 9));
                g.draw(new Line2D.Double(14, 11, 14, 13));
                g.draw(new Line2D.Double(14, 15, 14, 17));
            }),
            Map.entry(CARD, g -> {
                g.draw(new RoundRectangle2D.Double(2, 5, 20, 14, 4, 4));
                g.draw(new Line2D.Double(2, 10, 22, 10));
                g.draw(new Line2D.Double(6, 15, 10, 15));
            }),
            Map.entry(BELL, g -> {
                Path2D p = new Path2D.Double();
                p.moveTo(6, 16); p.lineTo(6, 11);
                p.curveTo(6, 7.5, 8.7, 5, 12, 5);
                p.curveTo(15.3, 5, 18, 7.5, 18, 11);
                p.lineTo(18, 16); p.lineTo(20, 18); p.lineTo(4, 18); p.closePath();
                g.draw(p);
                g.draw(new Arc2D.Double(10, 18.5, 4, 3, 180, 180, Arc2D.OPEN));
                g.draw(new Line2D.Double(12, 3, 12, 5));
            }),
            Map.entry(USER, g -> {
                g.draw(new Ellipse2D.Double(8, 3, 8, 8));
                g.draw(new Arc2D.Double(4, 13, 16, 14, 0, 180, Arc2D.OPEN));
            }),
            Map.entry(USERS, g -> {
                g.draw(new Ellipse2D.Double(5, 4, 7, 7));
                g.draw(new Arc2D.Double(2, 14, 13, 12, 0, 180, Arc2D.OPEN));
                g.draw(new Arc2D.Double(13, 4, 7, 7, -90, 180, Arc2D.OPEN));
                g.draw(new Arc2D.Double(15, 14, 7, 12, 0, 90, Arc2D.OPEN));
            }),
            Map.entry(CLIPBOARD, g -> {
                g.draw(new RoundRectangle2D.Double(5, 4, 14, 17, 3, 3));
                g.draw(new RoundRectangle2D.Double(9, 2.5, 6, 3.5, 2, 2));
                g.draw(new Line2D.Double(9, 11, 15, 11));
                g.draw(new Line2D.Double(9, 15, 15, 15));
            }),
            Map.entry(CHART, g -> {
                g.draw(path(3, 3, 3, 21, 21, 21));
                g.draw(new Line2D.Double(8, 17, 8, 12));
                g.draw(new Line2D.Double(13, 17, 13, 7));
                g.draw(new Line2D.Double(18, 17, 18, 10));
            }),
            Map.entry(LOGOUT, g -> {
                g.draw(path(9, 21, 5, 21, 5, 3, 9, 3));
                g.draw(new Line2D.Double(10, 12, 21, 12));
                g.draw(path(17, 8, 21, 12, 17, 16));
            }),
            Map.entry(REFRESH, g -> {
                g.draw(new Arc2D.Double(4, 4, 16, 16, 20, 270, Arc2D.OPEN));
                g.draw(path(20, 4, 20, 9, 15, 9));
            }),
            Map.entry(CLOCK, g -> {
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(path(12, 7, 12, 12, 15.5, 14));
            }),
            Map.entry(FLAG, g -> {
                g.draw(new Line2D.Double(5, 21, 5, 3));
                g.draw(path(5, 4, 19, 4, 16, 9, 19, 14, 5, 14));
            }),
            Map.entry(MAIL, g -> {
                g.draw(new RoundRectangle2D.Double(3, 5, 18, 14, 3, 3));
                g.draw(path(3, 7, 12, 13, 21, 7));
            }),
            Map.entry(CHECK, g -> g.draw(path(5, 12.5, 10, 17, 19, 7))),
            Map.entry(X, g -> {
                g.draw(new Line2D.Double(6, 6, 18, 18));
                g.draw(new Line2D.Double(18, 6, 6, 18));
            }),
            Map.entry(INFO, g -> {
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g.draw(new Line2D.Double(12, 11, 12, 16.5));
                g.fill(new Ellipse2D.Double(10.9, 6.9, 2.2, 2.2));
            }),
            Map.entry(ALERT, g -> {
                g.draw(path(12, 3, 22, 20, 2, 20, 12, 3));
                g.draw(new Line2D.Double(12, 9, 12, 14));
                g.fill(new Ellipse2D.Double(10.9, 16, 2.2, 2.2));
            }),
            Map.entry(PLUS, g -> {
                g.draw(new Line2D.Double(12, 5, 12, 19));
                g.draw(new Line2D.Double(5, 12, 19, 12));
            })
    );

    private static Path2D path(double... xy) {
        Path2D p = new Path2D.Double();
        p.moveTo(xy[0], xy[1]);
        for (int i = 2; i < xy.length; i += 2) p.lineTo(xy[i], xy[i + 1]);
        return p;
    }

    /** Swing Icon that scales the 24x24 drawing to the requested size. */
    private static final class LineIcon implements Icon {
        private final Consumer<Graphics2D> painter;
        private final int size;
        private final Color color;

        LineIcon(Consumer<Graphics2D> painter, int size, Color color) {
            this.painter = painter;
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.translate(x, y);
                double scale = size / 24.0;
                g2.scale(scale, scale);
                g2.setColor(color != null ? color : c != null ? c.getForeground() : Color.DARK_GRAY);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                painter.accept(g2);
            } finally {
                g2.dispose();
            }
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }
}
