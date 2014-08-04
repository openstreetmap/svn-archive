/**
 * 
 */
package com.tilusnet.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

/**
 * @author tilusnet <tilusnet@gmail.com>
 *
 */
public class AlignWaysTipsPanel extends JPanel {

    private static final long serialVersionUID = -8583989497599985140L;

    public AlignWaysTipsPanel() {
        initComponents();
    }

    private void initComponents() {

        Title = new JPanel();
        WelcomeTo = new JLabel();
        Icon = new JLabel();
        separator = new JSeparator();
        Intro = new JPanel();
        introText = new JLabel();
        scrollableSteps = new JScrollPane();
        steps = new JPanel();
        step01 = new JLabel();
        step02 = new JLabel();
        step03 = new JLabel();
        step04 = new JLabel();
        lastHint = new JLabel();
        dontShow = new JCheckBox();

        setAutoscrolls(true);

        WelcomeTo.setText("<html>\n<div style=\"font-family: \"sans-serif\"; font-weight: bold; font-style: italic;\">\n<span style=\"font-size: large;\">"
                + tr("Welcome to the</span><br>\n<span style=\"font-size: xx-large;\">AlignWay<span style=\"color: rgb(204, 85, 0);\">S</span> Plugin<br>\n</span>"
                        + "<span style=\"font-size: medium;\"><br>\n...or it rather should be called <br>\n<span style=\"font-size: large;\">AlignWayS(egments)</span> Plugin...")
                        + "</span>\n</div>\n</html>");

        WelcomeTo.setVerticalAlignment(SwingConstants.TOP);
        WelcomeTo.setPreferredSize(new Dimension(400, 128));

        Icon.setIcon(new ImageIcon(getClass().getResource("/images/tipsdialog/alignways128.png"))); // NOI18N
        GroupLayout TitleLayout = new GroupLayout(Title);
        Title.setLayout(TitleLayout);
        TitleLayout.setHorizontalGroup(
                TitleLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, TitleLayout.createSequentialGroup()
                        .addComponent(WelcomeTo, GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Icon, GroupLayout.PREFERRED_SIZE, 132, GroupLayout.PREFERRED_SIZE))
                );
        TitleLayout.setVerticalGroup(
                TitleLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(TitleLayout.createSequentialGroup()
                        .addComponent(Icon)
                        .addContainerGap())
                        .addComponent(WelcomeTo, GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                );

        Intro.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        introText.setText(tr("<html>\n<p style=\"font-family: sans-serif; font-weight: bold;\">AlignWays will\nhelp you to align two way segments. This can be handy when for instance\nyou sketch the outlines of a building and want its side to be parallel\nwith a street or road.<br>\n<br>\nSome tips may help before you start:\n</p>\n</html>\n\n"));
        introText.setVerticalAlignment(SwingConstants.TOP);

        scrollableSteps.setBorder(null);
        scrollableSteps.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        step01.setIcon(new ImageIcon(getClass().getResource("/images/tipsdialog/hlpRefSel.png"))); // NOI18N
        step01.setText(tr("<html>\n<div style=\"font-family: sans-serif;\">\n<ul>\n<li><b>Select a reference segment.</b> You can do this by <b><i><span style=\"color:green\">Ctrl-click</span></i></b>ing\non a segment. The other, to be aligned segment will become parallel to\nthis one. </li>\n</ul>\n</div>\n</html>\n\n"));
        step01.setVerticalAlignment(SwingConstants.TOP);

        step02.setIcon(new ImageIcon(getClass().getResource("/images/tipsdialog/hlpAlgSel.png"))); // NOI18N
        step02.setText(tr("<html>\n<div style=\"font-family:sans-serif\">\n<ul>\n  <li><b>Select the to be aligned segment.</b> You can do this by simply <b><i><span style=\"color:green\">click</span></i></b>ing on a different segment. \nThe rotation pivot will be highlighted by default in the centre of the segment.\n  </li>\n</ul>\n</div>\n</html>\n\n"));
        step02.setVerticalAlignment(SwingConstants.TOP);

        step03.setIcon(new ImageIcon(getClass().getResource("/images/tipsdialog/hlpPvtSel.png"))); // NOI18N
        step03.setText(tr("<html>\n<div style=\"font-family:sans-serif\">\n<ul>\n  <li>Optionally <b>change the rotation pivot point</b>. In order to get parallel with the reference segment, the to be aligned segment will rotate around this point. You can choose the two extremities or the centre of the segment by <b><i><span style=\"color:green\">click</span></i></b>ing nearby. \n  </li>\n</ul>\n</div>\n</html>\n\n"));
        step03.setVerticalAlignment(SwingConstants.TOP);

        step04.setIcon(new ImageIcon(getClass().getResource("/images/tipsdialog/hlpAlgCmd.png"))); // NOI18N
        step04.setText(tr("<html>\n<div style=\"font-family:sans-serif\">\n<ul>\n  <li><b>Align the segments.</b> Press <b><i><span style=\"color:green\">{0}"
                + "</span></i></b>. Alternatively you''ll find the command in the <b>Tools</b>\n menu or may want to place the action on the <b>toolbar</b>.\n  </li>\n</ul>\n</div>\n</html>\n\n",
                AlignWaysPlugin.getAwAction().getShortcut().getKeyText()));
        step04.setVerticalAlignment(SwingConstants.TOP);

        lastHint.setText(tr("<html>\n<div style=\"font-family:sans-serif\">\n<b>Last hint:</b> There is an easy way to start over your selections if you want: <b><i><span style=\"color:green\">Alt-Click</span></i></b> somewhere on the map.\n</div>\n</html>\n\n"));
        lastHint.setVerticalAlignment(SwingConstants.TOP);

        GroupLayout stepsLayout = new GroupLayout(steps);
        steps.setLayout(stepsLayout);
        stepsLayout.setHorizontalGroup(
                stepsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(stepsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(stepsLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(lastHint, GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                                .addComponent(step04, GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                                .addComponent(step03, GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                                .addComponent(step02, GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                                .addComponent(step01, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE))
                                .addGap(18, 18, 18))
                );
        stepsLayout.setVerticalGroup(
                stepsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(stepsLayout.createSequentialGroup()
                        .addComponent(step01, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(step02, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(step03, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(step04, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lastHint, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(22, Short.MAX_VALUE))
                );

        scrollableSteps.setViewportView(steps);

        dontShow.setText(tr("Don''t show this again"));

        GroupLayout IntroLayout = new GroupLayout(Intro);
        Intro.setLayout(IntroLayout);
        IntroLayout.setHorizontalGroup(
                IntroLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(IntroLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(dontShow, GroupLayout.PREFERRED_SIZE, 245, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(283, Short.MAX_VALUE))
                        .addComponent(scrollableSteps, GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
                        .addComponent(introText, GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
                );
        IntroLayout.setVerticalGroup(
                IntroLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, IntroLayout.createSequentialGroup()
                        .addComponent(introText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(scrollableSteps, GroupLayout.PREFERRED_SIZE, 209, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dontShow)
                        .addContainerGap())
                );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(separator, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
                                .addComponent(Title, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(Intro, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(Title, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(separator, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Intro, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(45, Short.MAX_VALUE))
                );
    }


    private JLabel Icon;
    private JPanel Intro;
    private JPanel Title;
    private JLabel WelcomeTo;
    private JCheckBox dontShow;
    private JLabel introText;
    private JLabel lastHint;
    private JScrollPane scrollableSteps;
    private JSeparator separator;
    private JLabel step01;
    private JLabel step02;
    private JLabel step03;
    private JLabel step04;
    private JPanel steps;

    public boolean isChkBoxSelected() {
        return dontShow.isSelected();
    }
}
