/*
 Sidebar.java

 Side menu for each dashboard with unread badges, expand/collapse, and improved aesthetics

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

package za.ac.cput.ui.layout;

import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.ImageManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

// Sidebar used by every role. Each dashboard passes in its own menu items.
public class Sidebar extends JPanel {

    private static final int WIDTH_EXPANDED  = 240;
    private static final int WIDTH_COLLAPSED = 64;
    private static final int ANIM_DELAY_MS   = 10;

    private final Map<String, JPanel> rowsByKey   = new LinkedHashMap<>();
    private final Map<String, JLabel> iconsByKey  = new LinkedHashMap<>();
    private final Map<String, JLabel> labelsByKey = new LinkedHashMap<>();
    private final Map<String, JLabel> badgesByKey = new LinkedHashMap<>();
    private String selectedKey;
    private final Consumer<String> onNavigate;

    private boolean collapsed    = false;
    private Timer   collapseTimer;
    private int     currentWidth = WIDTH_EXPANDED;

    // Header components that hide when collapsed
    private JPanel  headerTextPanel;

    // Bottom row components that hide when collapsed
    private JLabel  toggleIcon;
    private JLabel  toggleLabel;
    private JPanel  toggleRow;
    private JPanel  logoutRow;

    public Sidebar(List<NavItem> items, String initialSelectedKey, Consumer<String> onNavigate, Runnable onLogout) {
        this.onNavigate  = onNavigate;
        this.selectedKey = initialSelectedKey;

        setLayout(new BorderLayout());
        setBackground(AppTheme.SURFACE);
        setPreferredSize(new Dimension(WIDTH_EXPANDED, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppTheme.BORDER));

        add(buildBrandHeader(), BorderLayout.NORTH);
        add(buildNavList(items), BorderLayout.CENTER);
        add(buildBottomPanel(onLogout), BorderLayout.SOUTH);
    }

    // ── Brand header ──────────────────────────────────────────────────────────

    private JComponent buildBrandHeader() {
        JPanel header = new JPanel(new BorderLayout(AppTheme.SPACE_SM, 0));
        header.setBackground(AppTheme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.DIVIDER),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD)
        ));

        JLabel logo = new JLabel(ImageManager.getIcon(ImageManager.LOGO_ICON, -1, 36));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setVerticalAlignment(SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(36, 40));

        headerTextPanel = new JPanel();
        headerTextPanel.setLayout(new BoxLayout(headerTextPanel, BoxLayout.Y_AXIS));
        headerTextPanel.setOpaque(false);

        JLabel wordmark = new JLabel("MediTicket");
        wordmark.setFont(FontManager.headlineFont(Font.BOLD, 20));
        wordmark.setForeground(AppTheme.TEXT_PRIMARY);
        wordmark.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Clinic Management");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        subtitle.setForeground(AppTheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(2, 1, 0, 0));

        headerTextPanel.add(wordmark);
        headerTextPanel.add(subtitle);

        header.add(logo, BorderLayout.WEST);
        header.add(headerTextPanel, BorderLayout.CENTER);
        return header;
    }

    // ── Nav list ──────────────────────────────────────────────────────────────

    private JComponent buildNavList(List<NavItem> items) {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(AppTheme.SURFACE);
        list.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, AppTheme.SPACE_SM, AppTheme.SPACE_SM, AppTheme.SPACE_SM));

        for (NavItem item : items) {
            JPanel row = buildNavRow(item.getIcon(), item.getLabel(), item.getKey());
            rowsByKey.put(item.getKey(), row);
            list.add(row);
            list.add(Box.createVerticalStrut(2));
        }
        applySelectionStyles();

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JPanel buildNavRow(String iconEmoji, String label, String key) {
        final boolean[] hovered = {false};

        JPanel row = new JPanel(new BorderLayout(AppTheme.SPACE_SM + 2, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (key.equals(selectedKey)) {
                    g2.setColor(AppTheme.PRIMARY_LIGHT);
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                    g2.setColor(AppTheme.PRIMARY);
                    g2.fillRoundRect(2, 7, 3, getHeight() - 14, 3, 3);
                } else if (hovered[0]) {
                    g2.setColor(AppTheme.SURFACE_ALT);
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                }
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        String iconName = Icons.nameForEmoji(iconEmoji);
        JLabel iconLabel = new JLabel(Icons.get(iconName, 18, AppTheme.TEXT_SECONDARY));
        iconLabel.putClientProperty("iconName", iconName);
        iconsByKey.put(key, iconLabel);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        labelsByKey.put(key, textLabel);

        JLabel badge = new JLabel();
        badge.setFont(FontManager.bodyFont(Font.BOLD, 11));
        badge.setForeground(AppTheme.TEXT_ON_PRIMARY);
        badge.setOpaque(false);
        badge.setVisible(false);
        badge.setBorder(BorderFactory.createEmptyBorder(1, 7, 1, 7));
        badge.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), c.getHeight(), c.getHeight());
                g2.dispose();
                super.paint(g, c);
            }
        });
        badgesByKey.put(key, badge);

        row.add(iconLabel, BorderLayout.WEST);
        row.add(textLabel, BorderLayout.CENTER);
        row.add(badge, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered[0] = true;  row.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered[0] = false; row.repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                select(key);
                onNavigate.accept(key);
            }
        });
        return row;
    }

    // ── Bottom panel (collapse toggle + logout) ───────────────────────────────

    private JComponent buildBottomPanel(Runnable onLogout) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppTheme.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.DIVIDER),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, AppTheme.SPACE_SM, AppTheme.SPACE_MD, AppTheme.SPACE_SM)
        ));

        toggleRow = buildToggleRow();
        logoutRow = buildLogoutRow(onLogout);

        panel.add(toggleRow);
        panel.add(Box.createVerticalStrut(2));
        panel.add(logoutRow);
        return panel;
    }

    private JPanel buildToggleRow() {
        final boolean[] hovered = {false};

        JPanel row = new JPanel(new BorderLayout(AppTheme.SPACE_SM + 2, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                if (hovered[0]) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(AppTheme.SURFACE_ALT);
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                    g2.dispose();
                }
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        toggleIcon = new JLabel(Icons.get(Icons.CHEVRON_LEFT, 18, AppTheme.TEXT_SECONDARY));

        toggleLabel = new JLabel("Collapse");
        toggleLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        toggleLabel.setForeground(AppTheme.TEXT_SECONDARY);

        row.add(toggleIcon, BorderLayout.WEST);
        row.add(toggleLabel, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered[0] = true;  row.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered[0] = false; row.repaint(); }
            @Override public void mouseClicked(MouseEvent e) { toggleCollapse(); }
        });
        return row;
    }

    private JPanel buildLogoutRow(Runnable onLogout) {
        final boolean[] hovered = {false};
        final JLabel[] iconRef  = new JLabel[1];
        final JLabel[] textRef  = new JLabel[1];

        JPanel row = new JPanel(new BorderLayout(AppTheme.SPACE_SM + 2, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                if (hovered[0]) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(AppTheme.STATUS_DANGER_BG);
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                    g2.dispose();
                }
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel iconLabel = new JLabel(Icons.get(Icons.LOGOUT, 18, AppTheme.TEXT_SECONDARY));
        iconRef[0] = iconLabel;

        JLabel textLabel = new JLabel("Logout");
        textLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        textLabel.setForeground(AppTheme.TEXT_PRIMARY);
        textRef[0]  = textLabel;
        labelsByKey.put("__logout__", textLabel);

        row.add(iconLabel, BorderLayout.WEST);
        row.add(textLabel, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                hovered[0] = true;
                row.repaint();
                iconRef[0].setIcon(Icons.get(Icons.LOGOUT, 18, AppTheme.STATUS_DANGER));
                textRef[0].setForeground(AppTheme.STATUS_DANGER);
            }
            @Override public void mouseExited(MouseEvent e) {
                hovered[0] = false;
                row.repaint();
                iconRef[0].setIcon(Icons.get(Icons.LOGOUT, 18, AppTheme.TEXT_SECONDARY));
                textRef[0].setForeground(AppTheme.TEXT_PRIMARY);
            }
            @Override public void mouseClicked(MouseEvent e) { onLogout.run(); }
        });
        return row;
    }

    // ── Collapse / expand ─────────────────────────────────────────────────────

    private void toggleCollapse() {
        if (collapseTimer != null && collapseTimer.isRunning()) collapseTimer.stop();

        collapsed = !collapsed;
        int target = collapsed ? WIDTH_COLLAPSED : WIDTH_EXPANDED;

        // Show / hide text elements immediately — width animates separately
        boolean show = !collapsed;
        headerTextPanel.setVisible(show);
        toggleLabel.setVisible(show);
        for (JLabel lbl : labelsByKey.values()) lbl.setVisible(show);
        for (JLabel badge : badgesByKey.values()) {
            if (!show) badge.setVisible(false);
            else badge.setVisible(badge.getText() != null && !badge.getText().isEmpty());
        }

        toggleIcon.setIcon(Icons.get(
                collapsed ? Icons.CHEVRON_RIGHT : Icons.CHEVRON_LEFT,
                18, AppTheme.TEXT_SECONDARY));

        // Center icons in collapsed state; restore normal left padding when expanded
        int iconPad = collapsed ? (WIDTH_COLLAPSED - 18 - 4) / 2 : 12;
        for (JPanel row : rowsByKey.values()) {
            row.setBorder(BorderFactory.createEmptyBorder(10, iconPad, 10, iconPad));
        }
        toggleRow.setBorder(BorderFactory.createEmptyBorder(10, iconPad, 10, iconPad));
        logoutRow.setBorder(BorderFactory.createEmptyBorder(10, iconPad, 10, iconPad));

        collapseTimer = new Timer(ANIM_DELAY_MS, null);
        collapseTimer.addActionListener(e -> {
            int diff = target - currentWidth;
            if (Math.abs(diff) <= 3) {
                currentWidth = target;
                ((Timer) e.getSource()).stop();
            } else {
                currentWidth += diff / 3;
            }
            setPreferredSize(new Dimension(currentWidth, 0));
            revalidate();
            Container parent = getParent();
            if (parent != null) { parent.revalidate(); parent.repaint(); }
        });
        collapseTimer.start();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Shows a teal count badge on a nav row; 0 hides it. */
    public void setBadge(String key, int count) {
        JLabel badge = badgesByKey.get(key);
        if (badge == null) return;
        badge.setText(count > 99 ? "99+" : String.valueOf(count));
        badge.setVisible(count > 0 && !collapsed);
        JPanel row = rowsByKey.get(key);
        if (row != null) { row.revalidate(); row.repaint(); }
    }

    public void select(String key) {
        selectedKey = key;
        applySelectionStyles();
    }

    private void applySelectionStyles() {
        for (Map.Entry<String, JPanel> entry : rowsByKey.entrySet()) {
            String  k        = entry.getKey();
            JPanel  row      = entry.getValue();
            boolean selected = k.equals(selectedKey);
            row.repaint();

            JLabel icon = iconsByKey.get(k);
            if (icon != null) {
                icon.setIcon(Icons.get(
                        (String) icon.getClientProperty("iconName"), 18,
                        selected ? AppTheme.PRIMARY : AppTheme.TEXT_SECONDARY));
            }
            JLabel lbl = labelsByKey.get(k);
            if (lbl != null) {
                lbl.setFont(FontManager.bodyFont(selected ? Font.BOLD : Font.PLAIN, 13));
                lbl.setForeground(selected ? AppTheme.PRIMARY : AppTheme.TEXT_PRIMARY);
            }
        }
    }
}
