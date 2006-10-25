#ifndef MERKAARTOR_TAGMODEL_H_
#define MERKAARTOR_TAGMODEL_H_

#include <QtCore/QAbstractTableModel>

class MainWindow;
class MapFeature;

class TagModel : public QAbstractTableModel
{
	public:
		TagModel(MainWindow* aMain);
		~TagModel();

		void setFeature(MapFeature* aFeature);
		int rowCount(const QModelIndex &parent = QModelIndex()) const;
		int columnCount(const QModelIndex &parent = QModelIndex()) const;
		QVariant data(const QModelIndex &index, int role) const;
		QVariant headerData(int section, Qt::Orientation orientation, int role = Qt::DisplayRole) const;
		Qt::ItemFlags flags(const QModelIndex &index) const;
		bool setData(const QModelIndex &index, const QVariant &value, int role = Qt::EditRole); 
	private:
		MainWindow* Main;
		MapFeature* theFeature;
};

#endif


