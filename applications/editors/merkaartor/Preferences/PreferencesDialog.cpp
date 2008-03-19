//
// C++ Implementation: PreferencesDialog
//
// Description:
//
//
// Author: cbro <cbro@semperpax.com>, Bart Vanhauwaert (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#include "Preferences/PreferencesDialog.h"
#include "Preferences/WMSPreferencesDialog.h"
#include "Preferences/MerkaartorPreferences.h"
#include "PaintStyle/EditPaintStyle.h"

#include <QFileDialog>

PreferencesDialog::PreferencesDialog(QWidget* parent)
	: QDialog(parent)
{
	setupUi(this);
	for (int i=0; i < MerkaartorPreferences::instance()->getBgTypes().size(); ++i) {
		cbMapAdapter->insertItem(i, MerkaartorPreferences::instance()->getBgTypes()[i]);
	}

	loadPrefs();
}

PreferencesDialog::~PreferencesDialog()
{
}

void PreferencesDialog::on_buttonBox_clicked(QAbstractButton * button)
{
	if ((button == buttonBox->button(QDialogButtonBox::Apply))) {
		savePrefs();
	} else
		if ((button == buttonBox->button(QDialogButtonBox::Ok))) {
			savePrefs();
			this->accept();
		}
}

void PreferencesDialog::loadPrefs()
{
	edOsmUrl->setText(MerkaartorPreferences::instance()->getOsmWebsite());
	edOsmUser->setText(MerkaartorPreferences::instance()->getOsmUser());
    edOsmPwd->setText(MerkaartorPreferences::instance()->getOsmPassword());

	bbUseProxy->setChecked(MerkaartorPreferences::instance()->getProxyUse());
	edProxyHost->setText(MerkaartorPreferences::instance()->getProxyHost());
	edProxyPort->setText(QString().setNum(MerkaartorPreferences::instance()->getProxyPort()));

	edCacheDir->setText(MerkaartorPreferences::instance()->getCacheDir());
	sbCacheSize->setValue(MerkaartorPreferences::instance()->getCacheSize());

	cbMapAdapter->setCurrentIndex(MerkaartorPreferences::instance()->getBgType());
	if (MerkaartorPreferences::instance()->getBgType() != Bg_Wms) {
		btAdapterSetup->setEnabled(false);
		//grpWmsServers->setSizePolicy(QSizePolicy::Ignored, QSizePolicy::Ignored);
		//layout()->activate();
		//QApplication::processEvents();
		//setFixedSize(minimumSizeHint());
	}

	QString s = MerkaartorPreferences::instance()->getDefaultStyle();
	if (s == ":/Styles/Mapnik.mas")
		StyleMapnik->setChecked(true);
	else if (s== ":/Styles/Classic.mas")
		StyleClassic->setChecked(true);
	else
	{
		StyleCustom->setChecked(true);
		CustomStyleName->setEnabled(true);
		CustomStyleName->setText(s);
		BrowseStyle->setEnabled(true);
	}
}

void PreferencesDialog::savePrefs()
{
	MerkaartorPreferences::instance()->setOsmWebsite(edOsmUrl->text());
	MerkaartorPreferences::instance()->setOsmUser(edOsmUser->text());
	MerkaartorPreferences::instance()->setOsmPassword(edOsmPwd->text());
	MerkaartorPreferences::instance()->setProxyUse(bbUseProxy->isChecked());
	MerkaartorPreferences::instance()->setProxyHost(edProxyHost->text());
	MerkaartorPreferences::instance()->setProxyPort(edProxyPort->text().toInt());
	MerkaartorPreferences::instance()->setBgType((ImageBackgroundType)cbMapAdapter->currentIndex());

	MerkaartorPreferences::instance()->setCacheDir(edCacheDir->text());
	MerkaartorPreferences::instance()->setCacheSize(sbCacheSize->value());

	QString NewStyle;

	if (StyleMapnik->isChecked())
		NewStyle = ":/Styles/Mapnik.mas";
	else if (StyleClassic->isChecked())
		NewStyle = ":/Styles/Classic.mas";
	else
		NewStyle = CustomStyleName->text();

	if (NewStyle != MerkaartorPreferences::instance()->getDefaultStyle())
	{
		MerkaartorPreferences::instance()->setDefaultStyle(NewStyle);
		loadPainters(MerkaartorPreferences::instance()->getDefaultStyle());
	}
	MerkaartorPreferences::instance()->save();
}

void PreferencesDialog::on_cbMapAdapter_currentIndexChanged(int index)
{
	//grpWmsServers->setSizePolicy(QSizePolicy::Ignored, QSizePolicy::Ignored);
	btAdapterSetup->setEnabled(false);

	switch (index) {
		case Bg_Wms:
			//grpWmsServers->setSizePolicy(QSizePolicy::Preferred, QSizePolicy::Preferred);
			btAdapterSetup->setEnabled(true);
			break;
	}
	//layout()->activate();
	//QApplication::processEvents();
	//setFixedSize(minimumSizeHint());

}

void PreferencesDialog::on_BrowseStyle_clicked()
{
	QString s = QFileDialog::getOpenFileName(this,tr("Custom style"),"",tr("Merkaartor map style (*.mas)"));
	if (!s.isNull())
		CustomStyleName->setText(QDir::toNativeSeparators(s));
}

void PreferencesDialog::on_btAdapterSetup_clicked()
{
	switch (cbMapAdapter->currentIndex()) {
		case Bg_Wms:
			//grpWmsServers->setSizePolicy(QSizePolicy::Preferred, QSizePolicy::Preferred);
			WMSPreferencesDialog* WMSPref = new WMSPreferencesDialog();
			if (WMSPref->exec() == QDialog::Accepted) {
			}
			break;
	}
}