/*
 AppointmentDetailsDialog.java

 Doctor dialog for appointment details

 Author: Joshua Reid Adams (230317693)

 Date: 18th August 2026
*/

package za.ac.cput.ui.doctor.components;

import za.ac.cput.model.domain.Appointment;
import za.ac.cput.ui.layout.StatusPill;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;

public class AppointmentDetailsDialog {

    public static void show(Component parent, Appointment appointment) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 480);

        body.add(StitchDialog.header(dialog, Icons.CALENDAR, "Appointment Details", "Your Schedule"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(18));

        // Extract patient name parts for avatar
        String patientFirst = "";
        String patientLast  = "";
        if (appointment.getPatient() != null && appointment.getPatient().getName() != null) {
            String f = appointment.getPatient().getName().getFirstName();
            String l = appointment.getPatient().getName().getLastName();
            patientFirst = f != null ? f : "";
            patientLast  = l != null ? l : "";
        }
        String patientFullName = (patientFirst + " " + patientLast).trim();
        if (patientFullName.isEmpty()) patientFullName = "—";

        // 2-column meta grid — 3 rows x 2 cols
        JPanel grid = new JPanel(new GridLayout(3, 2, 24, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Row 1: Patient | Status
        grid.add(StitchDialog.metaCell("PATIENT", StitchDialog.personCell(
                StitchDialog.initials(patientFirst, patientLast),
                patientFullName, "Patient", AppTheme.TEXT_MUTED)));
        grid.add(StitchDialog.metaCell("STATUS", new StatusPill(appointment.getConfirmationStatus())));

        // Row 2: Date | Time
        String dateStr = appointment.getAppointmentDate() != null
                ? appointment.getAppointmentDate().toString() : "—";
        String timeStr = appointment.getAppointmentTime() != null
                ? appointment.getAppointmentTime().toString() : "—";
        grid.add(StitchDialog.metaCell("DATE", StitchDialog.iconRow(Icons.CALENDAR, dateStr, null)));
        grid.add(StitchDialog.metaCell("TIME", StitchDialog.iconRow(Icons.CLOCK, timeStr, null)));

        // Row 3: Reason (spans conceptually; second cell left empty)
        String reason = appointment.getReason();
        if (reason != null && reason.length() > 60) reason = reason.substring(0, 60) + "…";
        if (reason == null || reason.isBlank()) reason = "—";
        grid.add(StitchDialog.metaCell("REASON", reason));
        grid.add(new JPanel() {{ setOpaque(false); }});

        body.add(grid);
        body.add(Box.createVerticalStrut(20));

        // Footer
        JPanel footer = StitchDialog.buttonRow();
        JButton close = new JButton("Close");
        AppTheme.styleActionButton(close, false, null);
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);
        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 480);
    }
}
