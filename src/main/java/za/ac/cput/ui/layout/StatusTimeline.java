package za.ac.cput.ui.layout;

import za.ac.cput.model.domain.TicketStatus;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vertical ticket history used by the patient, doctor and staff ticket dialogs:
 * a dot per status joined by a line, a status pill, the date on the right and
 * any notes (diagnosis, closing reason) wrapped underneath.
 *
 * Replaces three copies of a history list whose long notes forced a horizontal
 * scrollbar and pushed the dates past the dialog's right edge.
 */
public final class StatusTimeline {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");
    private static final int RAIL = 22; // width of the dot-and-line column

    private StatusTimeline() {}

    public static JComponent build(List<TicketStatus> history) {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (history == null || history.isEmpty()) {
            JLabel none = new JLabel("No history recorded.");
            none.setFont(FontManager.bodyFont(Font.PLAIN, 12));
            none.setForeground(AppTheme.TEXT_MUTED);
            list.add(none);
            return list;
        }

        for (int i = 0; i < history.size(); i++) {
            list.add(entry(history.get(i), i == 0, i == history.size() - 1));
        }
        return list;
    }

    private static JComponent entry(TicketStatus status, boolean first, boolean last) {
        String type = status.getStatusType();
        Color dot = AppTheme.statusColor(type);

        JPanel row = new JPanel(new BorderLayout(AppTheme.SPACE_SM, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Rail: line above/below + coloured dot, drawn to the row's full height.
        JComponent rail = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = RAIL / 2, cy = 14;
                g2.setColor(AppTheme.BORDER);
                g2.setStroke(new BasicStroke(2f));
                if (!first) g2.drawLine(cx, 0, cx, cy);
                if (!last) g2.drawLine(cx, cy, cx, getHeight());
                g2.setColor(dot);
                g2.fillOval(cx - 5, cy - 5, 10, 10);
                g2.dispose();
            }
        };
        rail.setPreferredSize(new Dimension(RAIL, 10));
        row.add(rail, BorderLayout.WEST);

        JPanel body = new JPanel(new BorderLayout(0, 4));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(4, 0, last ? 0 : AppTheme.SPACE_MD, 0));

        JPanel head = new JPanel(new BorderLayout(AppTheme.SPACE_SM, 0));
        head.setOpaque(false);
        JPanel pillWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pillWrap.setOpaque(false);
        pillWrap.add(new StatusPill(type));
        JLabel date = new JLabel(status.getStatusDate() != null ? status.getStatusDate().format(DATE_FMT) : "");
        date.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        date.setForeground(AppTheme.TEXT_MUTED);
        head.add(pillWrap, BorderLayout.WEST);
        head.add(date, BorderLayout.EAST);
        body.add(head, BorderLayout.NORTH);

        if (status.getNotes() != null && !status.getNotes().isBlank()) {
            // JTextArea wraps to the available width (a JLabel would stay on one long line).
            JTextArea notes = new JTextArea(status.getNotes());
            notes.setLineWrap(true);
            notes.setWrapStyleWord(true);
            notes.setEditable(false);
            notes.setFocusable(false);
            notes.setOpaque(false);
            notes.setBorder(null);
            notes.setFont(FontManager.bodyFont(Font.PLAIN, 12));
            notes.setForeground(AppTheme.TEXT_SECONDARY);
            body.add(notes, BorderLayout.CENTER);
        }

        row.add(body, BorderLayout.CENTER);
        return row;
    }

    /**
     * Dialog content panel that always matches the scroll pane's width, so content
     * wraps instead of producing a horizontal scrollbar.
     */
    public static class WidthTrackingPanel extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return r.height; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
