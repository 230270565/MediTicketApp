/*
 TicketDetailsDialog.java

 Doctor dialog for ticket details

 Author: Joshua Reid Adams (230317693)

 Date: 18th August 2026
*/

package za.ac.cput.ui.doctor.components;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.PatientTicket;
import za.ac.cput.ui.layout.StatusPill;
import za.ac.cput.ui.layout.StatusTimeline;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;

public class TicketDetailsDialog {

    public static void show(Component parent, PatientTicket ticket, Runnable onChanged) {
        String ticketTitle = "Ticket #TK-" + String.format("%03d", ticket.getTicketId());

        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 540);

        body.add(StitchDialog.header(dialog, Icons.TICKET, ticketTitle, "Ticket Details"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(18));

        // Extract patient name parts
        String patientFirst = "";
        String patientLast  = "";
        if (ticket.getPatient() != null && ticket.getPatient().getName() != null) {
            String f = ticket.getPatient().getName().getFirstName();
            String l = ticket.getPatient().getName().getLastName();
            patientFirst = f != null ? f : "";
            patientLast  = l != null ? l : "";
        }
        String patientFullName = (patientFirst + " " + patientLast).trim();
        if (patientFullName.isEmpty()) patientFullName = "—";

        // Appointment date/time strings
        String dateStr = "—";
        String timeStr = "—";
        if (ticket.getAppointment() != null) {
            if (ticket.getAppointment().getAppointmentDate() != null)
                dateStr = ticket.getAppointment().getAppointmentDate().toString();
            if (ticket.getAppointment().getAppointmentTime() != null)
                timeStr = ticket.getAppointment().getAppointmentTime().toString();
        }

        // 2-column meta grid — 2 rows x 2 cols
        JPanel grid = new JPanel(new GridLayout(2, 2, 24, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Row 1: Patient | Status
        grid.add(StitchDialog.metaCell("PATIENT", StitchDialog.personCell(
                StitchDialog.initials(patientFirst, patientLast),
                patientFullName, "Patient", AppTheme.TEXT_MUTED)));
        grid.add(StitchDialog.metaCell("TICKET STATUS", new StatusPill(ticket.getCurrentStatus())));

        // Row 2: Date | Time
        grid.add(StitchDialog.metaCell("APPOINTMENT DATE",
                StitchDialog.iconRow(Icons.CALENDAR, dateStr, null)));
        grid.add(StitchDialog.metaCell("APPOINTMENT TIME",
                StitchDialog.iconRow(Icons.CLOCK, timeStr, null)));

        body.add(grid);
        body.add(Box.createVerticalStrut(20));

        // Status history section
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(12));
        body.add(StitchDialog.sectionHeader(Icons.TICKET, "Status History", null));
        body.add(Box.createVerticalStrut(8));

        JComponent timeline = StatusTimeline.build(ticket.getStatusHistory());
        timeline.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeline.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        body.add(timeline);

        body.add(Box.createVerticalStrut(20));

        // Status-dependent footer content
        String status = ticket.getCurrentStatus();
        if ("RESOLVED".equals(status)) {
            body.add(StitchDialog.infoBanner(
                    "Consultation complete. This ticket is now with clinic staff for payment processing."));
            body.add(Box.createVerticalStrut(12));
        }

        JPanel footer = StitchDialog.buttonRow();

        JButton close = new JButton("Close");
        AppTheme.styleActionButton(close, false, null);
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);

        if ("OPEN".equals(status)) {
            JButton start = new JButton("Start Consultation");
            AppTheme.styleActionButton(start, true, null);
            start.addActionListener(e -> startConsultation(dialog, parent, ticket, onChanged));
            footer.add(start);
        } else if ("IN_PROGRESS".equals(status)) {
            JButton complete = new JButton("Complete Consultation");
            AppTheme.styleActionButton(complete, true, null);
            complete.addActionListener(e -> {
                dialog.dispose();
                CompleteConsultationDialog.show(parent, ticket, onChanged);
            });
            footer.add(complete);
        }

        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 540);
    }

    private static void startConsultation(JDialog dialog, Component parent,
                                          PatientTicket ticket, Runnable onChanged) {
        BaseApiClient.ApiResult<PatientTicket> result = ApiClientProvider.getInstance()
                .patientTickets().progressStatus(ticket.getTicketId(), "IN_PROGRESS", null);

        if (result.isSuccess()) {
            dialog.dispose();
            AppDialog.show(parent, "Consultation Started",
                    "The ticket is now in progress.", AppDialog.Type.SUCCESS);
            if (onChanged != null) onChanged.run();
        } else {
            AppDialog.show(parent, "Unable to Start",
                    result.getMessage() != null ? result.getMessage() : "Something went wrong.",
                    AppDialog.Type.ERROR);
        }
    }
}
