package za.ac.cput.ui.clinicstaff.nurse.pages;
//Raul Everts 230270565
import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Notification;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.clinicstaff.components.NotificationDetailsDialog;
import za.ac.cput.ui.layout.NotificationCard;
import za.ac.cput.ui.layout.Refreshable;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Nurse notifications: newest first, unread ones highlighted.
 * Opening a notification marks it as read. LiveUpdater refreshes this page
 * every few seconds while it is visible, so new notifications appear on their own.
 */
public class NotificationsPage extends JPanel implements Refreshable {

    private JPanel historyList;
    private String activeFilter = "ALL";
    private JPanel filterBarContainer;
    private List<Notification> allNotifications = List.of();

    public NotificationsPage() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(
                AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG));

        JLabel title = new JLabel("Notifications");
        title.setFont(FontManager.headlineFont(Font.BOLD, 26));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Updates about your appointments, tickets and payments. Click one to open it.");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(2, 0, AppTheme.SPACE_MD, 0));

        filterBarContainer = new JPanel(new BorderLayout());
        filterBarContainer.setOpaque(false);
        filterBarContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterBarContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        historyList = new JPanel();
        historyList.setLayout(new BoxLayout(historyList, BoxLayout.Y_AXIS));
        historyList.setOpaque(false);

        JScrollPane scroll = new JScrollPane(historyList);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(subtitle);
        content.add(filterBarContainer);
        content.add(Box.createVerticalStrut(AppTheme.SPACE_MD));
        content.add(scroll);

        add(content, BorderLayout.CENTER);

        loadNotifications();
    }

    /** Called by LiveUpdater when this page is opened and every few seconds while visible. */
    @Override
    public void refresh() {
        loadNotifications();
    }

    private void loadNotifications() {
        int userId = SessionManager.getInstance().getUserId();
        BaseApiClient.ApiResult<List<Notification>> result =
                ApiClientProvider.getInstance().notifications().findByClinicStaff(userId);
        allNotifications = result.isSuccess() && result.getData() != null ? result.getData() : List.of();
        allNotifications = allNotifications.stream()
                .sorted((a, b) -> {
                    if (a.getNotificationDate() == null) return 1;
                    if (b.getNotificationDate() == null) return -1;
                    return b.getNotificationDate().compareTo(a.getNotificationDate());
                })
                .collect(Collectors.toList());
        rebuildFilterBar();
        renderNotifications();
    }

    // ── Filters: All / Unread / Read, each with its count ──

    private void rebuildFilterBar() {
        long unread = allNotifications.stream().filter(Notification::isUnread).count();
        long read = allNotifications.size() - unread;

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_SM, 0));
        bar.setOpaque(false);
        bar.add(filterButton("ALL", "All  " + allNotifications.size()));
        bar.add(filterButton("UNREAD", "Unread  " + unread));
        bar.add(filterButton("READ", "Read  " + read));

        filterBarContainer.removeAll();
        filterBarContainer.add(bar, BorderLayout.WEST);
        filterBarContainer.revalidate();
        filterBarContainer.repaint();
    }

    private JButton filterButton(String key, String text) {
        JButton btn = new JButton(text);
        AppTheme.styleFilterButton(btn, key.equals(activeFilter));
        btn.addActionListener(e -> {
            activeFilter = key;
            rebuildFilterBar();
            renderNotifications();
        });
        return btn;
    }

    private void renderNotifications() {
        historyList.removeAll();

        List<Notification> filtered = allNotifications.stream()
                .filter(n -> switch (activeFilter) {
                    case "UNREAD" -> n.isUnread();
                    case "READ" -> !n.isUnread();
                    default -> true;
                })
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            JLabel empty = new JLabel("UNREAD".equals(activeFilter)
                    ? "You're all caught up. No unread notifications."
                    : "No notifications yet. Updates about your appointments will appear here.");
            empty.setFont(FontManager.bodyFont(Font.PLAIN, 13));
            empty.setForeground(AppTheme.TEXT_MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, 0, 0, 0));
            historyList.add(empty);
        } else {
            for (Notification n : filtered) {
                historyList.add(new NotificationCard(n, null, () -> open(n)));
                historyList.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
            }
        }

        historyList.revalidate();
        historyList.repaint();
    }

    /** Opening a notification marks it as read (in the background), then shows the details. */
    private void open(Notification n) {
        if (n.isUnread()) {
            n.setNotificationStatus("READ");
            rebuildFilterBar();
            renderNotifications();
            int id = n.getNotificationId();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    ApiClientProvider.getInstance().notifications().markAsRead(id);
                    return null;
                }
            }.execute();
        }
        NotificationDetailsDialog.show(this, n, "You", null, null);
    }
}
