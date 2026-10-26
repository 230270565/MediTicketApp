/*
 CompleteConsultationDialog.java

 Doctor dialog for complete consultation

 Author: Jaden Clayton Abrahams (222206721)

 Date: 18th August 2026
*/

package za.ac.cput.ui.doctor.components;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.PatientTicket;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.StitchDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CompleteConsultationDialog {

    public static void show(Component parent, PatientTicket ticket, Runnable onCompleted) {
        JDialog dialog = StitchDialog.create(parent);
        StitchDialog.CardPanel card = StitchDialog.card();
        JPanel body = StitchDialog.body(dialog, card, 480);

        body.add(StitchDialog.header(dialog, Icons.CLIPBOARD, "Complete Consultation", "Resolve Ticket"));
        body.add(StitchDialog.divider());
        body.add(Box.createVerticalStrut(16));

        // Info banner describing what this action does
        body.add(StitchDialog.infoBanner(
                "<html>Record a diagnosis or summary. This marks the ticket resolved<br>"
                + "and the patient will be billed by clinic staff.</html>"));
        body.add(Box.createVerticalStrut(16));

        // Field label
        body.add(StitchDialog.fieldLabel("DIAGNOSIS / SUMMARY"));

        // Text area
        JTextArea notesArea = new JTextArea(5, 20);
        notesArea.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        notesScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        notesScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        body.add(notesScroll);

        // Error label
        JLabel errorLabel = StitchDialog.errorLabel();
        body.add(errorLabel);
        body.add(Box.createVerticalStrut(8));

        // Footer
        JPanel footer = StitchDialog.buttonRow();

        JButton cancel = new JButton("Cancel");
        AppTheme.styleActionButton(cancel, false, null);
        cancel.addActionListener(e -> dialog.dispose());
        footer.add(cancel);

        JButton submit = new JButton("Complete Consultation");
        AppTheme.styleActionButton(submit, true, null);
        submit.addActionListener(e -> {
            String notes = notesArea.getText().trim();
            if (notes.isEmpty()) {
                errorLabel.setText("Please enter a diagnosis or summary before completing.");
                return;
            }

            BaseApiClient.ApiResult<PatientTicket> result = ApiClientProvider.getInstance()
                    .patientTickets().progressStatus(ticket.getTicketId(), "RESOLVED", notes);

            if (result.isSuccess()) {
                dialog.dispose();
                AppDialog.show(parent, "Consultation Completed",
                        "The ticket has been resolved and the appointment marked complete.",
                        AppDialog.Type.SUCCESS);
                if (onCompleted != null) onCompleted.run();
            } else {
                errorLabel.setText(result.getMessage() != null
                        ? result.getMessage() : "Unable to complete consultation.");
            }
        });
        footer.add(submit);

        body.add(footer);
        body.add(Box.createVerticalStrut(24));

        StitchDialog.packAndShow(dialog, parent, 480);
    }
}
