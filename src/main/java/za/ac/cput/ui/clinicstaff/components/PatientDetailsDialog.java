/*
 PatientDetailsDialog.java

 Patient details dialog for clinicstaff — Stitch design system

 Author: Aidan Barends (230255639)

 Date: 26th October 2026
*/

package za.ac.cput.ui.clinicstaff.components;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Patient;
import za.ac.cput.ui.layout.StatusPill;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;

public class PatientDetailsDialog {

    public static void show(Component parent, Patient patient, Runnable onChanged) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 540);

        // ── Header ────────────────────────────────────────────────────────────
        String first = patient.getName() != null ? patient.getName().getFirstName() : "";
        String last  = patient.getName() != null ? patient.getName().getLastName()  : "";
        String initials = StitchDialog.initials(first, last);
        String patId = patient.getUserId() > 0 ? "#PAT-" + String.format("%04d", patient.getUserId()) : "";

        body.add(StitchDialog.header(dialog, Icons.USER, "Patient Details " + patId, "MediTicket Health Services · Verified Patient Record"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(14));

        // ── Person hero ───────────────────────────────────────────────────────
        JPanel hero = new JPanel(new BorderLayout(16, 0));
        hero.setOpaque(false);
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel personCell = (JPanel) StitchDialog.personCell(initials, fullName(patient), "Registered Concierge Patient", AppTheme.PRIMARY);
        hero.add(personCell, BorderLayout.CENTER);

        JPanel statusBadge = new JPanel();
        statusBadge.setOpaque(false);
        statusBadge.setLayout(new BoxLayout(statusBadge, BoxLayout.Y_AXIS));
        StatusPill statusPill = new StatusPill(patient.getAccountStatus() != null ? patient.getAccountStatus() : "—");
        statusPill.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel idVerified = new JLabel("Identity Verified");
        idVerified.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        idVerified.setForeground(AppTheme.STATUS_SUCCESS);
        idVerified.setAlignmentX(Component.RIGHT_ALIGNMENT);
        statusBadge.add(statusPill);
        statusBadge.add(Box.createVerticalStrut(4));
        statusBadge.add(idVerified);
        hero.add(statusBadge, BorderLayout.EAST);

        body.add(hero);
        body.add(Box.createVerticalStrut(16));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(16));

        // ── 4×2 meta grid ────────────────────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(4, 2, 24, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        grid.add(StitchDialog.metaCell("EMAIL ADDRESS", patient.getEmail() != null ? patient.getEmail() : "—"));
        grid.add(StitchDialog.metaCell("PHONE NUMBER", patient.getCellPhone() != null ? patient.getCellPhone() : "— (Not Provided)"));

        String dob = patient.getDob() != null
                ? patient.getDob().toString() + " (" + ageYears(patient.getDob()) + " yrs)"
                : "—";
        grid.add(StitchDialog.metaCell("DATE OF BIRTH", dob));

        String reg = patient.getDateRegistered() != null ? patient.getDateRegistered().toString() : "—";
        JPanel regPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        regPanel.setOpaque(false);
        JLabel regLabel = new JLabel(reg);
        regLabel.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        regLabel.setForeground(AppTheme.TEXT_PRIMARY);
        JLabel verifiedTag = new JLabel("VERIFIED");
        verifiedTag.setFont(FontManager.bodyFont(Font.BOLD, 10));
        verifiedTag.setForeground(AppTheme.STATUS_SUCCESS);
        regPanel.add(regLabel);
        regPanel.add(verifiedTag);
        grid.add(StitchDialog.metaCell("DATE REGISTERED", regPanel));

        String emergency = patient.getEmergencyContact() != null && !patient.getEmergencyContact().isBlank()
                ? patient.getEmergencyContact() : "— (Pending Update)";
        grid.add(StitchDialog.metaCell("EMERGENCY CONTACT", emergency));

        JPanel accStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        accStatus.setOpaque(false);
        String statusStr = patient.getAccountStatus() != null ? patient.getAccountStatus() : "—";
        JLabel statusValue = new JLabel(statusStr);
        statusValue.setFont(FontManager.bodyFont(Font.BOLD, 13));
        statusValue.setForeground(AppTheme.statusColor(statusStr));
        JLabel fullAccess = new JLabel("· Full Patient Access");
        fullAccess.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        fullAccess.setForeground(AppTheme.TEXT_MUTED);
        accStatus.add(statusValue);
        accStatus.add(fullAccess);
        grid.add(StitchDialog.metaCell("ACCOUNT STATUS", accStatus));

        // Last cell: empty
        JPanel empty = new JPanel();
        empty.setOpaque(false);
        grid.add(empty);

        body.add(grid);
        body.add(Box.createVerticalStrut(16));

        // ── Info banner ───────────────────────────────────────────────────────
        body.add(StitchDialog.banner(
                "Verified Clinical Health Record. POPIA & HIPAA compliant electronic record encrypted and synchronized with MediTicket Central Clinical Registry.",
                AppTheme.PRIMARY_LIGHT, AppTheme.PRIMARY));
        body.add(Box.createVerticalStrut(16));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(14));

        // ── Action buttons ────────────────────────────────────────────────────
        boolean isActive = "ACTIVE".equals(patient.getAccountStatus());

        JButton toggleButton = new JButton(isActive ? "Deactivate Patient" : "Activate Patient");
        toggleButton.setFont(FontManager.bodyFont(Font.BOLD, 13));
        toggleButton.setForeground(isActive ? AppTheme.STATUS_DANGER : AppTheme.STATUS_SUCCESS);
        toggleButton.setBackground(AppTheme.SURFACE);
        toggleButton.setFocusPainted(false);
        toggleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        Color toggleBorder = isActive ? AppTheme.STATUS_DANGER : AppTheme.STATUS_SUCCESS;
        toggleButton.putClientProperty("FlatLaf.style",
                "arc: 10; borderWidth: 1; focusWidth: 0; innerFocusWidth: 0; margin: 8,14,8,14; borderColor: " + colorHex(toggleBorder));
        toggleButton.addActionListener(e -> { dialog.dispose(); toggleStatus(parent, patient, onChanged); });

        JButton deleteButton = new JButton("Delete Patient");
        deleteButton.setFont(FontManager.bodyFont(Font.BOLD, 13));
        deleteButton.setForeground(AppTheme.STATUS_DANGER);
        deleteButton.setBackground(AppTheme.SURFACE);
        deleteButton.setBorderPainted(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        deleteButton.addActionListener(e -> { dialog.dispose(); deletePatient(parent, patient, onChanged); });

        body.add(toggleButton);
        body.add(Box.createVerticalStrut(8));
        body.add(deleteButton);
        body.add(Box.createVerticalStrut(16));

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel footer = StitchDialog.buttonRow();
        JButton close = new JButton("Close");
        AppTheme.styleActionButton(close, true, null);
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);
        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 540);
    }

    // ── Business logic (unchanged) ────────────────────────────────────────────

    private static void toggleStatus(Component parent, Patient patient, Runnable onChanged) {
        String newStatus = "ACTIVE".equals(patient.getAccountStatus()) ? "INACTIVE" : "ACTIVE";
        patient.setAccountStatus(newStatus);
        BaseApiClient.ApiResult<Patient> result = ApiClientProvider.getInstance().patients().update(patient);
        if (result.isSuccess()) {
            AppDialog.show(parent, "Patient Updated",
                    "This patient is now " + newStatus.toLowerCase() + ".", AppDialog.Type.SUCCESS);
        } else {
            AppDialog.show(parent, "Update Failed",
                    result.getMessage() != null ? result.getMessage() : "Could not update this patient.", AppDialog.Type.ERROR);
        }
        if (onChanged != null) onChanged.run();
    }

    private static void deletePatient(Component parent, Patient patient, Runnable onChanged) {
        int confirm = JOptionPane.showConfirmDialog(parent,
                "Permanently delete " + fullName(patient) + "?\n" +
                        "This also removes their appointments, tickets, notifications, and payment history.\n" +
                        "This cannot be undone.",
                "Delete Patient", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        BaseApiClient.ApiResult<Void> result = ApiClientProvider.getInstance().patients().delete(patient.getUserId());
        if (result.isSuccess()) {
            AppDialog.show(parent, "Patient Deleted", "The patient record has been removed.", AppDialog.Type.SUCCESS);
        } else {
            AppDialog.show(parent, "Delete Failed",
                    result.getMessage() != null ? result.getMessage() : "This patient could not be deleted.", AppDialog.Type.ERROR);
        }
        if (onChanged != null) onChanged.run();
    }

    private static String fullName(Patient patient) {
        if (patient.getName() == null) return "—";
        String first = patient.getName().getFirstName();
        String last  = patient.getName().getLastName();
        String name  = (first != null ? first : "") + " " + (last != null ? last : "");
        return name.isBlank() ? "—" : name.trim();
    }

    private static int ageYears(java.time.LocalDate dob) {
        return (int) java.time.temporal.ChronoUnit.YEARS.between(dob, java.time.LocalDate.now());
    }

    private static String colorHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
