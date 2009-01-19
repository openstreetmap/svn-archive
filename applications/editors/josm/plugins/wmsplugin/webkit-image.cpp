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
#include <QtNetwork/QNetworkProxy>
#include <QtCore/QProcess>

/* using mingw to set binary mode */
#ifdef WIN32
#include <io.h>
#include <fcntl.h>
#define BINARYSTDOUT setmode(fileno(stdout), O_BINARY);
#else
#define BINARYSTDOUT
#endif

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

  QStringList environment = QProcess::systemEnvironment();
  int idx = environment.indexOf(QRegExp("http_proxy=.*"));
  if(idx != -1)
  {
     QString proxyHost;
     int proxyPort = 8080;
     QStringList tmpList = environment.at(idx).split("=");
     QStringList host_port = tmpList.at(1).split(":");
     proxyHost = host_port.at(0);
     if(host_port.size() == 2)
     {
       bool ok;
       int port = host_port.at(1).toInt(&ok);
       if(ok)
         proxyPort = port;
     }

     QNetworkProxy proxy;
     proxy.setType(QNetworkProxy::HttpCachingProxy);
     proxy.setHostName(proxyHost);
     proxy.setPort(proxyPort);
     page->networkAccessManager()->setProxy(proxy);
  }

  QObject::connect(page, SIGNAL(loadFinished(bool)), s, SLOT(loaded(bool)));
  QObject::connect(s, SIGNAL(finish(void)), &a, SLOT(quit()));
  /* set some useful defaults for a webpage */
//  page->setViewportSize(QSize(1280,1024));
//  page->mainFrame()->setScrollBarPolicy(Qt::Horizontal, Qt::ScrollBarAlwaysOff);
//  page->mainFrame()->setScrollBarPolicy(Qt::Vertical, Qt::ScrollBarAlwaysOff);
  page->mainFrame()->load (QUrl(url));
  return a.exec();
}
