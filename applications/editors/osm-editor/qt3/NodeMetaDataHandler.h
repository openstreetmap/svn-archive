#ifndef NODEMETADATAHANDLER_H
#define NODEMETADATAHANDLER_H

#include <map>
#include <qstring.h>

namespace OpenStreetMap
{

class NodeMetaData
{
	public:
		QString key, value;

	NodeMetaData() { key=value=""; }
	NodeMetaData(QString k, QString v)  { key=k; value=v; }
	bool testmatch(const NodeMetaData& indata);
};

// Class for obtaining the permissions on a particular 
class NodeMetaDataHandler
{
private:
	std::map<QString,NodeMetaData> nData;

public:
	NodeMetaDataHandler();
	NodeMetaData getMetaData(const QString& type);
	QString getNodeType(const QString&, const QString&);
	bool keyExists(const QString& k);
};

}
#endif
