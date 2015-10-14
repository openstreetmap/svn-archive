package org.openstreetmap.josm.plugins.tofix.bean.items;

import org.openstreetmap.josm.plugins.tofix.bean.TaskCompleteBean;

/**
 *
 * @author ruben
 */
public class Item {

    private int status;
    private ItemKeeprightBean itemKeeprightBean;
    private ItemKrakatoaBean itemKrakatoaBean;
    private ItemUsaBuildingsBean itemUsabuildingsBean;
    private ItemTigerdeltaBean itemTigerdeltaBean;
    private ItemUnconnectedBean itemUnconnectedBean;
    private TaskCompleteBean taskCompleteBean;
    private ItemStrava itemStrava;
    private ItemSmallcomponents itemSmallcomponents;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public TaskCompleteBean getTaskCompleteBean() {
        return taskCompleteBean;
    }

    public void setTaskCompleteBean(TaskCompleteBean taskCompleteBean) {
        this.taskCompleteBean = taskCompleteBean;
    }

    public ItemKeeprightBean getItemKeeprightBean() {
        return itemKeeprightBean;
    }

    public void setItemKeeprightBean(ItemKeeprightBean itemKeeprightBean) {
        this.itemKeeprightBean = itemKeeprightBean;
    }

    public ItemKrakatoaBean getItemKrakatoaBean() {
        return itemKrakatoaBean;
    }

    public void setItemKrakatoaBean(ItemKrakatoaBean itemKrakatoaBean) {
        this.itemKrakatoaBean = itemKrakatoaBean;
    }

    public ItemUsaBuildingsBean getItemUsabuildingsBean() {
        return itemUsabuildingsBean;
    }

    public void setItemUsabuildingsBean(ItemUsaBuildingsBean itemNycbuildingsBean) {
        this.itemUsabuildingsBean = itemNycbuildingsBean;
    }

    public ItemTigerdeltaBean getItemTigerdeltaBean() {
        return itemTigerdeltaBean;
    }

    public void setItemTigerdeltaBean(ItemTigerdeltaBean itemTigerdeltaBean) {
        this.itemTigerdeltaBean = itemTigerdeltaBean;
    }

    public ItemUnconnectedBean getItemUnconnectedBean() {
        return itemUnconnectedBean;
    }

    public void setItemUnconnectedBean(ItemUnconnectedBean itemUnconnectedBean) {
        this.itemUnconnectedBean = itemUnconnectedBean;
    }

    public ItemStrava getItemStrava() {
        return itemStrava;
    }

    public void setItemStrava(ItemStrava itemStrava) {
        this.itemStrava = itemStrava;
    }

    public ItemSmallcomponents getItemSmallcomponents() {
        return itemSmallcomponents;
    }

    public void setItemSmallcomponents(ItemSmallcomponents itemSmallcomponents) {
        this.itemSmallcomponents = itemSmallcomponents;
    }

}
