#include "TrackPoint.h"

#include "Map/Projection.h"
#include "Utils/LineF.h"

#include <QtGui/QPainter>
#include <QProgressDialog>

TrackPoint::TrackPoint(const Coord& aCoord)
: Position(aCoord), Elevation(0.0), Speed(0.0)
{
}

TrackPoint::TrackPoint(const TrackPoint& other)
: MapFeature(other), Position(other.Position), Elevation(other.Elevation), Speed(other.Speed), Projected(other.Projected)
{
	setTime(other.time());
}

TrackPoint::~TrackPoint(void)
{
}

void TrackPoint::remove(unsigned int )
{
}

void TrackPoint::remove(MapFeature*)
{
}

unsigned int TrackPoint::size() const
{
	return 0;
}

unsigned int TrackPoint::find(MapFeature* ) const
{
	return 0;
}

MapFeature* TrackPoint::get(unsigned int )
{
	return NULL;
}

const MapFeature* TrackPoint::get(unsigned int ) const
{
	return NULL;
}

bool TrackPoint::isNull() const
{
	return Position.isNull();
}

const Coord& TrackPoint::position() const
{
	return Position;
}

void TrackPoint::setPosition(const Coord& aCoord)
{
	Position = aCoord;
	Projected = QPoint();
	notifyChanges();
}

const QPoint& TrackPoint::projection() const
{
	return Projected;
}

void TrackPoint::setProjection(const QPoint& aProjection)
{
	Projected = aProjection;
}

ProjectionType TrackPoint::projectionType() const
{
	return ProjectedType;
}

void TrackPoint::setProjectionType(const ProjectionType aProjectionType)
{
	ProjectedType = aProjectionType;
}

double TrackPoint::speed() const
{
	return Speed;
}

void TrackPoint::setSpeed(double aSpeed)
{
	Speed = aSpeed;
}

double TrackPoint::elevation() const
{
	return Elevation;
}

void TrackPoint::setElevation(double aElevation)
{
	Elevation = aElevation;
}

bool TrackPoint::notEverythingDownloaded() const
{
	return lastUpdated() == MapFeature::NotYetDownloaded;
}

CoordBox TrackPoint::boundingBox() const
{
	return CoordBox(Position,Position);
}

#ifdef GEOIMAGE
void TrackPoint::draw(QPainter& thePainter , const Projection& theProjection )
{
	if (!tagValue("Picture", "").isEmpty()) {
		QPoint me = theProjection.project(this);
		thePainter.setPen(QPen(QColor(0, 0, 0), 2));
		QRect box(me - QPoint(5, 3), me + QPoint(5, 3));
		thePainter.drawRect(box);
	}
}
#else
void TrackPoint::draw(QPainter& /* thePainter */, const Projection& /*theProjection*/ )
{
}
#endif

void TrackPoint::drawFocus(QPainter& thePainter, const Projection& theProjection, bool solid)
{
	thePainter.setPen(MerkaartorPreferences::instance()->getFocusColor());
	QPoint P(theProjection.project(this));
	QRect R(P-QPoint(3,3),QSize(6,6));
	thePainter.drawRect(R);
	R.adjust(-7, -7, 7, 7);
	thePainter.drawEllipse(R);

	if (M_PREFS->getShowParents() && solid) {
		for (unsigned int i=0; i<sizeParents(); ++i)
			getParent(i)->drawFocus(thePainter, theProjection, false);
	}
}

void TrackPoint::drawHover(QPainter& thePainter, const Projection& theProjection, bool solid)
{
	thePainter.setPen(MerkaartorPreferences::instance()->getHoverColor());
	QPoint P(theProjection.project(this));
	QRect R(P-QPoint(3,3),QSize(6,6));
	thePainter.drawRect(R);
	R.adjust(-7, -7, 7, 7);
	thePainter.drawEllipse(R);

	if (M_PREFS->getShowParents() && solid) {
		for (unsigned int i=0; i<sizeParents(); ++i)
			getParent(i)->drawHover(thePainter, theProjection, false);
	}
}

double TrackPoint::pixelDistance(const QPointF& Target, double, const Projection& theProjection) const
{
	return distance(Target,theProjection.project(Position));
}

void TrackPoint::cascadedRemoveIfUsing(MapDocument*, MapFeature*, CommandList*, const std::vector<MapFeature*>&)
{
}

QString TrackPoint::description() const
{
	QString s(tagValue("name",""));
	if (!s.isEmpty())
		return QString("%1 (%2)").arg(s).arg(id());
	return
		QString("%1").arg(id());
}

void TrackPoint::partChanged(MapFeature*, unsigned int)
{
}

RenderPriority TrackPoint::renderPriority(double) const
{
	return RenderPriority(RenderPriority::IsSingular,0);
}

