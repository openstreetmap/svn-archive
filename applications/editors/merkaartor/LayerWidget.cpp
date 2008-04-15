#include "LayerWidget.h"
#include "LayerDock.h"

#include "MainWindow.h"
#include "Map/MapDocument.h"
#include "Map/MapLayer.h"
#include "MapView.h"
#include "Preferences/MerkaartorPreferences.h"

#include <QtGui/QMouseEvent>
#include <QtGui/QPainter>

#define SAFE_DELETE(x) {delete (x); x = NULL;}

#define LINEHEIGHT 25

LayerWidget::LayerWidget(MapLayer* aLayer, QWidget* aParent)
: QAbstractButton(aParent), theLayer(aLayer), ctxMenu(0)
{
	setCheckable(true);
	setAutoExclusive(true) ;
	setFocusPolicy(Qt::NoFocus);
	visibleIcon = QPixmap(":Icons/eye.xpm");
	hiddenIcon = QPixmap(":Icons/empty.xpm");

	initActions();
}

QSize LayerWidget::minimumSizeHint () const
{
	return QSize(100, LINEHEIGHT);
}

QSize LayerWidget::sizeHint () const
{
	return QSize(100, LINEHEIGHT);
}

void LayerWidget::paintEvent(QPaintEvent*)
{
	QPainter P(this);

	P.drawLine(rect().bottomLeft(), rect().bottomRight());
	if (theLayer->isSelected()) {
		P.fillRect(rect().adjusted(20,0,0,-1),QBrush(palette().highlight()));
//		P.fillRect(20, 1, width()-19, rect().height()-1, QBrush(palette().highlight()));
		P.setPen(palette().highlightedText().color());
		P.drawText(rect().adjusted(23,0,0,-1), Qt::AlignLeft | Qt::AlignVCenter , theLayer->name());
	} else {
		P.fillRect(rect().adjusted(0,0,0,-1),backColor);
		P.setPen(QColor(0,0,0));
		P.drawText(rect().adjusted(23,0,0,-1), Qt::AlignLeft | Qt::AlignVCenter , theLayer->name());
	}

	if (theLayer->isVisible())
		P.drawPixmap(QPoint(2, rect().center().y()-visibleIcon.height()/2), visibleIcon);
	else
		P.drawPixmap(QPoint(2, rect().center().y()-hiddenIcon.height()/2), hiddenIcon);
}

void LayerWidget::mouseReleaseEvent(QMouseEvent* anEvent)
{
	if (anEvent->pos().x()<20)
	{
		theLayer->setVisible(!theLayer->isVisible());
		anEvent->ignore();
		update();
		emit(layerChanged(this, false));
	}
	else
	{
		if (!(dynamic_cast<ImageLayerWidget *>(this)))
			toggle();
	}
}

void LayerWidget::checkStateSet()
{
	theLayer->setSelected(isChecked());
	//emit (layerChanged(this));
}

MapLayer* LayerWidget::getMapLayer()
{
	return theLayer;
}

void LayerWidget::contextMenuEvent(QContextMenuEvent* anEvent)
{
	if (ctxMenu)
		ctxMenu->exec(anEvent->globalPos());
}

void LayerWidget::initActions()
{
	SAFE_DELETE(ctxMenu);
	ctxMenu = new QMenu(this);

	QStringList opStr;
	opStr << tr("Low") << tr("High") << tr("Opaque");

	QActionGroup* actgrp = new QActionGroup(this);
	QMenu* alphaMenu = new QMenu(tr("Opacity"), this);
	for (int i=0; i<opStr.size(); i++) {
		QAction* act = new QAction(opStr[i], alphaMenu);
		actgrp->addAction(act);
		qreal a = MerkaartorPreferences::instance()->getAlpha(opStr[i]);
		act->setData(a);
		act->setCheckable(true);
		if (theLayer->getAlpha() == a)
			act->setChecked(true);
		alphaMenu->addAction(act);
	}
	ctxMenu->addMenu(alphaMenu);
	connect(alphaMenu, SIGNAL(triggered(QAction*)), this, SLOT(setOpacity(QAction*)));

	QAction* closeAction = new QAction(tr("Delete"), this);
	connect(closeAction, SIGNAL(triggered()), this, SLOT(close()));
	ctxMenu->addAction(closeAction);
}

void LayerWidget::setOpacity(QAction *act)
{
	theLayer->setAlpha(act->data().toDouble());
	emit (layerChanged(this, false));
}

void LayerWidget::close()
{
	emit(layerClosed(theLayer));
}

void LayerWidget::zoomLayer(bool)
{
	emit (layerZoom(theLayer));
}


// DrawingLayerWidget

