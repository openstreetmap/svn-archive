package org.openstreetmap.josm.plugins.ohe.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

public class TimeRect extends JPanel implements MouseListener,
        MouseMotionListener {
    public static final int[] transformCursorTypes = new int[] {
            Cursor.MOVE_CURSOR, Cursor.N_RESIZE_CURSOR,
            Cursor.NE_RESIZE_CURSOR, Cursor.E_RESIZE_CURSOR,
            Cursor.SE_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR,
            Cursor.SW_RESIZE_CURSOR, Cursor.W_RESIZE_CURSOR,
            Cursor.NW_RESIZE_CURSOR };

    public static final int minuteResterize = 15;
    public static final int verticalNonDrawedPixels = 5;

    public static final boolean[][] transformDirections = new boolean[][] {
            { true, true, true, true }, // Drag
            { true, false, false, false }, // N
            { true, true, false, false }, // NE
            { false, true, false, false }, // E
            { false, true, true, false }, // SE
            { false, false, true, false }, // S
            { false, false, true, true }, // SW
            { false, false, false, true }, // W
            { true, false, false, true }, // NW
    };

    public static final int roundCornerSize = 8;
    private final int clickAreaSize = 16;

    private OheEditor editor;

    private int dayStart;
    private int dayEnd;
    private int minuteStart;
    private int minuteEnd;

    public TimeRect(OheEditor editor, int dayStart, int dayEnd,
            int minutesStart, int minutesEnd) {
        this.editor = editor;

        this.dayStart = dayStart;
        this.dayEnd = dayEnd;
        this.minuteStart = minutesStart;
        this.minuteEnd = minutesEnd;

        transformType = -1;

        setOpaque(true);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public int getDayStart() {
        return dayStart;
    }

    public int getDayEnd() {
        return dayEnd;
    }

    public int getMinuteStart() {
        return minuteStart;
    }

    public int getMinuteEnd() {
        return minuteEnd;
    }

    public void reposition() {
        setBounds(editor.getPanelBoundsForTimeinterval(dayStart, dayEnd + 1,
                minuteStart, minuteEnd));
        editor.contentPanel.repaint();
    }

    private boolean isZeroMinuteInterval() {
        return minuteStart == minuteEnd;
    }

    private boolean isOpenEndInterval() {
        return minuteEnd == 24 * 60 + 1;
    }

    private void updateTimeInterval(int newDayStart, int newDayEnd,
            int newMinuteStart, int newMinuteEnd) {
        dayStart = newDayStart;
        dayEnd = newDayEnd;
        minuteStart = newMinuteStart;
        minuteEnd = newMinuteEnd;

        editor.dialog.updateValueField(editor.timeRects);
        reposition();
    }

    @Override
    public void paintComponent(Graphics g) {
        drawTimeRect((Graphics2D) g, new Rectangle(0, 0, getWidth(),
                getHeight()), isZeroMinuteInterval(), isOpenEndInterval());
    }

    public static void drawTimeRect(Graphics2D g2D, Rectangle bounds,
            boolean isZeroMinuteInterval, boolean isOpenEndInterval) {

        Color innerColor = new Color(135, 135, 234);
        if (isOpenEndInterval)
            innerColor = new Color(234, 135, 135);

        int tmpRoundCornerSize = TimeRect.roundCornerSize;
        int verticalNonFilledBorder = 1;
        if (isZeroMinuteInterval) {
            innerColor = new Color(135, 234, 135);
            tmpRoundCornerSize = 0;
            verticalNonFilledBorder = verticalNonDrawedPixels;
        }

        g2D.setColor(innerColor);
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                .6f));
        g2D.fillRoundRect(bounds.x + 1, bounds.y + verticalNonFilledBorder,
                bounds.width - 2, bounds.height - 1 - 2
                        * verticalNonFilledBorder, tmpRoundCornerSize,
                tmpRoundCornerSize);

        g2D.setColor(new Color(255, 0, 0));
        g2D.setComposite(AlphaComposite
                .getInstance(AlphaComposite.SRC_OVER, 1f));
        g2D.drawRoundRect(bounds.x + 1, bounds.y + verticalNonFilledBorder,
                bounds.width - 2, bounds.height - 1 - 2
                        * verticalNonFilledBorder, tmpRoundCornerSize,
                tmpRoundCornerSize);

    }

    private int actualDayDrag;
    private int actualMinuteDrag;
    private int dragX;
    private int dragY;
    private int transformType;

    // Calculate where the Component was clicked and returns the
    // transformtype
    private int getTransformType(MouseEvent evt) {
        int tmpClickAreaWidth = Math.min(clickAreaSize, getWidth() / 3);
        int tmpClickAreaHeight = Math.min(clickAreaSize, getHeight() / 3);

        boolean isInNorthernTransformClickArea = evt.getY() < tmpClickAreaHeight;
        boolean isInEasternTransformClickArea = evt.getX() > getWidth()
                - tmpClickAreaWidth;
        boolean isInSouthernTransformClickArea = evt.getY() > getHeight()
                - tmpClickAreaHeight;
        boolean isInWesternTransformClickArea = evt.getX() < tmpClickAreaWidth;

        if (isZeroMinuteInterval()) {
            isInNorthernTransformClickArea = false;
            isInSouthernTransformClickArea = false;
        }

        int tType = 0;
        for (int i = 1; i < transformDirections.length && tType == 0; i++) {
            if (isInNorthernTransformClickArea == transformDirections[i][0]
                    && isInEasternTransformClickArea == transformDirections[i][1]
                    && isInSouthernTransformClickArea == transformDirections[i][2]
                    && isInWesternTransformClickArea == transformDirections[i][3])
                tType = i;
        }

        return tType;
    }

    public void showMenu(MouseEvent evt) {
        JPopupMenu menu = new JPopupMenu();
        final JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(
                tr("open end"), isOpenEndInterval());
        menu.add(cbMenuItem);
        cbMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cbMenuItem.isSelected())
                    updateTimeInterval(dayStart, dayEnd, minuteStart,
                            24 * 60 + 1);
                else
                    updateTimeInterval(dayStart, dayEnd, minuteStart, 24 * 60);
            }
        });
        menu.show(this, evt.getX(), evt.getY());
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
    }

    @Override
    public void mouseEntered(MouseEvent evt) {
    }

    @Override
    public void mouseExited(MouseEvent evt) {
        if (transformType < 0)
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mousePressed(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            showMenu(evt);
        } else {
            actualDayDrag = 0;
            actualMinuteDrag = 0;
            dragX = evt.getXOnScreen();
            dragY = evt.getYOnScreen();
            transformType = getTransformType(evt);
        }
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
        transformType = -1;
        if (evt.isPopupTrigger()) {
            showMenu(evt);
        }
    }

    @Override
    public void mouseDragged(MouseEvent evt) {
        if (transformType >= 0) {
            int xDiff = evt.getXOnScreen() - dragX;
            int yDiff = evt.getYOnScreen() - dragY;

            xDiff = (int) Math.round(xDiff / editor.getDayWidth())
                    - actualDayDrag;
            yDiff = (int) Math.round(yDiff
                    / (editor.getMinuteHeight() * minuteResterize))
                    * minuteResterize - actualMinuteDrag;

            if (xDiff != 0) {
                int newDayStart = dayStart;
                int newDayEnd = dayEnd;

                if (transformDirections[transformType][3])
                    newDayStart += xDiff;
                if (transformDirections[transformType][1])
                    newDayEnd += xDiff;

                if (newDayStart > newDayEnd) {
                    editor.removeTimeRect(this);
                    transformType = -1;
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                } else if (newDayStart >= 0 && newDayEnd <= 6) {
                    actualDayDrag += xDiff;
                    updateTimeInterval(newDayStart, newDayEnd, minuteStart,
                            minuteEnd);
                }
            }
            if (yDiff != 0 && transformType >= 0) {
                int newMinutesStart = minuteStart;
                int newMinutesEnd = minuteEnd;

                if (transformDirections[transformType][0])
                    newMinutesStart = newMinutesStart + yDiff;
                if (transformDirections[transformType][2]
                        && !isOpenEndInterval())
                    newMinutesEnd = newMinutesEnd + yDiff;

                if (newMinutesStart >= 0
                        && (newMinutesEnd <= 24 * 60 || isOpenEndInterval())) {
                    actualMinuteDrag += yDiff;
                    updateTimeInterval(dayStart, dayEnd, newMinutesStart,
                            newMinutesEnd);
                }
            }
        }
        editor.mousePositionChanged(evt.getX() + getX(), evt.getY() + getY(), true);
    }

    @Override
    public void mouseMoved(MouseEvent evt) {
        if (transformType < 0)
            setCursor(new Cursor(transformCursorTypes[getTransformType(evt)]));
        editor.mousePositionChanged(evt.getX() + getX(), evt.getY() + getY(), true);
    }
}