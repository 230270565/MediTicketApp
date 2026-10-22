package za.ac.cput.ui.layout;

/**
 * A page that can reload its data from the backend.
 *
 * Dashboards build every page once at login, so without this a page kept showing
 * whatever it loaded first until the user logged out and back in. LiveUpdater
 * calls refresh() when a page is opened and every few seconds while it is visible.
 */
public interface Refreshable {
    void refresh();
}
