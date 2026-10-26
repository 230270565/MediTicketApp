/*
 DashboardPage.java

 Admin Dashboard page

 Author: Matthew Barron (230398863)

 Date: 11th August 2026
*/

package za.ac.cput.ui.clinicstaff.admin.pages;

import za.ac.cput.ui.layout.Refreshable;
import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.auth.EmployeeAccessRequest;
import za.ac.cput.model.domain.Appointment;
import za.ac.cput.model.domain.ClinicStaff;
import za.ac.cput.model.domain.Doctor;
import za.ac.cput.model.domain.Notification;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.clinicstaff.components.SummaryCard;
import za.ac.cput.ui.patient.components.StatusBadge;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.ImageManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DashboardPage extends JPanel implements Refreshable {

    private final java.util.function.Consumer<String> navigate;

    private JLabel greetingLabel;
    private JLabel subtitleLabel;
    private SummaryCard staffCard, invitesCard, appointmentsCard, notificationsCard;
    private JPanel activityList;
    private JLabel activityEmptyLabel;
    private JPanel invitationList;
    private JLabel invitationEmptyLabel;
    private JPanel recentActivityList;
    private JLabel recentActivityEmptyLabel;
    private JLabel doctorCountLabel, nurseCountLabel, totalStaffLabel;
    private JLabel loadErrorLabel;

    private List<Appointment> allAppointments = new ArrayList<>();
    private List<Doctor> allDoctors = new ArrayList<>();
    private List<ClinicStaff> allStaff = new ArrayList<>();
    private List<EmployeeAccessRequest> pendingRequests = new ArrayList<>();
    private List<Notification> myNotifications = new ArrayList<>();
    private boolean loading = false;

    public DashboardPage() {
        this(null);
    }

    public DashboardPage(java.util.function.Consumer<String> navigate) {
        this.navigate = navigate;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(buildContent());
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(AppTheme.BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        refresh();
    }

    @Override
    public void refresh() {
        if (loading) return;
        loading = true;
        loadErrorLabel.setVisible(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String errorMessage = null;

            @Override
            protected Void doInBackground() {
                ApiClientProvider api = ApiClientProvider.getInstance();

                BaseApiClient.ApiResult<List<Appointment>> apptResult = api.appointments().getAll();
                if (apptResult.isSuccess()) {
                    allAppointments = apptResult.getData();
                } else {
                    errorMessage = "Couldn't reach the server. Make sure the backend is running.";
                    return null;
                }

                BaseApiClient.ApiResult<List<Doctor>> doctorResult = api.doctors().getAll();
                allDoctors = doctorResult.isSuccess() && doctorResult.getData() != null
                        ? doctorResult.getData() : new ArrayList<>();

                BaseApiClient.ApiResult<List<ClinicStaff>> staffResult = api.clinicStaff().getAll();
                allStaff = staffResult.isSuccess() && staffResult.getData() != null
                        ? staffResult.getData() : new ArrayList<>();

                BaseApiClient.ApiResult<List<EmployeeAccessRequest>> requestResult =
                        api.auth().getAccessRequests("PENDING");
                pendingRequests = requestResult.isSuccess() && requestResult.getData() != null
                        ? requestResult.getData() : new ArrayList<>();

                int userId = SessionManager.getInstance().getUserId();
                BaseApiClient.ApiResult<List<Notification>> notifResult =
                        api.notifications().findByClinicStaff(userId);
                myNotifications = notifResult.isSuccess() && notifResult.getData() != null
                        ? notifResult.getData() : new ArrayList<>();

                return null;
            }

            @Override
            protected void done() {
                loading = false;
                if (errorMessage != null) {
                    loadErrorLabel.setText(errorMessage);
                    loadErrorLabel.setVisible(true);
                    return;
                }
                applyData();
            }
        };
        worker.execute();
    }

    // -- Layout ----------------------------------------------------------

    private JComponent buildContent() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(AppTheme.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_XL, AppTheme.SPACE_LG));

        root.add(noVStretch(buildGreetingSection()));
        root.add(Box.createVerticalStrut(AppTheme.SPACE_LG));
        root.add(noVStretch(buildQuickActions()));
        root.add(Box.createVerticalStrut(AppTheme.SPACE_LG));

        loadErrorLabel = new JLabel(" ");
        loadErrorLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        loadErrorLabel.setForeground(AppTheme.STATUS_DANGER);
        loadErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadErrorLabel.setVisible(false);
        root.add(loadErrorLabel);

        root.add(buildBentoGrid());
        return root;
    }

    private JComponent buildBentoGrid() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        int gap = AppTheme.SPACE_MD;

        // Row 0: four summary cards
        staffCard         = new SummaryCard("Staff",               "—", AppTheme.PRIMARY);
        invitesCard       = new SummaryCard("Pending Invitations", "—", AppTheme.STATUS_WARNING);
        appointmentsCard  = new SummaryCard("Appointments",        "—", AppTheme.STATUS_INFO);
        notificationsCard = new SummaryCard("Unread Notifications","—", AppTheme.PRIMARY);
        JComponent[] cards = { staffCard, invitesCard, appointmentsCard, notificationsCard };
        for (int col = 0; col < 4; col++) {
            GridBagConstraints c = bentoConstraints(col, 0, 1, 1, gap);
            c.weighty = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            grid.add(cards[col], c);
        }

        // Row 1-2, col 0-1: Today's Clinic Activity (big tile)
        grid.add(buildClinicActivityCard(), bentoConstraints(0, 1, 2, 2, gap));

        // Row 1-2, col 2: Pending Invitations (individual request rows)
        grid.add(buildInvitationsCard(), bentoConstraints(2, 1, 1, 2, gap));

        // Row 1, col 3: Recent Activity
        grid.add(buildRecentActivityTile(), bentoConstraints(3, 1, 1, 1, gap));

        // Row 2, col 3: Staff Overview
        grid.add(buildStaffOverviewTile(), bentoConstraints(3, 2, 1, 1, gap));

        // Row 3: Manage Staff shortcut, full width
        GridBagConstraints manage = bentoConstraints(0, 3, 4, 1, gap);
        manage.weighty = 0;
        manage.fill = GridBagConstraints.HORIZONTAL;
        grid.add(buildManageStaffTile(), manage);

        // Row 4: tip banner, full width
        GridBagConstraints tip = bentoConstraints(0, 4, 4, 1, gap);
        tip.weighty = 0;
        tip.fill = GridBagConstraints.HORIZONTAL;
        grid.add(buildTipBanner(), tip);

        return grid;
    }

    private GridBagConstraints bentoConstraints(int gridx, int gridy, int gridwidth, int gridheight, int gap) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridx;
        c.gridy = gridy;
        c.gridwidth = gridwidth;
        c.gridheight = gridheight;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, gap, gap);
        return c;
    }

    private JComponent buildGreetingSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel eyebrow = new JLabel("CLINIC ADMINISTRATION");
        eyebrow.setFont(FontManager.bodyFont(Font.BOLD, 11));
        eyebrow.setForeground(AppTheme.PRIMARY);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        greetingLabel = new JLabel("Good Morning");
        greetingLabel.setFont(FontManager.headlineFont(Font.BOLD, 30));
        greetingLabel.setForeground(AppTheme.TEXT_PRIMARY);
        greetingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        greetingLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0));

        subtitleLabel = new JLabel("Here's an overview of today's clinic activity.");
        subtitleLabel.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(eyebrow);
        panel.add(greetingLabel);
        panel.add(subtitleLabel);
        return panel;
    }

    private JComponent buildQuickActions() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_SM, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(quickAction("Invite staff",  Icons.CLIPBOARD, true,  "ONBOARDING"));
        row.add(quickAction("Appointments",  Icons.CALENDAR,  false, "APPOINTMENTS"));
        row.add(quickAction("Reports",       Icons.CHART,     false, "REPORTS"));
        return row;
    }

    private JButton quickAction(String label, String icon, boolean primary, String pageKey) {
        JButton button = new JButton(label);
        AppTheme.styleActionButton(button, primary, icon);
        button.addActionListener(e -> { if (navigate != null) navigate.accept(pageKey); });
        return button;
    }

    private JComponent buildClinicActivityCard() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Today's Clinic Activity");
        title.setFont(FontManager.bodyFont(Font.BOLD, 15));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_MD, 0));
        card.add(title, BorderLayout.NORTH);

        activityList = new JPanel();
        activityList.setLayout(new BoxLayout(activityList, BoxLayout.Y_AXIS));
        activityList.setOpaque(false);

        activityEmptyLabel = new JLabel("No appointments scheduled for today.");
        activityEmptyLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        activityEmptyLabel.setForeground(AppTheme.TEXT_MUTED);
        activityEmptyLabel.setVisible(false);
        activityList.add(activityEmptyLabel);

        JScrollPane scroll = new JScrollPane(activityList);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        card.add(scroll, BorderLayout.CENTER);

        JButton viewAllBtn = smallLinkButton("View All Appointments", AppTheme.PRIMARY);
        viewAllBtn.addActionListener(e -> { if (navigate != null) navigate.accept("APPOINTMENTS"); });
        card.add(wrapRight(viewAllBtn), BorderLayout.SOUTH);

        return card;
    }

    private JComponent buildInvitationsCard() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Pending Invitations");
        title.setFont(FontManager.bodyFont(Font.BOLD, 15));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_MD, 0));
        card.add(title, BorderLayout.NORTH);

        invitationList = new JPanel();
        invitationList.setLayout(new BoxLayout(invitationList, BoxLayout.Y_AXIS));
        invitationList.setOpaque(false);

        invitationEmptyLabel = new JLabel("<html>No pending requests. Staff request access from the login screen.</html>");
        invitationEmptyLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        invitationEmptyLabel.setForeground(AppTheme.TEXT_MUTED);
        invitationEmptyLabel.setVisible(false);
        invitationList.add(invitationEmptyLabel);

        JScrollPane scroll = new JScrollPane(invitationList);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        card.add(scroll, BorderLayout.CENTER);

        JButton viewAllBtn = smallLinkButton("Go to Onboarding", AppTheme.PRIMARY);
        viewAllBtn.addActionListener(e -> { if (navigate != null) navigate.accept("ONBOARDING"); });
        card.add(wrapRight(viewAllBtn), BorderLayout.SOUTH);

        return card;
    }

    private JComponent buildRecentActivityTile() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Recent Activity");
        title.setFont(FontManager.bodyFont(Font.BOLD, 15));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_SM, 0));
        card.add(title, BorderLayout.NORTH);

        recentActivityList = new JPanel();
        recentActivityList.setLayout(new BoxLayout(recentActivityList, BoxLayout.Y_AXIS));
        recentActivityList.setOpaque(false);

        recentActivityEmptyLabel = new JLabel("Notifications and clinic events will appear here.");
        recentActivityEmptyLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        recentActivityEmptyLabel.setForeground(AppTheme.TEXT_MUTED);
        recentActivityEmptyLabel.setVisible(false);
        recentActivityList.add(recentActivityEmptyLabel);

        JScrollPane scroll = new JScrollPane(recentActivityList);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JComponent buildStaffOverviewTile() {
        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Staff Overview");
        title.setFont(FontManager.bodyFont(Font.BOLD, 13));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_SM));

        doctorCountLabel = staffStatRow("Doctors",    "—", null, card);
        nurseCountLabel   = staffStatRow("Nurses",     "—", null, card);
        totalStaffLabel   = staffStatRow("Total Staff","—", AppTheme.PRIMARY, card);

        card.add(Box.createVerticalGlue());
        return card;
    }

    // Adds a stat row to the parent panel and returns the value label so it can be updated later.
    private JLabel staffStatRow(String label, String value, Color valueColor, JPanel parent) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_XS, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        lbl.setForeground(AppTheme.TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setFont(FontManager.bodyFont(Font.BOLD, 13));
        val.setForeground(valueColor != null ? valueColor : AppTheme.TEXT_PRIMARY);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);

        parent.add(row);
        return val;
    }

    private JComponent buildManageStaffTile() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(AppTheme.SPACE_MD, 0));

        JPanel header = new JPanel(new BorderLayout(AppTheme.SPACE_SM, 0));
        header.setOpaque(false);
        header.add(new JLabel(Icons.get(Icons.USERS, 22, AppTheme.PRIMARY)), BorderLayout.WEST);
        JLabel titleLbl = new JLabel("Staff Directory");
        titleLbl.setFont(FontManager.bodyFont(Font.BOLD, 13));
        titleLbl.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(titleLbl, BorderLayout.CENTER);
        card.add(header, BorderLayout.WEST);

        JLabel desc = new JLabel("<html>View and manage all doctors, nurses, and admin staff.</html>");
        desc.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        desc.setForeground(AppTheme.TEXT_SECONDARY);
        card.add(desc, BorderLayout.CENTER);

        JButton openBtn = smallLinkButton("Open Staff", AppTheme.PRIMARY);
        openBtn.addActionListener(e -> { if (navigate != null) navigate.accept("STAFF"); });
        card.add(wrapRight(openBtn), BorderLayout.EAST);

        return card;
    }

    private JComponent buildTipBanner() {
        JPanel tipCard = new JPanel(new BorderLayout(AppTheme.SPACE_SM, 0));
        tipCard.setBackground(AppTheme.PRIMARY_LIGHT);
        tipCard.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD));
        tipCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tipIcon = new JLabel(ImageManager.getIcon(ImageManager.ICON_HEARTBEAT, 26, 26));
        JLabel tipText = new JLabel("<html><b>Tip:</b> Review pending invitations regularly so new staff can complete onboarding promptly.</html>");
        tipText.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        tipText.setForeground(AppTheme.PRIMARY_DARK);

        tipCard.add(tipIcon, BorderLayout.WEST);
        tipCard.add(tipText, BorderLayout.CENTER);
        return tipCard;
    }

    private JPanel cardPanel() {
        JPanel card = new JPanel();
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD)
        ));
        return card;
    }

    // Wraps a component so BoxLayout Y_AXIS won't vertically stretch it beyond its preferred height.
    private JComponent noVStretch(JComponent c) {
        JPanel w = new JPanel(new BorderLayout()) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        w.setOpaque(false);
        w.setAlignmentX(Component.LEFT_ALIGNMENT);
        w.add(c, BorderLayout.CENTER);
        return w;
    }

    // -- Data application ------------------------------------------------

    private void applyData() {
        String name = SessionManager.getInstance().getFullName();
        String firstName = name != null && !name.isBlank() ? name.split(" ")[0] : "there";
        greetingLabel.setText("Good " + timeOfDayGreeting() + ", " + firstName);

        int totalStaff = allDoctors.size() + allStaff.size();
        int unreadCount = (int) myNotifications.stream().filter(Notification::isUnread).count();

        staffCard.setValue(String.valueOf(totalStaff));
        invitesCard.setValue(String.valueOf(pendingRequests.size()));
        appointmentsCard.setValue(String.valueOf(allAppointments.size()));
        notificationsCard.setValue(String.valueOf(unreadCount));

        subtitleLabel.setText(pendingRequests.isEmpty()
                ? "Here's an overview of today's clinic activity."
                : "You have " + pendingRequests.size() + " pending invitation" + (pendingRequests.size() == 1 ? "" : "s") + " awaiting review.");

        long nurseCount = allStaff.stream().filter(s -> "NURSE".equals(s.getStaffRole())).count();
        doctorCountLabel.setText(String.valueOf(allDoctors.size()));
        nurseCountLabel.setText(String.valueOf(nurseCount));
        totalStaffLabel.setText(String.valueOf(totalStaff));

        rebuildClinicActivity();
        rebuildInvitationList();
        rebuildRecentActivity();
    }

    private void rebuildClinicActivity() {
        activityList.removeAll();

        LocalDate today = LocalDate.now();
        List<Appointment> todaysAppts = allAppointments.stream()
                .filter(a -> today.equals(a.getAppointmentDate()))
                .sorted(Comparator.comparing(a -> a.getAppointmentTime() != null
                        ? a.getAppointmentTime() : java.time.LocalTime.MIN))
                .toList();

        // Stats header row
        long pending   = allAppointments.stream().filter(a -> "PENDING".equals(a.getConfirmationStatus())).count();
        long completed = allAppointments.stream().filter(a -> "COMPLETED".equals(a.getConfirmationStatus())).count();
        long cancelled = allAppointments.stream().filter(a -> "CANCELLED".equals(a.getConfirmationStatus())).count();

        activityList.add(buildStatRow("Today's Appointments", String.valueOf(todaysAppts.size()), AppTheme.PRIMARY));
        activityList.add(buildStatRow("Pending",    String.valueOf(pending),   AppTheme.STATUS_WARNING));
        activityList.add(buildStatRow("Completed",  String.valueOf(completed), AppTheme.STATUS_SUCCESS));
        activityList.add(buildStatRow("Cancelled",  String.valueOf(cancelled), AppTheme.STATUS_DANGER));

        if (!todaysAppts.isEmpty()) {
            JSeparator sep = new JSeparator();
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            sep.setForeground(AppTheme.DIVIDER);
            activityList.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
            activityList.add(sep);
            activityList.add(Box.createVerticalStrut(AppTheme.SPACE_SM));

            for (Appointment appt : todaysAppts) {
                activityList.add(buildAppointmentRow(appt));
                activityList.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
            }
        }

        boolean empty = allAppointments.isEmpty();
        activityEmptyLabel.setVisible(empty);
        if (empty) activityList.add(activityEmptyLabel);

        activityList.revalidate();
        activityList.repaint();
    }

    private JComponent buildStatRow(String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_XS, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        lbl.setForeground(AppTheme.TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setFont(FontManager.bodyFont(Font.BOLD, 13));
        val.setForeground(valueColor);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JComponent buildAppointmentRow(Appointment appt) {
        JPanel row = new JPanel(new BorderLayout(AppTheme.SPACE_SM, 2));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.DIVIDER),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, 0, AppTheme.SPACE_SM, 0)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        String patient = appt.getPatient() != null && appt.getPatient().getName() != null
                ? appt.getPatient().getName().getFirstName() + " " + appt.getPatient().getName().getLastName()
                : "Unknown patient";
        JLabel nameLabel = new JLabel(patient.trim());
        nameLabel.setFont(FontManager.bodyFont(Font.BOLD, 13));
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);

        String doctor = appt.getDoctor() != null && appt.getDoctor().getName() != null
                ? "Dr. " + appt.getDoctor().getName().getLastName() : "Doctor TBD";
        String timeStr = appt.getAppointmentTime() != null
                ? appt.getAppointmentTime().format(java.time.format.DateTimeFormatter.ofPattern("h:mm a")) : "";
        JLabel meta = new JLabel(doctor + (timeStr.isEmpty() ? "" : "  â€¢  " + timeStr));
        meta.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        meta.setForeground(AppTheme.TEXT_SECONDARY);

        textCol.add(nameLabel);
        textCol.add(meta);

        row.add(textCol, BorderLayout.CENTER);
        row.add(wrapRight(new StatusBadge(appt.getConfirmationStatus())), BorderLayout.EAST);

        return row;
    }

    private void rebuildInvitationList() {
        invitationList.removeAll();

        List<EmployeeAccessRequest> requests = pendingRequests.stream()
                .sorted(Comparator.comparing(
                        r -> r.getRequestDate() != null ? r.getRequestDate() : java.time.LocalDateTime.MIN,
                        Comparator.reverseOrder()))
                .toList();

        for (EmployeeAccessRequest req : requests) {
            invitationList.add(buildInvitationRow(req));
            invitationList.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        }

        boolean empty = requests.isEmpty();
        invitationEmptyLabel.setVisible(empty);
        if (empty) invitationList.add(invitationEmptyLabel);

        invitationList.revalidate();
        invitationList.repaint();
    }

    private JComponent buildInvitationRow(EmployeeAccessRequest req) {
        JPanel row = new JPanel(new BorderLayout(AppTheme.SPACE_SM, 2));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.DIVIDER),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, 0, AppTheme.SPACE_SM, 0)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        JLabel emailLabel = new JLabel(req.getEmail() != null ? req.getEmail() : "—");
        emailLabel.setFont(FontManager.bodyFont(Font.BOLD, 13));
        emailLabel.setForeground(AppTheme.TEXT_PRIMARY);

        String roleText = "DOCTOR".equals(req.getRequestedUserType()) ? "Doctor"
                : req.getRequestedStaffRole() != null ? roleDisplayName(req.getRequestedStaffRole()) : "Clinic Staff";
        JLabel roleLabel = new JLabel(roleText);
        roleLabel.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        roleLabel.setForeground(AppTheme.TEXT_SECONDARY);

        textCol.add(emailLabel);
        textCol.add(roleLabel);

        row.add(textCol, BorderLayout.CENTER);
        row.add(wrapRight(new StatusBadge("PENDING")), BorderLayout.EAST);
        return row;
    }

    private void rebuildRecentActivity() {
        recentActivityList.removeAll();

        List<Notification> sorted = new ArrayList<>(myNotifications);
        sorted.sort(Comparator.comparing(Notification::getNotificationDate,
                Comparator.nullsLast(Comparator.reverseOrder())));

        int shown = 0;
        for (Notification n : sorted) {
            if (shown >= 3) break;
            recentActivityList.add(buildActivityItem(n));
            recentActivityList.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
            shown++;
        }

        recentActivityEmptyLabel.setVisible(shown == 0);
        if (shown == 0) recentActivityList.add(recentActivityEmptyLabel);

        recentActivityList.revalidate();
        recentActivityList.repaint();
    }

    private JComponent buildActivityItem(Notification n) {
        JPanel item = new JPanel();
        item.setOpaque(false);
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        String text = n.getNotificationMessage() != null ? n.getNotificationMessage() : "Notification";
        JLabel message = new JLabel("<html>" + text.replace("&", "&amp;").replace("<", "&lt;") + "</html>");
        message.setFont(FontManager.bodyFont(n.isUnread() ? Font.BOLD : Font.PLAIN, 12));
        message.setForeground(AppTheme.TEXT_PRIMARY);
        message.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel when = new JLabel(za.ac.cput.ui.layout.NotificationCard.timeAgo(n.getNotificationDate()));
        when.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        when.setForeground(AppTheme.TEXT_MUTED);
        when.setAlignmentX(Component.LEFT_ALIGNMENT);

        item.add(message);
        item.add(when);
        return item;
    }

    // -- Helpers ---------------------------------------------------------

    private JComponent wrapRight(JComponent comp) {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.add(comp);
        return wrap;
    }

    private JButton smallLinkButton(String text, Color color) {
        JButton btn = new JButton(text);
        AppTheme.styleLinkButton(btn);
        btn.setForeground(color);
        return btn;
    }

    private String roleDisplayName(String role) {
        return switch (role) {
            case "NURSE" -> "Nurse";
            case "ADMIN" -> "Admin";
            case "RECEPTIONIST" -> "Receptionist";
            default -> role;
        };
    }

    private String timeOfDayGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Morning";
        if (hour < 17) return "Afternoon";
        return "Evening";
    }
}
