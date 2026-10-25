/*
 AppointmentDetailsDialog.java

 Dialog for appointment details

 Author: Joshua Reid Adams (230317693)

 Date: 18th August 2026
*/

package za.ac.cput.ui.clinicstaff.components;

import za.ac.cput.model.domain.Appointment;
import za.ac.cput.ui.layout.StatusPill;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;

public class AppointmentDetailsDialog {

    public interface ActionCallback {
        void onApprove();
        void onReject();
    }

    public static void show(Component parent, Appointment appointment, ActionCallback callback) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 540);

        body.add(StitchDialog.header(dialog, Icons.CALENDAR, "Appointment Details", "Clinical Schedule"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(18));

        // Build patient name parts for avatar initials
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

        // Build doctor name parts
        String doctorFirst = "";
        String doctorLast  = "";
        if (appointment.getDoctor() != null && appointment.getDoctor().getName() != null) {
            String f = appointment.getDoctor().getName().getFirstName();
            String l = appointment.getDoctor().getName().getLastName();
            doctorFirst = f != null ? f : "";
            doctorLast  = l != null ? l : "";
        }
        String doctorDisplay   = doctorLast.isEmpty() ? "Unassigned" : "Dr. " + doctorLast;
        String doctorSpecialty = (appointment.getDoctor() != null
                && appointment.getDoctor().getSpecialty() != null)
                ? appointment.getDoctor().getSpecialty() : "Physician";

        // 2-column meta grid — 3 rows × 2 cols
        JPanel grid = new JPanel(new GridLayout(3, 2, 24, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Row 1: Patient | Attending Doctor
        grid.add(StitchDialog.metaCell("PATIENT", StitchDialog.personCell(
                StitchDialog.initials(patientFirst, patientLast),
                patientFullName, "Patient", AppTheme.TEXT_MUTED)));
        grid.add(StitchDialog.metaCell("ATTENDING DOCTOR", StitchDialog.personCell(
                StitchDialog.initials(doctorFirst, doctorLast),
                doctorDisplay, doctorSpecialty, AppTheme.PRIMARY)));

        // Row 2: Date | Time
        String dateStr = appointment.getAppointmentDate() != null
                ? appointment.getAppointmentDate().toString() : "—";
        String timeStr = appointment.getAppointmentTime() != null
                ? appointment.getAppointmentTime().toString() : "—";
        grid.add(StitchDialog.metaCell("DATE", StitchDialog.iconRow(Icons.CALENDAR, dateStr, null)));
        grid.add(StitchDialog.metaCell("TIME", StitchDialog.iconRow(Icons.CLOCK, timeStr, null)));

        // Row 3: Status | Reason
        grid.add(StitchDialog.metaCell("STATUS", new StatusPill(appointment.getConfirmationStatus())));
        String reason = appointment.getReason();
        if (reason != null && reason.length() > 30) reason = reason.substring(0, 30) + "…";
        if (reason == null || reason.isBlank()) reason = "—";
        grid.add(StitchDialog.metaCell("REASON", reason));

        body.add(grid);
        body.add(Box.createVerticalStrut(20));

        boolean isPending = "PENDING".equals(appointment.getConfirmationStatus());
        if (!isPending) {
            body.add(StitchDialog.infoBanner("No further action needed at this stage."));
            body.add(Box.createVerticalStrut(16));
        }

        // Footer
        JPanel footer = StitchDialog.buttonRow();

        JButton close = new JButton("Close");
        AppTheme.styleActionButton(close, false, null);
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);

        if (isPending) {
            JButton reject = new JButton("Reject");
            reject.setFont(FontManager.bodyFont(Font.BOLD, 13));
            reject.setForeground(AppTheme.STATUS_DANGER);
            reject.setBackground(AppTheme.SURFACE);
            reject.setFocusPainted(false);
            reject.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            reject.putClientProperty("FlatLaf.style",
                    "arc: 10; borderWidth: 1; focusWidth: 0; innerFocusWidth: 0; margin: 9,16,9,16;"
                    + " borderColor: #d64545; hoverBackground: #fae6e6;");
            reject.addActionListener(e -> { dialog.dispose(); callback.onReject(); });
            footer.add(reject);

            JButton approve = new JButton("Approve");
            approve.setFont(FontManager.bodyFont(Font.BOLD, 13));
            approve.setForeground(AppTheme.TEXT_ON_PRIMARY);
            approve.setBackground(AppTheme.STATUS_SUCCESS);
            approve.setBorderPainted(false);
            approve.setFocusPainted(false);
            approve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            approve.putClientProperty("FlatLaf.style",
                    "arc: 10; focusWidth: 0; innerFocusWidth: 0; margin: 9,16,9,16;"
                    + " hoverBackground: #27895f;");
            approve.addActionListener(e -> { dialog.dispose(); callback.onApprove(); });
            footer.add(approve);
        }

        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 540);
    }
}
