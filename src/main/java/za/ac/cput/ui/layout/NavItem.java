/*
 NavItem.java

 One menu item in the sidebar

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

public class NavItem {
    private final String key;
    private final String icon; // an Icons.* name (older callers may still pass an emoji; Sidebar maps both)
    private final String label;

    public NavItem(String key, String icon, String label) {
        this.key = key;
        this.icon = icon;
        this.label = label;
    }

    public String getKey() { return key; }
    public String getIcon() { return icon; }
    public String getLabel() { return label; }
}
