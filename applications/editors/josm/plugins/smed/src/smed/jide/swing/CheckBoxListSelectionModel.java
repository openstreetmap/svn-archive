// License: GPL. For details, see LICENSE file.
// Copyright (c) 2010 by Werner Koenig
// this is a modified version of CheckBoxListSelectionModel.java,
// which is part of the jide-oss, see https://jide-oss.dev.java.net


package smed.jide.swing;

import javax.swing.*;

public class CheckBoxListSelectionModel extends DefaultListSelectionModel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ListModel _model;

    public CheckBoxListSelectionModel() {
        setSelectionMode(MULTIPLE_INTERVAL_SELECTION);
    }

    public CheckBoxListSelectionModel(ListModel model) {
        _model = model;
        setSelectionMode(MULTIPLE_INTERVAL_SELECTION);
    }

    public ListModel getModel() {
        return _model;
    }

    public void setModel(ListModel model) {
        int oldLength = 0;
        int newLength = 0;
        if (_model != null) {
            oldLength = _model.getSize();
        }
        _model = model;
        if (_model != null) {
            newLength = _model.getSize();
        }
        if (oldLength > newLength) {
            removeIndexInterval(newLength, oldLength);
        }
    }

    /**
     * Overrides so that inserting a row will not be selected automatically if the row after it is selected.
     *
     * @param index  the index where the rows will be inserted.
     * @param length the number of the rows that will be inserted.
     * @param before it's before or after the index.
     */
    @Override
    public void insertIndexInterval(int index, int length, boolean before) {
        if (before) {
            boolean old = isSelectedIndex(index);
            super.setValueIsAdjusting(true);
            try {
                if (old) {
                    removeSelectionInterval(index, index);
                }
                super.insertIndexInterval(index, length, before);
                if (old) {
                    addSelectionInterval(index + length, index + length);
                }
            }
            finally {
                super.setValueIsAdjusting(false);
            }
        }
        else {
            super.insertIndexInterval(index, length, before);
        }
    }
}
