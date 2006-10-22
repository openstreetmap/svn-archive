package org.openstreetmap.josm;

import java.util.Locale;

import org.openstreetmap.josm.tools.I18n;
import org.xnap.commons.i18n.I18nFactory;

public class TranslationLoader_ro {

	public TranslationLoader_ro() throws Exception {
		Class<?> c = Class.forName("org.openstreetmap.josm.Translation_ro");
		Locale.setDefault(new Locale("ro"));
		I18n.i18n = I18nFactory.getI18n(c);
	}
}
