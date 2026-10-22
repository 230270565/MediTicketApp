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
        Point mouse;
        try {
            mouse = table.getMousePosition();
        } catch (HeadlessException e) {
            mouse = null; // no mouse (e.g. rendering a preview)
        }
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

    /**
     * Table cell renderer: paints the pill directly. (JTable paints renderers without
     * laying them out, so a panel-with-a-pill renderer would draw at zero size.)
     */
    private static final class Renderer extends DefaultTableCellRenderer {
        private Color rowBg = AppTheme.SURFACE;
        private Color pillBg = AppTheme.STATUS_NEUTRAL_BG;

        Renderer() {
            setFont(FontManager.bodyFont(Font.BOLD, 11));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            String status = value != null ? value.toString() : null;
            setText(AppTheme.label(status));
            setForeground(AppTheme.statusColor(status));
            pillBg = AppTheme.statusBackground(status);
            rowBg = rowBackground(table, row, isSelected);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(rowBg);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String text = getText();
            int pillW = fm.stringWidth(text) + 20;
            int pillH = fm.getHeight() + 6;
            int x = 8, y = (getHeight() - pillH) / 2;
            g2.setColor(pillBg);
            g2.fillRoundRect(x, y, pillW, pillH, pillH, pillH);
            g2.setColor(getForeground());
            g2.drawString(text, x + 10, y + 3 + fm.getAscent());
            g2.dispose();
        }
    }
}
