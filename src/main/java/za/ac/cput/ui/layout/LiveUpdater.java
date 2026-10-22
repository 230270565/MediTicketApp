package za.ac.cput.ui.layout;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Notification;
import za.ac.cput.session.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Keeps a dashboard current without logging out and back in.
 *
 *  - pageOpened(): reloads a page the moment the user opens it.
 *  - every REFRESH_MS: reloads the page on screen and the unread notification
 *    count (sidebar badge + header bell). Skipped while a dialog is open, so a
 *    form someone is filling in never redraws underneath them.
 *
 * This is polling, not server push: simple, no backend change, and on localhost
 * a refresh costs a few milliseconds. stop() must be called on logout.
 */
public class LiveUpdater {

    private static final int REFRESH_MS = 10_000;

    private final JPanel pageContainer;
    private final Sidebar sidebar;
    private final TopHeader topHeader;
    private final String notificationsKey;
    private final Timer timer;
    private boolean countInFlight = false;

    public LiveUpdater(JPanel pageContainer, Sidebar sidebar, TopHeader topHeader, String notificationsKey) {
        this.pageContainer = pageContainer;
        this.sidebar = sidebar;
        this.topHeader = topHeader;
        this.notificationsKey = notificationsKey;
        this.timer = new Timer(REFRESH_MS, e -> tick());
        this.timer.setRepeats(true);
    }

    public void start() {
        updateUnreadCount();
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    /** Call after CardLayout.show(): reloads the page that just became visible. */
    public void pageOpened() {
        if (!timer.isRunning()) return; // during construction pages have just loaded
        refreshVisiblePage();
        updateUnreadCount();
    }

    private void tick() {
        if (!pageContainer.isShowing() || dialogOpen()) return;
        refreshVisiblePage();
        updateUnreadCount();
    }

    private void refreshVisiblePage() {
        for (Component page : pageContainer.getComponents()) {
            if (page.isVisible() && page instanceof Refreshable refreshable) {
                try {
                    refreshable.refresh();
                } catch (RuntimeException ex) {
                    // A failed refresh (e.g. backend restarting) must not stop the timer.
                }
                return;
            }
        }
    }

    /** True while any modal dialog of this app is on screen. */
    private static boolean dialogOpen() {
        for (Window w : Window.getWindows()) {
            if (w instanceof Dialog d && d.isVisible() && d.isModal()) return true;
        }
        return false;
    }

    /** Counts this user's unread notifications off the UI thread, then updates badges. */
    private void updateUnreadCount() {
        if (countInFlight) return;
        countInFlight = true;
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                SessionManager session = SessionManager.getInstance();
                var api = ApiClientProvider.getInstance().notifications();
                BaseApiClient.ApiResult<List<Notification>> result = switch (String.valueOf(session.getUserType())) {
                    case "PATIENT" -> api.findByPatient(session.getUserId());
                    case "DOCTOR" -> api.findByDoctor(session.getUserId());
                    default -> api.findByClinicStaff(session.getUserId());
                };
                if (!result.isSuccess() || result.getData() == null) return -1;
                return (int) result.getData().stream().filter(Notification::isUnread).count();
            }

            @Override
            protected void done() {
                countInFlight = false;
                try {
                    int unread = get();
                    if (unread < 0) return;
                    sidebar.setBadge(notificationsKey, unread);
                    topHeader.setUnreadCount(unread);
                } catch (Exception ignored) {
                    // keep the previous badge value
                }
            }
        }.execute();
    }
}
