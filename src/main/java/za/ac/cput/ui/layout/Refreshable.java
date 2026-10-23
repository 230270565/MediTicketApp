/*
 Refreshable.java

 Pages that can reload their data implement this

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

// Pages that can reload their data. LiveUpdater calls refresh() so pages don't go stale.
public interface Refreshable {
    void refresh();
}