DrawingLayerWidget::DrawingLayerWidget(DrawingMapLayer* aLayer, QWidget* aParent)
	: LayerWidget(aLayer, aParent)
{
	backColor = QColor(255,255,255);
	initActions();
}

void DrawingLayerWidget::initActions()
{
	LayerWidget::initActions();
	ctxMenu->addSeparator();

	QAction* actZoom = new QAction(tr("Zoom"), ctxMenu);
	ctxMenu->addAction(actZoom);
	connect(actZoom, SIGNAL(triggered(bool)), this, SLOT(zoomLayer(bool)));
}

// ImageLayerWidget

ImageLayerWidget::ImageLayerWidget(ImageMapLayer* aLayer, QWidget* aParent)
: LayerWidget(aLayer, aParent), wmsMenu(0) //, actgrWms(0)
{
	backColor = QColor(128,128,128);
	//actgrAdapter = new QActionGroup(this);

	actNone = new QAction(MerkaartorPreferences::instance()->getBgTypes()[Bg_None], this);
	//actNone->setCheckable(true);
	actNone->setChecked((MerkaartorPreferences::instance()->getBgType() == Bg_None));
	connect(actNone, SIGNAL(triggered(bool)), this, SLOT(setNone(bool)));

// 	actOSM = new QAction(MerkaartorPreferences::instance()->getBgTypes()[Bg_OSM], this);
// 	//actNone->setCheckable(true);
// 	actOSM->setChecked((MerkaartorPreferences::instance()->getBgType() == Bg_OSM));
// 	connect(actOSM, SIGNAL(triggered(bool)), this, SLOT(setOSM(bool)));

#ifdef YAHOO
	actLegalYahoo = new QAction(MerkaartorPreferences::instance()->getBgTypes()[Bg_Yahoo], this);
	//actYahoo->setCheckable(true);
	actLegalYahoo->setChecked((MerkaartorPreferences::instance()->getBgType() == Bg_Yahoo));
	connect(actLegalYahoo, SIGNAL(triggered(bool)), this, SLOT(setLegalYahoo(bool)));
#endif
#ifdef YAHOO_ILLEGAL
	actYahoo = new QAction(MerkaartorPreferences::instance()->getBgTypes()[Bg_Yahoo_illegal], this);
	//actYahoo->setCheckable(true);
	actYahoo->setChecked((MerkaartorPreferences::instance()->getBgType() == Bg_Yahoo_illegal));
	connect(actYahoo, SIGNAL(triggered(bool)), this, SLOT(setYahoo(bool)));
#endif
#ifdef GOOGLE_ILLEGAL
	actGoogle = new QAction(MerkaartorPreferences::instance()->getBgTypes()[Bg_Google_illegal], this);
	//actGoogle->setCheckable(true);
	actGoogle->setChecked((MerkaartorPreferences::instance()->getBgType() == Bg_Google_illegal));
	connect(actGoogle, SIGNAL(triggered(bool)), this, SLOT(setGoogle(bool)));
#endif
	initActions();
}

ImageLayerWidget::~ImageLayerWidget()
{
}

void ImageLayerWidget::setWms(QAction* act)
{
	WmsServerList* L = MerkaartorPreferences::instance()->getWmsServers();
	WmsServer S = L->value(act->data().toString());
	MerkaartorPreferences::instance()->setSelectedWmsServer(S.WmsName);

	((ImageMapLayer *)theLayer)->setMapAdapter(Bg_Wms);
	theLayer->setVisible(true);

	this->update(rect());
	emit (layerChanged(this, true));
}

void ImageLayerWidget::setTms(QAction* act)
{
	TmsServerList* L = MerkaartorPreferences::instance()->getTmsServers();
	TmsServer S = L->value(act->data().toString());
	MerkaartorPreferences::instance()->setSelectedTmsServer(S.TmsName);

	((ImageMapLayer *)theLayer)->setMapAdapter(Bg_Tms);
	theLayer->setVisible(true);

	this->update(rect());
	emit (layerChanged(this, true));
}

#ifdef YAHOO
void ImageLayerWidget::setLegalYahoo(bool)
{
	((ImageMapLayer *)theLayer)->setMapAdapter(Bg_Yahoo);
	theLayer->setVisible(true);

	this->update(rect());
	emit (layerChanged(this, true));
}
#endif

#ifdef YAHOO_ILLEGAL
void ImageLayerWidget::setYahoo(bool)
{
	((ImageMapLayer *)theLayer)->setMapAdapter(Bg_Yahoo_illegal);
	theLayer->setVisible(true);

	this->update(rect());
	emit (layerChanged(this, true));
}
#endif

#ifdef GOOGLE_ILLEGAL
void ImageLayerWidget::setGoogle(bool)
{
	((ImageMapLayer *)theLayer)->setMapAdapter(Bg_Google_illegal);
	theLayer->setVisible(true);

	this->update(rect());
	emit (layerChanged(this, true));
}
#endif

