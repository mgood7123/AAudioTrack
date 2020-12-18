package smallville7123.UI;

public class WindowsContextMenuItem {
    boolean isHeader;
    String title;
    WindowsContextMenu parent;
    WindowsContextMenu subMenu;

    WindowsContextMenuItem(String title, WindowsContextMenu parent, WindowsContextMenu subMenu, boolean isHeader) {
        this.isHeader = isHeader;
        this.title = title;
        this.parent = parent;
        this.subMenu = subMenu;
    }
}
