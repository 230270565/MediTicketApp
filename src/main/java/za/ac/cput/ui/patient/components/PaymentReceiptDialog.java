/*
 PaymentReceiptDialog.java

 Patient Payment Receipt Dialog — Stitch design system rewrite

 Author: Abdullahi Raage Farah (230971091)

 Date: 25th October 2026
*/

package za.ac.cput.ui.patient.components;

import za.ac.cput.model.domain.Appointment;
import za.ac.cput.model.domain.Payment;
import za.ac.cput.ui.layout.StatusPill;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

public class PaymentReceiptDialog {

    public static void show(Component parent, Payment payment) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 520);

        // ── Header ────────────────────────────────────────────────────────────
        body.add(StitchDialog.header(dialog, Icons.CARD, "Payment Receipt", "MediTicket Health Services"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(18));

        // ── Amount hero ───────────────────────────────────────────────────────
        JPanel amountHero = new JPanel();
        amountHero.setLayout(new BoxLayout(amountHero, BoxLayout.Y_AXIS));
        amountHero.setOpaque(false);
        amountHero.setAlignmentX(Component.LEFT_ALIGNMENT);

        String amountText = payment.getPaymentAmount() != null
                ? "R" + payment.getPaymentAmount().setScale(2, RoundingMode.HALF_UP)
                : "—";
        JLabel amountLabel = new JLabel(amountText);
        amountLabel.setFont(FontManager.headlineFont(Font.BOLD, 20));
        amountLabel.setForeground(AppTheme.TEXT_PRIMARY);
        amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pillRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        pillRow.setOpaque(false);
        pillRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        pillRow.add(new StatusPill(payment.getPaymentStatus()));

        amountHero.add(amountLabel);
        amountHero.add(pillRow);
        body.add(amountHero);
        body.add(Box.createVerticalStrut(18));

        // ── 3×2 detail grid ───────────────────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(3, 2, 24, 12));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        grid.add(StitchDialog.metaCell("DOCTOR",           doctorName(payment)));
        grid.add(StitchDialog.metaCell("APPOINTMENT DATE", appointmentDate(payment)));
        grid.add(StitchDialog.metaCell("PAYMENT DATE",     paymentDate(payment)));
        grid.add(StitchDialog.metaCell("METHOD",           methodDisplayName(payment.getPaymentMethod())));
        grid.add(StitchDialog.metaCell("PAYMENT ID",       String.valueOf(payment.getPaymentId())));
        grid.add(StitchDialog.metaCell("STATUS",           new StatusPill(payment.getPaymentStatus())));

        body.add(grid);
        body.add(Box.createVerticalStrut(20));

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel footer = StitchDialog.buttonRow();
        JButton close = new JButton("Close");
        AppTheme.styleActionButton(close, true, null);
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);
        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 520);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String doctorName(Payment payment) {
        Appointment appt = payment.getAppointment();
        if (appt == null || appt.getDoctor() == null || appt.getDoctor().getName() == null) return "—";
        String last = appt.getDoctor().getName().getLastName();
        return "Dr. " + (last != null ? last : "—");
    }

    private static String appointmentDate(Payment payment) {
        Appointment appt = payment.getAppointment();
        if (appt == null || appt.getAppointmentDate() == null) return "—";
        return appt.getAppointmentDate().toString();
    }

    private static String paymentDate(Payment payment) {
        if (payment.getPaymentDate() == null) return "—";
        return payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }

    private static String methodDisplayName(String method) {
        if (method == null) return "—";
        return switch (method) {
            case "CASH"        -> "Cash";
            case "CARD"        -> "Card";
            case "EFT"         -> "EFT";
            case "MEDICAL_AID" -> "Medical Aid";
            default            -> method;
        };
    }
}
