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

LayerWidget::LayerWidget(QWidget* aParent)
: QAbstractButton(aParent), ctxMenu(0)
{
	setCheckable(true);
	setAutoExclusive(true) ;
	setFocusPolicy(Qt::NoFocus);
	visibleIcon = QPixmap(":Icons/eye.xpm");
	hiddenIcon = QPixmap(":Icons/empty.xpm");
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


// DrawingLayerWidget

DrawingLayerWidget::DrawingLayerWidget(DrawingMapLayer* aLayer, QWidget* aParent)
: LayerWidget(aParent)
{
	theLayer = aLayer;
	backColor = QColor(255,255,255);
}

// ImageLayerWidget

ImageLayerWidget::ImageLayerWidget(ImageMapLayer* aLayer, QWidget* aParent)
: LayerWidget(aParent) //, actgrWms(0)
{
	theLayer = aLayer;
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

#ifdef yahoo_illegal
	actYahoo = new QAction(MerkaartorPreferences::instance()->getBgTypes()[Bg_Yahoo_illegal], this);
	//actYahoo->setCheckable(true);
	actYahoo->setChecked((MerkaartorPreferences::instance()->getBgType() == Bg_Yahoo_illegal));
	connect(actYahoo, SIGNAL(triggered(bool)), this, SLOT(setYahoo(bool)));
#endif
#ifdef google_illegal
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

#ifdef yahoo_illegal
void ImageLayerWidget::setYahoo(bool)
{
	((ImageMapLayer *)theLayer)->setMapAdapter(Bg_Yahoo_illegal);
	theLayer->setVisible(true);

	this->update(rect());
	emit (layerChanged(this, true));
}
#endif

#ifdef google_illegal
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

	SAFE_DELETE(ctxMenu);

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
#ifdef yahoo_illegal
	actYahoo->setChecked((MerkaartorPreferences::instance()->getBgType() == Bg_Yahoo_illegal));
#endif
#ifdef google_illegal
	actGoogle->setChecked((MerkaartorPreferences::instance()->getBgType() == Bg_Google_illegal));
#endif

	ctxMenu = new QMenu(this);
	ctxMenu->addAction(actNone);

	ctxMenu->addMenu(wmsMenu);
	connect(wmsMenu, SIGNAL(triggered(QAction*)), this, SLOT(setWms(QAction*)));

	ctxMenu->addMenu(tmsMenu);
	connect(tmsMenu, SIGNAL(triggered(QAction*)), this, SLOT(setTms(QAction*)));

// 	ctxMenu->addAction(actOSM);
#ifdef yahoo_illegal
	ctxMenu->addAction(actYahoo);
#endif
#ifdef google_illegal
	ctxMenu->addAction(actGoogle);
#endif
}

// TrackLayerWidget

TrackLayerWidget::TrackLayerWidget(TrackMapLayer* aLayer, QWidget* /* aParent */)
{
	theLayer = aLayer;
	backColor = QColor(255,255,255);

	ctxMenu = new QMenu(this);

	QAction* actExtract = new QAction("Extract Drawing layer", ctxMenu);
	ctxMenu->addAction(actExtract);
	connect(actExtract, SIGNAL(triggered(bool)), this, SLOT(extractLayer(bool)));
}

TrackLayerWidget::~TrackLayerWidget()
{
}

void TrackLayerWidget::extractLayer(bool)
{
	((TrackMapLayer*)theLayer)->extractLayer();
	emit (layerChanged(this, false));
}
