/*
 ApproveAppointmentDialog.java

 Approve appointment dialog — Stitch design system

 Author: Joshua Reid Adams (230317693)

 Date: 26th October 2026
*/

package za.ac.cput.ui.clinicstaff.components;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Appointment;
import za.ac.cput.model.domain.Doctor;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ApproveAppointmentDialog {

    public static void show(Component parent, Appointment appointment, Runnable onApproved) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 480);

        // ── Header ────────────────────────────────────────────────────────────
        body.add(StitchDialog.header(dialog, Icons.CHECK, "Approve Appointment",
                "Assign a doctor and confirm"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(16));

        // ── Info note ─────────────────────────────────────────────────────────
        body.add(StitchDialog.infoBanner(
                "Select a doctor to assign. The patient will be notified once confirmed."));
        body.add(Box.createVerticalStrut(14));

        // ── Doctor dropdown ───────────────────────────────────────────────────
        body.add(StitchDialog.fieldLabel("Doctor"));
        body.add(Box.createVerticalStrut(6));

        JComboBox<Doctor> doctorCombo = new JComboBox<>();
        doctorCombo.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        doctorCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        doctorCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        doctorCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Doctor doctor) {
                    String name = doctor.getName() != null
                            ? "Dr. " + safe(doctor.getName().getFirstName()) + " " + safe(doctor.getName().getLastName())
                            : "Doctor #" + doctor.getUserId();
                    setText(name + (doctor.getSpecialty() != null ? " — " + doctor.getSpecialty() : ""));
                }
                return this;
            }
        });

        BaseApiClient.ApiResult<List<Doctor>> doctorResult = ApiClientProvider.getInstance().doctors().getAll();
        if (doctorResult.isSuccess() && doctorResult.getData() != null) {
            for (Doctor doctor : doctorResult.getData()) {
                doctorCombo.addItem(doctor);
            }
        }
        body.add(doctorCombo);

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

        JButton confirm = new JButton("Approve Appointment");
        AppTheme.styleActionButton(confirm, true, Icons.CHECK);
        confirm.addActionListener(e -> {
            Doctor selected = (Doctor) doctorCombo.getSelectedItem();
            if (selected == null) { errorLabel.setText("Please select a doctor."); return; }

            int staffId = SessionManager.getInstance().getUserId();
            BaseApiClient.ApiResult<Appointment> result = ApiClientProvider.getInstance()
                    .appointments().approve(appointment.getAppointmentId(), selected.getUserId(), staffId);

            if (result.isSuccess()) {
                dialog.dispose();
                AppDialog.show(parent, "Appointment Approved",
                        "The appointment has been confirmed and assigned to the selected doctor.", AppDialog.Type.SUCCESS);
                if (onApproved != null) onApproved.run();
            } else {
                errorLabel.setText(result.getMessage() != null ? result.getMessage() : "Unable to approve appointment.");
            }
        });
        footer.add(confirm);

        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 480);
    }

    private static String safe(String s) { return s != null ? s : ""; }
}
