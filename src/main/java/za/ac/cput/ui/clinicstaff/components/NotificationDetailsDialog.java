/*
 NotificationDetailsDialog.java

 Clinicstaff notification details dialog — Stitch design system

 Author: Raul Jaaim Everts (230270565)

 Date: 26th October 2026
*/

package za.ac.cput.ui.clinicstaff.components;

import za.ac.cput.model.domain.Notification;
import za.ac.cput.ui.layout.NotificationCard;
import za.ac.cput.ui.layout.StatusPill;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class NotificationDetailsDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");

    public static void show(Component parent, Notification notification, String recipientName,
                            Runnable onSendFollowUp, Runnable onMarkAsRead) {

        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 540);

        // ── Header ────────────────────────────────────────────────────────────
        String[] stage = NotificationCard.stage(notification.getRelatedStatus());
        String subtitle = (stage != null ? stage[0] : "Notification") + " · " + NotificationCard.channel(notification);
        body.add(StitchDialog.header(dialog, Icons.BELL, "Notification Details", subtitle));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(18));

        // ── Meta grid ─────────────────────────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(3, 2, 24, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, grid.getPreferredSize().height));

        // RECIPIENT
        String name = recipientName != null && !recipientName.isBlank() ? recipientName : "—";
        String initials = name.length() > 0 ? String.valueOf(name.charAt(0)).toUpperCase() : "?";
        grid.add(StitchDialog.metaCell("RECIPIENT", StitchDialog.personCell(initials, name, null, null)));

        // CATEGORY
        StatusPill categoryPill = stage != null
                ? new StatusPill(stage[0], stage[1])
                : new StatusPill("—", "NEUTRAL");
        grid.add(StitchDialog.metaCell("CATEGORY", wrapPill(categoryPill)));

        // DELIVERED VIA — use the existing helper which knows the channel logic
        String channelText = NotificationCard.channel(notification);
        JPanel channels = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        channels.setOpaque(false);
        channels.add(StitchDialog.outlineChip("In-app", Icons.BELL));
        if (channelText != null && channelText.contains("Email")) {
            channels.add(StitchDialog.outlineChip("Email", Icons.MAIL));
        }
        grid.add(StitchDialog.metaCell("DELIVERED VIA", channels));

        // STATUS
        StatusPill statusPill = notification.isUnread()
                ? new StatusPill("Unread", "OPEN")
                : new StatusPill("Read", "READ");
        grid.add(StitchDialog.metaCell("STATUS", wrapPill(statusPill)));

        // SENT DATE & TIME
        String sentDate = notification.getNotificationDate() != null
                ? notification.getNotificationDate().format(DATE_FMT) : "—";
        grid.add(StitchDialog.metaCell("SENT DATE & TIME", sentDate));

        // ASSOCIATED TICKET
        if (notification.getTicket() != null) {
            JComponent ticketChip = StitchDialog.chip(
                    "#TK-" + String.format("%03d", notification.getTicket().getTicketId()));
            grid.add(StitchDialog.metaCell("ASSOCIATED TICKET", ticketChip));
        } else {
            grid.add(StitchDialog.metaCell("ASSOCIATED TICKET", "—"));
        }

        body.add(grid);
        body.add(Box.createVerticalStrut(20));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(14));

        // ── Message Content ───────────────────────────────────────────────────
        body.add(StitchDialog.sectionHeader(Icons.CLIPBOARD, "Message Content", null));
        body.add(Box.createVerticalStrut(10));

        JTextArea messageArea = new JTextArea(notification.getNotificationMessage() != null
                ? notification.getNotificationMessage() : "—");
        messageArea.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        messageArea.setForeground(AppTheme.TEXT_PRIMARY);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setOpaque(false);
        messageArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        body.add(StitchDialog.contentBox(messageArea));
        body.add(Box.createVerticalStrut(20));

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel footer = StitchDialog.buttonRow();

        JButton close = new JButton("Close");
        AppTheme.styleActionButton(close, false, null);
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);

        if (onSendFollowUp != null && notification.getTicket() != null) {
            JButton followUp = new JButton("Send Follow-up");
            AppTheme.styleActionButton(followUp, true, Icons.MAIL);
            followUp.addActionListener(e -> { dialog.dispose(); onSendFollowUp.run(); });
            footer.add(followUp);
        }

        if (onMarkAsRead != null && !"READ".equals(notification.getNotificationStatus())) {
            JButton markRead = new JButton("Mark as Read");
            AppTheme.styleActionButton(markRead, true, Icons.CHECK);
            markRead.addActionListener(e -> { dialog.dispose(); onMarkAsRead.run(); });
            footer.add(markRead);
        }

        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 540);
    }

    private static JComponent wrapPill(JComponent pill) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        row.setOpaque(false);
        row.add(pill);
        return row;
    }
}
