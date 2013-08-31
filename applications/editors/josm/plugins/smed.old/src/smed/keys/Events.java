package smed.keys;

import java.awt.event.KeyEvent;
import java.util.ListResourceBundle;

public class Events extends ListResourceBundle {

	private Object[][] contents = {
			{ "SmedMenuBar.001", new Integer(KeyEvent.VK_F) },
	};

	@Override
	protected Object[][] getContents() {
		return contents;
	}
}
