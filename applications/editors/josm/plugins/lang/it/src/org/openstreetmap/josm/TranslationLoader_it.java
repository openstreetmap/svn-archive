package org.openstreetmap.josm;

import java.util.Locale;

import org.openstreetmap.josm.tools.I18n;
import org.xnap.commons.i18n.I18nFactory;

public class TranslationLoader_it {

	public TranslationLoader_it() throws Exception {
		Class<?> c = Class.forName("org.openstreetmap.josm.Translation_it");
		Locale.setDefault(new Locale("it"));
		I18n.i18n = I18nFactory.getI18n(c);
	}
}
