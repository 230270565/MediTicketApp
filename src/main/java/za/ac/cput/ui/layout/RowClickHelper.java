/*
 RowClickHelper.java

 Opens a details dialog when a table row is clicked

 Authors:
  Joshua Reid Adams (230317693)
  Abdullahi Raage Farah (230971091)
  Aidan Barends (230255639)
  Jaden Clayton Abrahams (222206721)
  Joshua Peter Bonzet (221312536)
  Matthew Barron (230398863)
  Raul Jaaim Everts (230270565)

 Date: 18th August 2026
*/

package za.ac.cput.ui.layout;

import za.ac.cput.ui.theme.AppTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer;

// Clicking a table row opens its details dialog, so we don't need an Action column.
// The id column stays in the model so we know which row was clicked.
public class RowClickHelper {

    public static void makeRowsClickable(JTable table, int idColumn, IntConsumer onRowClicked) {
        table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0) return;
                int modelRow = table.convertRowIndexToModel(row);
                Object idValue = table.getModel().getValueAt(modelRow, idColumn);
                if (idValue instanceof Integer id) {
                    onRowClicked.accept(id);
                }
            }
        });

        // Hover colour, same as the sidebar.
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            private int lastHoverRow = -1;

            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != lastHoverRow) {
                    lastHoverRow = row;
                    table.repaint();
                }
            }
        });

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                Point mousePos = table.getMousePosition();
                boolean hovered = mousePos != null && table.rowAtPoint(mousePos) == row;
                c.setBackground(hovered ? AppTheme.SURFACE_ALT : AppTheme.SURFACE);
                return c;
            }
        });
    }
}
