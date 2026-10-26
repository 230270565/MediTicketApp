/*
 TopHeader.java

 Header bar with the page title, notification bell and user

 Authors:
  Joshua Reid Adams (230317693)
  Abdullahi Raage Farah (230971091)
  Aidan Barends (230255639)
  Jaden Clayton Abrahams (222206721)
  Joshua Peter Bonzet (221312536)
  Matthew Barron (230398863)
  Raul Jaaim Everts (230270565)

 Date: 11th August 2026
*/

package za.ac.cput.ui.layout;

import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.Icons;
import za.ac.cput.ui.theme.AvatarManager;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TopHeader extends JPanel {

    private JLabel notificationBadge;
    private JLabel avatarLabel;
    private JLabel nameLabel;
    private JLabel roleLabel;
    private JLabel pageTitleLabel;

    public TopHeader() {
        this(null);
    }

    public TopHeader(Runnable onProfileClick) {
        setLayout(new BorderLayout());
        setBackground(AppTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, AppTheme.SPACE_LG, AppTheme.SPACE_MD, AppTheme.SPACE_LG)
        ));
        setPreferredSize(new Dimension(0, 72));

        pageTitleLabel = new JLabel("");
        pageTitleLabel.setFont(FontManager.bodyFont(Font.BOLD, 15));
        pageTitleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        pageTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        add(buildProfileSection(onProfileClick), BorderLayout.WEST);
        add(pageTitleLabel, BorderLayout.CENTER);
        add(buildRightSection(), BorderLayout.EAST);
    }

    private JComponent buildProfileSection(Runnable onProfileClick) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_SM, 0));
        panel.setOpaque(false);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        SessionManager session = SessionManager.getInstance();

        avatarLabel = new JLabel(AvatarManager.getCircularAvatar(session.getUserId(), 40));

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);

        String displayName = (session.getFullName() != null && !session.getFullName().isBlank())
                ? session.getFullName()
                : session.getEmail();

        nameLabel = new JLabel(displayName != null ? displayName : "—");
        nameLabel.setFont(FontManager.bodyFont(Font.BOLD, 14));
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        roleLabel = new JLabel(resolveRoleLabel(session));
        roleLabel.setFont(FontManager.bodyFont(Font.BOLD, 10));
        roleLabel.setForeground(AppTheme.TEXT_MUTED);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textStack.add(nameLabel);
        textStack.add(roleLabel);

        panel.add(avatarLabel);
        panel.add(textStack);

        if (onProfileClick != null) {
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onProfileClick.run();
                }
            });
        }

        return panel;
    }

    // Updates the name, role and photo after the profile changes.
    public void refreshProfile() {
        SessionManager session = SessionManager.getInstance();

        String displayName = (session.getFullName() != null && !session.getFullName().isBlank())
                ? session.getFullName()
                : session.getEmail();

        nameLabel.setText(displayName != null ? displayName : "—");
        roleLabel.setText(resolveRoleLabel(session));
        avatarLabel.setIcon(AvatarManager.getCircularAvatar(session.getUserId(), 40));
    }

    private String resolveRoleLabel(SessionManager session) {
        if ("CLINIC_STAFF".equals(session.getUserType())) {
            return "ADMIN".equals(session.getStaffRole()) ? "ADMINISTRATOR" : "NURSE";
        }
        if ("DOCTOR".equals(session.getUserType())) return "DOCTOR";
        if ("PATIENT".equals(session.getUserType())) return "PATIENT";
        return "";
    }

    private JComponent buildRightSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACE_MD, 0));
        panel.setOpaque(false);
        // The "Search medical records" box was removed: it had no behaviour behind it.
        panel.add(buildNotificationBell());
        return panel;
    }

    private Runnable onBellClick;

    /** What the bell does. Each dashboard opens its Notifications page. */
    public void setOnBellClick(Runnable onBellClick) {
        this.onBellClick = onBellClick;
    }

    private JComponent buildNotificationBell() {
        JPanel wrapper = new JPanel(null);
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(40, 40));
        wrapper.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        wrapper.setToolTipText("Notifications");

        JLabel bell = new JLabel(Icons.get(Icons.BELL, 22, AppTheme.TEXT_SECONDARY));
        bell.setBounds(0, 4, 36, 36);
        bell.setHorizontalAlignment(SwingConstants.CENTER);

        // Red count bubble on the bell's top-right corner.
        notificationBadge = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.STATUS_DANGER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        notificationBadge.setFont(FontManager.bodyFont(Font.BOLD, 10));
        notificationBadge.setForeground(Color.WHITE);
        notificationBadge.setBounds(20, 0, 20, 16);
        notificationBadge.setVisible(false);

        wrapper.add(notificationBadge);
        wrapper.add(bell);
        wrapper.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onBellClick != null) onBellClick.run();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                bell.setIcon(Icons.get(Icons.BELL, 22, AppTheme.PRIMARY));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                bell.setIcon(Icons.get(Icons.BELL, 22, AppTheme.TEXT_SECONDARY));
            }
        });
        return wrapper;
    }

    /** Updates the page title shown in the center of the header. */
    public void setPageTitle(String title) {
        pageTitleLabel.setText(title != null ? title : "");
    }

    /** Unread notification count on the bell; 0 hides the bubble. */
    public void setUnreadCount(int count) {
        notificationBadge.setText(count > 9 ? "9+" : String.valueOf(count));
        notificationBadge.setVisible(count > 0);
    }
}
