#include <QPixmap>
#include <QPainter>
#include <QWheelEvent>
#include <QSvgRenderer>

#include "PixmapWidget.h"
#include "Preferences/MerkaartorPreferences.h"

PixmapWidget::PixmapWidget( QWidget *parent )
	: QWidget( parent ), m_pm(0), m_svgr(0), zoomFactor(1.0), done(false)
{
}

void PixmapWidget::loadFile( const QString &filename )
{
	m_pm = new QPixmap( filename );
	// All that follows in not used, as QPixmap loads SVG
	//   but could be useful if we want to do a real SVG preview
	if (m_pm->isNull()) {
		SAFE_DELETE(m_pm);
		m_svgr = new QSvgRenderer(filename, this);
		if (m_svgr->isValid())
			SAFE_DELETE(m_svgr);
	}

	if (!m_pm && m_svgr) {
		m_pm = new QPixmap(width(), height());
		QPainter p(m_pm);
		m_svgr->render(&p);
	}
}

void PixmapWidget::setPixmap( const QPixmap &aPix )
{
	m_pm = new QPixmap( aPix );
}

QPixmap* PixmapWidget::pixmap()
{
	return m_pm;
}

bool PixmapWidget::isPixmap()
{
	return (m_pm != NULL && m_svgr == NULL);
}

bool PixmapWidget::isSvg()
{
	return (m_svgr != NULL);
}

PixmapWidget::~PixmapWidget()
{
	delete m_pm;
}

void PixmapWidget::setZoomFactor( float f )
{
	if( f == zoomFactor )
		return;

	zoomFactor = f;
	emit( zoomFactorChanged( zoomFactor ) );

	update();
}

void PixmapWidget::paintEvent( QPaintEvent * /*anEvent*/ )
{
	int xoffset, yoffset;
	bool drawBorder = true;

	xoffset = Delta.x();
	yoffset = Delta.y();

	QPainter p( this );
	p.setRenderHint(QPainter::Antialiasing);
	p.save();
	p.translate( xoffset, yoffset );
	p.scale( zoomFactor, zoomFactor );
	p.drawPixmap( 0, 0, *m_pm );
	p.restore();
	if( drawBorder )
	{
		p.setPen( Qt::black );
		p.drawRect( xoffset-1, yoffset-1, int(m_pm->width()*zoomFactor+1), int(m_pm->height()*zoomFactor+1) );
	}
}

void PixmapWidget::wheelEvent( QWheelEvent *anEvent )
{
	float f;

	f = zoomFactor + 0.001*anEvent->delta();
	if( f < 32.0/m_pm->width() )
		f = 32.0/m_pm->width();


	QPoint p = anEvent->pos() - Delta;
	Delta -= (p / zoomFactor * f) - p;

	setZoomFactor( f );
}

void PixmapWidget::mousePressEvent ( QMouseEvent * anEvent )
{
	if (
		((anEvent->buttons() & Qt::RightButton) && !M_PREFS->getMouseSingleButton()) ||
		((anEvent->buttons() & Qt::LeftButton) && M_PREFS->getMouseSingleButton())
		)
	{
		Panning = true;
		LastPan = anEvent->pos();
	}
}

void PixmapWidget::mouseReleaseEvent ( QMouseEvent * anEvent )
{
	if (
		((anEvent->buttons() & Qt::RightButton) && !M_PREFS->getMouseSingleButton()) ||
		((anEvent->buttons() & Qt::LeftButton) && M_PREFS->getMouseSingleButton())
		)
	{
		if (Panning) {
			Panning = false;
		}
	}
}

void PixmapWidget::mouseMoveEvent ( QMouseEvent * anEvent )
{
	if (
		((anEvent->buttons() & Qt::RightButton) && !M_PREFS->getMouseSingleButton()) ||
		((anEvent->buttons() & Qt::LeftButton) && M_PREFS->getMouseSingleButton())
		)
	{
		if (Panning) {
			Delta += anEvent->pos() - LastPan;
			LastPan = anEvent->pos();
			update();
		}
	}
}

void PixmapWidget::resizeEvent ( QResizeEvent * anEvent )
{
	if (!done) {
		double rd = (double)m_pm->width() / (double)m_pm->height();
		double rw = (double)width() / (double)height();
		zoomFactor = rd < rw ? (double)height() / (double)m_pm->height() : (double)width() / (double)m_pm->width();
		done = true;
		Delta = QPointF(((double)width() - (double)m_pm->width() * zoomFactor) /2, ((double)height() - (double)m_pm->height()* zoomFactor) / 2).toPoint();
	}


	QWidget::resizeEvent(anEvent);
}
