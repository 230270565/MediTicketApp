/*
 MedicalAidDialog.java

 Patient dialog for medical aid — Stitch design system rewrite

 Author: Abdullahi Raage Farah (230971091)

 Date: 25th October 2026
*/

package za.ac.cput.ui.patient.components;

import za.ac.cput.model.domain.Payment;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;
import java.math.RoundingMode;

public class MedicalAidDialog {

    private static final String SCHEME_PROMPT = "Select your scheme";

    private static final String[] SCHEMES = {
            SCHEME_PROMPT, "Discovery Health", "Momentum Health",
            "Bonitas", "Medihelp", "Other"
    };

    public static void show(Component parent, Payment payment) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 480);

        // ── Header ────────────────────────────────────────────────────────────
        body.add(StitchDialog.header(dialog, Icons.CLIPBOARD,
                "Medical Aid Claim",
                "Submit or update your claim details"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(18));

        // ── Amount hero ───────────────────────────────────────────────────────
        JLabel amountLabel = new JLabel("Amount to Claim");
        amountLabel.setFont(FontManager.bodyFont(Font.BOLD, 12));
        amountLabel.setForeground(AppTheme.TEXT_MUTED);
        amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel amountValue = new JLabel("R" + (payment.getPaymentAmount() != null
                ? payment.getPaymentAmount().setScale(2, RoundingMode.HALF_UP) : "0.00"));
        amountValue.setFont(FontManager.headlineFont(Font.BOLD, 28));
        amountValue.setForeground(AppTheme.TEXT_PRIMARY);
        amountValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountValue.setBorder(BorderFactory.createEmptyBorder(2, 0, AppTheme.SPACE_MD, 0));

        body.add(amountLabel);
        body.add(amountValue);

        // ── Form fields ───────────────────────────────────────────────────────
        JComboBox<String> schemeCombo = new JComboBox<>(SCHEMES);
        schemeCombo.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        schemeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        schemeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JTextField memberNumber = styledField("123456789");
        JTextField dependentCode = styledField("e.g. 01 — leave blank if main member");

        JLabel errorLabel = StitchDialog.errorLabel();

        body.add(StitchDialog.fieldLabel("Medical Aid Scheme"));
        body.add(schemeCombo);
        body.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        body.add(StitchDialog.fieldLabel("Membership Number"));
        body.add(memberNumber);
        body.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        body.add(StitchDialog.fieldLabel("Dependent Code (optional)"));
        body.add(dependentCode);
        body.add(errorLabel);
        body.add(Box.createVerticalStrut(20));

        // ── Footer ────────────────────────────────────────────────────────────
        JButton cancel = new JButton("Cancel");
        AppTheme.styleActionButton(cancel, false, null);
        cancel.addActionListener(e -> dialog.dispose());

        JButton submit = new JButton("Submit Claim Details");
        AppTheme.styleActionButton(submit, true, null);
        submit.addActionListener(e -> {
            String scheme = (String) schemeCombo.getSelectedItem();
            if (scheme == null || SCHEME_PROMPT.equals(scheme)) {
                errorLabel.setText("Please select your medical aid scheme.");
                return;
            }

            String member = memberNumber.getText().replaceAll("\\s", "");
            if (member.isEmpty()) {
                errorLabel.setText("Please enter your membership number.");
                return;
            }
            if (!member.matches("\\d{9}")) {
                errorLabel.setText("Membership number must be exactly 9 digits.");
                return;
            }

            String dependent = dependentCode.getText().trim();
            if (!dependent.isEmpty() && !dependent.matches("\\d{1,2}")) {
                errorLabel.setText("Dependent code must be 1 or 2 digits.");
                return;
            }
            errorLabel.setText(" ");

            dialog.dispose();

            AppDialog.show(parent, "Claim Submitted",
                    "Sent to " + scheme + ".\nThis payment stays pending until approved.",
                    AppDialog.Type.SUCCESS);
        });

        JPanel footer = StitchDialog.buttonRow();
        footer.add(cancel);
        footer.add(submit);
        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 480);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static JTextField styledField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }
}
