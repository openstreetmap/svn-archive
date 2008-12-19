/* compile with
moc webkit-image.cpp >webkit-image.h
g++ webkit-image.cpp -o webkit-image -lQtCore -lQtWebKit -lQtGui -s -O2
or under Windows:
g++ webkit-image.cpp -o webkit-image -lQtCore4 -lQtWebKit4 -lQtGui4 -s O2
adding the correct directories with -L or -I:
-I C:\Progra~1\Qt\include -L C:\Progra~1\Qt\lib
*/
#include <QtGui/QApplication>
#include <QtGui/QPainter>
#include <QtCore/QFile>
#include <QtCore/QString>
#include <QtWebKit/QWebPage>
#include <QtWebKit/QWebFrame>

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
  Save(QWebPage *p) : page(p) {};

public slots:
  void loaded(bool ok)
  {
    if(ok)
    {
      page->setViewportSize(page->mainFrame()->contentsSize());
      QImage im(page->viewportSize(), QImage::Format_ARGB32);
      QPainter painter(&im);
      page->mainFrame()->render(&painter);

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
  QWebPage * page;
};

#include "webkit-image.h"

int main(int argc, char **argv)
{
  if(argc != 2)
    return 20;
  QString url = QString(argv[1]);

  QApplication a(argc, argv);
  QWebPage * page = new QWebPage();
  Save * s = new Save(page);

  QObject::connect(page, SIGNAL(loadFinished(bool)), s, SLOT(loaded(bool)));
  QObject::connect(s, SIGNAL(finish(void)), &a, SLOT(quit()));
  page->mainFrame()->load (QUrl(url));
  return a.exec();
}
