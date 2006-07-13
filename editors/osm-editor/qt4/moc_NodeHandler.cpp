/****************************************************************************
** Meta object code from reading C++ file 'NodeHandler.h'
**
** Created: Wed Jul 12 17:29:43 2006
**      by: The Qt Meta Object Compiler version 59 (Qt 4.1.4)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "NodeHandler.h"
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'NodeHandler.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 59
#error "This file was generated using the moc from 4.1.4. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

static const uint qt_meta_data_OpenStreetMap__NodeHandler[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       3,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // signals: signature, parameters, type, tag, flags
      28,   27,   27,   27, 0x05,
      51,   27,   27,   27, 0x05,

 // slots: signature, parameters, type, tag, flags
      73,   71,   27,   27, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_OpenStreetMap__NodeHandler[] = {
    "OpenStreetMap::NodeHandler\0\0newNodeAddedSig(void*)\0"
    "finalNodeAddedSig()\0,\0newNodeAdded(QByteArray,void*)\0"
};

const QMetaObject OpenStreetMap::NodeHandler::staticMetaObject = {
    { &QObject::staticMetaObject, qt_meta_stringdata_OpenStreetMap__NodeHandler,
      qt_meta_data_OpenStreetMap__NodeHandler, 0 }
};

const QMetaObject *OpenStreetMap::NodeHandler::metaObject() const
{
    return &staticMetaObject;
}

void *OpenStreetMap::NodeHandler::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_OpenStreetMap__NodeHandler))
	return static_cast<void*>(const_cast<NodeHandler*>(this));
    return QObject::qt_metacast(_clname);
}

int OpenStreetMap::NodeHandler::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: newNodeAddedSig((*reinterpret_cast< void*(*)>(_a[1]))); break;
        case 1: finalNodeAddedSig(); break;
        case 2: newNodeAdded((*reinterpret_cast< const QByteArray(*)>(_a[1])),(*reinterpret_cast< void*(*)>(_a[2]))); break;
        }
        _id -= 3;
    }
    return _id;
}

// SIGNAL 0
void OpenStreetMap::NodeHandler::newNodeAddedSig(void * _t1)
{
    void *_a[] = { 0, const_cast<void*>(reinterpret_cast<const void*>(&_t1)) };
    QMetaObject::activate(this, &staticMetaObject, 0, _a);
}

// SIGNAL 1
void OpenStreetMap::NodeHandler::finalNodeAddedSig()
{
    QMetaObject::activate(this, &staticMetaObject, 1, 0);
}
