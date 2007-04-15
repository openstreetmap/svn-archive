package org.openstreetmap.josm;

import java.util.Locale;

import org.openstreetmap.josm.tools.I18n;
import org.xnap.commons.i18n.I18nFactory;

public class TranslationLoader_fr {

	public TranslationLoader_fr() throws Exception {
		Class<?> c = Class.forName("org.openstreetmap.josm.Translation_fr");
		Locale.setDefault(Locale.FRENCH);
		I18n.i18n = I18nFactory.getI18n(c);
	}
}
