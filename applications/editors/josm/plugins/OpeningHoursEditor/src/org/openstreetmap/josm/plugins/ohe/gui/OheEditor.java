package org.openstreetmap.josm.plugins.ohe.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.ohe.ClockSystem;
import org.openstreetmap.josm.plugins.ohe.OpeningTimeUtils;
import org.openstreetmap.josm.plugins.ohe.parser.OpeningTimeCompiler;

public class OheEditor extends JPanel implements MouseListener, MouseMotionListener {
    final OheDialogPanel dialog;

    final private JScrollPane scrollPane;
    final JPanel contentPanel;

    ArrayList<TimeRect> timeRects;

    final private int dayAxisHeight = 20;
    final private int timeAxisWidth = 45;

    public OheEditor(OheDialogPanel oheDialogPanel) {
        dialog = oheDialogPanel;

        // the MainPanel for showing the TimeRects
        contentPanel = new JPanel() {
            @Override
            public void setSize(Dimension d) {
                super.setSize(d);
                repositionTimeRects();
            }

            @Override
            public void paintComponent(Graphics g) {
                if (OheEditor.this.isEnabled()) {
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    
                    // draw the time from 12PM to 00AM in a different color
                    if (dialog.getHourMode() == ClockSystem.TWELVE_HOURS) {
                        g.setColor(new Color(255, 255, 218));
                        g.fillRect(0, getMinutePosition(12 * 60), getWidth(), getHeight() - getMinutePosition(12 * 60));
                    }

                    // horizontal Lines
                    for (int i = 0; i <= 24; ++i) {
                        if (i % 3 == 0) {
                            g.setColor(Color.BLACK);
                        } else {
                            g.setColor(Color.LIGHT_GRAY);
                        }

                        g.drawLine(0, getMinutePosition(i * 60), getWidth(), getMinutePosition(i * 60));
                    }

                    // vertical Lines
                    g.setColor(Color.BLACK);
                    for (int i = 0; i <= 7; ++i) {
                        g.drawLine(getDayPosition(i), 0, getDayPosition(i), getHeight());
                    }

                    // if a new Rect is dragged draw it
                    if (day0 >= 0) {
                        Graphics2D g2D = (Graphics2D) g;

                        int day2 = Math.min(day0, day1);
                        int day3 = Math.max(day0, day1);
                        int minute2 = Math.min(minute0, minute1);
                        int minute3 = Math.max(minute0, minute1);
                        Rectangle bounds = getPanelBoundsForTimeinterval(day2, day3 + 1, minute2, minute3);

                        TimeRect.drawTimeRect(g2D, bounds, minute2 == minute3, false);
                    }
                } else {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        contentPanel.addMouseListener(this);
        contentPanel.addMouseMotionListener(this);
        contentPanel.setLayout(null);
        contentPanel.setPreferredSize(new Dimension(180, 384));

        initTimeRects();

        scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(contentPanel);

        // the upper Panel for showing Weekdays
        scrollPane.setColumnHeaderView(new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(contentPanel.getWidth(), dayAxisHeight);
            }

            @Override
            public void paintComponent(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());

                g.setColor(Color.BLACK);
                for (int i = 0; i < 7; ++i) {
                    if (i > 0) {
                        g.drawLine(getDayPosition(i), 0, getDayPosition(i), getHeight());
                    }

                    String text = OpeningTimeCompiler.WEEKDAYS[i];
                    g.drawString(text, (int) (getDayPosition(i + 0.5) - g.getFontMetrics().stringWidth(text) * 0.5),
                            (int) (dayAxisHeight * 0.5 + g.getFontMetrics().getHeight() * 0.35));
                }
            }
        });

        // the left Panel for showing the hours
        scrollPane.setRowHeaderView(new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(timeAxisWidth, contentPanel.getHeight());
            }

            @Override
            public void paintComponent(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                
                // draw the time from 12PM to 00AM in a different color
                if (dialog.getHourMode() == ClockSystem.TWELVE_HOURS) {
                    g.setColor(new Color(255, 255, 218));
                    g.fillRect(0, getMinutePosition(12 * 60), getWidth(), getHeight() - getMinutePosition(12 * 60));
                }

                for (int i = 0; i <= 24; ++i) {
                    if (i % 3 == 0) {
                        g.setColor(Color.BLACK);
                        if (i % 24 != 0) {
                            String text = OpeningTimeUtils.timeString(i * 60, dialog.getHourMode(), false);
                            g.drawString(text, (timeAxisWidth - g.getFontMetrics().stringWidth(text)) / 2,
                                    getMinutePosition(i * 60) + (int) (g.getFontMetrics().getHeight() * 0.35));
                        }
                    } else {
                        g.setColor(Color.LIGHT_GRAY);
                    }

                    g.drawLine(getWidth() - 4, getMinutePosition(i * 60), getWidth(), getMinutePosition(i * 60));
                }

                g.setColor(Color.BLACK);
                String text = OpeningTimeUtils.timeString(0, dialog.getHourMode(), false);
                g.drawString(text, (timeAxisWidth - g.getFontMetrics().stringWidth(text)) / 2, getMinutePosition(0)
                        + (int) (g.getFontMetrics().getHeight() * 1.0));
                if (dialog.getHourMode() == ClockSystem.TWELVE_HOURS) {
                    text = "AM";
                    g.drawString(text, (timeAxisWidth - g.getFontMetrics().stringWidth(text)) / 2, getMinutePosition(0)
                            + (int) (g.getFontMetrics().getHeight() * 1.85));
                    text = "PM";
                    g.drawString(text, (timeAxisWidth - g.getFontMetrics().stringWidth(text)) / 2,
                            getMinutePosition(12 * 60) + (int) (g.getFontMetrics().getHeight() * 1.2));
                }
            }
        });

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    // update all the TimeRects with new Data
    public void initTimeRects() {
        contentPanel.removeAll();

        ArrayList<int[]> time;
        try {
            time = dialog.getTime();
        } catch (Exception exc) {
            setEnabled(false);
            return;
        }

        setEnabled(true);
        timeRects = new ArrayList<TimeRect>();
        if (time != null) {
            for (int[] timeRectValues : time) {
                int day0 = timeRectValues[0];
                int day1 = timeRectValues[1];
                int minute0 = timeRectValues[2];
                int minute1 = timeRectValues[3];
                TimeRect timeRect = new TimeRect(OheEditor.this, day0, day1, minute0, minute1);
                timeRects.add(timeRect);
                contentPanel.add(timeRect);
            }
        }

        repositionTimeRects();
        repaint();
    }

    protected void repositionTimeRects() {
        if (timeRects != null) {
            for (TimeRect timeRect : timeRects) {
                timeRect.reposition();
            }
        }
    }

    // returns the physical Borders of the TimeRect on the mainPanel
    public Rectangle getPanelBoundsForTimeinterval(int dayStart, int dayEnd, int minutesStart, int minutesEnd) {
        int x = getDayPosition(dayStart);
        int y = getMinutePosition(minutesStart);
        int width = getDayPosition(dayEnd) - getDayPosition(dayStart);
        int height = getMinutePosition(minutesEnd) - getMinutePosition(minutesStart);

        if (minutesStart == minutesEnd)
            return new Rectangle(x, y - 2 - TimeRect.verticalNonDrawedPixels, width, height + 5 + 2
                    * TimeRect.verticalNonDrawedPixels);

        return new Rectangle(x, y, width, height + 1);
    }

    public double getDayWidth() {
        return (contentPanel.getWidth() - 1) / 7.0;
    }

    public int getDayPosition(double d) {
        return (int) (d * getDayWidth());
    }

    public double getMinuteHeight() {
        return (contentPanel.getHeight() - 1) / (24.0 * 60);
    }

    public int getMinutePosition(int minute) {
        return (int) (minute * getMinuteHeight());
    }

    // removes the given timerect from the panel and from the arraylist
    public void removeTimeRect(TimeRect timeRectToRemove) {
        timeRects.remove(timeRectToRemove);
        contentPanel.remove(timeRectToRemove);
        dialog.updateValueField(timeRects);
        repaint();
    }

    // drawing a new Rect
    private int day0 = -1;
    private int minute0;
    private int day1;
    private int minute1;
    private int xDragStart;
    private int yDragStart;

    @Override
    public void mouseClicked(MouseEvent evt) {
    }

    @Override
    public void mouseEntered(MouseEvent evt) {
        if (!isEnabled()) {
            return; // allow no mouse actions when the editor is not enabled
        }
        mousePositionChanged(0, 0, true);
    }

    @Override
    public void mouseExited(MouseEvent evt) {
        if (!isEnabled()) {
            return; // allow no mouse actions when the editor is not enabled
        }
        mousePositionChanged(0, 0, false);
    }

    @Override
    public void mousePressed(MouseEvent evt) {
        if (!isEnabled()) {
            return; // allow no mouse actions when the editor is not enabled
        }
        day0 = (int) Math.floor(evt.getX() / getDayWidth());
        minute0 = (int) Math.floor(evt.getY() / (getMinuteHeight() * TimeRect.minuteResterize))
        * TimeRect.minuteResterize;
        day1 = day0;
        minute1 = minute0;
        xDragStart = evt.getX();
        yDragStart = evt.getY();
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
        if (!isEnabled()) {
            return; // allow no mouse actions when the editor is not enabled
        }
        // mouse must be moved 5px before creating a rect
        if (xDragStart == -1 || Math.abs(evt.getX() - xDragStart) + Math.abs(evt.getY() - yDragStart) > 5) {
            int day2 = Math.min(day0, day1);
            int day3 = Math.max(day0, day1);
            int minute2 = Math.min(minute0, minute1);
            int minute3 = Math.max(minute0, minute1);

            TimeRect timeRect = new TimeRect(OheEditor.this, day2, day3, minute2, minute3);
            timeRects.add(timeRect);
            contentPanel.add(timeRect);
            timeRect.reposition();
            dialog.updateValueField(timeRects);

            day0 = -1;
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent evt) {
        if (!isEnabled()) {
            return; // allow no mouse actions when the editor is not enabled
        }
        // mouse must be moved 5px before drawing a rect
        if (xDragStart == -1 || Math.abs(evt.getX() - xDragStart) + Math.abs(evt.getY() - yDragStart) > 5) {
            xDragStart = -1;
            day1 = (int) Math.floor(evt.getX() / getDayWidth());
            minute1 = (int) Math.floor(evt.getY() / (getMinuteHeight() * TimeRect.minuteResterize))
                    * TimeRect.minuteResterize;

            // ensure that the new time is in a valid range
            day1 = Math.max(day1, 0);
            day1 = Math.min(day1, 6);
            minute1 = Math.max(minute1, 0);
            minute1 = Math.min(minute1, 24 * 60);

            repaint();
        }
        mousePositionChanged(evt.getX(), evt.getY(), true);
    }

    @Override
    public void mouseMoved(MouseEvent evt) {
        if (!isEnabled()) {
            return; // allow no mouse actions when the editor is not enabled
        }
        mousePositionChanged(evt.getX(), evt.getY(), true);
    }

    public void mousePositionChanged(int x, int y, boolean mouseInside) {
        if (mouseInside) {
            int actualDay = (int) Math.floor(x / getDayWidth());
            int minutes = (int) Math.floor(y / (getMinuteHeight() * TimeRect.minuteResterize))
                    * TimeRect.minuteResterize;
            actualDay = Math.max(0, Math.min(6, actualDay));
            minutes = Math.max(0, Math.min(24 * 60, minutes));
            dialog.setMousePositionText(OpeningTimeCompiler.WEEKDAYS[actualDay] + " "
                    + OpeningTimeUtils.timeString(minutes, dialog.getHourMode(), true));
        } else {
            dialog.setMousePositionText("-");
        }
    }
}