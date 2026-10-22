package za.ac.cput.ui.patient;
//AIDAN BARENDS - 230155639
import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.AppFrame;
import za.ac.cput.ui.layout.NavItem;
import za.ac.cput.ui.layout.Sidebar;
import za.ac.cput.ui.layout.TopHeader;
import za.ac.cput.ui.layout.LiveUpdater;
import za.ac.cput.ui.patient.pages.*;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PatientDashboard extends JPanel {

    private final AppFrame appFrame;
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageContainer = new JPanel(pageLayout);

    private Sidebar sidebar;
    private TopHeader topHeader;
    private LiveUpdater live; // real-time refresh of the visible page + unread badges

    private static final String PAGE_HOME = "HOME";
    private static final String PAGE_APPOINTMENTS = "APPOINTMENTS";
    private static final String PAGE_TICKETS = "TICKETS";
    private static final String PAGE_PAYMENTS = "PAYMENTS";
    private static final String PAGE_NOTIFICATIONS = "NOTIFICATIONS";
    private static final String PAGE_PROFILE = "PROFILE";

    public PatientDashboard(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);

        List<NavItem> navItems = List.of(
                new NavItem(PAGE_HOME, Icons.HOME, "Dashboard"),
                new NavItem(PAGE_APPOINTMENTS, Icons.CALENDAR, "Appointments"),
                new NavItem(PAGE_TICKETS, Icons.TICKET, "Tickets"),
                new NavItem(PAGE_PAYMENTS, Icons.CARD, "Payments"),
                new NavItem(PAGE_NOTIFICATIONS, Icons.BELL, "Notifications"),
                new NavItem(PAGE_PROFILE, Icons.USER, "Profile")
        );

        sidebar = new Sidebar(navItems, PAGE_HOME, this::showPage, this::onLogout);

        topHeader = new TopHeader(() -> {
            sidebar.select(PAGE_PROFILE);
            showPage(PAGE_PROFILE);
        });

        registerPages();

        live = new LiveUpdater(pageContainer, sidebar, topHeader, PAGE_NOTIFICATIONS);
        topHeader.setOnBellClick(() -> {
            sidebar.select(PAGE_NOTIFICATIONS);
            showPage(PAGE_NOTIFICATIONS);
        });

        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setBackground(AppTheme.BACKGROUND);
        rightSide.add(topHeader, BorderLayout.NORTH);
        rightSide.add(pageContainer, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(rightSide, BorderLayout.CENTER);

        showPage(PAGE_HOME);
        live.start();
    }


    private void registerPages() {
        pageContainer.add(new DashboardPage(this::navigateTo), PAGE_HOME);
        pageContainer.add(new AppointmentsPage(), PAGE_APPOINTMENTS);
        pageContainer.add(new TicketsPage(), PAGE_TICKETS);
        pageContainer.add(new PaymentsPage(), PAGE_PAYMENTS);
        pageContainer.add(new NotificationsPage(), PAGE_NOTIFICATIONS);
        pageContainer.add(new ProfilePage(topHeader::refreshProfile), PAGE_PROFILE);
    }

    private JComponent placeholder(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.BACKGROUND);
        JLabel label = new JLabel(message);
        label.setFont(FontManager.bodyFont(Font.PLAIN, 15));
        label.setForeground(AppTheme.TEXT_SECONDARY);
        panel.add(label);
        return panel;
    }

    private void showPage(String key) {
        pageLayout.show(pageContainer, key);
        if (live != null) live.pageOpened();
    }

    /** Lets the home page's quick actions open another page. */
    public void navigateTo(String pageKey) {
        sidebar.select(pageKey);
        showPage(pageKey);
    }

    private void onLogout() {
        live.stop();
        ApiClientProvider.getInstance().getBaseApiClient().clearAuthToken();
        SessionManager.getInstance().clear();
        appFrame.showScreen(AppFrame.SCREEN_LOGIN);
    }
}