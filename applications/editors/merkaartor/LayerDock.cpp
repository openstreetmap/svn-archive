#include "LayerDock.h"
#include "LayerWidget.h"

#include "MainWindow.h"
#include "Map/MapDocument.h"
#include "Map/MapLayer.h"

#include <QPushButton>

#define SAFE_DELETE(x) {delete (x); x = NULL;}

#define LINEHEIGHT 25

LayerDock::LayerDock(MainWindow* aMain)
: QDockWidget(aMain), Main(aMain), Scroller(0), Content(0), Layout(0), butGroup(0)
{
	setMinimumSize(220,100);
	setWindowTitle(tr("Layers"));
	setObjectName("layersDock");

	createContent();
}

LayerDock::~LayerDock()
{
}

void LayerDock::clearLayers()
{
	for (int i=layerList.size()-1; i >= 0; i--) {
		butGroup->removeButton(layerList[i].second);
		Layout->removeWidget(layerList[i].second);
		delete layerList[i].second;
		layerList.removeAt(i);
	}
}

void LayerDock::addLayer(MapLayer* aLayer)
{
	LayerWidget* w = aLayer->newWidget();
	layerList.append(qMakePair(aLayer, w));
	butGroup->addButton(w);
	Layout->insertWidget(layerList.size()-1, w);
	w->setChecked(aLayer->isSelected());

	connect(w, SIGNAL(layerChanged(LayerWidget*,bool)), this, SLOT(layerChanged(LayerWidget*,bool)));

	update();
}

void LayerDock::deleteLayer(MapLayer* aLayer)
{
	for (int i=layerList.size()-1; i >= 0; i--) {
		if (layerList[i].first == aLayer) {
			butGroup->removeButton(layerList[i].second);
			Layout->removeWidget(layerList[i].second);
			delete layerList[i].second;
			layerList.removeAt(i);
		}
	}

	update();
}

void LayerDock::createContent()
{
	delete Scroller;

	Scroller = new QScrollArea;
	Scroller->setBackgroundRole(QPalette::Base);
	Scroller->setHorizontalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
	QVBoxLayout scrollerLayout(Scroller);
	Content = new QGroupBox();
	Content->setFlat(true);
	Layout = new QVBoxLayout(Content);
	Layout->setSpacing(0);
	Layout->setMargin(0);

	butGroup = new QButtonGroup(Content);

	Layout->addStretch();
	setWidget(Scroller);
	Scroller->setWidget(Content);
	Scroller->setWidgetResizable(true);

	update();
}

void LayerDock::resizeEvent(QResizeEvent* )
{
}

MapLayer* LayerDock::activeLayer()
{
 	return ((LayerWidget *)butGroup->checkedButton())->getMapLayer();
}

void LayerDock::layerChanged(LayerWidget*, bool adjustViewport)
{
	emit(layersChanged(adjustViewport));
}

