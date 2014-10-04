package org.insignificant.josm.plugins.imagewaypoint;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.tools.Shortcut;

public final class ImageWayPointDialog {
    private static final class ImageComponent extends JComponent {
    private static final long serialVersionUID = -5207198660736375133L;

    private Image image;

    public ImageComponent() {
        this.image = null;
    }

    @Override
    public final void paint(final Graphics g) {
        if (null == this.image || 0 >= this.image.getWidth(null)
            || 0 >= this.image.getHeight(null)) {
        g.setColor(Color.white);
        g.fillRect(0, 0, this.getSize().width, this.getSize().height);
        } else {
        final int maxWidth = this.getSize().width;
        final int maxHeight = this.getSize().height;
        final int imageWidth = this.image.getWidth(null);
        final int imageHeight = this.image.getHeight(null);

        final double aspect = 1.0 * imageWidth / imageHeight;

        // what's the width if the height is 100%?
        final int widthIfHeightIsMax = (int) (aspect * maxHeight);

        // now find the real width and height
        final int resizedWidth;
        final int resizedHeight;
        if (widthIfHeightIsMax > maxWidth) {
            // oops - burst the width - so width should be the max, and
            // work out the resulting height
            resizedWidth = maxWidth;
            resizedHeight = (int) (resizedWidth / aspect);
        } else {
            // that'll do...
            resizedWidth = widthIfHeightIsMax;
            resizedHeight = maxHeight;
        }

        g.drawImage(this.image,
            (maxWidth - resizedWidth) / 2,
            (maxHeight - resizedHeight) / 2,
            resizedWidth,
            resizedHeight,
            Color.black,
            null);
        }
    }

    public final void setImage(final Image image) {
        this.image = image;
        this.repaint();
    }
    }

    private static final class ImageChangeListener implements
        IImageChangeListener {
    private final ImageWayPointDialog dialog;

    public ImageChangeListener(final ImageWayPointDialog dialog) {
        this.dialog = dialog;
    }

    public final void onAvailableImageEntriesChanged(
        final ImageEntries entries) {
        this.dialog.imageDisplay.setImage(entries.getCurrentImage());
        this.dialog.updateUI();
    }

    public final void onSelectedImageEntryChanged(final ImageEntries entries) {
        this.dialog.imageDisplay.setImage(entries.getCurrentImage());
        this.dialog.updateUI();
    }
    }

    private static final class PreviousAction extends JosmAction {

    public PreviousAction() {
        super(tr("Previous"),
        null,
        tr("Previous image"),
        null,
        false);
    }

    public final void actionPerformed(final ActionEvent actionEvent) {
        if (ImageEntries.getInstance().hasPrevious()) {
        ImageEntries.getInstance().previous();
        }
    }
    }

    private static final class NextAction extends JosmAction {

    public NextAction() {
        super(tr("Next"), null, tr("Next image"), null, false);
    }

    public final void actionPerformed(final ActionEvent actionEvent) {
        if (ImageEntries.getInstance().hasNext()) {
        ImageEntries.getInstance().next();
        }
    }
    }

    private static final class RotateLeftAction extends JosmAction {

    public RotateLeftAction() {
        super(tr("Rotate left"),
        null,
        tr("Rotate image left"),
        null,
        false);
    }

    public final void actionPerformed(final ActionEvent actionEvent) {
        ImageEntries.getInstance().rotateCurrentImageLeft();
    }
    }

    private static final class RotateRightAction extends JosmAction {

    public RotateRightAction() {
        super(tr("Rotate right"),
        null,
        tr("Rotate image right"),
        null,
        false);
    }

    public final void actionPerformed(final ActionEvent actionEvent) {
        ImageEntries.getInstance().rotateCurrentImageRight();
    }
    }

    private static final ImageWayPointDialog INSTANCE = new ImageWayPointDialog();
    private final ToggleDialog dialog;
    private final ImageComponent imageDisplay;
    private final Action previousAction;
    private final Action nextAction;
    private final Action rotateLeftAction;
    private final Action rotateRightAction;

    private final IImageChangeListener listener;

    private ImageWayPointDialog() {
    this.dialog = new ToggleDialog(tr("WayPoint Image"),
        "imagewaypoint",
        tr("Display non-geotagged photos"),
        Shortcut.registerShortcut("subwindow:imagewaypoint", tr("Toggle: {0}", tr("WayPoint Image")),
        KeyEvent.VK_Y, Shortcut.ALT_SHIFT),
        200);

    this.previousAction = new PreviousAction();
    this.nextAction = new NextAction();
    this.rotateLeftAction = new RotateLeftAction();
    this.rotateRightAction = new RotateRightAction();

    final JButton previousButton = new JButton(this.previousAction);
    final JButton nextButton = new JButton(this.nextAction);
    final JButton rotateLeftButton = new JButton(this.rotateLeftAction);
    final JButton rotateRightButton = new JButton(this.rotateRightAction);

    // default layout, FlowLayout, is fine
    final JPanel buttonPanel = new JPanel();
    buttonPanel.add(previousButton);
    buttonPanel.add(nextButton);
    buttonPanel.add(rotateLeftButton);
    buttonPanel.add(rotateRightButton);

    final JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());

    this.imageDisplay = new ImageComponent();
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    mainPanel.add(this.imageDisplay, BorderLayout.CENTER);

    this.listener = new ImageChangeListener(this);
    ImageEntries.getInstance().addListener(this.listener);

    this.updateUI();
    dialog.add(mainPanel);
    }

    private final void updateUI() {
    this.previousAction.setEnabled(ImageEntries.getInstance().hasPrevious());
    this.nextAction.setEnabled(ImageEntries.getInstance().hasNext());
    this.rotateLeftAction.setEnabled(null != ImageEntries.getInstance()
        .getCurrentImageEntry());
    this.rotateRightAction.setEnabled(null != ImageEntries.getInstance()
        .getCurrentImageEntry());

    if (null != Main.map) {
        Main.map.repaint();
    }
    }

    public static ImageWayPointDialog getInstance() {
    return ImageWayPointDialog.INSTANCE;
    }

    public final ToggleDialog getDisplayComponent() {
    return this.dialog;
    }
}
