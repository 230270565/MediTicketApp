/*
 StaffDetailsDialog.java

 Doctor / clinicstaff details dialog — Stitch design system

 Author: Matthew Barron (230398863)

 Date: 26th October 2026
*/

package za.ac.cput.ui.clinicstaff.components;

import za.ac.cput.model.domain.ClinicStaff;
import za.ac.cput.model.domain.Doctor;
import za.ac.cput.ui.layout.StatusPill;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;

public class StaffDetailsDialog {

    public static void showDoctor(Component parent, Doctor doctor) {
        String first = doctor.getName() != null ? doctor.getName().getFirstName() : "";
        String last  = doctor.getName() != null ? doctor.getName().getLastName()  : "";
        String specialty = doctor.getSpecialty() != null ? doctor.getSpecialty() : "Medical Staff";
        String docId = doctor.getUserId() > 0 ? "#DOC-" + String.format("%04d", doctor.getUserId()) : "";
        showImpl(parent, Icons.USER, "Doctor Details " + docId, "MediTicket Health Services · Credentialled Practitioner",
                first, last, specialty, buildDoctorGrid(doctor));
    }

    public static void showClinicStaff(Component parent, ClinicStaff staff) {
        String first = staff.getName() != null ? staff.getName().getFirstName() : "";
        String last  = staff.getName() != null ? staff.getName().getLastName()  : "";
        String roleLabel = "ADMIN".equals(staff.getStaffRole()) ? "Administrator" : "Nurse";
        String dept = staff.getDepartment() != null ? staff.getDepartment() : "Clinic Staff";
        String staffId = staff.getUserId() > 0 ? "#STF-" + String.format("%04d", staff.getUserId()) : "";
        showImpl(parent, Icons.USERS, roleLabel + " Details " + staffId, "MediTicket Health Services · Clinic Staff",
                first, last, dept, buildClinicStaffGrid(staff));
    }

