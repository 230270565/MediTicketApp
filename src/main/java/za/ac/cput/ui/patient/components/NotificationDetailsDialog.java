/*
 NotificationDetailsDialog.java

 Patient-facing notification details dialog — matches the Stitch design mockup.
 Shows metadata in a 2-column grid, the message, an optional payment summary
 card, and context-aware action buttons.

 Author: Raul Jaaim Everts (230270565)

 Date: 25th October 2026
*/

package za.ac.cput.ui.patient.components;

import za.ac.cput.model.domain.Notification;
import za.ac.cput.model.domain.Payment;
import za.ac.cput.ui.layout.NotificationCard;
import za.ac.cput.ui.layout.StatusPill;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class NotificationDetailsDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");
    private static final int ACCENT_H = 4;
    private static final int SHADOW_SIZE = 14;
    private static final int CARD_RADIUS = 16;

    /**
     * @param parent        calling component (used to centre the dialog)
     * @param notification  the notification to display
     * @param payment       optional Payment object — shown as a summary card when present
     * @param onViewTicket  called when "View Ticket" is clicked (pass null to hide the button)
     * @param onPay         called when "Pay" is clicked (pass null to hide the button)
     */
    public static void show(Component parent, Notification notification,
                            Payment payment,
                            Runnable onViewTicket,
                            Runnable onPay) {

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(ACCENT_H + 20, 28, 0, 28));

        // ── Header ──────────────────────────────────────────────────
        body.add(buildHeader(dialog, notification));
        body.add(divider());
        body.add(Box.createVerticalStrut(16));

        // ── 2-column metadata grid ───────────────────────────────────
        body.add(buildMetaGrid(notification, onViewTicket));
        body.add(Box.createVerticalStrut(16));

        // ── Message content ──────────────────────────────────────────
        body.add(buildMessageSection(notification, payment));
        body.add(Box.createVerticalStrut(20));

        // ── Footer ───────────────────────────────────────────────────
        body.add(buildFooter(dialog, notification, payment, onViewTicket, onPay));
        body.add(Box.createVerticalStrut(24));

        card.add(body, BorderLayout.CENTER);

        DragHandler drag = new DragHandler(dialog);
        card.addMouseListener(drag);
        card.addMouseMotionListener(drag);

        dialog.setContentPane(card);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(540, 0));
        dialog.setLocationRelativeTo(parent);

        dialog.getRootPane().registerKeyboardAction(
                (ActionEvent e) -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.setVisible(true);
    }

    // ── Section builders ─────────────────────────────────────────────────────

    private static JComponent buildHeader(JDialog dialog, Notification notification) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(0, 0, 14, 0));

        // Bell icon circle
        JPanel iconCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconCircle.setOpaque(false);
        iconCircle.setPreferredSize(new Dimension(44, 44));
        iconCircle.setMinimumSize(new Dimension(44, 44));
        iconCircle.setMaximumSize(new Dimension(44, 44));
        iconCircle.setLayout(new GridBagLayout());
        JLabel bellIcon = new JLabel(Icons.get(Icons.BELL, 20, AppTheme.PRIMARY));
        iconCircle.add(bellIcon);

        // Title + subtitle
        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel title = new JLabel("Notification Details");
        title.setFont(FontManager.headlineFont(Font.BOLD, 17));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        String[] stage = NotificationCard.stage(notification.getRelatedStatus());
        String channel = NotificationCard.channel(notification);
        String subtitleText = (stage != null ? stage[0] : "Notification") + "  •  " + channel;
        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subtitle);

        // X close button
        JButton closeBtn = new JButton(Icons.get(Icons.X, 14, AppTheme.TEXT_MUTED));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(28, 28));
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(iconCircle);
        left.add(titles);

        row.add(left, BorderLayout.CENTER);
        row.add(closeBtn, BorderLayout.EAST);
        return row;
    }

    private static JComponent buildMetaGrid(Notification notification, Runnable onViewTicket) {
        JPanel grid = new JPanel(new GridLayout(3, 2, 24, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Row 1: Recipient | Category
        JPanel recipientComp = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        recipientComp.setOpaque(false);
        recipientComp.add(avatarCircle("Y"));
        recipientComp.add(recipientLabel("You", "(Primary Account)"));
        grid.add(metaCell("RECIPIENT", recipientComp));

        String[] stage = NotificationCard.stage(notification.getRelatedStatus());
        JComponent categoryComp = stage != null
                ? new StatusPill(stage[0], stage[1])
                : new JLabel("—");
        grid.add(metaCell("CATEGORY", categoryComp));

        // Row 2: Delivered via | Status
        JPanel channelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        channelPanel.setOpaque(false);
        channelPanel.add(chipTag("In-app", Icons.BELL));
        if (notification.isEmailSent()) {
            channelPanel.add(new JLabel("+"));
            channelPanel.add(chipTag("Email", Icons.MAIL));
        }
        grid.add(metaCell("DELIVERED VIA", channelPanel));

        StatusPill statusPill = notification.isUnread()
                ? new StatusPill("Unread", "OPEN")
                : new StatusPill("Read", "READ");
        grid.add(metaCell("STATUS", statusPill));

        // Row 3: Sent date | Associated ticket
        String sentText = notification.getNotificationDate() != null
                ? notification.getNotificationDate().format(DATE_FMT) : "—";
        JLabel sentLabel = new JLabel(sentText);
        sentLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        sentLabel.setForeground(AppTheme.TEXT_PRIMARY);
        grid.add(metaCell("SENT DATE & TIME", sentLabel));

        JComponent ticketComp;
        if (notification.getTicket() != null) {
            String tkLabel = "TK-" + String.format("%03d", notification.getTicket().getTicketId());
            ticketComp = ticketBadge(tkLabel, onViewTicket);
        } else {
            JLabel none = new JLabel("—");
            none.setFont(FontManager.bodyFont(Font.PLAIN, 13));
            none.setForeground(AppTheme.TEXT_MUTED);
            ticketComp = none;
        }
        grid.add(metaCell("ASSOCIATED TICKET", ticketComp));

        return grid;
    }

    private static JComponent buildMessageSection(Notification notification, Payment payment) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Section header row
        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);
        sectionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionTitle = new JLabel("MESSAGE CONTENT");
        sectionTitle.setFont(FontManager.bodyFont(Font.BOLD, 10));
        sectionTitle.setForeground(AppTheme.TEXT_MUTED);
        sectionHeader.add(sectionTitle, BorderLayout.WEST);

        boolean isActionRequired = "PAYMENT_DUE".equals(notification.getRelatedStatus())
                && (payment == null || !"PAID".equals(payment != null ? payment.getPaymentStatus() : null));
        if (isActionRequired) {
            StatusPill badge = new StatusPill("Action Required", "PENDING");
            JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            badgeWrap.setOpaque(false);
            badgeWrap.add(badge);
            sectionHeader.add(badgeWrap, BorderLayout.EAST);
        }
        section.add(sectionHeader);
        section.add(Box.createVerticalStrut(10));

        // Message body
        JTextArea msgArea = new JTextArea(notification.getNotificationMessage() != null
                ? notification.getNotificationMessage() : "—");
        msgArea.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        msgArea.setForeground(AppTheme.TEXT_PRIMARY);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setEditable(false);
        msgArea.setOpaque(false);
        msgArea.setBorder(null);
        msgArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(msgArea);

        // Payment summary card (only when a payment is provided)
        if (payment != null) {
            section.add(Box.createVerticalStrut(12));
            section.add(buildPaymentCard(payment, notification));
        }

        return section;
    }

    private static JComponent buildPaymentCard(Payment payment, Notification notification) {
        JPanel card = new JPanel(new GridLayout(2, 3, 0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.SURFACE_ALT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.RADIUS_MD, AppTheme.RADIUS_MD);
                g2.setColor(AppTheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.RADIUS_MD, AppTheme.RADIUS_MD);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        NumberFormat currencyFmt = NumberFormat.getCurrencyInstance(new Locale("en", "ZA"));
        String amountStr = payment.getPaymentAmount() != null
                ? currencyFmt.format(payment.getPaymentAmount()) : "—";
        String methodStr = AppTheme.label(payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "—");
        String refStr = notification.getTicket() != null
                ? "TK-" + String.format("%03d", notification.getTicket().getTicketId()) : "—";

        card.add(payCardHeader("AMOUNT"));
        card.add(payCardHeader("METHOD"));
        card.add(payCardHeader("REFERENCE"));
        card.add(payCardValue(amountStr));
        card.add(payCardValue(methodStr));
        card.add(payCardValue(refStr));

        return card;
    }

    private static JComponent buildFooter(JDialog dialog, Notification notification,
                                          Payment payment, Runnable onViewTicket, Runnable onPay) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton close = new JButton("Close");
        AppTheme.styleActionButton(close, false, null);
        close.addActionListener(e -> dialog.dispose());
        row.add(close);

        if (onViewTicket != null && notification.getTicket() != null) {
            JButton viewTicket = new JButton("View Ticket");
            AppTheme.styleActionButton(viewTicket, false, null);
            viewTicket.addActionListener(e -> { dialog.dispose(); onViewTicket.run(); });
            row.add(viewTicket);
        }

        if (onPay != null && "PAYMENT_DUE".equals(notification.getRelatedStatus())) {
            String label = "Pay";
            if (payment != null && payment.getPaymentAmount() != null) {
                NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "ZA"));
                label = "Pay " + fmt.format(payment.getPaymentAmount()) + " →";
            }
            JButton payBtn = new JButton(label);
            AppTheme.styleActionButton(payBtn, true, null);
            payBtn.addActionListener(e -> { dialog.dispose(); onPay.run(); });
            row.add(payBtn);
        }

        return row;
    }

    // ── Reusable component helpers ────────────────────────────────────────────

    private static JComponent metaCell(String label, JComponent value) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(FontManager.bodyFont(Font.BOLD, 10));
        lbl.setForeground(AppTheme.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        value.setAlignmentX(Component.LEFT_ALIGNMENT);

        cell.add(lbl);
        cell.add(Box.createVerticalStrut(4));
        cell.add(value);
        return cell;
    }

    private static JComponent avatarCircle(String initial) {
        JPanel circle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(AppTheme.PRIMARY);
                g2.setFont(FontManager.bodyFont(Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initial)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initial, x, y);
                g2.dispose();
            }
        };
        circle.setOpaque(false);
        circle.setPreferredSize(new Dimension(26, 26));
        circle.setMinimumSize(new Dimension(26, 26));
        circle.setMaximumSize(new Dimension(26, 26));
        return circle;
    }

    private static JComponent recipientLabel(String name, String sub) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(FontManager.bodyFont(Font.BOLD, 13));
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel(sub);
        subLabel.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        subLabel.setForeground(AppTheme.TEXT_MUTED);

        col.add(nameLabel);
        col.add(subLabel);
        return col;
    }

    private static JComponent chipTag(String text, String iconName) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.SURFACE_ALT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.setColor(AppTheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setBorder(new EmptyBorder(2, 8, 2, 8));

        JLabel icon = new JLabel(Icons.get(iconName, 11, AppTheme.TEXT_MUTED));
        JLabel label = new JLabel(text);
        label.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        label.setForeground(AppTheme.TEXT_SECONDARY);

        chip.add(icon);
        chip.add(label);
        return chip;
    }

    private static JComponent ticketBadge(String label, Runnable onClick) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));

        JLabel icon = new JLabel(Icons.get(Icons.TICKET, 11, AppTheme.PRIMARY));
        JLabel text = new JLabel(label);
        text.setFont(FontManager.bodyFont(Font.BOLD, 11));
        text.setForeground(AppTheme.PRIMARY);

        badge.add(icon);
        badge.add(text);

        if (onClick != null) {
            badge.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            badge.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
            });
        }
        return badge;
    }

    private static JComponent divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private static JLabel payCardHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FontManager.bodyFont(Font.BOLD, 10));
        l.setForeground(AppTheme.TEXT_MUTED);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private static JLabel payCardValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FontManager.bodyFont(Font.BOLD, 13));
        l.setForeground(AppTheme.TEXT_PRIMARY);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    // ── Custom painted card ───────────────────────────────────────────────────

    private static class CardPanel extends JPanel {

        CardPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(0, 0, SHADOW_SIZE, SHADOW_SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - SHADOW_SIZE;
            int h = getHeight() - SHADOW_SIZE;

            // Soft shadow
            for (int i = SHADOW_SIZE; i > 0; i--) {
                g2.setColor(new Color(0, 0, 0, 3));
                g2.fillRoundRect(i / 2, i / 2, w + (SHADOW_SIZE - i), h + (SHADOW_SIZE - i),
                        CARD_RADIUS, CARD_RADIUS);
            }

            // White card
            g2.setColor(AppTheme.SURFACE);
            g2.fillRoundRect(0, 0, w, h, CARD_RADIUS, CARD_RADIUS);

            // Border
            g2.setColor(AppTheme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, CARD_RADIUS, CARD_RADIUS);

            // Teal accent bar at the top
            g2.setColor(AppTheme.PRIMARY);
            g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, CARD_RADIUS * 2, CARD_RADIUS, CARD_RADIUS));
            g2.fillRect(0, 0, w, ACCENT_H);
            g2.setClip(null);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Drag handler (no title bar) ───────────────────────────────────────────

    private static class DragHandler extends MouseAdapter {
        private final JDialog dialog;
        private Point offset;

        DragHandler(JDialog dialog) { this.dialog = dialog; }

        @Override public void mousePressed(MouseEvent e) { offset = e.getPoint(); }

        @Override public void mouseDragged(MouseEvent e) {
            Point loc = dialog.getLocation();
            dialog.setLocation(loc.x + e.getX() - offset.x, loc.y + e.getY() - offset.y);
        }
    }
}
