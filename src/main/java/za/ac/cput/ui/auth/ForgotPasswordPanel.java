/*
 ForgotPasswordPanel.java

 Forgot Password screen

 Author: Joshua Reid Adams (230317693)

 Date: 18th August 2026
*/

package za.ac.cput.ui.auth;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.auth.ForgotPasswordRequest;
import za.ac.cput.ui.AppFrame;
import za.ac.cput.ui.auth.components.LabeledTextField;
import za.ac.cput.ui.auth.components.PrimaryButton;
import za.ac.cput.ui.theme.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutionException;

public class ForgotPasswordPanel extends JPanel {

    private final AppFrame appFrame;
    private LabeledTextField emailField;
    private JLabel errorLabel;
    private PrimaryButton sendButton;

    public ForgotPasswordPanel(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new GridBagLayout());
        setBackground(AppTheme.BACKGROUND);
        add(buildCard());
    }

    private JComponent buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_XL, AppTheme.SPACE_XL, AppTheme.SPACE_XL, AppTheme.SPACE_XL)
        ));
        card.setPreferredSize(new Dimension(440, 400));

        // Lock icon badge
        JPanel iconBadge = new JPanel() {
            @Override protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setLayout(new java.awt.GridBagLayout());
        iconBadge.setMaximumSize(new Dimension(52, 52));
        iconBadge.setPreferredSize(new Dimension(52, 52));
        iconBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lockIcon = new JLabel("🔒");
        lockIcon.setFont(lockIcon.getFont().deriveFont(22f));
        iconBadge.add(lockIcon);
        card.add(iconBadge);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_SM));

        // "PASSWORD RECOVERY" chip
        JLabel chip = new JLabel("PASSWORD RECOVERY PROTOCOL") {
            @Override protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(FontManager.bodyFont(Font.BOLD, 10));
        chip.setForeground(AppTheme.PRIMARY);
        chip.setOpaque(false);
        chip.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        chip.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(chip);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_SM));

        JLabel title = new JLabel("Forgot Your Password?");
        title.setFont(FontManager.headlineFont(Font.BOLD, 22));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("<html><div style='text-align:center;width:320px;'>"
                + "Enter the email address associated with your account. "
                + "We'll send you a verification code to reset your password.</div></html>");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XS, 0, AppTheme.SPACE_LG, 0));

        emailField = new LabeledTextField("Email");
        emailField.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JLabel emailHint = new JLabel("We'll send a one-time code to this address.");
        emailHint.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        emailHint.setForeground(AppTheme.TEXT_MUTED);
        emailHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailHint.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        errorLabel.setForeground(AppTheme.STATUS_DANGER);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sendButton = new PrimaryButton("Send Reset Code  →");
        sendButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        sendButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        sendButton.addActionListener(e -> onSendCode());

        JLabel backToLogin = new JLabel("← Back to Login");
        backToLogin.setFont(FontManager.bodyFont(Font.BOLD, 13));
        backToLogin.setForeground(AppTheme.TEXT_SECONDARY);
        backToLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backToLogin.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, 0, 0, 0));
        backToLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { appFrame.showScreen(AppFrame.SCREEN_LOGIN); }
        });

        JLabel footer = new JLabel("<html><div style='text-align:center;width:320px;color:#999;font-size:10px;'>"
                + "This code expires in 10 minutes. If you did not request a reset, you can safely ignore this.</div></html>");
        footer.setFont(FontManager.bodyFont(Font.PLAIN, 10));
        footer.setForeground(AppTheme.TEXT_MUTED);
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, 0, 0, 0));

        card.add(title);
        card.add(subtitle);
        card.add(emailField);
        card.add(emailHint);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        card.add(sendButton);
        card.add(backToLogin);
        card.add(footer);

        return card;
    }

    private void onSendCode() {
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.contains("@")) {
            errorLabel.setText("Please enter a valid email address.");
            return;
        }
        errorLabel.setText(" ");
        sendButton.setEnabled(false);
        sendButton.setText("Sending...");

        new SwingWorker<BaseApiClient.ApiResult<String>, Void>() {
            @Override
            protected BaseApiClient.ApiResult<String> doInBackground() {
                return ApiClientProvider.getInstance().auth().forgotPassword(new ForgotPasswordRequest(email));
            }

            @Override
            protected void done() {
                sendButton.setEnabled(true);
                sendButton.setText("Send Reset Code  →");
                BaseApiClient.ApiResult<String> result;
                try {
                    result = get();
                } catch (InterruptedException | ExecutionException ex) {
                    errorLabel.setText("Something went wrong. Please try again.");
                    return;
                }
                if (result.isSuccess()) {
                    appFrame.getVerifyResetCodePanel().setEmail(email);
                    emailField.getField().setText("");
                    appFrame.showScreen(AppFrame.SCREEN_VERIFY_RESET_CODE);
                } else {
                    errorLabel.setText("Something went wrong. Please try again.");
                }
            }
        }.execute();
    }
}
