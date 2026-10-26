/*
 BookAppointmentDialog.java

 Patient dialog to book a new appointment — Stitch design system

 Author: Joshua Reid Adams (230317693)

 Date: 26th October 2026
*/

package za.ac.cput.ui.patient.components;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.TimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;
import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Appointment;
import za.ac.cput.model.domain.Patient;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class BookAppointmentDialog {

    public static void show(Component parent, Runnable onBooked) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 480);

        // ── Header ────────────────────────────────────────────────────────────
        body.add(StitchDialog.header(dialog, Icons.CALENDAR, "Book New Appointment",
                "Request a new appointment with the clinic"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(18));

        // ── Preferred Date ────────────────────────────────────────────────────
        body.add(styledFieldLabel("Preferred Date", true));
        body.add(Box.createVerticalStrut(6));

        DatePickerSettings dateSettings = new DatePickerSettings();
        DatePicker datePicker = new DatePicker(dateSettings);
        dateSettings.setDateRangeLimits(LocalDate.now(), null);
        datePicker.setAlignmentX(Component.LEFT_ALIGNMENT);
        datePicker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        body.add(datePicker);
        body.add(Box.createVerticalStrut(14));

        // ── Preferred Time ────────────────────────────────────────────────────
        body.add(styledFieldLabel("Preferred Time", true));
        body.add(Box.createVerticalStrut(6));

        TimePickerSettings timeSettings = new TimePickerSettings();
        TimePicker timePicker = new TimePicker(timeSettings);
        timePicker.setAlignmentX(Component.LEFT_ALIGNMENT);
        timePicker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        body.add(timePicker);
        body.add(Box.createVerticalStrut(14));

        // ── Reason for Visit ──────────────────────────────────────────────────
        JPanel reasonHeader = new JPanel(new BorderLayout());
        reasonHeader.setOpaque(false);
        reasonHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        reasonHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JLabel reasonLbl = styledFieldLabel("Reason for Visit", true);
        reasonLbl.setBorder(null);
        JLabel maxChars = new JLabel("Max 500 characters");
        maxChars.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        maxChars.setForeground(AppTheme.TEXT_MUTED);
        reasonHeader.add(reasonLbl, BorderLayout.WEST);
        reasonHeader.add(maxChars, BorderLayout.EAST);
        body.add(reasonHeader);
        body.add(Box.createVerticalStrut(6));

        JTextArea reasonArea = new JTextArea(5, 20);
        reasonArea.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        reasonArea.setForeground(AppTheme.TEXT_PRIMARY);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBackground(AppTheme.SURFACE_ALT);
        reasonArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        reasonArea.putClientProperty("caretColor", AppTheme.PRIMARY);

        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1, true));
        reasonScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        reasonScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        body.add(reasonScroll);
        body.add(Box.createVerticalStrut(14));

        // ── Info note ─────────────────────────────────────────────────────────
        body.add(StitchDialog.infoBanner(
                "A nurse or admin will review your request and confirm a doctor and appointment time shortly."));
        body.add(Box.createVerticalStrut(10));

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

        JButton submit = new JButton("Request Appointment");
        AppTheme.styleActionButton(submit, true, Icons.CHECK);
        submit.addActionListener(e -> {
            LocalDate date = datePicker.getDate();
            LocalTime time = timePicker.getTime();
            String reason = reasonArea.getText().trim();

            if (date == null) { errorLabel.setText("Please select a date."); return; }
            if (time == null) { errorLabel.setText("Please select a time."); return; }
            if (reason.isEmpty()) { errorLabel.setText("Please enter a reason for your visit."); return; }
            if (reason.length() > 500) { errorLabel.setText("Reason may not exceed 500 characters."); return; }
            errorLabel.setText(" ");

            Appointment appointment = new Appointment();
            appointment.setAppointmentDate(date);
            appointment.setAppointmentTime(time);
            appointment.setReason(reason);
            appointment.setConfirmationStatus("PENDING");

            Patient self = new Patient();
            self.setUserId(SessionManager.getInstance().getUserId());
            appointment.setPatient(self);

            BaseApiClient.ApiResult<Appointment> result =
                    ApiClientProvider.getInstance().appointments().create(appointment);

            if (result.isSuccess()) {
                dialog.dispose();
                AppDialog.show(parent, "Appointment Requested",
                        "Your appointment request has been submitted for review.", AppDialog.Type.SUCCESS);
                if (onBooked != null) onBooked.run();
            } else {
                errorLabel.setText(result.getMessage() != null ? result.getMessage() : "Unable to submit request.");
            }
        });
        footer.add(submit);

        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 480);
    }

    private static JLabel styledFieldLabel(String text, boolean required) {
        JLabel label = new JLabel(required ? text + " *" : text);
        label.setFont(FontManager.bodyFont(Font.BOLD, 13));
        label.setForeground(AppTheme.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return label;
    }
}
