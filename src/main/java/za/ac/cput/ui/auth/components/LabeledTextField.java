/*
 LabeledTextField.java

 Text field with a label above it

 Authors:
  Joshua Reid Adams (230317693)
  Abdullahi Raage Farah (230971091)
  Aidan Barends (230255639)
  Jaden Clayton Abrahams (222206721)
  Joshua Peter Bonzet (221312536)
  Matthew Barron (230398863)
  Raul Jaaim Everts (230270565)

 Date: 11th August 2026
*/

package za.ac.cput.ui.auth.components;

import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;

public class LabeledTextField extends JPanel {

    private final JTextField field = new JTextField();

    public LabeledTextField(String labelText) {
        setLayout(new BorderLayout(0, 6));
        setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(FontManager.bodyFont(Font.BOLD, 13));
        label.setForeground(AppTheme.TEXT_PRIMARY);

        field.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(0, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        add(label, BorderLayout.NORTH);
        add(field, BorderLayout.CENTER);
    }

    public String getText() { return field.getText(); }
    public JTextField getField() { return field; }
    public void clear() {
        field.setText("");
    }
}
