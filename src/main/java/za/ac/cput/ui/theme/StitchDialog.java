/*
 StitchDialog.java

 Shared infrastructure for every dialog in the Stitch design system.
 Provides the undecorated card, teal accent bar, standard header, meta cells,
 banners, and form helpers so each dialog only has to write its content.

 Authors:
  Joshua Reid Adams (230317693)
  Abdullahi Raage Farah (230971091)
  Aidan Barends (230255639)
  Jaden Clayton Abrahams (222206721)
  Joshua Peter Bonzet (221312536)
  Matthew Barron (230398863)
  Raul Jaaim Everts (230270565)

 Date: 25th October 2026
*/

package za.ac.cput.ui.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public final class StitchDialog {

    public static final int ACCENT_H    = 4;
    public static final int SHADOW_SIZE = 14;
    public static final int CARD_RADIUS = 16;

    private StitchDialog() {}

    // ── Dialog + card factory ─────────────────────────────────────────────────

    /** Creates an undecorated, transparent dialog ready for a card panel. */
    public static JDialog create(Component parent) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        return dialog;
    }

    /** The rounded card with a soft shadow and teal accent bar at the top. */
    public static CardPanel card() {
        return new CardPanel();
    }

    /**
     * Builds the body panel (BoxLayout Y_AXIS, correct padding), wires up Esc,
     * attaches drag, packs the dialog and centres it.
     *
     * @param minWidth  minimum card width in px
     */
    public static JPanel body(JDialog dialog, CardPanel card, int minWidth) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(ACCENT_H + 20, 28, 0, 28));

        card.setLayout(new BorderLayout());
        card.add(body, BorderLayout.CENTER);

        DragHandler drag = new DragHandler(dialog);
        card.addMouseListener(drag);
        card.addMouseMotionListener(drag);

        dialog.setContentPane(card);
        dialog.getRootPane().registerKeyboardAction(
                (ActionEvent e) -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Pack is done lazily (after body is populated) — caller calls packAndShow().
        return body;
    }

    /** Packs the dialog, enforces the minimum width and centres it relative to parent. */
    public static void packAndShow(JDialog dialog, Component parent, int minWidth) {
        dialog.pack();
        dialog.setMinimumSize(new Dimension(minWidth, 0));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    // ── Standard header ───────────────────────────────────────────────────────

    /**
     * Icon-circle + title + subtitle + X close button.
     * Subtitle may be {@code null} to omit it.
     */
    public static JComponent header(JDialog dialog, String iconName, String title, String subtitle) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(iconCircle(iconName));

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontManager.headlineFont(Font.BOLD, 17));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titles.add(titleLabel);

        if (subtitle != null && !subtitle.isBlank()) {
            titles.add(Box.createVerticalStrut(2));
            JLabel subLabel = new JLabel(subtitle);
            subLabel.setFont(FontManager.bodyFont(Font.PLAIN, 12));
            subLabel.setForeground(AppTheme.TEXT_MUTED);
            titles.add(subLabel);
        }

        left.add(titles);
        row.add(left, BorderLayout.CENTER);
        row.add(closeButton(dialog), BorderLayout.EAST);
        return row;
    }

    // ── Divider ───────────────────────────────────────────────────────────────

    public static JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // ── Meta cells (2-column grids) ───────────────────────────────────────────

    /** Label stacked above a value component — used in GridLayout cells. */
    public static JComponent metaCell(String label, JComponent value) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(FontManager.bodyFont(Font.BOLD, 10));
        lbl.setForeground(AppTheme.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        value.setAlignmentX(Component.LEFT_ALIGNMENT);

        cell.add(lbl);
        cell.add(Box.createVerticalStrut(5));
        cell.add(value);
        return cell;
    }

    /** Label stacked above a plain text value. */
    public static JComponent metaCell(String label, String value) {
        JLabel v = new JLabel(value != null && !value.isBlank() ? value : "—");
        v.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        v.setForeground(AppTheme.TEXT_PRIMARY);
        return metaCell(label, v);
    }

    // ── Person cell (avatar + name + sub-line) ────────────────────────────────

    public static JComponent personCell(String initials, String name, String sub, Color subColor) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.add(avatar(initials));

        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        JLabel nameLabel = new JLabel(name != null && !name.isBlank() ? name : "—");
        nameLabel.setFont(FontManager.bodyFont(Font.BOLD, 13));
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel(sub != null ? sub : "");
        subLabel.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        subLabel.setForeground(subColor != null ? subColor : AppTheme.TEXT_MUTED);

        col.add(nameLabel);
        col.add(subLabel);
        row.add(col);
        return row;
    }

    // ── Icon + value row ──────────────────────────────────────────────────────

    public static JComponent iconRow(String iconName, String mainText, String trailingText) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        row.add(new JLabel(Icons.get(iconName, 14, AppTheme.TEXT_MUTED)));

        JLabel main = new JLabel(mainText != null ? mainText : "—");
        main.setFont(FontManager.bodyFont(Font.BOLD, 13));
        main.setForeground(AppTheme.TEXT_PRIMARY);
        row.add(main);

        if (trailingText != null && !trailingText.isBlank()) {
            JLabel trail = new JLabel(trailingText);
            trail.setFont(FontManager.bodyFont(Font.PLAIN, 12));
            trail.setForeground(AppTheme.TEXT_MUTED);
            row.add(trail);
        }
        return row;
    }

    // ── Info / status banner ──────────────────────────────────────────────────

    public static JComponent banner(String message, Color bg, Color iconColor) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.RADIUS_MD, AppTheme.RADIUS_MD);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));
        panel.add(new JLabel(Icons.get(Icons.INFO, 15, iconColor)));
        JLabel msg = new JLabel(message);
        msg.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        msg.setForeground(AppTheme.TEXT_SECONDARY);
        panel.add(msg);
        return panel;
    }

    /** Info banner using default teal style. */
    public static JComponent infoBanner(String message) {
        return banner(message, AppTheme.PRIMARY_LIGHT, AppTheme.PRIMARY);
    }

    // ── Content box (rounded bg, used for quoted reasons, messages) ───────────

    public static JComponent contentBox(JComponent inner) {
        JPanel box = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.SURFACE_ALT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.RADIUS_MD, AppTheme.RADIUS_MD);
                g2.setColor(AppTheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.RADIUS_MD, AppTheme.RADIUS_MD);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        box.setOpaque(false);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setBorder(new EmptyBorder(14, 16, 14, 16));
        box.add(inner, BorderLayout.CENTER);
        return box;
    }

    // ── Section header (icon + ALL-CAPS label + optional right badge) ─────────

    public static JComponent sectionHeader(String iconName, String label, JComponent badge) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(new JLabel(Icons.get(iconName, 13, AppTheme.TEXT_MUTED)));
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(FontManager.bodyFont(Font.BOLD, 10));
        lbl.setForeground(AppTheme.TEXT_MUTED);
        left.add(lbl);
        row.add(left, BorderLayout.WEST);

        if (badge != null) {
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            right.setOpaque(false);
            right.add(badge);
            row.add(right, BorderLayout.EAST);
        }
        return row;
    }

    // ── Form helpers ──────────────────────────────────────────────────────────

    /** Bold small-caps field label used above form inputs. */
    public static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FontManager.bodyFont(Font.BOLD, 11));
        l.setForeground(AppTheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 4, 0));
        return l;
    }

    /** Standard single-line text input matching the design system. */
    public static JTextField styledField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        if (placeholder != null) field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return field;
    }

    /** Error label used inside forms. Invisible until text is set. */
    public static JLabel errorLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        l.setForeground(AppTheme.STATUS_DANGER);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(6, 0, 0, 0));
        return l;
    }

    // ── Button row ────────────────────────────────────────────────────────────

    /** Right-aligned row for footer buttons. */
    public static JPanel buttonRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    // ── Teal chip (consultation type, channels, etc.) ─────────────────────────

    public static JComponent chip(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 12, 4, 12));
        JLabel l = new JLabel(text);
        l.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        l.setForeground(AppTheme.PRIMARY);
        p.add(l);
        return p;
    }

    /** Outline chip in neutral style (icon + label). Used for channels: "In-app", "Email". */
    public static JComponent outlineChip(String text, String iconName) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.SURFACE_ALT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.setColor(AppTheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(2, 8, 2, 8));
        p.add(new JLabel(Icons.get(iconName, 11, AppTheme.TEXT_MUTED)));
        JLabel l = new JLabel(text);
        l.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        l.setForeground(AppTheme.TEXT_SECONDARY);
        p.add(l);
        return p;
    }

    // ── Avatar / icon circles ─────────────────────────────────────────────────

    /** Teal circle with up-to-2-letter initials. */
    public static JPanel avatar(String initials) {
        final String s = initials != null ? initials : "?";
        JPanel circle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(AppTheme.PRIMARY);
                g2.setFont(FontManager.bodyFont(Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(s,
                        (getWidth() - fm.stringWidth(s)) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        circle.setOpaque(false);
        Dimension d = new Dimension(30, 30);
        circle.setPreferredSize(d);
        circle.setMinimumSize(d);
        circle.setMaximumSize(d);
        return circle;
    }

    /** Teal circle containing a drawn icon — used in dialog headers. */
    public static JPanel iconCircle(String iconName) {
        JPanel circle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        circle.setOpaque(false);
        circle.setLayout(new GridBagLayout());
        circle.add(new JLabel(Icons.get(iconName, 20, AppTheme.PRIMARY)));
        Dimension d = new Dimension(44, 44);
        circle.setPreferredSize(d);
        circle.setMinimumSize(d);
        circle.setMaximumSize(d);
        return circle;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static String initials(String first, String last) {
        String f = first != null && !first.isBlank() ? String.valueOf(first.charAt(0)).toUpperCase() : "";
        String l = last  != null && !last.isBlank()  ? String.valueOf(last.charAt(0)).toUpperCase()  : "";
        return f + l;
    }

    public static String htmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static JButton closeButton(JDialog dialog) {
        JButton btn = new JButton(Icons.get(Icons.X, 14, AppTheme.TEXT_MUTED));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(28, 28));
        btn.addActionListener(e -> dialog.dispose());
        return btn;
    }

    // ── CardPanel ─────────────────────────────────────────────────────────────

    public static class CardPanel extends JPanel {

        public CardPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(0, 0, SHADOW_SIZE, SHADOW_SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - SHADOW_SIZE;
            int h = getHeight() - SHADOW_SIZE;

            // Shadow
            for (int i = SHADOW_SIZE; i > 0; i--) {
                g2.setColor(new Color(0, 0, 0, 3));
                g2.fillRoundRect(i / 2, i / 2, w + (SHADOW_SIZE - i), h + (SHADOW_SIZE - i),
                        CARD_RADIUS, CARD_RADIUS);
            }

            // Card body
            g2.setColor(AppTheme.SURFACE);
            g2.fillRoundRect(0, 0, w, h, CARD_RADIUS, CARD_RADIUS);

            // Border
            g2.setColor(AppTheme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, CARD_RADIUS, CARD_RADIUS);

            // Teal accent bar (clipped to the rounded top)
            g2.setColor(AppTheme.PRIMARY);
            g2.setClip(new RoundRectangle2D.Float(0, 0, w, CARD_RADIUS * 2, CARD_RADIUS, CARD_RADIUS));
            g2.fillRect(0, 0, w, ACCENT_H);
            g2.setClip(null);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── DragHandler ───────────────────────────────────────────────────────────

    private static class DragHandler extends MouseAdapter {
        private final JDialog dialog;
        private Point offset;

        DragHandler(JDialog dialog) { this.dialog = dialog; }

        @Override public void mousePressed(MouseEvent e)  { offset = e.getPoint(); }
        @Override public void mouseDragged(MouseEvent e)  {
            Point loc = dialog.getLocation();
            dialog.setLocation(loc.x + e.getX() - offset.x, loc.y + e.getY() - offset.y);
        }
    }
}
