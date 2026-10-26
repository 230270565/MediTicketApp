/*
 AccessRequestDetailsDialog.java

 Access request details dialog — Stitch design system

 Author: Matthew Barron (230398863)

 Date: 26th October 2026
*/

package za.ac.cput.ui.clinicstaff.components;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.auth.EmployeeAccessRequest;
import za.ac.cput.ui.layout.StatusPill;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class AccessRequestDetailsDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd (EEEE)");

    public static void show(Component parent, EmployeeAccessRequest request, Runnable onChanged) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 520);

        // ── Header ────────────────────────────────────────────────────────────
        String reqId = request.getRequestId() > 0
                ? "#REQ-" + request.getRequestDate().toLocalDate().toString() : "";
        body.add(StitchDialog.header(dialog, Icons.USER, "Access Request " + reqId,
                "MediTicket Health Services · Credential Verification Protocol"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(14));

        // ── Applicant hero ────────────────────────────────────────────────────
        String email = request.getEmail() != null ? request.getEmail() : "—";
        String initials = email.length() > 0 ? String.valueOf(email.charAt(0)).toUpperCase() : "?";
        // Use first two chars of the email local-part as initials
        int at = email.indexOf('@');
        String localPart = at > 0 ? email.substring(0, at) : email;
        if (localPart.length() >= 2) initials = localPart.substring(0, 2).toUpperCase();

        JPanel hero = new JPanel(new BorderLayout(16, 0));
        hero.setOpaque(false);
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        String roleLabel = requestedRoleLabel(request);
        String branchLabel = "Credential Application · Quiet Branch Practice";
        JComponent personCell = StitchDialog.personCell(initials, applicantName(request), "· " + branchLabel, AppTheme.PRIMARY);
        hero.add(personCell, BorderLayout.CENTER);

        boolean isPending = "PENDING".equals(request.getStatus());
        String statusStr = request.getStatus() != null ? request.getStatus() : "—";
        StatusPill statusPill = isPending
                ? new StatusPill("PENDING", "PENDING")
                : new StatusPill(statusStr, statusStr);

        JPanel statusGroup = new JPanel();
        statusGroup.setOpaque(false);
        statusGroup.setLayout(new BoxLayout(statusGroup, BoxLayout.Y_AXIS));
        statusGroup.add(statusPill);
        JLabel awaitingLabel = new JLabel(isPending ? "Awaiting Clearance" : "");
        awaitingLabel.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        awaitingLabel.setForeground(AppTheme.TEXT_MUTED);
        awaitingLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        statusGroup.add(Box.createVerticalStrut(4));
        statusGroup.add(awaitingLabel);
        hero.add(statusGroup, BorderLayout.EAST);

        body.add(hero);
        body.add(Box.createVerticalStrut(16));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(16));

        // ── 3×2 meta grid ────────────────────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(3, 2, 24, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        grid.add(StitchDialog.metaCell("APPLICANT EMAIL", email));

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        rolePanel.setOpaque(false);
        JLabel roleLbl = new JLabel(roleLabel);
        roleLbl.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        roleLbl.setForeground(AppTheme.TEXT_PRIMARY);
        rolePanel.add(roleLbl);
        if (request.getRequestedStaffRole() != null) {
            rolePanel.add(StitchDialog.chip(request.getRequestedStaffRole()));
        }
        grid.add(StitchDialog.metaCell("REQUESTED ROLE", rolePanel));

        String reqDate = request.getRequestDate() != null
                ? request.getRequestDate().toLocalDate().format(DATE_FMT) : "—";
        grid.add(StitchDialog.metaCell("DATE REQUESTED", reqDate));

        JPanel authScope = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        authScope.setOpaque(false);
        JLabel scopeStatus = new JLabel(isPending ? "⚠ Pending Review" : (request.getStatus() != null ? request.getStatus() : "—"));
        scopeStatus.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        scopeStatus.setForeground(isPending ? new java.awt.Color(0xB45309) : AppTheme.statusColor(request.getStatus()));
        authScope.add(scopeStatus);
        grid.add(StitchDialog.metaCell("AUTHORIZATION SCOPE", authScope));

        grid.add(StitchDialog.metaCell("REQUEST ID", "#REQ-" + request.getRequestId()));
        grid.add(new JPanel() {{ setOpaque(false); }});

        body.add(grid);
        body.add(Box.createVerticalStrut(16));

        // ── System action banner ──────────────────────────────────────────────
        body.add(StitchDialog.banner(
                "System Action: Approving sends an invitation email to this address with cryptographic onboarding credentials and clinic access permissions.",
                AppTheme.PRIMARY_LIGHT, AppTheme.PRIMARY));
        body.add(Box.createVerticalStrut(16));

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel footer = StitchDialog.buttonRow();

        JButton close = new JButton("Close");
        AppTheme.styleActionButton(close, false, null);
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);

        if (isPending) {
            JButton rejectButton = new JButton("✕ Reject Request");
            rejectButton.setFont(FontManager.bodyFont(Font.BOLD, 13));
            rejectButton.setForeground(AppTheme.TEXT_ON_PRIMARY);
            rejectButton.setBackground(AppTheme.STATUS_DANGER);
            rejectButton.setBorderPainted(false);
            rejectButton.setFocusPainted(false);
            rejectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            rejectButton.putClientProperty("FlatLaf.style", "arc: 10; focusWidth: 0; innerFocusWidth: 0; margin: 9,16,9,16;");
            rejectButton.addActionListener(e -> reject(dialog, parent, request, onChanged));
            footer.add(rejectButton);

            JButton approveButton = new JButton("✓ Approve Request");
            approveButton.setFont(FontManager.bodyFont(Font.BOLD, 13));
            approveButton.setForeground(AppTheme.TEXT_ON_PRIMARY);
            approveButton.setBackground(AppTheme.STATUS_SUCCESS);
            approveButton.setBorderPainted(false);
            approveButton.setFocusPainted(false);
            approveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            approveButton.putClientProperty("FlatLaf.style", "arc: 10; focusWidth: 0; innerFocusWidth: 0; margin: 9,16,9,16;");
            approveButton.addActionListener(e -> approve(dialog, parent, request, onChanged));
            footer.add(approveButton);
        }

        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 520);
    }

    // ── Business logic (unchanged) ────────────────────────────────────────────

    private static void approve(JDialog dialog, Component parent, EmployeeAccessRequest request, Runnable onChanged) {
        int confirm = JOptionPane.showConfirmDialog(dialog,
                "Approve access for " + request.getEmail() + "? An invitation email will be sent.",
                "Approve Request", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        BaseApiClient.ApiResult<String> result =
                ApiClientProvider.getInstance().auth().approveAccessRequest(request.getRequestId());

        if (result.isSuccess()) {
            dialog.dispose();
            AppDialog.show(parent, "Request Approved",
                    "An invitation email has been sent to the employee.", AppDialog.Type.SUCCESS);
            if (onChanged != null) onChanged.run();
        } else {
            AppDialog.show(parent, "Unable to Approve",
                    result.getMessage() != null ? result.getMessage() : "Something went wrong.", AppDialog.Type.ERROR);
        }
    }

    private static void reject(JDialog dialog, Component parent, EmployeeAccessRequest request, Runnable onChanged) {
        String notes = JOptionPane.showInputDialog(dialog, "Reason for rejection (optional):",
                "Reject Request", JOptionPane.PLAIN_MESSAGE);

        BaseApiClient.ApiResult<String> result =
                ApiClientProvider.getInstance().auth().rejectAccessRequest(request.getRequestId(), notes);

        if (result.isSuccess()) {
            dialog.dispose();
            AppDialog.show(parent, "Request Rejected", "The request has been rejected.", AppDialog.Type.INFO);
            if (onChanged != null) onChanged.run();
        } else {
            AppDialog.show(parent, "Unable to Reject",
                    result.getMessage() != null ? result.getMessage() : "Something went wrong.", AppDialog.Type.ERROR);
        }
    }

    private static String requestedRoleLabel(EmployeeAccessRequest request) {
        String userType = request.getRequestedUserType();
        String staffRole = request.getRequestedStaffRole();
        if ("DOCTOR".equals(userType)) return "Doctor";
        if (staffRole != null) return "Clinic Staff — " + staffRole;
        return userType != null ? userType : "—";
    }

    private static String applicantName(EmployeeAccessRequest request) {
        String email = request.getEmail();
        if (email == null) return "Unknown Applicant";
        int at = email.indexOf('@');
        if (at <= 0) return email;
        String local = email.substring(0, at);
        // Convert "clyde.donovan96787" → "Clyde Donovan"
        String[] parts = local.split("[._\\d-]+");
        StringBuilder name = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank()) {
                if (name.length() > 0) name.append(" ");
                name.append(part.substring(0, 1).toUpperCase());
                if (part.length() > 1) name.append(part.substring(1).toLowerCase());
            }
        }
        return name.length() > 0 ? name.toString() : email;
    }
}
