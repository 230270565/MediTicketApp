/*
 Paginator.java

 Reusable pagination bar: "Showing X to Y of Z items • Rows per page: 8  < Previous [1][2][3] Next >"

 Authors: All team members
 Date: 11th August 2026
*/
package za.ac.cput.ui.layout;

import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;

public class Paginator extends JPanel {

    private int currentPage = 1;
    private int totalItems  = 0;
    private final int    pageSize;
    private final String itemLabel;
    private final Runnable onChange;

    private JLabel statusLabel;
    private JPanel navPanel;

    public Paginator(String itemLabel, Runnable onChange) {
        this(itemLabel, 8, onChange);
    }

    public Paginator(String itemLabel, int pageSize, Runnable onChange) {
        this.itemLabel = itemLabel;
        this.pageSize  = pageSize;
        this.onChange  = onChange;

        setLayout(new BorderLayout());
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.DIVIDER),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, 0, AppTheme.SPACE_SM, 0)));

        statusLabel = new JLabel();
        statusLabel.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        statusLabel.setForeground(AppTheme.TEXT_SECONDARY);

        navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        navPanel.setOpaque(false);

        add(statusLabel, BorderLayout.WEST);
        add(navPanel,    BorderLayout.EAST);
        rebuild();
    }

    /** Call after filtering. Updates the display and clamps the page if needed. */
    public void setTotal(int total) {
        totalItems = total;
        int maxPage = Math.max(1, (int) Math.ceil((double) total / pageSize));
        if (currentPage > maxPage) currentPage = maxPage;
        rebuild();
    }

    /** Reset to page 1 — call when the active filter changes. */
    public void reset() { currentPage = 1; }

    public int getOffset()    { return (currentPage - 1) * pageSize; }
    public int getPageSize()  { return pageSize; }
    public int getTotalPages(){ return Math.max(1, (int) Math.ceil((double) totalItems / pageSize)); }

    private void rebuild() {
        int from = totalItems == 0 ? 0 : getOffset() + 1;
        int to   = Math.min(getOffset() + pageSize, totalItems);

        statusLabel.setText(totalItems == 0
                ? "No " + itemLabel
                : "Showing " + from + " to " + to + " of " + totalItems + " " + itemLabel
                  + "  •  Rows per page: " + pageSize);

        navPanel.removeAll();
        navPanel.add(navBtn("< Previous", currentPage > 1,              () -> goTo(currentPage - 1)));

        int total = getTotalPages();
        int start = Math.max(1, Math.min(currentPage - 2, total - 4));
        int end   = Math.min(total, start + 4);
        for (int p = start; p <= end; p++) {
            final int page = p;
            navPanel.add(pageBtn(String.valueOf(p), p == currentPage, () -> goTo(page)));
        }

        navPanel.add(navBtn("Next >",     currentPage < total,          () -> goTo(currentPage + 1)));
        navPanel.revalidate();
        navPanel.repaint();
    }

    private JButton navBtn(String text, boolean enabled, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        btn.setForeground(enabled ? AppTheme.TEXT_SECONDARY : AppTheme.TEXT_MUTED);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setEnabled(enabled);
        if (enabled) { btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.addActionListener(e -> action.run()); }
        return btn;
    }

    private JButton pageBtn(String text, boolean active, Runnable action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                if (active) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(AppTheme.PRIMARY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(FontManager.bodyFont(active ? Font.BOLD : Font.PLAIN, 12));
        btn.setForeground(active ? AppTheme.TEXT_ON_PRIMARY : AppTheme.TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(28, 28));
        if (!active) { btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.addActionListener(e -> action.run()); }
        return btn;
    }

    private void goTo(int page) { currentPage = page; rebuild(); if (onChange != null) onChange.run(); }
}
