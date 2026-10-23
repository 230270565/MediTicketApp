/*
 Main.java

 Starts the MediTicket desktop app

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

package za.ac.cput;

import za.ac.cput.ui.AppFrame;
import za.ac.cput.ui.theme.AppTheme;

import javax.swing.*;

// Starts the app. The theme has to load before any Swing component is made, then AppFrame opens.
public class Main {

    public static void main(String[] args) {
        AppTheme.initialize();

        SwingUtilities.invokeLater(() -> {
            AppFrame frame = new AppFrame();
            frame.setVisible(true);
        });
    }
}
