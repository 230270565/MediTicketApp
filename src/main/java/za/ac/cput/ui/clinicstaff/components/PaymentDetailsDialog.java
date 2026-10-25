/*
 PaymentDetailsDialog.java

 Dialog for payment details — rewritten for the Stitch design system.

 Author: Abdullahi Raage Farah (230971091)

 Date: 25th October 2026
*/

package za.ac.cput.ui.clinicstaff.components;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Appointment;
import za.ac.cput.model.domain.Payment;
import za.ac.cput.ui.layout.StatusPill;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;
import java.math.RoundingMode;

public class PaymentDetailsDialog {

    public static void show(Component parent, Payment payment, Runnable onChanged) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 520);

        body.add(StitchDialog.header(dialog, Icons.CARD, "Payment Details", "Billing Record"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(18));

        // 3×2 grid
        JPanel grid = new JPanel(new GridLayout(3, 2, 24, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));

        // Row 1: Patient | Doctor
        String patFirst = patientFirstName(payment);
        String patLast  = patientLastName(payment);
        grid.add(StitchDialog.metaCell("PATIENT",
                StitchDialog.personCell(
                        StitchDialog.initials(patFirst, patLast),
                        patientName(payment), "Patient", AppTheme.TEXT_MUTED)));

        String docFirst = doctorFirstName(payment);
        String docLast  = doctorLastName(payment);
        grid.add(StitchDialog.metaCell("DOCTOR",
                StitchDialog.personCell(
                        StitchDialog.initials(docFirst, docLast),
                        doctorName(payment), "Doctor", AppTheme.TEXT_MUTED)));

        // Row 2: Appointment Date | Method
        grid.add(StitchDialog.metaCell("APPOINTMENT DATE", appointmentDate(payment)));
        grid.add(StitchDialog.metaCell("METHOD",
                payment.getPaymentMethod() != null
                        ? AppTheme.label(payment.getPaymentMethod())
                        : "—"));

        // Row 3: Amount (bold, large) | Status
        JLabel amountLabel = new JLabel(payment.getPaymentAmount() != null
                ? "R" + payment.getPaymentAmount().setScale(2, RoundingMode.HALF_UP) : "—");
        amountLabel.setFont(FontManager.bodyFont(Font.BOLD, 18));
        amountLabel.setForeground(AppTheme.TEXT_PRIMARY);
        grid.add(StitchDialog.metaCell("AMOUNT", amountLabel));
        grid.add(StitchDialog.metaCell("STATUS", new StatusPill(payment.getPaymentStatus())));

        body.add(grid);
        body.add(Box.createVerticalStrut(18));

        // Info / action section based on status
        boolean isEft        = "EFT".equals(payment.getPaymentMethod());
        boolean isMedicalAid = "MEDICAL_AID".equals(payment.getPaymentMethod());
        boolean isPending    = "PENDING".equals(payment.getPaymentStatus());

        if (isPending && isEft) {
            body.add(StitchDialog.infoBanner(
                    "Waiting for the patient to complete payment on their dashboard."));
            body.add(Box.createVerticalStrut(8));
        } else if (isPending) {
            String noteText = isMedicalAid
                    ? "Awaiting medical aid authorisation — confirm once approved."
                    : "Collected in person — confirm once received.";
            body.add(StitchDialog.infoBanner(noteText));
            body.add(Box.createVerticalStrut(10));

            JButton markPaid = new JButton("Mark as Paid");
            AppTheme.styleActionButton(markPaid, true, Icons.CHECK);
            markPaid.setBackground(AppTheme.STATUS_SUCCESS);
            markPaid.addActionListener(e -> markAsPaid(dialog, parent, payment, onChanged));
            JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            btnWrap.setOpaque(false);
            btnWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnWrap.add(markPaid);
            body.add(btnWrap);
            body.add(Box.createVerticalStrut(8));
        } else if ("FAILED".equals(payment.getPaymentStatus())) {
            body.add(StitchDialog.banner("This payment attempt failed.",
                    AppTheme.STATUS_DANGER_BG, AppTheme.STATUS_DANGER));
            body.add(Box.createVerticalStrut(8));
        } else if ("REFUNDED".equals(payment.getPaymentStatus())) {
            body.add(StitchDialog.infoBanner("This payment has been refunded."));
            body.add(Box.createVerticalStrut(8));
        }

        body.add(Box.createVerticalStrut(20));

        JPanel footer = StitchDialog.buttonRow();
        JButton close = new JButton("Close");
        AppTheme.styleActionButton(close, false, null);
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);
        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 520);
    }

    private static void markAsPaid(JDialog dialog, Component parent, Payment payment, Runnable onChanged) {
        String amount = payment.getPaymentAmount().setScale(2, RoundingMode.HALF_UP).toString();
        String prompt = "MEDICAL_AID".equals(payment.getPaymentMethod())
                ? "Confirm that the medical aid claim for R" + amount + " has been approved?"
                : "Confirm that payment of R" + amount + " has been received in person?";

        int confirm = JOptionPane.showConfirmDialog(dialog, prompt,
                "Mark as Paid", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        payment.setPaymentStatus("PAID");
        BaseApiClient.ApiResult<Payment> result = ApiClientProvider.getInstance().payments().update(payment);

        if (result.isSuccess()) {
            dialog.dispose();
            AppDialog.show(parent, "Payment Confirmed",
                    "The payment has been marked as paid, and the ticket has been closed.", AppDialog.Type.SUCCESS);
            if (onChanged != null) onChanged.run();
        } else {
            AppDialog.show(parent, "Unable to Update Payment",
                    result.getMessage() != null ? result.getMessage() : "Something went wrong.", AppDialog.Type.ERROR);
        }
    }

    private static String patientName(Payment payment) {
        Appointment appt = payment.getAppointment();
        if (appt == null || appt.getPatient() == null || appt.getPatient().getName() == null) return "—";
        String first = appt.getPatient().getName().getFirstName();
        String last  = appt.getPatient().getName().getLastName();
        return (first != null ? first : "") + " " + (last != null ? last : "");
    }

    private static String patientFirstName(Payment payment) {
        Appointment appt = payment.getAppointment();
        if (appt == null || appt.getPatient() == null || appt.getPatient().getName() == null) return "";
        return appt.getPatient().getName().getFirstName() != null
                ? appt.getPatient().getName().getFirstName() : "";
    }

    private static String patientLastName(Payment payment) {
        Appointment appt = payment.getAppointment();
        if (appt == null || appt.getPatient() == null || appt.getPatient().getName() == null) return "";
        return appt.getPatient().getName().getLastName() != null
                ? appt.getPatient().getName().getLastName() : "";
    }

    private static String doctorName(Payment payment) {
        Appointment appt = payment.getAppointment();
        if (appt == null || appt.getDoctor() == null || appt.getDoctor().getName() == null) return "—";
        String last = appt.getDoctor().getName().getLastName();
        return "Dr. " + (last != null ? last : "—");
    }

    private static String doctorFirstName(Payment payment) {
        Appointment appt = payment.getAppointment();
        if (appt == null || appt.getDoctor() == null || appt.getDoctor().getName() == null) return "";
        return appt.getDoctor().getName().getFirstName() != null
                ? appt.getDoctor().getName().getFirstName() : "";
    }

    private static String doctorLastName(Payment payment) {
        Appointment appt = payment.getAppointment();
        if (appt == null || appt.getDoctor() == null || appt.getDoctor().getName() == null) return "";
        return appt.getDoctor().getName().getLastName() != null
                ? appt.getDoctor().getName().getLastName() : "";
    }

    private static String appointmentDate(Payment payment) {
        Appointment appt = payment.getAppointment();
        if (appt == null || appt.getAppointmentDate() == null) return "—";
        return appt.getAppointmentDate() + " " +
                (appt.getAppointmentTime() != null ? appt.getAppointmentTime() : "");
    }
}
