/****************************************************************************
** Meta object code from reading C++ file 'BatchUploader.h'
**
** Created: Wed Jul 12 17:29:37 2006
**      by: The Qt Meta Object Compiler version 59 (Qt 4.1.4)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "BatchUploader.h"
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'BatchUploader.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 59
#error "This file was generated using the moc from 4.1.4. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

static const uint qt_meta_data_OpenStreetMap__BatchUploader[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       6,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // signals: signature, parameters, type, tag, flags
      30,   29,   29,   29, 0x05,
      37,   29,   29,   29, 0x05,

 // slots: signature, parameters, type, tag, flags
      54,   52,   29,   29, 0x0a,
      82,   52,   29,   29, 0x0a,
     117,  113,   29,   29, 0x0a,
     148,  146,   29,   29, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_OpenStreetMap__BatchUploader[] = {
    "OpenStreetMap::BatchUploader\0\0done()\0error(QString)\0,\0"
    "nodeAdded(QByteArray,void*)\0segmentAdded(QByteArray,void*)\0i,e\0"
    "handleHttpError(int,QString)\0e\0handleError(QString)\0"
};

const QMetaObject OpenStreetMap::BatchUploader::staticMetaObject = {
    { &QObject::staticMetaObject, qt_meta_stringdata_OpenStreetMap__BatchUploader,
      qt_meta_data_OpenStreetMap__BatchUploader, 0 }
};

const QMetaObject *OpenStreetMap::BatchUploader::metaObject() const
{
    return &staticMetaObject;
}

void *OpenStreetMap::BatchUploader::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_OpenStreetMap__BatchUploader))
	return static_cast<void*>(const_cast<BatchUploader*>(this));
    return QObject::qt_metacast(_clname);
}

int OpenStreetMap::BatchUploader::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: done(); break;
        case 1: error((*reinterpret_cast< const QString(*)>(_a[1]))); break;
        case 2: nodeAdded((*reinterpret_cast< const QByteArray(*)>(_a[1])),(*reinterpret_cast< void*(*)>(_a[2]))); break;
        case 3: segmentAdded((*reinterpret_cast< const QByteArray(*)>(_a[1])),(*reinterpret_cast< void*(*)>(_a[2]))); break;
        case 4: handleHttpError((*reinterpret_cast< int(*)>(_a[1])),(*reinterpret_cast< const QString(*)>(_a[2]))); break;
        case 5: handleError((*reinterpret_cast< const QString(*)>(_a[1]))); break;
        }
        _id -= 6;
    }
    return _id;
}

// SIGNAL 0
void OpenStreetMap::BatchUploader::done()
{
    QMetaObject::activate(this, &staticMetaObject, 0, 0);
}

// SIGNAL 1
void OpenStreetMap::BatchUploader::error(const QString & _t1)
{
    void *_a[] = { 0, const_cast<void*>(reinterpret_cast<const void*>(&_t1)) };
    QMetaObject::activate(this, &staticMetaObject, 1, _a);
}
