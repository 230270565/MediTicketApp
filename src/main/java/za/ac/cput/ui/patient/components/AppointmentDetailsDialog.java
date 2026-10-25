/*
 AppointmentDetailsDialog.java

 Patient appointment details dialog — matches the Stitch design system.
 Undecorated card with teal accent bar, 2-column metadata grid,
 reason-for-visit section, info banner, and a context-aware footer button.

 Author: Joshua Reid Adams (230317693)

 Date: 25th October 2026
*/

package za.ac.cput.ui.patient.components;

import za.ac.cput.model.domain.Appointment;
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
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class AppointmentDetailsDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int ACCENT_H    = 4;
    private static final int SHADOW_SIZE = 14;
    private static final int CARD_RADIUS = 16;

    public static void show(Component parent, Appointment appointment) {
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

        body.add(buildHeader(dialog));
        body.add(divider());
        body.add(Box.createVerticalStrut(18));
        body.add(buildMetaGrid(appointment));
        body.add(Box.createVerticalStrut(18));
        body.add(buildReasonSection(appointment));
        body.add(Box.createVerticalStrut(12));
        body.add(buildInfoBanner(appointment));
        body.add(Box.createVerticalStrut(20));
        body.add(buildFooter(dialog, appointment));
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

    private static JComponent buildHeader(JDialog dialog) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(0, 0, 14, 0));

        // Calendar icon circle
        JPanel iconCircle = iconCircle(Icons.CALENDAR);

        // Title + subtitle
        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel title = new JLabel("Appointment Details");
        title.setFont(FontManager.headlineFont(Font.BOLD, 17));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Clinical Schedule  •  MediTicket Health Services");
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

    private static JComponent buildMetaGrid(Appointment appointment) {
        JPanel grid = new JPanel(new GridLayout(3, 2, 24, 18));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Row 1: Patient | Attending Doctor
        grid.add(metaCell("PATIENT",   personCell(patientInitials(appointment), patientName(appointment), "Primary Patient", null)));
        grid.add(metaCell("ATTENDING DOCTOR", personCell(doctorInitials(appointment), doctorName(appointment), doctorRole(appointment), AppTheme.PRIMARY)));

        // Row 2: Date | Time
        String dateText = appointment.getAppointmentDate() != null
                ? appointment.getAppointmentDate().format(DATE_FMT) : "—";
        String dayText = appointment.getAppointmentDate() != null
                ? "(" + appointment.getAppointmentDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + ")" : "";
        grid.add(metaCell("DATE", iconValueRow(Icons.CALENDAR, dateText, dayText)));

        String timeText = appointment.getAppointmentTime() != null
                ? appointment.getAppointmentTime().format(TIME_FMT) : "—";
        grid.add(metaCell("TIME", iconValueRow(Icons.CLOCK, timeText, "SAST")));

        // Row 3: Status | Consultation type
        String status = appointment.getConfirmationStatus();
        grid.add(metaCell("STATUS", new StatusPill(status != null ? status : "—")));
        grid.add(metaCell("CONSULTATION TYPE", consultationChip("In-person Clinical")));

        return grid;
    }

    private static JComponent buildReasonSection(Appointment appointment) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Section header row
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(new JLabel(Icons.get(Icons.CLIPBOARD, 13, AppTheme.TEXT_MUTED)));
        JLabel sectionLabel = new JLabel("REASON FOR VISIT");
        sectionLabel.setFont(FontManager.bodyFont(Font.BOLD, 10));
        sectionLabel.setForeground(AppTheme.TEXT_MUTED);
        left.add(sectionLabel);
        header.add(left, BorderLayout.WEST);

        String status = appointment.getConfirmationStatus();
        if ("CONFIRMED".equals(status) || "COMPLETED".equals(status)) {
            StatusPill verifiedBadge = new StatusPill("Verified Note", "READ");
            JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            badgeWrap.setOpaque(false);
            badgeWrap.add(verifiedBadge);
            header.add(badgeWrap, BorderLayout.EAST);
        }

        section.add(header);
        section.add(Box.createVerticalStrut(10));

        // Quote box
        String reason = appointment.getReason() != null && !appointment.getReason().isBlank()
                ? "“" + appointment.getReason() + "”" : "“No reason provided.”";

        JPanel reasonBox = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.SURFACE_ALT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.RADIUS_MD, AppTheme.RADIUS_MD);
                g2.setColor(AppTheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.RADIUS_MD, AppTheme.RADIUS_MD);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        reasonBox.setOpaque(false);
        reasonBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        reasonBox.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel reasonLabel = new JLabel("<html><i>" + htmlEscape(reason) + "</i></html>");
        reasonLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        reasonLabel.setForeground(AppTheme.TEXT_SECONDARY);
        reasonBox.add(reasonLabel, BorderLayout.CENTER);

        section.add(reasonBox);
        return section;
    }

    private static JComponent buildInfoBanner(Appointment appointment) {
        String status = appointment.getConfirmationStatus();
        String message;
        Color bg;
        Color iconColor;

        if ("REJECTED".equals(status)) {
            message = "This appointment was not approved. Please contact the clinic or submit a new request.";
            bg = AppTheme.STATUS_DANGER_BG;
            iconColor = AppTheme.STATUS_DANGER;
        } else if ("CANCELLED".equals(status)) {
            message = "This appointment has been cancelled.";
            bg = AppTheme.SURFACE_ALT;
            iconColor = AppTheme.TEXT_MUTED;
        } else if ("COMPLETED".equals(status)) {
            message = "This consultation is complete. Check your tickets for any follow-up actions.";
            bg = AppTheme.STATUS_SUCCESS_BG;
            iconColor = AppTheme.STATUS_SUCCESS;
        } else if ("CONFIRMED".equals(status)) {
            message = "No further action needed at this stage.";
            bg = AppTheme.PRIMARY_LIGHT;
            iconColor = AppTheme.PRIMARY;
        } else {
            message = "Your appointment is awaiting confirmation from the clinic.";
            bg = AppTheme.SURFACE_ALT;
            iconColor = AppTheme.TEXT_MUTED;
        }

        JPanel banner = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.RADIUS_MD, AppTheme.RADIUS_MD);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setOpaque(false);
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setBorder(new EmptyBorder(12, 14, 12, 14));

        banner.add(new JLabel(Icons.get(Icons.INFO, 15, iconColor)));
        JLabel msg = new JLabel(message);
        msg.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        msg.setForeground(AppTheme.TEXT_SECONDARY);
        banner.add(msg);
        return banner;
    }

    private static JComponent buildFooter(JDialog dialog, Appointment appointment) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton close = new JButton("Close");
        AppTheme.styleActionButton(close, false, null);
        close.addActionListener(e -> dialog.dispose());
        row.add(close);

        String status = appointment.getConfirmationStatus();
        if ("CONFIRMED".equals(status)) {
            JButton confirmedBtn = new JButton("Appointment Confirmed");
            AppTheme.styleActionButton(confirmedBtn, true, Icons.CHECK);
            confirmedBtn.setEnabled(false);
            row.add(confirmedBtn);
        } else if ("PENDING".equals(status)) {
            JButton pendingBtn = new JButton("Awaiting Confirmation");
            AppTheme.styleActionButton(pendingBtn, false, null);
            pendingBtn.setEnabled(false);
            row.add(pendingBtn);
        }

        return row;
    }

    // ── Component helpers ─────────────────────────────────────────────────────

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
        cell.add(Box.createVerticalStrut(5));
        cell.add(value);
        return cell;
    }

    private static JComponent personCell(String initials, String name, String sub, Color subColor) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.add(avatarCircle(initials));

        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(FontManager.bodyFont(Font.BOLD, 13));
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel(sub);
        subLabel.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        subLabel.setForeground(subColor != null ? subColor : AppTheme.TEXT_MUTED);

        col.add(nameLabel);
        col.add(subLabel);
        row.add(col);
        return row;
    }

    private static JComponent iconValueRow(String iconName, String mainText, String trailingText) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        row.add(new JLabel(Icons.get(iconName, 14, AppTheme.TEXT_MUTED)));

        JLabel main = new JLabel(mainText);
        main.setFont(FontManager.bodyFont(Font.BOLD, 13));
        main.setForeground(AppTheme.TEXT_PRIMARY);
        row.add(main);

        if (trailingText != null && !trailingText.isBlank()) {
            JLabel trailing = new JLabel(trailingText);
            trailing.setFont(FontManager.bodyFont(Font.PLAIN, 12));
            trailing.setForeground(AppTheme.TEXT_MUTED);
            row.add(trailing);
        }
        return row;
    }

    private static JComponent consultationChip(String text) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setBorder(new EmptyBorder(4, 12, 4, 12));
        JLabel lbl = new JLabel(text);
        lbl.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        lbl.setForeground(AppTheme.PRIMARY);
        chip.add(lbl);
        return chip;
    }

    private static JPanel iconCircle(String iconName) {
        JPanel circle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        circle.setOpaque(false);
        circle.setPreferredSize(new Dimension(44, 44));
        circle.setMinimumSize(new Dimension(44, 44));
        circle.setMaximumSize(new Dimension(44, 44));
        circle.setLayout(new GridBagLayout());
        circle.add(new JLabel(Icons.get(iconName, 20, AppTheme.PRIMARY)));
        return circle;
    }

    private static JPanel avatarCircle(String initials) {
        JPanel circle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(AppTheme.PRIMARY);
                g2.setFont(FontManager.bodyFont(Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initials)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initials, x, y);
                g2.dispose();
            }
        };
        circle.setOpaque(false);
        circle.setPreferredSize(new Dimension(30, 30));
        circle.setMinimumSize(new Dimension(30, 30));
        circle.setMaximumSize(new Dimension(30, 30));
        return circle;
    }

    private static JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // ── Data helpers ──────────────────────────────────────────────────────────

    private static String patientInitials(Appointment a) {
        if (a.getPatient() == null || a.getPatient().getName() == null) return "P";
        String f = a.getPatient().getName().getFirstName();
        String l = a.getPatient().getName().getLastName();
        return initials(f, l);
    }

    private static String patientName(Appointment a) {
        if (a.getPatient() == null || a.getPatient().getName() == null) return "Patient";
        String f = a.getPatient().getName().getFirstName();
        String l = a.getPatient().getName().getLastName();
        return ((f != null ? f : "") + " " + (l != null ? l : "")).trim();
    }

    private static String doctorInitials(Appointment a) {
        if (a.getDoctor() == null || a.getDoctor().getName() == null) return "?";
        String f = a.getDoctor().getName().getFirstName();
        String l = a.getDoctor().getName().getLastName();
        return initials(f, l);
    }

    private static String doctorName(Appointment a) {
        if (a.getDoctor() == null || a.getDoctor().getName() == null) return "Not yet assigned";
        String l = a.getDoctor().getName().getLastName();
        return "Dr. " + (l != null ? l : "—");
    }

    private static String doctorRole(Appointment a) {
        if (a.getDoctor() == null) return "Attending Physician";
        String specialty = a.getDoctor().getSpecialty();
        return specialty != null && !specialty.isBlank() ? specialty : "Clinical Specialist";
    }

    private static String initials(String first, String last) {
        String f = first != null && !first.isBlank() ? String.valueOf(first.charAt(0)).toUpperCase() : "";
        String l = last  != null && !last.isBlank()  ? String.valueOf(last.charAt(0)).toUpperCase()  : "";
        return f + l;
    }

    private static String htmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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

            for (int i = SHADOW_SIZE; i > 0; i--) {
                g2.setColor(new Color(0, 0, 0, 3));
                g2.fillRoundRect(i / 2, i / 2, w + (SHADOW_SIZE - i), h + (SHADOW_SIZE - i),
                        CARD_RADIUS, CARD_RADIUS);
            }

            g2.setColor(AppTheme.SURFACE);
            g2.fillRoundRect(0, 0, w, h, CARD_RADIUS, CARD_RADIUS);

            g2.setColor(AppTheme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, CARD_RADIUS, CARD_RADIUS);

            // Teal accent bar clipped to the top rounded corners
            g2.setColor(AppTheme.PRIMARY);
            g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, CARD_RADIUS * 2, CARD_RADIUS, CARD_RADIUS));
            g2.fillRect(0, 0, w, ACCENT_H);
            g2.setClip(null);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Drag handler ─────────────────────────────────────────────────────────

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
