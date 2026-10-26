/*
 GeneratePaymentDialog.java

 Generate payment dialog — Stitch design system

 Author: Abdullahi Raage Farah (230971091)

 Date: 26th October 2026
*/

package za.ac.cput.ui.clinicstaff.components;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Payment;
import za.ac.cput.model.domain.PatientTicket;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class GeneratePaymentDialog {

    public static void show(Component parent, PatientTicket ticket, Runnable onGenerated) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 460);

        // ── Header ────────────────────────────────────────────────────────────
        body.add(StitchDialog.header(dialog, Icons.CARD, "Payment Request",
                "Generate a payment for this ticket"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(16));

        // ── Info note ─────────────────────────────────────────────────────────
        body.add(StitchDialog.infoBanner(
                "Creates a pending payment request visible on the patient's Payments page."));
        body.add(Box.createVerticalStrut(14));

        // ── Amount field ──────────────────────────────────────────────────────
        body.add(StitchDialog.fieldLabel("Consultation Fee (R)"));
        body.add(Box.createVerticalStrut(6));

        JTextField amountField = StitchDialog.styledField("e.g. 150.00");
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        body.add(amountField);
        body.add(Box.createVerticalStrut(14));

        // ── Method combo ──────────────────────────────────────────────────────
        body.add(StitchDialog.fieldLabel("Payment Method"));
        body.add(Box.createVerticalStrut(6));

        JComboBox<String> methodCombo = new JComboBox<>(new String[]{"CASH", "CARD", "EFT", "MEDICAL_AID"});
        methodCombo.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        methodCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        methodCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        body.add(methodCombo);

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

        JButton generate = new JButton("Generate Request");
        AppTheme.styleActionButton(generate, true, Icons.CARD);
        generate.addActionListener(e -> {
            String amountStr = amountField.getText().trim();
            if (amountStr.isEmpty()) {
                errorLabel.setText("Please enter a fee amount.");
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr).setScale(2, RoundingMode.HALF_UP);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    errorLabel.setText("Amount must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException ex) {
                errorLabel.setText("Please enter a valid number.");
                return;
            }
            errorLabel.setText(" ");

            Payment payment = new Payment();
            payment.setAppointment(ticket.getAppointment());
            payment.setPaymentAmount(amount);
            payment.setPaymentMethod((String) methodCombo.getSelectedItem());
            payment.setPaymentStatus("PENDING");

            BaseApiClient.ApiResult<Payment> result = ApiClientProvider.getInstance().payments().create(payment);

            if (result.isSuccess()) {
                dialog.dispose();
                AppDialog.show(parent, "Payment Request Generated",
                        "A payment request for R" + amount + " is now pending on the patient's Payments page.",
                        AppDialog.Type.SUCCESS);
                if (onGenerated != null) onGenerated.run();
            } else {
                errorLabel.setText(result.getMessage() != null ? result.getMessage() : "Unable to generate payment.");
            }
        });
        footer.add(generate);

        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 460);
    }
}