QString TrackPoint::toXML(unsigned int lvl, QProgressDialog * progress)
{
	if (progress)
		progress->setValue(progress->value()+1);

	QString S(lvl*2, ' ');
	S += "<node id=\"%1\" lat=\"%2\" lon=\"%3\">\n";
	S += tagsToXML(lvl+1);
	S += QString(lvl*2, ' ') + "</node>\n";
	return S.arg(stripToOSMId(id())).arg(intToAng(position().lat()),0,'f',8).arg(intToAng(position().lon()),0,'f',8);
}

bool TrackPoint::toXML(QDomElement xParent, QProgressDialog & progress)
{
	bool OK = true;

	QDomElement e = xParent.ownerDocument().createElement("node");
	xParent.appendChild(e);

	e.setAttribute("id", xmlId());
	e.setAttribute("lon",QString::number(intToAng(Position.lon()),'f',8));
	e.setAttribute("lat", QString::number(intToAng(Position.lat()),'f',8));
	e.setAttribute("timestamp", time().toString(Qt::ISODate)+"Z");
	e.setAttribute("user", user());
	e.setAttribute("actor", (int)lastUpdated());

	tagsToXML(e);

	progress.setValue(progress.value()+1);
	return OK;
}

bool TrackPoint::toGPX(QDomElement xParent, QProgressDialog & progress)
{
	bool OK = true;

	QString s = tagValue("_waypoint_","");
	QDomElement e;
	if (!s.isEmpty())
		e = xParent.ownerDocument().createElement("wpt");
	else
		e = xParent.ownerDocument().createElement("trkpt");
	xParent.appendChild(e);

	e.setAttribute("xml:id", xmlId());
	e.setAttribute("lon",QString::number(intToAng(Position.lon()),'f',8));
	e.setAttribute("lat", QString::number(intToAng(Position.lat()),'f',8));

	QDomElement c = xParent.ownerDocument().createElement("time");
	e.appendChild(c);
	QDomText v = c.ownerDocument().createTextNode(time().toString(Qt::ISODate)+"Z");
	c.appendChild(v);

	s = tagValue("name","");
	if (!s.isEmpty()) {
		QDomElement c = xParent.ownerDocument().createElement("name");
		e.appendChild(c);
		QDomText v = c.ownerDocument().createTextNode(s);
		c.appendChild(v);
	}
	if (elevation()) {
		QDomElement c = xParent.ownerDocument().createElement("ele");
		e.appendChild(c);
		QDomText v = c.ownerDocument().createTextNode(QString::number(elevation(),'f',6));
		c.appendChild(v);
	}
	s = tagValue("_comment_","");
	if (!s.isEmpty()) {
		QDomElement c = xParent.ownerDocument().createElement("cmt");
		e.appendChild(c);
		QDomText v = c.ownerDocument().createTextNode(s);
		c.appendChild(v);
	}
	s = tagValue("_description_","");
	if (!s.isEmpty()) {
		QDomElement c = xParent.ownerDocument().createElement("desc");
		e.appendChild(c);
		QDomText v = c.ownerDocument().createTextNode(s);
		c.appendChild(v);
	}

	progress.setValue(progress.value()+1);
	return OK;
}

TrackPoint * TrackPoint::fromXML(MapDocument* d, MapLayer* L, const QDomElement e)
{
	double Lat = e.attribute("lat").toDouble();
	double Lon = e.attribute("lon").toDouble();

	QDateTime time;
	time = QDateTime::fromString(e.attribute("timestamp").left(19), "yyyy-MM-ddTHH:mm:ss");
	QString user = e.attribute("user");
	MapFeature::ActorType A = (MapFeature::ActorType)(e.attribute("actor", "2").toInt());

	QString id = (e.hasAttribute("id") ? e.attribute("id") : e.attribute("xml:id"));
	if (!id.startsWith('{'))
		id = "node_" + id;
	TrackPoint* Pt = dynamic_cast<TrackPoint*>(d->getFeature(id));
	if (!Pt) {
		Pt = new TrackPoint(Coord(angToInt(Lat),angToInt(Lon)));
		Pt->setId(id);
		Pt->setLastUpdated(A);
		L->add(Pt);
	} else {
		if (Pt->layer() != L) {
			Pt->layer()->remove(Pt);
			L->add(Pt);
		}
		Pt->setPosition(Coord(angToInt(Lat), angToInt(Lon)));
		if (Pt->lastUpdated() == MapFeature::NotYetDownloaded)
			Pt->setLastUpdated(A);
	}
	Pt->setTime(time);
	Pt->setUser(user);

	MapFeature::tagsFromXML(d, Pt, e);

	return Pt;
}

