/*
 DashboardPage.java

 Patient Dashboard page

 Author: Aidan Barends (230255639)

 Date: 11th August 2026
*/

package za.ac.cput.ui.patient.pages;

import za.ac.cput.ui.layout.Refreshable;
import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.model.domain.Appointment;
import za.ac.cput.model.domain.Notification;
import za.ac.cput.model.domain.PatientTicket;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPage extends JPanel implements Refreshable {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    private final java.util.function.Consumer<String> navigate;

    private JLabel greetingLabel;
    private JLabel subtitleLabel;
    private SummaryCard pendingAppointmentsCard, activeTicketsCard, outstandingPaymentsCard, notificationsCard;
    private JPanel appointmentList;
    private JLabel appointmentEmptyLabel;
    private JPanel ticketList;
    private JLabel ticketEmptyLabel;
    private JPanel activityList;
    private JLabel activityEmptyLabel;
    private JPanel timelineBody;
    private JLabel loadErrorLabel;

    private List<Appointment> myAppointments = new ArrayList<>();
    private List<PatientTicket> myTickets = new ArrayList<>();
    private List<Notification> myNotifications = new ArrayList<>();
    private int outstandingPayments = 0;
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
                int patientId = SessionManager.getInstance().getUserId();
                ApiClientProvider api = ApiClientProvider.getInstance();

                var apptResult = api.appointments().findByPatient(patientId);
                if (apptResult.isSuccess()) {
                    myAppointments = apptResult.getData();
                } else {
                    errorMessage = "Couldn't reach the server. Make sure the backend is running.";
                    return null;
                }

                var ticketResult = api.patientTickets().findByPatientUserId(patientId);
                myTickets = ticketResult.isSuccess() && ticketResult.getData() != null
                        ? ticketResult.getData() : new ArrayList<>();

                var notifResult = api.notifications().findByPatient(patientId);
                myNotifications = notifResult.isSuccess() && notifResult.getData() != null
                        ? notifResult.getData() : new ArrayList<>();

                outstandingPayments = 0;
                var paymentResult = api.payments().getAll();
                if (paymentResult.isSuccess() && paymentResult.getData() != null) {
                    outstandingPayments = (int) paymentResult.getData().stream()
                            .filter(p -> p.getAppointment() != null
                                    && p.getAppointment().getPatient() != null
                                    && p.getAppointment().getPatient().getUserId() == patientId
                                    && "PENDING".equals(p.getPaymentStatus()))
                            .count();
                }

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
        pendingAppointmentsCard = new SummaryCard("Pending Appointments", "—", AppTheme.PRIMARY);
        activeTicketsCard       = new SummaryCard("Active Tickets",        "—", AppTheme.STATUS_INFO);
        outstandingPaymentsCard = new SummaryCard("Outstanding Payments",  "—", AppTheme.ACCENT_DARK);
        notificationsCard       = new SummaryCard("Unread Notifications",  "—", AppTheme.PRIMARY);
        JComponent[] cards = { pendingAppointmentsCard, activeTicketsCard, outstandingPaymentsCard, notificationsCard };
        for (int col = 0; col < 4; col++) {
            GridBagConstraints c = bentoConstraints(col, 0, 1, 1, gap);
            c.weighty = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            grid.add(cards[col], c);
        }

        // Row 1-2, col 0-1: Upcoming Appointments (big tile)
        grid.add(buildAppointmentsCard(), bentoConstraints(0, 1, 2, 2, gap));

        // Row 1-2, col 2: My Tickets
        grid.add(buildTicketsCard(), bentoConstraints(2, 1, 1, 2, gap));

        // Row 1, col 3: Recent Activity
        grid.add(buildActivityTile(), bentoConstraints(3, 1, 1, 1, gap));

        // Row 2, col 3: Appointment Status (timeline)
        grid.add(buildStatusTile(), bentoConstraints(3, 2, 1, 1, gap));

        // Row 3: Book Appointment shortcut, full width
        GridBagConstraints book = bentoConstraints(0, 3, 4, 1, gap);
        book.weighty = 0;
        book.fill = GridBagConstraints.HORIZONTAL;
        grid.add(buildBookAppointmentTile(), book);

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

        JLabel eyebrow = new JLabel("PATIENT PORTAL");
        eyebrow.setFont(FontManager.bodyFont(Font.BOLD, 11));
        eyebrow.setForeground(AppTheme.PRIMARY);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        greetingLabel = new JLabel("Good Morning");
        greetingLabel.setFont(FontManager.headlineFont(Font.BOLD, 30));
        greetingLabel.setForeground(AppTheme.TEXT_PRIMARY);
        greetingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        greetingLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0));

        subtitleLabel = new JLabel("Welcome back to MediTicket. Everything you need is right here.");
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
        row.add(quickAction("Book appointment", Icons.CALENDAR, true,  "APPOINTMENTS"));
        row.add(quickAction("My tickets",       Icons.TICKET,   false, "TICKETS"));
        row.add(quickAction("Payments",         Icons.CARD,     false, "PAYMENTS"));
        return row;
    }

    private JButton quickAction(String label, String icon, boolean primary, String pageKey) {
        JButton button = new JButton(label);
        AppTheme.styleActionButton(button, primary, icon);
        button.addActionListener(e -> { if (navigate != null) navigate.accept(pageKey); });
        return button;
    }

    private JComponent buildAppointmentsCard() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Upcoming Appointments");
        title.setFont(FontManager.bodyFont(Font.BOLD, 15));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_MD, 0));
        card.add(title, BorderLayout.NORTH);

        appointmentList = new JPanel();
        appointmentList.setLayout(new BoxLayout(appointmentList, BoxLayout.Y_AXIS));
        appointmentList.setOpaque(false);

        appointmentEmptyLabel = new JLabel("<html>No upcoming appointments. Use <b>Book appointment</b> above to schedule one.</html>");
        appointmentEmptyLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        appointmentEmptyLabel.setForeground(AppTheme.TEXT_MUTED);
        appointmentEmptyLabel.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, 0, 0, 0));
        appointmentEmptyLabel.setVisible(false);
        appointmentList.add(appointmentEmptyLabel);

        JScrollPane scroll = new JScrollPane(appointmentList);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        card.add(scroll, BorderLayout.CENTER);

        JButton viewAllBtn = smallLinkButton("Book New Appointment", AppTheme.PRIMARY);
        viewAllBtn.addActionListener(e -> { if (navigate != null) navigate.accept("APPOINTMENTS"); });
        card.add(wrapRight(viewAllBtn), BorderLayout.SOUTH);

        return card;
    }

    private JComponent buildTicketsCard() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("My Tickets");
        title.setFont(FontManager.bodyFont(Font.BOLD, 15));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_MD, 0));
        card.add(title, BorderLayout.NORTH);

        ticketList = new JPanel();
        ticketList.setLayout(new BoxLayout(ticketList, BoxLayout.Y_AXIS));
        ticketList.setOpaque(false);

        ticketEmptyLabel = new JLabel("<html>No active tickets. Clinic staff open tickets on your behalf after appointments.</html>");
        ticketEmptyLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        ticketEmptyLabel.setForeground(AppTheme.TEXT_MUTED);
        ticketEmptyLabel.setVisible(false);
        ticketList.add(ticketEmptyLabel);

        JScrollPane scroll = new JScrollPane(ticketList);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        card.add(scroll, BorderLayout.CENTER);

        JButton viewAllBtn = smallLinkButton("View All Tickets", AppTheme.PRIMARY);
        viewAllBtn.addActionListener(e -> { if (navigate != null) navigate.accept("TICKETS"); });
        card.add(wrapRight(viewAllBtn), BorderLayout.SOUTH);

        return card;
    }

    private JComponent buildActivityTile() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Recent Activity");
        title.setFont(FontManager.bodyFont(Font.BOLD, 15));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_SM, 0));
        card.add(title, BorderLayout.NORTH);

        activityList = new JPanel();
        activityList.setLayout(new BoxLayout(activityList, BoxLayout.Y_AXIS));
        activityList.setOpaque(false);

        activityEmptyLabel = new JLabel("Activity from appointments and tickets will appear here.");
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

        return card;
    }

    private JComponent buildStatusTile() {
        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Appointment Status");
        title.setFont(FontManager.bodyFont(Font.BOLD, 13));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_SM));

        timelineBody = new JPanel();
        timelineBody.setLayout(new BoxLayout(timelineBody, BoxLayout.Y_AXIS));
        timelineBody.setOpaque(false);
        timelineBody.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(timelineBody);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private JComponent buildBookAppointmentTile() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(AppTheme.SPACE_MD, 0));

        JPanel header = new JPanel(new BorderLayout(AppTheme.SPACE_SM, 0));
        header.setOpaque(false);
        header.add(new JLabel(Icons.get(Icons.CALENDAR, 22, AppTheme.PRIMARY)), BorderLayout.WEST);
        JLabel titleLbl = new JLabel("Book an Appointment");
        titleLbl.setFont(FontManager.bodyFont(Font.BOLD, 13));
        titleLbl.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(titleLbl, BorderLayout.CENTER);
        card.add(header, BorderLayout.WEST);

        JLabel desc = new JLabel("<html>Schedule a consultation with one of our doctors.</html>");
        desc.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        desc.setForeground(AppTheme.TEXT_SECONDARY);
        card.add(desc, BorderLayout.CENTER);

        JButton openBtn = smallLinkButton("Book Now", AppTheme.PRIMARY);
        openBtn.addActionListener(e -> { if (navigate != null) navigate.accept("APPOINTMENTS"); });
        card.add(wrapRight(openBtn), BorderLayout.EAST);

        return card;
    }

    private JComponent buildTipBanner() {
        JPanel tipCard = new JPanel(new BorderLayout(AppTheme.SPACE_SM, 0));
        tipCard.setBackground(AppTheme.PRIMARY_LIGHT);
        tipCard.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD));
        tipCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tipIcon = new JLabel(ImageManager.getIcon(ImageManager.ICON_HEARTBEAT, 26, 26));
        JLabel tipText = new JLabel("<html><b>Tip:</b> Keep your contact details up to date for faster triage and appointment confirmations.</html>");
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

        long pendingCount = myAppointments.stream()
                .filter(a -> "PENDING".equals(a.getConfirmationStatus()))
                .count();
        long activeTicketCount = myTickets.stream()
                .filter(t -> t.getCurrentStatus() != null
                        && !"RESOLVED".equals(t.getCurrentStatus())
                        && !"CLOSED".equals(t.getCurrentStatus()))
                .count();
        int unreadCount = (int) myNotifications.stream().filter(Notification::isUnread).count();

        pendingAppointmentsCard.setValue(String.valueOf(pendingCount));
        activeTicketsCard.setValue(String.valueOf(activeTicketCount));
        outstandingPaymentsCard.setValue(String.valueOf(outstandingPayments));
        notificationsCard.setValue(String.valueOf(unreadCount));

        subtitleLabel.setText(pendingCount > 0
                ? "You have " + pendingCount + " pending appointment" + (pendingCount == 1 ? "" : "s") + "."
                : "Welcome back. Everything you need is right here.");

        rebuildAppointmentList();
        rebuildTicketList();
        rebuildActivity();
        rebuildTimeline();
    }

    private void rebuildAppointmentList() {
        appointmentList.removeAll();

        List<Appointment> upcoming = myAppointments.stream()
                .filter(a -> a.getAppointmentDate() != null && !a.getAppointmentDate().isBefore(LocalDate.now()))
                .filter(a -> !"CANCELLED".equals(a.getConfirmationStatus()) && !"REJECTED".equals(a.getConfirmationStatus()))
                .sorted(Comparator.comparing(Appointment::getAppointmentDate))
                .collect(Collectors.toList());

        for (Appointment appt : upcoming) {
            appointmentList.add(buildAppointmentRow(appt));
            appointmentList.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        }

        boolean empty = upcoming.isEmpty();
        appointmentEmptyLabel.setVisible(empty);
        if (empty) appointmentList.add(appointmentEmptyLabel);

        appointmentList.revalidate();
        appointmentList.repaint();
    }

    private JComponent buildAppointmentRow(Appointment appt) {
        JPanel row = new JPanel(new BorderLayout(AppTheme.SPACE_SM, 2));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.DIVIDER),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, 0, AppTheme.SPACE_SM, 0)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        String reason = appt.getReason() != null && !appt.getReason().isBlank()
                ? appt.getReason() : "General Consultation";
        JLabel reasonLabel = new JLabel(reason);
        reasonLabel.setFont(FontManager.bodyFont(Font.BOLD, 13));
        reasonLabel.setForeground(AppTheme.TEXT_PRIMARY);

        String dateStr = appt.getAppointmentDate() != null ? appt.getAppointmentDate().format(DATE_FMT) : "Date TBD";
        String timeStr = appt.getAppointmentTime() != null ? appt.getAppointmentTime().format(TIME_FMT) : "";
        JLabel meta = new JLabel(dateStr + (timeStr.isEmpty() ? "" : "  â€¢  " + timeStr));
        meta.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        meta.setForeground(AppTheme.TEXT_SECONDARY);

        textCol.add(reasonLabel);
        textCol.add(meta);

        row.add(textCol, BorderLayout.CENTER);
        row.add(wrapRight(new StatusBadge(appt.getConfirmationStatus())), BorderLayout.EAST);
        return row;
    }

    private void rebuildTicketList() {
        ticketList.removeAll();

        List<PatientTicket> active = myTickets.stream()
                .filter(t -> t.getCurrentStatus() != null
                        && !"RESOLVED".equals(t.getCurrentStatus())
                        && !"CLOSED".equals(t.getCurrentStatus()))
                .sorted(Comparator.comparing(
                        t -> t.getTicketCreatedDate() != null
                                ? t.getTicketCreatedDate() : java.time.LocalDateTime.MAX))
                .collect(Collectors.toList());

        for (PatientTicket ticket : active) {
            ticketList.add(buildTicketRow(ticket));
            ticketList.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        }

        boolean empty = active.isEmpty();
        ticketEmptyLabel.setVisible(empty);
        if (empty) ticketList.add(ticketEmptyLabel);

        ticketList.revalidate();
        ticketList.repaint();
    }

    private JComponent buildTicketRow(PatientTicket ticket) {
        JPanel row = new JPanel(new BorderLayout(AppTheme.SPACE_SM, 2));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.DIVIDER),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, 0, AppTheme.SPACE_SM, 0)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        String desc = ticket.getTicketDescription() != null && !ticket.getTicketDescription().isBlank()
                ? ticket.getTicketDescription() : "No description";
        JLabel textLabel = new JLabel("TK-" + String.format("%03d", ticket.getTicketId()) + "  â€¢  " + desc);
        textLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        textLabel.setForeground(AppTheme.TEXT_PRIMARY);

        row.add(textLabel, BorderLayout.CENTER);
        row.add(wrapRight(new StatusBadge(ticket.getCurrentStatus())), BorderLayout.EAST);
        return row;
    }

    private void rebuildActivity() {
        activityList.removeAll();

        List<Notification> sorted = new ArrayList<>(myNotifications);
        sorted.sort(Comparator.comparing(Notification::getNotificationDate,
                Comparator.nullsLast(Comparator.reverseOrder())));

        int shown = 0;
        for (Notification n : sorted) {
            if (shown >= 3) break;
            activityList.add(buildActivityItem(n));
            activityList.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
            shown++;
        }

        activityEmptyLabel.setVisible(shown == 0);
        if (shown == 0) activityList.add(activityEmptyLabel);

        activityList.revalidate();
        activityList.repaint();
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

    private void rebuildTimeline() {
        timelineBody.removeAll();

        Optional<Appointment> nextOpt = myAppointments.stream()
                .filter(a -> a.getAppointmentDate() != null && !a.getAppointmentDate().isBefore(LocalDate.now()))
                .filter(a -> !"CANCELLED".equals(a.getConfirmationStatus())
                        && !"REJECTED".equals(a.getConfirmationStatus())
                        && !"COMPLETED".equals(a.getConfirmationStatus()))
                .min(Comparator.comparing(Appointment::getAppointmentDate));

        if (nextOpt.isEmpty()) {
            JLabel empty = new JLabel("<html>No upcoming appointments.</html>");
            empty.setFont(FontManager.bodyFont(Font.PLAIN, 12));
            empty.setForeground(AppTheme.TEXT_MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            timelineBody.add(empty);
        } else {
            String status = nextOpt.get().getConfirmationStatus();
            boolean confirmed = "CONFIRMED".equals(status) || "COMPLETED".equals(status);
            boolean completed  = "COMPLETED".equals(status);

            List<Object[]> steps = List.of(
                    new Object[]{"Requested",    true},
                    new Object[]{"Under Review", "PENDING".equals(status) || confirmed || completed},
                    new Object[]{"Confirmed",    confirmed || completed},
                    new Object[]{"Completed",    completed}
            );
            for (int i = 0; i < steps.size(); i++) {
                Object[] step = steps.get(i);
                timelineBody.add(timelineStep((String) step[0], (boolean) step[1], i == steps.size() - 1));
            }
        }

        timelineBody.revalidate();
        timelineBody.repaint();
    }

    private JComponent timelineStep(String label, boolean done, boolean isLast) {
        JPanel row = new JPanel(new BorderLayout(AppTheme.SPACE_SM, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, isLast ? 20 : 30));
        row.add(timelineRail(done, isLast), BorderLayout.WEST);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FontManager.bodyFont(done ? Font.BOLD : Font.PLAIN, 11));
        lbl.setForeground(done ? AppTheme.TEXT_PRIMARY : AppTheme.TEXT_MUTED);
        row.add(lbl, BorderLayout.CENTER);
        return row;
    }

    private JComponent timelineRail(boolean done, boolean isLast) {
        JPanel rail = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int dotSize = 10;
                int cx = getWidth() / 2;
                int cy = 2;
                if (!isLast) {
                    g2.setColor(done ? AppTheme.PRIMARY_LIGHT : AppTheme.DIVIDER);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawLine(cx, cy + dotSize, cx, getHeight());
                }
                if (done) {
                    g2.setColor(AppTheme.PRIMARY);
                    g2.fillOval(cx - dotSize / 2, cy, dotSize, dotSize);
                } else {
                    g2.setColor(AppTheme.SURFACE);
                    g2.fillOval(cx - dotSize / 2, cy, dotSize, dotSize);
                    g2.setColor(AppTheme.BORDER);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawOval(cx - dotSize / 2, cy, dotSize, dotSize);
                }
                g2.dispose();
            }
        };
        rail.setOpaque(false);
        rail.setPreferredSize(new Dimension(18, isLast ? 20 : 30));
        return rail;
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

    private String timeOfDayGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Morning";
        if (hour < 17) return "Afternoon";
        return "Evening";
    }
}