    private static void showImpl(Component parent, String iconName, String title, String subtitle,
                                 String first, String last, String sub, JPanel grid) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 520);

        // ── Header ────────────────────────────────────────────────────────────
        body.add(StitchDialog.header(dialog, iconName, title, subtitle));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(14));

        // ── Person hero ───────────────────────────────────────────────────────
        JPanel hero = new JPanel(new BorderLayout(16, 0));
        hero.setOpaque(false);
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        String initials = StitchDialog.initials(first, last);
        JComponent personCell = StitchDialog.personCell(initials,
                ((first + " " + last).trim().isEmpty() ? "—" : (first + " " + last).trim()),
                "· " + sub, AppTheme.PRIMARY);
        hero.add(personCell, BorderLayout.CENTER);

        JPanel statusGroup = new JPanel();
        statusGroup.setOpaque(false);
        statusGroup.setLayout(new BoxLayout(statusGroup, BoxLayout.Y_AXIS));
        // placeholder — will be replaced per dialog type
        statusGroup.add(Box.createVerticalGlue());
        hero.add(statusGroup, BorderLayout.EAST);

        body.add(hero);
        body.add(Box.createVerticalStrut(16));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(16));

        // ── 4×2 grid ─────────────────────────────────────────────────────────
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(grid);
        body.add(Box.createVerticalStrut(16));

        // ── Info banner ───────────────────────────────────────────────────────
        body.add(StitchDialog.banner(
                "Verified Medical Practitioner. HPCSA Registered & SABS ISO/IEC 27001 compliant electronic profile generated through MediTicket clinical registry.",
                AppTheme.PRIMARY_LIGHT, AppTheme.PRIMARY));
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

    private static JPanel buildDoctorGrid(Doctor doctor) {
        JPanel grid = new JPanel(new GridLayout(4, 2, 24, 14));
        grid.setOpaque(false);

        grid.add(StitchDialog.metaCell("EMAIL ADDRESS", doctor.getEmail() != null ? doctor.getEmail() : "—"));
        grid.add(StitchDialog.metaCell("PHONE NUMBER", doctor.getCellPhone() != null ? doctor.getCellPhone() : "—"));

        String dob = doctor.getDob() != null
                ? doctor.getDob().toString() + " (" + ageYears(doctor.getDob()) + " yrs)"
                : "—";
        grid.add(StitchDialog.metaCell("DATE OF BIRTH", dob));

        // Specialty with short code chip
        JPanel specPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        specPanel.setOpaque(false);
        String spec = doctor.getSpecialty() != null ? doctor.getSpecialty() : "—";
        JLabel specLabel = new JLabel(spec);
        specLabel.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        specLabel.setForeground(AppTheme.TEXT_PRIMARY);
        specPanel.add(specLabel);
        if (doctor.getSpecialty() != null && doctor.getSpecialty().length() >= 4) {
            specPanel.add(StitchDialog.chip(doctor.getSpecialty().substring(0, Math.min(4, doctor.getSpecialty().length())).toLowerCase()));
        }
        grid.add(StitchDialog.metaCell("CLINICAL SPECIALTY", specPanel));

        // License with "Valid" tag
        JPanel licPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        licPanel.setOpaque(false);
        String lic = doctor.getLicenseNumber() != null ? doctor.getLicenseNumber() : "—";
        JLabel licLabel = new JLabel(lic);
        licLabel.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        licLabel.setForeground(AppTheme.TEXT_PRIMARY);
        JLabel validTag = new JLabel("Valid");
        validTag.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        validTag.setForeground(AppTheme.STATUS_SUCCESS);
        licPanel.add(licLabel);
        licPanel.add(validTag);
        grid.add(StitchDialog.metaCell("LICENSE NUMBER", licPanel));

        // Practitioner standing
        String statusStr = doctor.getAccountStatus() != null ? doctor.getAccountStatus().toString() : "—";
        JPanel standing = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        standing.setOpaque(false);
        JLabel standingLabel = new JLabel(statusStr);
        standingLabel.setFont(FontManager.bodyFont(Font.BOLD, 13));
        standingLabel.setForeground(AppTheme.statusColor(statusStr));
        JLabel privLabel = new JLabel("· full privileges");
        privLabel.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        privLabel.setForeground(AppTheme.TEXT_MUTED);
        standing.add(standingLabel);
        standing.add(privLabel);
        grid.add(StitchDialog.metaCell("PRACTITIONER STANDING", standing));

        JPanel empty = new JPanel();
        empty.setOpaque(false);
        grid.add(empty);

        return grid;
    }

    private static JPanel buildClinicStaffGrid(ClinicStaff staff) {
        JPanel grid = new JPanel(new GridLayout(4, 2, 24, 14));
        grid.setOpaque(false);

        grid.add(StitchDialog.metaCell("EMAIL ADDRESS", staff.getEmail() != null ? staff.getEmail() : "—"));
        grid.add(StitchDialog.metaCell("PHONE NUMBER", staff.getCellPhone() != null ? staff.getCellPhone() : "—"));

        String dob = staff.getDob() != null
                ? staff.getDob().toString() + " (" + ageYears(staff.getDob()) + " yrs)"
                : "—";
        grid.add(StitchDialog.metaCell("DATE OF BIRTH", dob));
        grid.add(StitchDialog.metaCell("ROLE", staff.getStaffRole() != null ? staff.getStaffRole() : "—"));
        grid.add(StitchDialog.metaCell("DEPARTMENT", staff.getDepartment() != null ? staff.getDepartment() : "—"));

        String statusStr = staff.getAccountStatus() != null ? staff.getAccountStatus().toString() : "—";
        JPanel standing = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        standing.setOpaque(false);
        JLabel statusLabel = new JLabel(statusStr);
        statusLabel.setFont(FontManager.bodyFont(Font.BOLD, 13));
        statusLabel.setForeground(AppTheme.statusColor(statusStr));
        standing.add(statusLabel);
        grid.add(StitchDialog.metaCell("ACCOUNT STATUS", standing));

        JPanel e1 = new JPanel(); e1.setOpaque(false); grid.add(e1);
        JPanel e2 = new JPanel(); e2.setOpaque(false); grid.add(e2);

        return grid;
    }

    private static int ageYears(java.time.LocalDate dob) {
        return (int) java.time.temporal.ChronoUnit.YEARS.between(dob, java.time.LocalDate.now());
    }
}
