/**
 *
 */
package at.dallermassl.josm.plugin.pluginmanager;

import java.util.ArrayList;
import java.util.List;

import static org.openstreetmap.josm.tools.I18n.tr;
import javax.swing.table.AbstractTableModel;

/**
 * @author cdaller
 *
 */
public class PluginTableModel extends AbstractTableModel {
    private List<SiteDescription> sites;
    private List<PluginDescription> plugins;

    public PluginTableModel(List<SiteDescription> descriptions) {
        this.sites = descriptions;
        update();
    }

    private void update() {
        plugins = new ArrayList<PluginDescription>();
        for (SiteDescription site : sites) {
            plugins.addAll(site.getPlugins());
        }
    }



    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return 3;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return plugins.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        PluginDescription plugin = plugins.get(rowIndex);
        switch(columnIndex) {
        case 0: return plugin.isInstall();
        case 1: return plugin.getName();
        case 2: if(plugin.getInstalledVersion() != null) {
            return plugin.getInstalledVersion() + " -> " + plugin.getVersion();
        } else {
            return plugin.getVersion();
        }
        default: throw new IllegalArgumentException("Illegal Column Index " + columnIndex);
        }
    }



    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if(columnIndex != 0) {
            return;
        }
        PluginDescription plugin = plugins.get(rowIndex);
        plugin.setInstall(Boolean.TRUE.equals(value));
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex) {
        case 0: return Boolean.class;
        case 1:
        case 2: return Object.class;
        default: throw new IllegalArgumentException("Illegal Column Index " + columnIndex);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column) {
        switch(column) {
        case 0: return "";
        case 1: return tr("Name");
        case 2: return tr("Version");
        default: throw new IllegalArgumentException("Illegal Column Index " + column);
        }
    }

    /**
     * Installs all selected plugins.
     */
    public void install() {
        for(PluginDescription plugin : plugins) {
            if(plugin.isInstall()) {
                System.out.println("Installing plugin " + plugin.getName());
                plugin.install();
            }
        }

    }

    /**
     * @return the sites
     */
    public List<SiteDescription> getSites() {
        return this.sites;
    }

    /**
     * @param sites the sites to set
     */
    public void setSites(List<SiteDescription> sites) {
        this.sites = sites;
    }

    /**
     * @return the plugins
     */
    public List<PluginDescription> getPlugins() {
        return this.plugins;
    }

    /**
     * @param plugins the plugins to set
     */
    public void setPlugins(List<PluginDescription> plugins) {
        this.plugins = plugins;
    }





}
