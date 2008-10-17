package org.openstreetmap.josm;

import java.util.Locale;

import org.openstreetmap.josm.tools.I18n;
import org.xnap.commons.i18n.I18nFactory;

public class TranslationLoader_ru {

	public TranslationLoader_ru() throws Exception {
		Class<?> c = Class.forName("org.openstreetmap.josm.Translation_ru");
		Locale.setDefault(new Locale("ru"));
		I18n.i18n = I18nFactory.getI18n(c);
	}
}
