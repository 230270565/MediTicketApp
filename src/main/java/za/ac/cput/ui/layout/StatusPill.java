package za.ac.cput.ui.layout;

import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

/**
 * Rounded status pill: tinted background + coloured text with a readable label
 * ("IN_PROGRESS" shows as "In progress"). Colours come from
 * AppTheme.statusColor()/statusBackground(), so every page agrees.
 *
 * Use it as a label:          new StatusPill("PAID")
 * or on a table column:       StatusPill.install(table)   // every column named "Status"
 */
public class StatusPill extends JLabel {

    private Color pillBackground;

    public StatusPill(String status) {
        setFont(FontManager.bodyFont(Font.BOLD, 11));
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        setStatus(status);
    }

    /** Pill with custom text, coloured like {@code colorStatus} (e.g. "Ticket · Open" in OPEN colours). */
    public StatusPill(String text, String colorStatus) {
        this(colorStatus);
        setText(text);
    }

    public void setStatus(String status) {
        setText(AppTheme.label(status));
        setForeground(AppTheme.statusColor(status));
        pillBackground = AppTheme.statusBackground(status);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(pillBackground);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    /**
     * Puts a pill renderer on every column headed "Status", and a readable-label
     * renderer on "Method" columns (MEDICAL_AID -> Medical aid). Safe on any table.
     */
    public static void install(JTable table) {
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            String header = String.valueOf(column.getHeaderValue());
            if ("Status".equalsIgnoreCase(header)) {
                column.setCellRenderer(new Renderer());
            } else if ("Method".equalsIgnoreCase(header)) {
                column.setCellRenderer(new LabelRenderer());
            }
        }
    }

    private static Color rowBackground(JTable table, int row, boolean isSelected) {
        if (isSelected) return table.getSelectionBackground();
        Point mouse = table.getMousePosition();
        boolean hovered = mouse != null && table.rowAtPoint(mouse) == row;
        return hovered ? AppTheme.SURFACE_ALT : AppTheme.SURFACE;
    }

    /** Plain text cell showing AppTheme.label(value), with the shared hover tint. */
    private static final class LabelRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table,
                    value != null ? AppTheme.label(value.toString()) : "", isSelected, false, row, column);
            setBackground(rowBackground(table, row, isSelected));
            return this;
        }
    }

    /** Table cell renderer: the pill sits left-aligned and vertically centred in the cell. */
    private static final class Renderer extends DefaultTableCellRenderer {
        private final JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        private final StatusPill pill = new StatusPill(null);

        Renderer() {
            cell.add(pill);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            pill.setStatus(value != null ? value.toString() : null);
            cell.setBackground(rowBackground(table, row, isSelected));
            int pillHeight = pill.getPreferredSize().height;
            int top = Math.max(0, (table.getRowHeight(row) - pillHeight) / 2);
            ((FlowLayout) cell.getLayout()).setVgap(top);
            return cell;
        }
    }
}
