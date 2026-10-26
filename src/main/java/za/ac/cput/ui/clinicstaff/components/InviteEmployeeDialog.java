/*
 InviteEmployeeDialog.java

 Invite employee dialog — Stitch design system

 Author: Matthew Barron (230398863)

 Date: 26th October 2026
*/

package za.ac.cput.ui.clinicstaff.components;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.auth.EmployeeInviteRequest;
import za.ac.cput.ui.auth.components.LabeledTextField;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;

public class InviteEmployeeDialog {

    public static void show(Component parent, String userType, String staffRole, Runnable onSent) {
        boolean isDoctor = "DOCTOR".equals(userType);
        String role = isDoctor ? "Doctor" : "Nurse";

        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 460);

        // ── Header ────────────────────────────────────────────────────────────
        body.add(StitchDialog.header(dialog, Icons.MAIL, "Invite " + role,
                "Send an invitation to join MediTicket"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(16));

        // ── Info banner ───────────────────────────────────────────────────────
        body.add(StitchDialog.infoBanner("An invitation code will be sent to this email address."));
        body.add(Box.createVerticalStrut(14));

        // ── Email field ───────────────────────────────────────────────────────
        LabeledTextField emailField = new LabeledTextField("Email");
        emailField.getField().putClientProperty("JTextField.placeholderText",
                isDoctor ? "doctor@example.com" : "nurse@example.com");
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        body.add(emailField);

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

        JButton send = new JButton("Send Invitation");
        AppTheme.styleActionButton(send, true, Icons.MAIL);
        send.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty() || !email.contains("@")) {
                errorLabel.setText("Please enter a valid email address.");
                return;
            }

            EmployeeInviteRequest request = new EmployeeInviteRequest();
            request.setEmail(email);
            request.setUserType(userType);
            request.setStaffRole(staffRole);

            BaseApiClient.ApiResult<String> result = ApiClientProvider.getInstance().auth().inviteEmployee(request);

            if (result.isSuccess()) {
                dialog.dispose();
                AppDialog.show(parent, "Invitation Sent",
                        "An invitation has been sent to " + email + ".", AppDialog.Type.SUCCESS);
                if (onSent != null) onSent.run();
            } else {
                errorLabel.setText(result.getMessage() != null ? result.getMessage() : "Unable to send invitation.");
            }
        });
        footer.add(send);

        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 460);
    }
}
