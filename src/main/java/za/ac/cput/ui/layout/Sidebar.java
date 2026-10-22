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

/**
 * Generic role-agnostic sidebar. Callers pass in their own NavItem list
 * (admin, nurse, doctor, patient all differ), so this component contains
 * no role-specific logic — just rendering, selection state, and click
 * dispatch back to whoever constructed it.
 */
public class Sidebar extends JPanel {

    private static final int WIDTH = 240;

    private final Map<String, JPanel> rowsByKey = new LinkedHashMap<>();
    private final Map<String, JLabel> labelsByKey = new LinkedHashMap<>();
    private String selectedKey;
    private final Consumer<String> onNavigate;

    public Sidebar(List<NavItem> items, String initialSelectedKey, Consumer<String> onNavigate, Runnable onLogout) {
        this.onNavigate = onNavigate;
        this.selectedKey = initialSelectedKey;

        setLayout(new BorderLayout());
        setBackground(AppTheme.SURFACE);
        setPreferredSize(new Dimension(WIDTH, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppTheme.BORDER));

        add(buildBrandHeader(), BorderLayout.NORTH);
        add(buildNavList(items), BorderLayout.CENTER);
        add(buildLogoutRow(onLogout), BorderLayout.SOUTH);
    }

    private JComponent buildBrandHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.SURFACE);
        header.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, AppTheme.SPACE_MD, AppTheme.SPACE_LG, AppTheme.SPACE_MD));

        // ===== Brand row: logo icon + "MediTicket" wordmark, side by side =====
        JPanel brandRow = new JPanel();
        brandRow.setLayout(new BoxLayout(brandRow, BoxLayout.X_AXIS));
        brandRow.setOpaque(false);
        brandRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        int logoHeight = 40;
        JLabel logo = new JLabel(ImageManager.getIcon(ImageManager.LOGO_ICON, -1, logoHeight));
        logo.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel wordmark = new JLabel("MediTicket");
        wordmark.setFont(FontManager.headlineFont(Font.BOLD, 24)); // tweak size here to match icon height visually
        wordmark.setForeground(AppTheme.TEXT_PRIMARY);
        wordmark.setBorder(BorderFactory.createEmptyBorder(0, AppTheme.SPACE_SM, 0, 0));
        wordmark.setAlignmentY(Component.CENTER_ALIGNMENT);

        brandRow.add(logo);
        brandRow.add(wordmark);

        JLabel subtitle = new JLabel("Clinic Management");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        subtitle.setForeground(AppTheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 0));

        header.add(brandRow);
        header.add(subtitle);
        return header;
    }

    private JComponent buildNavList(List<NavItem> items) {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(AppTheme.SURFACE);
        list.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, AppTheme.SPACE_SM, AppTheme.SPACE_SM, AppTheme.SPACE_SM));

        for (NavItem item : items) {
            JPanel row = buildRow(item.getIcon(), item.getLabel());
            rowsByKey.put(item.getKey(), row);
            row.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    select(item.getKey());
                    onNavigate.accept(item.getKey());
                }
            });
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

    private JPanel buildRow(String icon, String label) {
        JPanel row = new JPanel(new BorderLayout(AppTheme.SPACE_SM + 2, 0));
        row.setOpaque(true);
        row.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // Vector icon instead of an emoji glyph: Swing on Windows drew emoji as
        // black boxes. Icons.nameForEmoji keeps the existing NavItem lists working.
        String iconName = Icons.nameForEmoji(icon);
        JLabel iconLabel = new JLabel(Icons.get(iconName, 18, AppTheme.TEXT_SECONDARY));
        iconLabel.putClientProperty("iconName", iconName);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        labelsByKey.put(label, textLabel);

        // Unread-count badge, hidden until setBadge() gives it a count.
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

        row.add(iconLabel, BorderLayout.WEST);
        row.add(textLabel, BorderLayout.CENTER);
        row.add(badge, BorderLayout.EAST);
        return row;
    }

    /** Shows a teal count badge on a nav row (e.g. unread notifications); 0 hides it. */
    public void setBadge(String key, int count) {
        JPanel row = rowsByKey.get(key);
        if (row == null) return;
        JLabel badge = (JLabel) row.getComponent(2);
        badge.setText(count > 99 ? "99+" : String.valueOf(count));
        badge.setVisible(count > 0);
        row.revalidate();
        row.repaint();
    }

    public void select(String key) {
        selectedKey = key;
        applySelectionStyles();
    }

    private void applySelectionStyles() {
        for (Map.Entry<String, JPanel> entry : rowsByKey.entrySet()) {
            boolean selected = entry.getKey().equals(selectedKey);
            JPanel row = entry.getValue();
            row.setBackground(selected ? AppTheme.PRIMARY_LIGHT : AppTheme.SURFACE);
            JLabel iconLabel = (JLabel) row.getComponent(0);
            iconLabel.setIcon(Icons.get((String) iconLabel.getClientProperty("iconName"), 18,
                    selected ? AppTheme.PRIMARY : AppTheme.TEXT_SECONDARY));
            JLabel textLabel = (JLabel) row.getComponent(1);
            textLabel.setFont(FontManager.bodyFont(selected ? Font.BOLD : Font.PLAIN, 13));
            textLabel.setForeground(selected ? AppTheme.PRIMARY : AppTheme.TEXT_PRIMARY);
        }
    }

    private JComponent buildLogoutRow(Runnable onLogout) {
        JPanel row = buildRow(Icons.LOGOUT, "Logout");
        row.setBackground(AppTheme.SURFACE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.DIVIDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { onLogout.run(); }
        });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(AppTheme.SURFACE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, AppTheme.SPACE_SM, AppTheme.SPACE_MD, AppTheme.SPACE_SM));
        wrapper.add(row, BorderLayout.CENTER);
        return wrapper;
    }
}