void ImageLayerWidget::setNone(bool)
{
	((ImageMapLayer *)theLayer)->setMapAdapter(Bg_None);

	this->update(rect());
	emit (layerChanged(this, true));
}

/*void ImageLayerWidget::setOSM(bool)
{
	((ImageMapLayer *)theLayer)->setMapAdapter(Bg_OSM);
	theLayer->setVisible(true);

	this->update(rect());
	emit (layerChanged(this, true));
}
*/

void ImageLayerWidget::initActions()
{
	//if (actgrWms)
	//	delete actgrWms;
	//actgrWms = new QActionGroup(this);

	LayerWidget::initActions();
	ctxMenu->addSeparator();

	wmsMenu = new QMenu(MerkaartorPreferences::instance()->getBgTypes()[Bg_Wms], this);
	WmsServerList* WmsServers = MerkaartorPreferences::instance()->getWmsServers();
	WmsServerListIterator wi(*WmsServers);
	while (wi.hasNext()) {
		wi.next();
		WmsServer S = wi.value();
		QAction* act = new QAction(S.WmsName, wmsMenu);
		act->setData(S.WmsName);
		//act->setCheckable(true);
		wmsMenu->addAction(act);
		//actgrAdapter->addAction(act);
		//actgrWms->addAction(act);
		if (MerkaartorPreferences::instance()->getBgType() == Bg_Wms)
			if (S.WmsName == MerkaartorPreferences::instance()->getSelectedWmsServer())
				act->setChecked(true);
	}

	tmsMenu = new QMenu(MerkaartorPreferences::instance()->getBgTypes()[Bg_Tms], this);
	TmsServerList* TmsServers = MerkaartorPreferences::instance()->getTmsServers();
	TmsServerListIterator ti(*TmsServers);
	while (ti.hasNext()) {
		ti.next();
		TmsServer S = ti.value();
		QAction* act = new QAction(S.TmsName, tmsMenu);
		act->setData(S.TmsName);
		tmsMenu->addAction(act);
		if (MerkaartorPreferences::instance()->getBgType() == Bg_Tms)
			if (S.TmsName == MerkaartorPreferences::instance()->getSelectedTmsServer())
				act->setChecked(true);
	}

	actNone->setChecked((MerkaartorPreferences::instance()->getBgType() == Bg_None));
#ifdef YAHOO
	actLegalYahoo->setChecked((MerkaartorPreferences::instance()->getBgType() == Bg_Yahoo));
#endif
#ifdef YAHOO_ILLEGAL
	actYahoo->setChecked((MerkaartorPreferences::instance()->getBgType() == Bg_Yahoo_illegal));
#endif
#ifdef GOOGLE_ILLEGAL
	actGoogle->setChecked((MerkaartorPreferences::instance()->getBgType() == Bg_Google_illegal));
#endif

	ctxMenu->addAction(actNone);

	ctxMenu->addMenu(wmsMenu);
	connect(wmsMenu, SIGNAL(triggered(QAction*)), this, SLOT(setWms(QAction*)));

	ctxMenu->addMenu(tmsMenu);
	connect(tmsMenu, SIGNAL(triggered(QAction*)), this, SLOT(setTms(QAction*)));

// 	ctxMenu->addAction(actOSM);
#ifdef YAHOO
	ctxMenu->addAction(actLegalYahoo);
#endif
#ifdef YAHOO_ILLEGAL
	ctxMenu->addAction(actYahoo);
#endif
#ifdef GOOGLE_ILLEGAL
	ctxMenu->addAction(actGoogle);
#endif
}

// TrackLayerWidget

TrackLayerWidget::TrackLayerWidget(TrackMapLayer* aLayer, QWidget* aParent)
	: LayerWidget(aLayer, aParent)
{
	backColor = QColor(255,255,255);
	initActions();
}

void TrackLayerWidget::initActions()
{
	LayerWidget::initActions();
	ctxMenu->addSeparator();

	QAction* actExtract = new QAction(tr("Extract Drawing layer"), ctxMenu);
	ctxMenu->addAction(actExtract);
	connect(actExtract, SIGNAL(triggered(bool)), this, SLOT(extractLayer(bool)));

	QAction* actZoom = new QAction(tr("Zoom"), ctxMenu);
	ctxMenu->addAction(actZoom);
	connect(actZoom, SIGNAL(triggered(bool)), this, SLOT(zoomLayer(bool)));
}

TrackLayerWidget::~TrackLayerWidget()
{
}

void TrackLayerWidget::extractLayer(bool)
{
	((TrackMapLayer*)theLayer)->extractLayer();
	emit (layerChanged(this, false));
}


