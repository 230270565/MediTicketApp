/*
 ElevatedCard.java

 Card panel with a soft shadow

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

import za.ac.cput.ui.theme.AppTheme;

import javax.swing.*;
import java.awt.*;

public class ElevatedCard extends JPanel {

    private static final int SHADOW_SIZE = 5;

    private final int radius;
    private boolean drawBorder = true;

    public ElevatedCard() {
        this(AppTheme.RADIUS_MD);
    }

    public ElevatedCard(int radius) {
        this.radius = radius;
        setOpaque(false);
        setBackground(AppTheme.SURFACE);
        // Leave space for the shadow.
        setBorder(BorderFactory.createEmptyBorder(0, 0, SHADOW_SIZE + 1, SHADOW_SIZE + 1));
    }

    public void setDrawBorder(boolean drawBorder) {
        this.drawBorder = drawBorder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth() - SHADOW_SIZE - 1;
        int h = getHeight() - SHADOW_SIZE - 1;
        if (w <= 0 || h <= 0) {
            super.paintComponent(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Shadow made from a few see-through rounded rectangles.
        for (int i = SHADOW_SIZE; i >= 1; i--) {
            int alpha = 3 + (SHADOW_SIZE - i) * 2;
            g2.setColor(new Color(20, 30, 32, alpha));
            g2.fillRoundRect(i / 2, i, w, h, radius, radius);
        }

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, w, h, radius, radius);

        if (drawBorder) {
            g2.setColor(AppTheme.BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
