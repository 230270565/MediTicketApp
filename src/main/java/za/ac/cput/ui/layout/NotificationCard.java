package za.ac.cput.ui.layout;

import za.ac.cput.model.domain.Notification;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * One row in a Notifications list, shared by the patient, doctor and nurse pages.
 *
 * Unread: teal bar on the left, light teal background, bold message and a "New" pill.
 * Read:   plain white card, regular-weight message.
 * Top line: stage pill (what it is about, e.g. "Ticket · Open"), time, channel.
 */
public class NotificationCard extends JPanel {

    private static final Color UNREAD_TINT = new Color(0xF1F8F8);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM, HH:mm");

    private final boolean unread;

    public NotificationCard(Notification n, String recipientLine, Runnable onClick) {
        this.unread = n.isUnread();
        setLayout(new BorderLayout(0, 6));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, unread ? 20 : 16, 12, 16));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── meta row: [stage pill] time · channel                [New] ──
        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        meta.setOpaque(false);
        String[] stage = stage(n.getRelatedStatus());
        if (stage != null) meta.add(new StatusPill(stage[0], stage[1]));
        meta.add(muted(timeAgo(n.getNotificationDate()) + "  ·  " + channel(n)));
        if (recipientLine != null) meta.add(muted("·  " + recipientLine));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(meta, BorderLayout.WEST);
        if (unread) {
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            right.setOpaque(false);
            right.add(new StatusPill("New", "OPEN"));
            top.add(right, BorderLayout.EAST);
        }

        JLabel message = new JLabel("<html>" + escape(n.getNotificationMessage()) + "</html>");
        message.setFont(FontManager.bodyFont(unread ? Font.BOLD : Font.PLAIN, 13));
        message.setForeground(unread ? AppTheme.TEXT_PRIMARY : AppTheme.TEXT_SECONDARY);

        add(top, BorderLayout.NORTH);
        add(message, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (onClick != null) onClick.run(); }
        });
    }

    // Height follows content, so a short message never leaves an empty band (old nurse-page bug).
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight(), arc = 12;
        g2.setColor(unread ? UNREAD_TINT : AppTheme.SURFACE);
        g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
        g2.setColor(AppTheme.BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
        if (unread) {
            g2.setColor(AppTheme.PRIMARY);
            g2.fillRoundRect(0, 0, 5, h - 1, 5, 5);
            g2.fillRect(2, 0, 4, h - 1);
        }
        g2.dispose();
        super.paintComponent(g);
    }

    // ── helpers shared with the details dialog ──

    /** {label, colour-status} for a stage code, or null when the notification has none (older rows). */
    public static String[] stage(String code) {
        if (code == null) return null;
        return switch (code) {
            case "APPOINTMENT_PENDING" -> new String[]{"Appointment · Pending", "PENDING"};
            case "APPOINTMENT_REJECTED" -> new String[]{"Appointment · Rejected", "REJECTED"};
            case "TICKET_OPEN" -> new String[]{"Ticket · Open", "OPEN"};
            case "TICKET_RESOLVED" -> new String[]{"Ticket · Resolved", "RESOLVED"};
            case "PAYMENT_DUE" -> new String[]{"Payment · Due", "PENDING"};
            case "TICKET_CLOSED" -> new String[]{"Ticket · Closed", "CLOSED"};
            default -> new String[]{AppTheme.label(code), code};
        };
    }

    /** "In-app + Email" when an email copy was delivered, otherwise "In-app". */
    public static String channel(Notification n) {
        return n.isEmailSent() ? "In-app + Email" : "In-app";
    }

    public static String timeAgo(LocalDateTime when) {
        if (when == null) return "—";
        Duration d = Duration.between(when, LocalDateTime.now());
        long min = d.toMinutes();
        if (min < 1) return "Just now";
        if (min < 60) return min + " min ago";
        if (min < 24 * 60) return (min / 60) + " h ago";
        return when.format(DAY);
    }

    private static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        l.setForeground(AppTheme.TEXT_MUTED);
        return l;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
