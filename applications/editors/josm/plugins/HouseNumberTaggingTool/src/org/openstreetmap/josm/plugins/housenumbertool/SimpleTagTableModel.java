package org.openstreetmap.josm.plugins.housenumbertool;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * @author Oliver Raupach 10.01.2012 <http://www.oliver-raupach.de>
 */
public class SimpleTagTableModel extends AbstractTableModel
{

   /**
    * 
    */
   private static final long serialVersionUID = -7740659524204094788L;
   private List<TagData> modelData = new ArrayList<TagData>();
   private String displayTag;

   @Override
   public int getRowCount()
   {
      return modelData.size();
   }

   @Override
   public int getColumnCount()
   {
      return 1;
   }

   @Override
   public Object getValueAt(int rowIndex, int columnIndex)
   {

      return modelData.get(rowIndex).getValue();
   }

   @Override
   public String getColumnName(int column)
   {
      if (column == 0)
      {
         return tr("Proposals");
      }
      else
      {
         return "";
      }
   }

   @Override
   public boolean isCellEditable(int rowIndex, int columnIndex)
   {
      return false;
   }

   public List<TagData> getModelData()
   {
      return modelData;
   }

   public void setModelData(List<TagData> modelData)
   {
      this.modelData = modelData;
      fireTableDataChanged();
   }

   public String getDisplayTag()
   {
      return displayTag;
   }

   public void setDisplayTag(String displayTag)
   {
      this.displayTag = displayTag;
   }

}
