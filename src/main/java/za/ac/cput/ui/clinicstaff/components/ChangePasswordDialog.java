/*
 ChangePasswordDialog.java

 Change password dialog — Stitch design system

 Author: Joshua Reid Adams (230317693)

 Date: 26th October 2026
*/

package za.ac.cput.ui.clinicstaff.components;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.auth.ChangePasswordRequest;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.auth.components.ToggleablePasswordField;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog {

    public static void show(Component parent) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 460);

        // ── Header ────────────────────────────────────────────────────────────
        body.add(StitchDialog.header(dialog, Icons.USER, "Change Password",
                "Update your credentials to maintain account security and access."));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(18));

        // ── Fields ────────────────────────────────────────────────────────────
        ToggleablePasswordField oldPassword = new ToggleablePasswordField("Current Password");
        oldPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        oldPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        // New password with hint
        JPanel newPassHeader = new JPanel(new BorderLayout());
        newPassHeader.setOpaque(false);
        newPassHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        newPassHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JLabel newPassTitle = new JLabel("New Password");
        newPassTitle.setFont(FontManager.bodyFont(Font.BOLD, 13));
        newPassTitle.setForeground(AppTheme.TEXT_PRIMARY);
        JLabel minChars = new JLabel("Min. 8 characters");
        minChars.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        minChars.setForeground(AppTheme.TEXT_MUTED);
        newPassHeader.add(newPassTitle, BorderLayout.WEST);
        newPassHeader.add(minChars, BorderLayout.EAST);

        ToggleablePasswordField newPassword = new ToggleablePasswordField("New Password");
        newPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        newPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JLabel strengthHint = new JLabel("Include at least one uppercase letter, a number, and a special character.");
        strengthHint.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        strengthHint.setForeground(AppTheme.STATUS_DANGER);
        strengthHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        ToggleablePasswordField confirmPassword = new ToggleablePasswordField("Confirm New Password");
        confirmPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        // ── Error label ───────────────────────────────────────────────────────
        JLabel errorLabel = StitchDialog.errorLabel();

        // ── Info banner ───────────────────────────────────────────────────────
        JComponent infoBanner = StitchDialog.infoBanner(
                "Updating your password will automatically sign out other active clinical sessions across secondary devices.");

        body.add(oldPassword);
        body.add(Box.createVerticalStrut(10));
        body.add(newPassHeader);
        body.add(Box.createVerticalStrut(4));
        body.add(newPassword);
        body.add(Box.createVerticalStrut(4));
        body.add(strengthHint);
        body.add(Box.createVerticalStrut(10));
        body.add(confirmPassword);
        body.add(Box.createVerticalStrut(12));
        body.add(infoBanner);
        body.add(Box.createVerticalStrut(6));
        body.add(errorLabel);
        body.add(Box.createVerticalStrut(16));

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel footer = StitchDialog.buttonRow();

        JButton cancel = new JButton("Cancel");
        AppTheme.styleActionButton(cancel, false, null);
        cancel.addActionListener(e -> dialog.dispose());
        footer.add(cancel);

        JButton submit = new JButton("Update Password");
        AppTheme.styleActionButton(submit, true, null);
        submit.addActionListener(e -> {
            String oldPass     = new String(oldPassword.getPassword());
            String newPass     = new String(newPassword.getPassword());
            String confirmPass = new String(confirmPassword.getPassword());

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                errorLabel.setText("Please fill in all fields.");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                errorLabel.setText("New passwords do not match.");
                return;
            }
            if (newPass.length() < 8) {
                errorLabel.setText("New password must be at least 8 characters.");
                return;
            }

            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setEmail(SessionManager.getInstance().getEmail());
            request.setOldPassword(oldPass);
            request.setNewPassword(newPass);

            BaseApiClient.ApiResult<String> result = ApiClientProvider.getInstance().auth().changePassword(request);

            if (result.isSuccess()) {
                dialog.dispose();
                AppDialog.show(parent, "Password Updated",
                        "Your password has been changed successfully.", AppDialog.Type.SUCCESS);
            } else {
                errorLabel.setText(result.getMessage() != null ? result.getMessage() : "Unable to change password.");
            }
        });
        footer.add(submit);

        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 460);
    }
}
