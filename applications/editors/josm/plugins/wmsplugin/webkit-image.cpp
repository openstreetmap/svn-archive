/* compile with
moc webkit-image.cpp >webkit-image.h
g++ webkit-image.cpp -o webkit-image -lQtCore -lQtWebKit -lQtGui
*/

#include <QtGui/QApplication>
#include <QtCore/QFile>
#include <QtCore/QString>
#include <QtCore/QDebug>
#include <QtWebKit/QWebView>

/* using mingw to set binary mode */
#ifdef WIN32
#include <io.h>
#include <fcntl.h>
#define BINARYSTDOUT setmode(fileno(stdout), O_BINARY);
#else
#define BINARYSTDOUT
#endif

#define WIDTH 2000

class Save : public QObject
{
Q_OBJECT
public:
  Save(QWebView *v) : view(v) {};

public slots:
  void loaded(bool ok)
  {
    if(ok)
    {
      QImage im = QPixmap::grabWidget(view).toImage();
      QRgb white = QColor(255,255,255).rgb();
      /* sometimes the white is shifted one bit */
      QRgb white2 = QColor(254,254,254).rgb();

      /* didn't find a way to reduce image directly, so we scan for white background */
      bool iswhite = true;
      int xsize = WIDTH;
      int ysize = WIDTH;
      for(int x = xsize-1; iswhite && x > 0; --x)
      {
        for(int y = 0; iswhite && y < ysize; ++y)
        {
          QRgb p = im.pixel(x, y);
          if(p != white && p != white2 & p)
            iswhite = false;
        }
        if(iswhite)
          xsize = x;
      }
      iswhite = true;
      for(int y = ysize-1; iswhite && y > 0; --y)
      {
        for(int x = 0; iswhite && x < xsize; ++x)
        {
          QRgb p = im.pixel(x, y);
          if(p != white && p != white2 && p)
            iswhite = false;
        }
        if(iswhite)
          ysize = y;
      }
      /* didn't find a way to clip the QImage directly, so we reload it */
      QPixmap p = QPixmap::grabWidget(view, 0,0,xsize,ysize);
      QFile f;
      BINARYSTDOUT
      if(f.open(stdout, QIODevice::WriteOnly|QIODevice::Unbuffered))
        p.save(&f, "JPG");
    }
    emit finish();
  }
signals:
  void finish(void);

private:
  QWebView *view;
};

#include "webkit-image.h"

int main(int argc, char **argv)
{
  if(argc != 2)
    return 20;
  QString url = QString(argv[1]);

  QApplication a( argc, argv );
  QWebView *view = new QWebView();
  Save *s = new Save(view);

  QObject::connect(view, SIGNAL(loadFinished(bool)), s, SLOT(loaded(bool)));
  QObject::connect(s, SIGNAL(finish(void)), &a, SLOT(quit()));
  view->resize(WIDTH,WIDTH);
  view->load(QUrl(url));
  return a.exec();
}
