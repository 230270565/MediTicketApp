/*
 PatientDetailsDialog.java

 Doctor dialog for patient details

 Author: Aidan Barends (230255639)

 Date: 11th August 2026
*/

package za.ac.cput.ui.doctor.components;

import za.ac.cput.model.domain.Patient;
import za.ac.cput.ui.layout.StatusPill;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import java.awt.*;

public class PatientDetailsDialog {

    public static void show(Component parent, Patient patient) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 480);

        body.add(StitchDialog.header(dialog, Icons.USER, "Patient Details", "Health Record"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(18));

        // Extract name parts for avatar initials
        String first = "";
        String last  = "";
        if (patient.getName() != null) {
            String f = patient.getName().getFirstName();
            String l = patient.getName().getLastName();
            first = f != null ? f : "";
            last  = l != null ? l : "";
        }
        String fullName = (first + " " + last).trim();
        if (fullName.isEmpty()) fullName = "—";

        // Patient identity row
        body.add(StitchDialog.personCell(
                StitchDialog.initials(first, last),
                fullName, "Patient", AppTheme.TEXT_MUTED));
        body.add(Box.createVerticalStrut(16));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(16));

        // 2-column meta grid — 3 rows x 2 cols
        JPanel grid = new JPanel(new GridLayout(3, 2, 24, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Row 1: Email | Phone
        String email = patient.getEmail();
        String phone = patient.getCellPhone();
        grid.add(StitchDialog.metaCell("EMAIL", email != null && !email.isBlank() ? email : "—"));
        grid.add(StitchDialog.metaCell("PHONE", phone != null && !phone.isBlank() ? phone : "—"));

        // Row 2: Date of Birth | Emergency Contact
        String dob = patient.getDob() != null ? patient.getDob().toString() : "—";
        String emergency = patient.getEmergencyContact();
        grid.add(StitchDialog.metaCell("DATE OF BIRTH",
                StitchDialog.iconRow(Icons.CALENDAR, dob, null)));
        grid.add(StitchDialog.metaCell("EMERGENCY CONTACT",
                emergency != null && !emergency.isBlank() ? emergency : "—"));

        // Row 3: Account Status
        grid.add(StitchDialog.metaCell("ACCOUNT STATUS", new StatusPill(patient.getAccountStatus())));
        grid.add(new JPanel() {{ setOpaque(false); }});

        body.add(grid);
        body.add(Box.createVerticalStrut(20));

        // Footer
        JPanel footer = StitchDialog.buttonRow();
        JButton close = new JButton("Close");
        AppTheme.styleActionButton(close, false, null);
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);
        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 480);
    }
}
