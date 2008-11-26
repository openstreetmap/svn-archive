/* compile with
moc webkit-image.cpp >webkit-image.h
g++ webkit-image.cpp -o webkit-image -lQtCore -lQtWebKit -lQtGui -s -O2
or under Windows:
g++ webkit-image.cpp -o webkit-image -lQtCore4 -lQtWebKit4 -lQtGui4 -s O2
adding the correct directories with -L or -I:
-I C:\Progra~1\Qt\include -L C:\Progra~1\Qt\lib
*/
#include <QtGui/QApplication>
#include <QtCore/QFile>
#include <QtCore/QString>
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
  void setGeometry(const QRect &r)
  {
    view->setGeometry(r);
  }
  void loaded(bool ok)
  {
    if(ok)
    {
      QImage im = QPixmap::grabWidget(view).toImage();

      QFile f;
      BINARYSTDOUT
      if(f.open(stdout, QIODevice::WriteOnly|QIODevice::Unbuffered))
      {
        if(!im.save(&f, "JPEG"))
        {
          im.save(&f, "PNG");
        }
      }
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
  view->resize(WIDTH,WIDTH);

  QObject::connect(view, SIGNAL(loadFinished(bool)), s, SLOT(loaded(bool)));
  QObject::connect(s, SIGNAL(finish(void)), &a, SLOT(quit()));
  QObject::connect(view->page(), SIGNAL(geometryChangeRequested(const QRect &)), s, SLOT(setGeometry(const QRect &)));
  view->load(QUrl(url));
  return a.exec();
}
