/*
 CreateTicketDialog.java

 Create ticket dialog — Stitch design system

 Author: Joshua Reid Adams (230317693)

 Date: 26th October 2026
*/

package za.ac.cput.ui.clinicstaff.components;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Appointment;
import za.ac.cput.model.domain.PatientTicket;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class CreateTicketDialog {

    public static void show(Component parent, Appointment appointment, Runnable onCreated) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 480);

        // ── Header ────────────────────────────────────────────────────────────
        body.add(StitchDialog.header(dialog, Icons.TICKET, "Create Ticket",
                "Open a consultation record"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(16));

        // ── Info note ─────────────────────────────────────────────────────────
        body.add(StitchDialog.infoBanner(
                "Creates a consultation ticket for this appointment starting as OPEN."));
        body.add(Box.createVerticalStrut(14));

        // ── Read-only 2×2 meta ────────────────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(2, 2, 24, 10));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.add(StitchDialog.metaCell("PATIENT", patientName(appointment)));
        grid.add(StitchDialog.metaCell("DOCTOR", doctorName(appointment)));
        grid.add(StitchDialog.metaCell("APPOINTMENT", appointmentDate(appointment)));
        grid.add(new JPanel() {{ setOpaque(false); }});
        body.add(grid);
        body.add(Box.createVerticalStrut(14));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(14));

        // ── Description field ─────────────────────────────────────────────────
        body.add(StitchDialog.fieldLabel("Ticket Description"));
        body.add(Box.createVerticalStrut(6));

        JTextArea descArea = new JTextArea(4, 20);
        descArea.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        descArea.setForeground(AppTheme.TEXT_PRIMARY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(AppTheme.SURFACE_ALT);
        descArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1, true));
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        body.add(descScroll);

        // ── Error label ───────────────────────────────────────────────────────
        JLabel errorLabel = StitchDialog.errorLabel();
        body.add(errorLabel);
        body.add(Box.createVerticalStrut(16));

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel footer = StitchDialog.buttonRow();

        JButton cancel = new JButton("Cancel");
        AppTheme.styleActionButton(cancel, false, null);
        cancel.addActionListener(e -> dialog.dispose());
        footer.add(cancel);

        JButton create = new JButton("Create Ticket");
        AppTheme.styleActionButton(create, true, Icons.TICKET);
        create.addActionListener(e -> {
            String description = descArea.getText().trim();
            if (description.isEmpty()) {
                errorLabel.setText("Please enter a ticket description.");
                return;
            }

            PatientTicket ticket = new PatientTicket();
            ticket.setPatient(appointment.getPatient());
            ticket.setAppointment(appointment);
            ticket.setTicketDescription(description);
            ticket.setTicketCreatedDate(LocalDateTime.now());

            BaseApiClient.ApiResult<PatientTicket> result = ApiClientProvider.getInstance().patientTickets().create(ticket);

            if (result.isSuccess()) {
                dialog.dispose();
                AppDialog.show(parent, "Ticket Created",
                        "A consultation ticket has been created for this appointment.", AppDialog.Type.SUCCESS);
                if (onCreated != null) onCreated.run();
            } else {
                errorLabel.setText(result.getMessage() != null ? result.getMessage() : "Unable to create ticket.");
            }
        });
        footer.add(create);

        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 480);
    }

    private static String patientName(Appointment appointment) {
        if (appointment.getPatient() == null || appointment.getPatient().getName() == null) return "—";
        String first = appointment.getPatient().getName().getFirstName();
        String last  = appointment.getPatient().getName().getLastName();
        return ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
    }

    private static String doctorName(Appointment appointment) {
        if (appointment.getDoctor() == null || appointment.getDoctor().getName() == null) return "—";
        String last = appointment.getDoctor().getName().getLastName();
        return "Dr. " + (last != null ? last : "—");
    }

    private static String appointmentDate(Appointment appointment) {
        if (appointment.getAppointmentDate() == null) return "—";
        return appointment.getAppointmentDate().toString() + " "
                + (appointment.getAppointmentTime() != null ? appointment.getAppointmentTime() : "");
    }
}
