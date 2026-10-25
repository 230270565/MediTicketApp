/*
 TicketDetailsDialog.java

 Dialog for ticket details — rewritten for the Stitch design system.

 Author: Joshua Reid Adams (230317693)

 Date: 25th October 2026
*/

package za.ac.cput.ui.clinicstaff.components;

import za.ac.cput.ui.layout.StatusTimeline;
import za.ac.cput.ui.layout.StatusPill;
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
import java.time.format.DateTimeFormatter;

public class TicketDetailsDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");

    public static void show(Component parent, PatientTicket ticket, Payment existingPayment, Runnable onChanged) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 520);

        body.add(StitchDialog.header(dialog, Icons.TICKET,
                "Ticket #TK-" + String.format("%03d", ticket.getTicketId()),
                "Consultation Record"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(18));

        // 2×2 grid: Patient | Doctor / Appointment | Ticket Status
        JPanel grid = new JPanel(new GridLayout(2, 2, 24, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        String patientFirst = (ticket.getPatient() != null && ticket.getPatient().getName() != null)
                ? ticket.getPatient().getName().getFirstName() : "";
        String patientLast  = (ticket.getPatient() != null && ticket.getPatient().getName() != null)
                ? ticket.getPatient().getName().getLastName()  : "";
        grid.add(StitchDialog.metaCell("PATIENT",
                StitchDialog.personCell(
                        StitchDialog.initials(patientFirst, patientLast),
                        patientName(ticket), "Patient", AppTheme.TEXT_MUTED)));

        String docFirst = (ticket.getAppointment() != null
                && ticket.getAppointment().getDoctor() != null
                && ticket.getAppointment().getDoctor().getName() != null)
                ? ticket.getAppointment().getDoctor().getName().getFirstName() : "";
        String docLast  = (ticket.getAppointment() != null
                && ticket.getAppointment().getDoctor() != null
                && ticket.getAppointment().getDoctor().getName() != null)
                ? ticket.getAppointment().getDoctor().getName().getLastName() : "";
        grid.add(StitchDialog.metaCell("DOCTOR",
                StitchDialog.personCell(
                        StitchDialog.initials(docFirst, docLast),
                        doctorName(ticket), "Doctor", AppTheme.TEXT_MUTED)));

        String apptStr = (ticket.getAppointment() != null && ticket.getAppointment().getAppointmentDate() != null)
                ? ticket.getAppointment().getAppointmentDate() + " " +
                  (ticket.getAppointment().getAppointmentTime() != null
                          ? ticket.getAppointment().getAppointmentTime() : "")
                : "—";
        grid.add(StitchDialog.metaCell("APPOINTMENT", apptStr));
        grid.add(StitchDialog.metaCell("TICKET STATUS", new StatusPill(ticket.getCurrentStatus())));

        body.add(grid);
        body.add(Box.createVerticalStrut(20));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(14));

        // Status History section
        body.add(StitchDialog.sectionHeader(Icons.CLOCK, "Status History", null));
        body.add(Box.createVerticalStrut(10));
        JComponent timeline = StatusTimeline.build(ticket.getStatusHistory());
        timeline.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(timeline);
        body.add(Box.createVerticalStrut(20));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(14));

        // Payment section
        body.add(StitchDialog.sectionHeader(Icons.CARD, "Payment", null));
        body.add(Box.createVerticalStrut(10));

        if (existingPayment != null) {
            JPanel payGrid = new JPanel(new GridLayout(1, 2, 24, 14));
            payGrid.setOpaque(false);
            payGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
            payGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            payGrid.add(StitchDialog.metaCell("AMOUNT", "R" + formatAmount(existingPayment.getPaymentAmount())));
            payGrid.add(StitchDialog.metaCell("METHOD",
                    existingPayment.getPaymentMethod() != null ? existingPayment.getPaymentMethod() : "—"));
            body.add(payGrid);
            body.add(Box.createVerticalStrut(10));

            JPanel statusGrid = new JPanel(new GridLayout(1, 2, 24, 14));
            statusGrid.setOpaque(false);
            statusGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
            statusGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            statusGrid.add(StitchDialog.metaCell("STATUS", new StatusPill(existingPayment.getPaymentStatus())));
            JPanel empty = new JPanel(); empty.setOpaque(false);
            statusGrid.add(empty);
            body.add(statusGrid);
            body.add(Box.createVerticalStrut(10));

            boolean isEft     = "EFT".equals(existingPayment.getPaymentMethod());
            boolean isPending = "PENDING".equals(existingPayment.getPaymentStatus());

            if (isPending && isEft) {
                body.add(StitchDialog.infoBanner(
                        "Waiting for the patient to complete payment on their dashboard."));
                body.add(Box.createVerticalStrut(8));
            } else if (isPending) {
                body.add(StitchDialog.infoBanner("Collected in person — confirm once received."));
                body.add(Box.createVerticalStrut(10));
                JButton markPaid = new JButton("Mark as Paid");
                AppTheme.styleActionButton(markPaid, true, Icons.CHECK);
                markPaid.setBackground(AppTheme.STATUS_SUCCESS);
                markPaid.addActionListener(e -> markAsPaid(dialog, parent, existingPayment, onChanged));
                JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                btnWrap.setOpaque(false);
                btnWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
                btnWrap.add(markPaid);
                body.add(btnWrap);
                body.add(Box.createVerticalStrut(8));
            } else if ("FAILED".equals(existingPayment.getPaymentStatus())) {
                body.add(StitchDialog.banner("This payment attempt failed.",
                        AppTheme.STATUS_DANGER_BG, AppTheme.STATUS_DANGER));
                body.add(Box.createVerticalStrut(8));
            } else if ("REFUNDED".equals(existingPayment.getPaymentStatus())) {
                body.add(StitchDialog.infoBanner("This payment has been refunded."));
                body.add(Box.createVerticalStrut(8));
            }

        } else if ("RESOLVED".equals(ticket.getCurrentStatus())) {
            body.add(StitchDialog.infoBanner("No payment request has been generated yet."));
            body.add(Box.createVerticalStrut(10));
            JButton generate = new JButton("Generate Payment Request");
            AppTheme.styleActionButton(generate, true, null);
            generate.addActionListener(e -> {
                dialog.dispose();
                GeneratePaymentDialog.show(parent, ticket, onChanged);
            });
            JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            btnWrap.setOpaque(false);
            btnWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnWrap.add(generate);
            body.add(btnWrap);
            body.add(Box.createVerticalStrut(8));
        } else {
            JLabel notReady = new JLabel("Payment becomes available once the ticket is resolved.");
            notReady.setFont(FontManager.bodyFont(Font.PLAIN, 13));
            notReady.setForeground(AppTheme.TEXT_MUTED);
            notReady.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(notReady);
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
        int confirm = JOptionPane.showConfirmDialog(dialog,
                "Confirm that payment of R" + formatAmount(payment.getPaymentAmount())
                        + " has been received in person?",
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

    private static String formatAmount(BigDecimal amount) {
        return amount != null ? amount.setScale(2, RoundingMode.HALF_UP).toString() : "0.00";
    }

    private static String patientName(PatientTicket ticket) {
        if (ticket.getPatient() == null || ticket.getPatient().getName() == null) return "—";
        return fullName(ticket.getPatient().getName().getFirstName(),
                ticket.getPatient().getName().getLastName());
    }

    private static String doctorName(PatientTicket ticket) {
        if (ticket.getAppointment() == null || ticket.getAppointment().getDoctor() == null
                || ticket.getAppointment().getDoctor().getName() == null) return "—";
        return "Dr. " + fullName(ticket.getAppointment().getDoctor().getName().getFirstName(),
                ticket.getAppointment().getDoctor().getName().getLastName());
    }

    private static String fullName(String first, String last) {
        StringBuilder sb = new StringBuilder();
        if (first != null) sb.append(first);
        if (last  != null) sb.append(" ").append(last);
        return sb.length() > 0 ? sb.toString().trim() : "—";
    }
}
