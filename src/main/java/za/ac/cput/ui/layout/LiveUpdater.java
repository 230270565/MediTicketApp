/*
 LiveUpdater.java

 Reloads the open page and the badges every few seconds

 Authors:
  Joshua Reid Adams (230317693)
  Abdullahi Raage Farah (230971091)
  Aidan Barends (230255639)
  Jaden Clayton Abrahams (222206721)
  Joshua Peter Bonzet (221312536)
  Matthew Barron (230398863)
  Raul Jaaim Everts (230270565)

 Date: 22nd October 2026
*/

package za.ac.cput.ui.layout;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Notification;
import za.ac.cput.session.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

// Keeps the dashboard up to date without logging out.
// Reloads a page when it is opened, then every few seconds along with the badges.
// It skips a refresh while a dialog is open. Call stop() on logout.
public class LiveUpdater {

    private static final int REFRESH_MS = 10_000;

    private final JPanel pageContainer;
    private final Sidebar sidebar;
    private final TopHeader topHeader;
    private final String notificationsKey;
    private final Timer timer;
    private boolean countInFlight = false;

    // The dashboard currently on screen, so a page can ask for the badges to update now.
    private static LiveUpdater active;

    /** Recount unread notifications right away (e.g. after one is opened). */
    public static void refreshBadgesNow() {
        if (active != null) active.updateUnreadCount();
    }

    public LiveUpdater(JPanel pageContainer, Sidebar sidebar, TopHeader topHeader, String notificationsKey) {
        this.pageContainer = pageContainer;
        this.sidebar = sidebar;
        this.topHeader = topHeader;
        this.notificationsKey = notificationsKey;
        this.timer = new Timer(REFRESH_MS, e -> tick());
        this.timer.setRepeats(true);
    }

    public void start() {
        active = this;
        updateUnreadCount();
        timer.start();
    }

    public void stop() {
        timer.stop();
        if (active == this) active = null;
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
