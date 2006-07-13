/****************************************************************************
** Meta object code from reading C++ file 'HTTPHandler.h'
**
** Created: Wed Jul 12 17:29:38 2006
**      by: The Qt Meta Object Compiler version 59 (Qt 4.1.4)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "HTTPHandler.h"
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'HTTPHandler.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 59
#error "This file was generated using the moc from 4.1.4. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

static const uint qt_meta_data_OpenStreetMap__HTTPHandler[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       5,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // signals: signature, parameters, type, tag, flags
      30,   28,   27,   27, 0x05,
      65,   28,   27,   27, 0x05,
      96,   27,   27,   27, 0x05,

 // slots: signature, parameters, type, tag, flags
     119,   27,   27,   27, 0x0a,
     163,   28,   27,   27, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_OpenStreetMap__HTTPHandler[] = {
    "OpenStreetMap::HTTPHandler\0\0,\0responseReceived(QByteArray,void*)\0"
    "httpErrorOccurred(int,QString)\0errorOccurred(QString)\0"
    "responseHeaderReceived(QHttpResponseHeader)\0responseReceived(int,bool)\0"
};

const QMetaObject OpenStreetMap::HTTPHandler::staticMetaObject = {
    { &QObject::staticMetaObject, qt_meta_stringdata_OpenStreetMap__HTTPHandler,
      qt_meta_data_OpenStreetMap__HTTPHandler, 0 }
};

const QMetaObject *OpenStreetMap::HTTPHandler::metaObject() const
{
    return &staticMetaObject;
}

void *OpenStreetMap::HTTPHandler::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_OpenStreetMap__HTTPHandler))
	return static_cast<void*>(const_cast<HTTPHandler*>(this));
    return QObject::qt_metacast(_clname);
}

int OpenStreetMap::HTTPHandler::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: responseReceived((*reinterpret_cast< const QByteArray(*)>(_a[1])),(*reinterpret_cast< void*(*)>(_a[2]))); break;
        case 1: httpErrorOccurred((*reinterpret_cast< int(*)>(_a[1])),(*reinterpret_cast< const QString(*)>(_a[2]))); break;
        case 2: errorOccurred((*reinterpret_cast< const QString(*)>(_a[1]))); break;
        case 3: responseHeaderReceived((*reinterpret_cast< const QHttpResponseHeader(*)>(_a[1]))); break;
        case 4: responseReceived((*reinterpret_cast< int(*)>(_a[1])),(*reinterpret_cast< bool(*)>(_a[2]))); break;
        }
        _id -= 5;
    }
    return _id;
}

// SIGNAL 0
void OpenStreetMap::HTTPHandler::responseReceived(const QByteArray & _t1, void * _t2)
{
    void *_a[] = { 0, const_cast<void*>(reinterpret_cast<const void*>(&_t1)), const_cast<void*>(reinterpret_cast<const void*>(&_t2)) };
    QMetaObject::activate(this, &staticMetaObject, 0, _a);
}

// SIGNAL 1
void OpenStreetMap::HTTPHandler::httpErrorOccurred(int _t1, const QString & _t2)
{
    void *_a[] = { 0, const_cast<void*>(reinterpret_cast<const void*>(&_t1)), const_cast<void*>(reinterpret_cast<const void*>(&_t2)) };
    QMetaObject::activate(this, &staticMetaObject, 1, _a);
}

// SIGNAL 2
void OpenStreetMap::HTTPHandler::errorOccurred(const QString & _t1)
{
    void *_a[] = { 0, const_cast<void*>(reinterpret_cast<const void*>(&_t1)) };
    QMetaObject::activate(this, &staticMetaObject, 2, _a);
}
