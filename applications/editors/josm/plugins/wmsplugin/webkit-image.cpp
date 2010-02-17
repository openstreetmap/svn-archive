#include <QtGui/QApplication>
#include <QtGui/QPainter>
#include <QtCore/QFile>
#include <QtCore/QString>
#include <QtCore/QUrl>
#include <QtWebKit/QWebPage>
#include <QtWebKit/QWebFrame>
#include <QtNetwork/QNetworkProxy>
#include <QtCore/QProcess>
#if QT_VERSION >= 0x040500
#include <QtGui/QDesktopServices>
#include <QtNetwork/QNetworkDiskCache>
#endif

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
    QString scheme = "http";   // default
    QString proxyHost;

    int proxyPort = 8080;
    QStringList tmpList = environment.at(idx).split("=");

#if QT_VERSION >= 0x040600  // Qt4.6: Use QUrl::fromUserInput
    // set URL (and guess if proto scheme missing)
    QUrl url (QUrl::fromUserInput(tmpList.at(1)));
#else
    // set URL
    QUrl url (tmpList.at(1));
#endif
    if (url.isValid() && !url.host().isEmpty())
    {
      proxyHost = url.host();

      if (url.port() != -1)
        proxyPort = url.port();

      if (!url.scheme().isEmpty())
        scheme = url.scheme();

      if (scheme == "http")   // we support only http
      {
        QNetworkProxy proxy;
        proxy.setType(QNetworkProxy::HttpCachingProxy);
        proxy.setHostName(proxyHost);
        proxy.setPort(proxyPort);
        if (!url.userName().isEmpty())
          proxy.setUser(url.userName());
        if (!url.password().isEmpty())
          proxy.setPassword(url.password());
        page->networkAccessManager()->setProxy(proxy);
      }
    }
    else /* manual mode */
    {
      QStringList proto_host_port = tmpList.at(1).split("://");
      QStringList host_port;
      if (proto_host_port.size() == 2)  // string has proto
      {
        scheme = proto_host_port.at(0);
        host_port = proto_host_port.at(1).split(":");
      }
      else  // no proto (or invalid format with several delimiters)
      {
        host_port = tmpList.at(1).split(":");
      }
      if (scheme == "http")   // we support only http
      {
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
    }
  }

#if QT_VERSION >= 0x040500
  QNetworkDiskCache *diskCache = new QNetworkDiskCache(page);
  QString location = QDesktopServices::storageLocation(QDesktopServices::CacheLocation);
  diskCache->setCacheDirectory(location);
  page->networkAccessManager()->setCache(diskCache);
#endif

  QObject::connect(page, SIGNAL(loadFinished(bool)), s, SLOT(loaded(bool)));
  QObject::connect(s, SIGNAL(finish(void)), &a, SLOT(quit()));
  /* set some useful defaults for a webpage */
//  page->setViewportSize(QSize(1280,1024));
//  page->mainFrame()->setScrollBarPolicy(Qt::Horizontal, Qt::ScrollBarAlwaysOff);
//  page->mainFrame()->setScrollBarPolicy(Qt::Vertical, Qt::ScrollBarAlwaysOff);
  page->mainFrame()->load (QUrl(url));
  return a.exec();
}
