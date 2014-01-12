package org.openstreetmap.josm.plugins.czechaddress.gui.databaseeditors;

import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Street;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Suburb;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Reasoner;

/**
 * Utitilies for editing the database
 *
 * @author Radomír Ćernoch
 */
public abstract class EditorFactory {

    public static boolean isEditable(AddressElement elem) {
        return (elem instanceof House) ||
               (elem instanceof Street) ||
               (elem instanceof Suburb);
    }

    public static boolean edit(AddressElement elem) {
        if (elem instanceof House)
            return editHouse((House) elem);

        if (elem instanceof Street)
            return editStreet((Street) elem);

        return false;
    }

    public static boolean editStreet(Street street) {
        StreetEditor dialog = (StreetEditor) new StreetEditor(street).showDialog();
        if (dialog.getValue() == 1) {
            Reasoner r = Reasoner.getInstance();
            synchronized (r) {
                r.openTransaction();
                street.setName(dialog.getStreetName());
                for (House house : street.getHouses())
                    r.update(house);
                r.update(street);
                r.closeTransaction();
            }
            return true;
        }
        return false;
    }

    public static boolean editHouse(House house) {
        HouseEditor dialog = (HouseEditor) new HouseEditor(house).showDialog();
        if (dialog.getValue() == 1) {
            Reasoner r = Reasoner.getInstance();
            synchronized (r) {
                r.openTransaction();
                house.setCP(dialog.getCP());
                house.setCO(dialog.getCO());
                r.update(house);
                r.closeTransaction();
            }
            return true;
        }
        return false;
    }

    public static boolean editSuburb(Suburb suburb) {
        SuburbEditor dialog = (SuburbEditor) new SuburbEditor(suburb).showDialog();
        if (dialog.getValue() == 1) {
            suburb.setName(dialog.getSuburbName());
            return true;
        }
        return false;
    }
}
