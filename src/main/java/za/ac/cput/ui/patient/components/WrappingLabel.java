/*
 WrappingLabel.java

 Label that wraps long text onto new lines

 Authors:
  Joshua Reid Adams (230317693)
  Abdullahi Raage Farah (230971091)
  Aidan Barends (230255639)
  Jaden Clayton Abrahams (222206721)
  Joshua Peter Bonzet (221312536)
  Matthew Barron (230398863)
  Raul Jaaim Everts (230270565)

 Date: 25th August 2026
*/

package za.ac.cput.ui.patient.components;

import javax.swing.*;
import java.awt.*;

public class WrappingLabel extends JTextArea {

    public WrappingLabel(String text, Font font, Color color) {
        super(text);
        setFont(font);
        setForeground(color);
        setLineWrap(true);
        setWrapStyleWord(true);
        setEditable(false);
        setFocusable(false);
        setOpaque(false);
        setBorder(null);
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