TrackPoint * TrackPoint::fromGPX(MapDocument* d, MapLayer* L, const QDomElement e)
{
	double Lat = e.attribute("lat").toDouble();
	double Lon = e.attribute("lon").toDouble();

	QString id = (e.hasAttribute("id") ? e.attribute("id") : e.attribute("xml:id"));
	if (!id.startsWith('{'))
		id = "node_" + id;

	TrackPoint* Pt = dynamic_cast<TrackPoint*>(d->getFeature(id));
	if (!Pt) {
		Pt = new TrackPoint(Coord(angToInt(Lat),angToInt(Lon)));
		Pt->setId(id);
		Pt->setLastUpdated(MapFeature::Log);
		L->add(Pt);
	} else {
		Pt->setPosition(Coord(angToInt(Lat), angToInt(Lon)));
		if (Pt->lastUpdated() == MapFeature::NotYetDownloaded)
			Pt->setLastUpdated(MapFeature::OSMServer);
	}

	if (e.tagName() == "wpt")
		Pt->setTag("_waypoint_", "yes");

	QDateTime time;
	QDomElement c = e.firstChildElement();
	while(!c.isNull()) {
		if (c.tagName() == "time") {
			QString dtm = c.text();
			Pt->setTime(QDateTime::fromString(dtm.left(19), "yyyy-MM-ddTHH:mm:ss"));
		} else
		if (c.tagName() == "ele") {
			Pt->setElevation(c.text().toFloat());
		} else
		if (c.tagName() == "cmt") {
			Pt->setTag("_comment_", c.text(), false);
		} else
		if (c.tagName() == "desc") {
			Pt->setTag("_description_", c.text(), false);
		}

		c = c.nextSiblingElement();
	}

	return Pt;
}

QString TrackPoint::toHtml()
{
	QString D;
	unsigned int i;


	D += "<i>"+QApplication::translate("MapFeature", "timestamp")+": </i>" + time().toString(Qt::ISODate) + "<br/>";
	D += "<i>"+QApplication::translate("MapFeature", "coord")+": </i>" + QString::number(intToAng(position().lat()), 'f', 4) + " / " + QString::number(intToAng(position().lon()), 'f', 4) + "<br/>";

	if (elevation())
		D += "<i>"+QApplication::translate("MapFeature", "elevation")+": </i>" + QString::number(elevation(), 'f', 4) + "<br/>";
	if (speed())
		D += "<i>"+QApplication::translate("MapFeature", "speed")+": </i>" + QString::number(speed(), 'f', 4) + "<br/>";

	if ((i = findKey("_waypoint_")) < tagSize()) {
		D += "<p><b>"+QApplication::translate("MapFeature", "Waypoint")+"</b><br/>";

		if ((i = findKey("_description_")) < tagSize())
			D += "<i>"+QApplication::translate("MapFeature", "description")+": </i>" + tagValue(i) + "<br/>";

		if ((i = findKey("_comment_")) < tagSize())
			D += "<i>"+QApplication::translate("MapFeature", "comment")+": </i>" + tagValue(i) + "<br/>";
	}

	D += "<i>"+QApplication::translate("MapFeature", "layer")+": </i>";
	if (layer())
		D += layer()->name();
	D += "<br/>";
	
	return MapFeature::toMainHtml(QApplication::translate("MapFeature", "Node"), "node").arg(D);
}

void TrackPoint::toBinary(QDataStream& ds, QHash <QString, quint64>& theIndex)
{
	Q_UNUSED(theIndex);

	theIndex["N" + QString::number(idToLong())] = ds.device()->pos();
	ds << (qint8)'N' << idToLong() << (qint32)(Position.lon()) << (qint32)(Position.lat());
}

TrackPoint* TrackPoint::fromBinary(MapDocument* d, OsbMapLayer* L, QDataStream& ds, qint8 c, qint64 id)
{
	Q_UNUSED(c);
//	Q_ASSERT(id != 27145981);

	qint32	lon;
	qint32	lat;
	QString strId;

	ds >> lon;
	ds >> lat;

	if (!L)
		return NULL;

	Coord cd( lat, lon );
	if (id < 1)
		strId = QString::number(id);
	else
		strId = QString("node_%1").arg(QString::number(id));

	TrackPoint* Pt = CAST_NODE(d->getFeature(strId));
	if (!Pt) {
		Pt = new TrackPoint(cd);
		Pt->setId(strId);
		Pt->setLastUpdated(MapFeature::OSMServer);
		L->add(Pt);
	} else {
		Pt->setPosition(cd);
		if (Pt->lastUpdated() == MapFeature::NotYetDownloaded)
			Pt->setLastUpdated(MapFeature::OSMServer);
	}

	return Pt;
}
