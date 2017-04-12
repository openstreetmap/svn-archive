// License: GPL. For details, see LICENSE file.
package org.insignificant.josm.plugins.imagewaypoint;

final class ImageChangeListener implements
    IImageChangeListener {
    private final ImageWayPointDialog dialog;

    ImageChangeListener(final ImageWayPointDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void onAvailableImageEntriesChanged(
        final ImageEntries entries) {
        this.dialog.imageDisplay.setImage(entries.getCurrentImage());
        this.dialog.updateGUI();
    }

    @Override
    public void onSelectedImageEntryChanged(final ImageEntries entries) {
        this.dialog.imageDisplay.setImage(entries.getCurrentImage());
        this.dialog.updateGUI();
    }
}
