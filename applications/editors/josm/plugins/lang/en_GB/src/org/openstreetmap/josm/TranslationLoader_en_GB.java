package org.openstreetmap.josm;

import java.util.Locale;

import org.openstreetmap.josm.tools.I18n;
import org.xnap.commons.i18n.I18nFactory;

public class TranslationLoader_en_GB {

	public TranslationLoader_en_GB() throws Exception {
		Class<?> c = Class.forName("org.openstreetmap.josm.Translation_en_GB");
		Locale.setDefault(Locale.UK);
		I18n.i18n = I18nFactory.getI18n(c);
	}
}
