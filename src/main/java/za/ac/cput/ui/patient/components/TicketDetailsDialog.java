/*
 TicketDetailsDialog.java

 Patient dialog for ticket details — Stitch design system rewrite

 Author: Joshua Reid Adams (230317693)

 Date: 25th October 2026
*/

package za.ac.cput.ui.patient.components;

import za.ac.cput.ui.layout.StatusTimeline;
import za.ac.cput.ui.layout.StatusPill;
import za.ac.cput.model.domain.PatientTicket;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class TicketDetailsDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");

    public static void show(Component parent, PatientTicket ticket) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 520);

        // ── Header ────────────────────────────────────────────────────────────
        body.add(StitchDialog.header(dialog, Icons.TICKET,
                "Ticket #TK-" + String.format("%03d", ticket.getTicketId()),
                "Consultation Record"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(18));

        // ── 2×2 meta grid ────────────────────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(2, 2, 24, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Doctor
        grid.add(StitchDialog.metaCell("DOCTOR", doctorName(ticket)));

        // Appointment (date + time)
        String apptText = "—";
        if (ticket.getAppointment() != null && ticket.getAppointment().getAppointmentDate() != null) {
            apptText = ticket.getAppointment().getAppointmentDate().toString();
            if (ticket.getAppointment().getAppointmentTime() != null) {
                apptText += " " + ticket.getAppointment().getAppointmentTime();
            }
        }
        grid.add(StitchDialog.metaCell("APPOINTMENT", apptText));

        // Ticket Status
        grid.add(StitchDialog.metaCell("TICKET STATUS", new StatusPill(ticket.getCurrentStatus())));

        // Empty 4th cell
        JPanel empty = new JPanel();
        empty.setOpaque(false);
        grid.add(empty);

        body.add(grid);
        body.add(Box.createVerticalStrut(18));

        // ── Status History ────────────────────────────────────────────────────
        body.add(StitchDialog.sectionHeader(Icons.CLIPBOARD, "Status History", null));
        body.add(Box.createVerticalStrut(8));
        JComponent timeline = StatusTimeline.build(ticket.getStatusHistory());
        timeline.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(timeline);

        body.add(Box.createVerticalStrut(14));

        // ── Info banner ───────────────────────────────────────────────────────
        boolean resolved = "RESOLVED".equals(ticket.getCurrentStatus());
        body.add(StitchDialog.banner(
                resolved
                        ? "Your consultation is complete. Check your Payments page to settle any outstanding balance."
                        : "Your ticket is being processed. Check back for status updates.",
                resolved ? AppTheme.STATUS_SUCCESS_BG : AppTheme.PRIMARY_LIGHT,
                resolved ? AppTheme.STATUS_SUCCESS    : AppTheme.PRIMARY));

        body.add(Box.createVerticalStrut(20));

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel footer = StitchDialog.buttonRow();
        JButton close = new JButton("Close");
        AppTheme.styleActionButton(close, false, null);
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);
        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 520);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String doctorName(PatientTicket ticket) {
        if (ticket.getAppointment() == null || ticket.getAppointment().getDoctor() == null
                || ticket.getAppointment().getDoctor().getName() == null) return "—";
        String last = ticket.getAppointment().getDoctor().getName().getLastName();
        return "Dr. " + (last != null ? last : "—");
    }
}
