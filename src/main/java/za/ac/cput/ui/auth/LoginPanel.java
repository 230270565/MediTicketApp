/*
 LoginPanel.java

 Login screen — split hero + form layout matching the Stitch design.

 Author: Joshua Reid Adams (230317693)

 Date: 11th August 2026
*/

package za.ac.cput.ui.auth;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.auth.AuthResponse;
import za.ac.cput.model.auth.LoginRequest;
import za.ac.cput.security.JwtUtil;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.AppFrame;
import za.ac.cput.ui.auth.components.LabeledPasswordField;
import za.ac.cput.ui.auth.components.LabeledTextField;
import za.ac.cput.ui.auth.components.PrimaryButton;
import za.ac.cput.ui.clinicstaff.admin.AdminDashboard;
import za.ac.cput.ui.clinicstaff.nurse.NurseDashboard;
import za.ac.cput.ui.doctor.DoctorDashboard;
import za.ac.cput.ui.patient.PatientDashboard;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.ImageManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginPanel extends JPanel {

    private final AppFrame appFrame;

    private LabeledTextField emailField;
    private LabeledPasswordField passwordField;
    private JLabel errorLabel;
    private PrimaryButton signInButton;

    public LoginPanel(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new GridLayout(1, 2));
        add(buildHeroSection());
        add(buildFormSection());
    }

    // ── Hero (left) ───────────────────────────────────────────────────────────

    private JComponent buildHeroSection() {
        JPanel hero = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Background photo
                Image bg = ImageManager.getImage(ImageManager.HERO_BACKGROUND);
                if (bg != null) {
                    g2.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                }
                // Dark overlay
                g2.setColor(new Color(0, 0, 0, 130));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Teal gradient from bottom (subtle brand wash)
                GradientPaint tealGradient = new GradientPaint(
                        0, getHeight(), new Color(14, 124, 134, 100),
                        0, getHeight() / 2, new Color(14, 124, 134, 0));
                g2.setPaint(tealGradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        hero.setOpaque(false);

        // Top bar: logo + wordmark
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(24, 28, 0, 28));

        ImageIcon logoIcon = ImageManager.getIcon(ImageManager.LOGO_ICON, 32, 32);
        if (logoIcon != null) {
            JLabel logoImg = new JLabel(logoIcon);
            topBar.add(logoImg);
            topBar.add(Box.createHorizontalStrut(10));
        }
        JPanel wordmark = new JPanel();
        wordmark.setLayout(new BoxLayout(wordmark, BoxLayout.Y_AXIS));
        wordmark.setOpaque(false);
        JLabel appName = new JLabel("MediTicket");
        appName.setFont(FontManager.headlineFont(Font.BOLD, 16));
        appName.setForeground(Color.WHITE);
        JLabel appSub = new JLabel("CLINICAL HEALTHCARE SUITE");
        appSub.setFont(FontManager.bodyFont(Font.BOLD, 9));
        appSub.setForeground(AppTheme.PRIMARY);
        wordmark.add(appName);
        wordmark.add(appSub);
        topBar.add(wordmark);

        hero.add(topBar, BorderLayout.NORTH);

        // Centre content
        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setOpaque(false);
        centre.setBorder(BorderFactory.createEmptyBorder(0, 36, 0, 36));

        // "Next-Generation Care Management" chip
        JPanel chip = buildHeroChip("Next-Generation Care Management");
        chip.setAlignmentX(Component.LEFT_ALIGNMENT);
        centre.add(chip);
        centre.add(Box.createVerticalStrut(16));

        JLabel headline = new JLabel("<html><b>Care,<br>Coordinated.</b></html>");
        headline.setFont(FontManager.headlineFont(Font.BOLD, 34));
        headline.setForeground(Color.WHITE);
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);
        centre.add(headline);
        centre.add(Box.createVerticalStrut(8));

        JLabel tagline = new JLabel("<html><i>Precision in every consult.</i></html>");
        tagline.setFont(FontManager.headlineFont(Font.BOLD, 20));
        tagline.setForeground(AppTheme.PRIMARY);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        centre.add(tagline);
        centre.add(Box.createVerticalStrut(16));

        JLabel description = new JLabel(
                "<html><div style='width:280px;color:#CCCCCC;font-size:13px;line-height:1.5;'>"
                + "Welcome to the unified healthcare workspace. Streamline appointments, "
                + "monitor live clinical triage queues, and access comprehensive digital "
                + "health records in one verified platform."
                + "</div></html>");
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        centre.add(description);
        centre.add(Box.createVerticalStrut(28));

        // Feature chips
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        chips.setOpaque(false);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        chips.add(buildFeatureChip("✓ Rapid Triage & Ticketing"));
        chips.add(buildFeatureChip("✓ POPIA & HIPAA Compliant"));
        chips.add(buildFeatureChip("⇄ Real-Time Queue Tracking"));
        centre.add(chips);

        // Wrapper to vertically centre the content block
        JPanel centreWrapper = new JPanel(new GridBagLayout());
        centreWrapper.setOpaque(false);
        centreWrapper.add(centre);
        hero.add(centreWrapper, BorderLayout.CENTER);

        // Bottom footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 28, 20, 28));

        JLabel partnerLabel = new JLabel("St. Jude & Regional Partner Hospital Network");
        partnerLabel.setFont(FontManager.bodyFont(Font.PLAIN, 10));
        partnerLabel.setForeground(new Color(170, 170, 170));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        statusPanel.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(0x34D399));
                g.fillOval(0, 2, 8, 8);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(8, 12); }
        };
        dot.setOpaque(false);
        statusPanel.add(dot);
        JLabel versionLabel = new JLabel("System Online  •  Version 3.4.1");
        versionLabel.setFont(FontManager.bodyFont(Font.PLAIN, 10));
        versionLabel.setForeground(new Color(170, 170, 170));
        statusPanel.add(versionLabel);

        footer.add(partnerLabel, BorderLayout.WEST);
        footer.add(statusPanel, BorderLayout.EAST);
        hero.add(footer, BorderLayout.SOUTH);

        return hero;
    }

    private JPanel buildHeroChip(String text) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(14, 124, 134, 60));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.setColor(AppTheme.PRIMARY);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setOpaque(false);
        chip.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        // Small teal dot
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(AppTheme.PRIMARY);
                g.fillOval(0, 1, 6, 6);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(8, 8); }
        };
        dot.setOpaque(false);
        chip.add(dot);
        chip.add(Box.createHorizontalStrut(6));
        JLabel lbl = new JLabel(text);
        lbl.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        lbl.setForeground(AppTheme.PRIMARY);
        chip.add(lbl);
        return chip;
    }

    private JPanel buildFeatureChip(String text) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setOpaque(false);
        chip.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        JLabel lbl = new JLabel(text);
        lbl.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        lbl.setForeground(new Color(200, 200, 200));
        chip.add(lbl);
        return chip;
    }

    // ── Form (right) ──────────────────────────────────────────────────────────

    private JComponent buildFormSection() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(AppTheme.SURFACE);

        // Top bar: "Need Support?" right-aligned
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 16));
        topBar.setBackground(AppTheme.SURFACE);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER));
        JLabel supportLabel = new JLabel("🛎  Need Support?");
        supportLabel.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        supportLabel.setForeground(AppTheme.TEXT_SECONDARY);
        supportLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topBar.add(supportLabel);
        outer.add(topBar, BorderLayout.NORTH);

        // Centre: the form
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(AppTheme.SURFACE);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(380, 480));

        JLabel title = new JLabel("Welcome Back");
        title.setFont(FontManager.headlineFont(Font.BOLD, 28));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "<html><span style='color:#0E7C86;'>Sign in</span> to access your clinical dashboard, tickets,<br>and health records.</html>");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(6, 0, AppTheme.SPACE_LG, 0));

        emailField = new LabeledTextField("Email Address");
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        passwordField = new LabeledPasswordField("Password");
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        passwordField.onForgotPasswordClick(this::onForgotPassword);

        // Remember checkbox
        JCheckBox rememberMe = new JCheckBox("Remember this workstation for 30 days");
        rememberMe.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        rememberMe.setForeground(AppTheme.TEXT_SECONDARY);
        rememberMe.setOpaque(false);
        rememberMe.setAlignmentX(Component.LEFT_ALIGNMENT);
        rememberMe.putClientProperty("FlatLaf.style", "arc: 4;");

        errorLabel = new JLabel(" ");
        errorLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        errorLabel.setForeground(AppTheme.STATUS_DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        signInButton = new PrimaryButton("Sign In  →");
        signInButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        signInButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        signInButton.addActionListener(e -> onSignIn());

        JLabel patientSignup = buildLinkRow("New patient?", "Create an account", this::onPatientSignup);
        JLabel staffSignup = buildLinkRow("Clinic staff or medical practitioner?", "Professional signup request", this::onStaffSignupRequest);

        form.add(title);
        form.add(subtitle);
        form.add(emailField);
        form.add(Box.createVerticalStrut(AppTheme.SPACE_MD));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        form.add(rememberMe);
        form.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(AppTheme.SPACE_XS));
        form.add(signInButton);
        form.add(Box.createVerticalStrut(AppTheme.SPACE_LG));
        form.add(patientSignup);
        form.add(Box.createVerticalStrut(AppTheme.SPACE_XS));
        form.add(staffSignup);

        wrapper.add(form);
        outer.add(wrapper, BorderLayout.CENTER);

        // Bottom: "End-to-End Encrypted Session" footer
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(AppTheme.SURFACE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 0, 12, 0)));

        JLabel securityLine = new JLabel("🔒  End-to-End Encrypted Session");
        securityLine.setFont(FontManager.bodyFont(Font.BOLD, 11));
        securityLine.setForeground(AppTheme.TEXT_SECONDARY);
        securityLine.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel securitySub = new JLabel("Protected by 256-bit TLS  •  Certified POPIA & HIPAA Compliant");
        securitySub.setFont(FontManager.bodyFont(Font.PLAIN, 10));
        securitySub.setForeground(AppTheme.TEXT_MUTED);
        securitySub.setAlignmentX(Component.CENTER_ALIGNMENT);
        securitySub.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        footer.add(securityLine);
        footer.add(securitySub);
        outer.add(footer, BorderLayout.SOUTH);

        return outer;
    }

    private JLabel buildLinkRow(String prefix, String linkText, Runnable onClick) {
        JLabel combined = new JLabel(
                "<html>" + prefix + " <span style='color:#0E7C86;font-weight:bold;'>" + linkText + "</span></html>"
        );
        combined.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        combined.setForeground(AppTheme.TEXT_SECONDARY);
        combined.setAlignmentX(Component.LEFT_ALIGNMENT);
        combined.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        combined.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { onClick.run(); }
        });
        return combined;
    }

    // ── Auth logic (unchanged) ─────────────────────────────────────────────────

    private void onSignIn() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both email and password.");
            return;
        }

        errorLabel.setText(" ");
        signInButton.setEnabled(false);
        signInButton.setText("Signing In...");

        SwingWorker<BaseApiClient.ApiResult<AuthResponse>, Void> worker =
                new SwingWorker<>() {
                    @Override
                    protected BaseApiClient.ApiResult<AuthResponse> doInBackground() {
                        return ApiClientProvider.getInstance().auth().login(new LoginRequest(email, password));
                    }

                    @Override
                    protected void done() {
                        signInButton.setEnabled(true);
                        signInButton.setText("Sign In  →");

                        BaseApiClient.ApiResult<AuthResponse> result;
                        try {
                            result = get();
                        } catch (Exception e) {
                            errorLabel.setText("Something went wrong. Please try again.");
                            return;
                        }

                        handleLoginResult(result, email);
                    }
                };

        worker.execute();
    }

    private void handleLoginResult(BaseApiClient.ApiResult<AuthResponse> result, String email) {
        if (!result.isSuccess()) {
            errorLabel.setText(result.getMessage() != null
                    ? "Invalid email or password."
                    : "Could not reach the server.");
            return;
        }

        AuthResponse auth = result.getData();
        String token = auth.getAccessToken();

        SessionManager session = SessionManager.getInstance();
        session.setAccessToken(token);
        session.setRefreshToken(auth.getRefreshToken());
        session.setUserId(JwtUtil.extractUserId(token));
        session.setUserType(JwtUtil.extractUserType(token));
        session.setStaffRole(JwtUtil.extractStaffRole(token));
        session.setEmail(email);

        ApiClientProvider.getInstance().getBaseApiClient().setAuthToken(token);

        if ("CLINIC_STAFF".equals(session.getUserType())) {
            var staff = ApiClientProvider.getInstance().clinicStaff().findByEmail(session.getEmail());
            if (staff.isSuccess() && staff.getData() != null) session.setFullName(staff.getData().getName().getFullName());
        } else if ("PATIENT".equals(session.getUserType())) {
            var patient = ApiClientProvider.getInstance().patients().findByEmail(session.getEmail());
            if (patient.isSuccess() && patient.getData() != null) session.setFullName(patient.getData().getName().getFullName());
        } else if ("DOCTOR".equals(session.getUserType())) {
            var doctor = ApiClientProvider.getInstance().doctors().findByEmail(session.getEmail());
            if (doctor.isSuccess() && doctor.getData() != null) session.setFullName(doctor.getData().getName().getFullName());
        }

        if (session.isAdmin()) {
            AdminDashboard dashboard = new AdminDashboard(appFrame);
            appFrame.addScreen(AppFrame.SCREEN_ADMIN_DASHBOARD, dashboard);
            appFrame.showScreen(AppFrame.SCREEN_ADMIN_DASHBOARD);
        } else if ("DOCTOR".equals(session.getUserType())) {
            DoctorDashboard dashboard = new DoctorDashboard(appFrame);
            appFrame.addScreen(AppFrame.SCREEN_DOCTOR_DASHBOARD, dashboard);
            appFrame.showScreen(AppFrame.SCREEN_DOCTOR_DASHBOARD);
        } else if ("PATIENT".equals(session.getUserType())) {
            PatientDashboard dashboard = new PatientDashboard(appFrame);
            appFrame.addScreen(AppFrame.SCREEN_PATIENT_DASHBOARD, dashboard);
            appFrame.showScreen(AppFrame.SCREEN_PATIENT_DASHBOARD);
        } else if ("CLINIC_STAFF".equals(session.getUserType())) {
            NurseDashboard dashboard = new NurseDashboard(appFrame);
            appFrame.addScreen(AppFrame.SCREEN_NURSE_DASHBOARD, dashboard);
            appFrame.showScreen(AppFrame.SCREEN_NURSE_DASHBOARD);
        } else {
            JOptionPane.showMessageDialog(this, "Logged in as " + session.getUserType()
                    + " — dashboard not built yet.", "Login OK", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onForgotPassword() {
        appFrame.showScreen(AppFrame.SCREEN_FORGOT_PASSWORD);
    }

    private void onPatientSignup() {
        appFrame.showScreen(AppFrame.SCREEN_PATIENT_SIGNUP);
    }

    private void onStaffSignupRequest() {
        appFrame.showScreen(AppFrame.SCREEN_ACCESS_REQUEST);
    }
}
