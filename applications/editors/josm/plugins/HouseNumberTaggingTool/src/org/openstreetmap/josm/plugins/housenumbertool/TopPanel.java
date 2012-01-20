package org.openstreetmap.josm.plugins.housenumbertool;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * @author Oliver Raupach 18.01.2012 <http://www.oliver-raupach.de>
 */
public class TopPanel extends JPanel
{

   /**
    * 
    */
   private static final long serialVersionUID = 5319317445299227702L;

   public TopPanel ()
   {
      setLayout(new GridLayout(0,1));
      setPreferredSize(new Dimension(200,30));
      setBackground(Color.YELLOW);
      Border paddingBorder = BorderFactory.createEmptyBorder(10,10,10,10);
      JLabel topLabel = new JLabel(tr("Housenumber-Editor. Create or update these tags."), SwingConstants.LEFT);
      topLabel.setBorder(paddingBorder);
      add(topLabel);
   }
}
