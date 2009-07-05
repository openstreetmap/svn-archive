#ifndef PIXMAPWIDGET_H
#define PIXMAPWIDGET_H

#include <QWidget>
#include <QString>

class QPixmap;
class QSvgRenderer;

class PixmapWidget : public QWidget
{
	Q_OBJECT

public:
	PixmapWidget( QWidget *parent=0 );
	~PixmapWidget();

	void loadFile( const QString &filename );
	void setPixmap( const QPixmap &aPix );
	QPixmap* pixmap();

	bool isPixmap();
	bool isSvg();

public slots:
	void setZoomFactor( float );

signals:
	void zoomFactorChanged( float );

protected:
	virtual void paintEvent( QPaintEvent* anEvent);
	virtual void wheelEvent( QWheelEvent* anEvent);
	virtual void resizeEvent ( QResizeEvent * anEvent );

	virtual void mousePressEvent ( QMouseEvent * anEvent ) ;
	virtual void mouseReleaseEvent ( QMouseEvent * anEvent );
	virtual void mouseMoveEvent ( QMouseEvent * anEvent );

private:
	QPixmap *m_pm;
	QSvgRenderer *m_svgr;
	double zoomFactor;
	bool Panning;
	QPoint Delta;
	QPoint FirstPan, LastPan;
	bool done;
};

#endif // PIXMAPWIDGET_H
