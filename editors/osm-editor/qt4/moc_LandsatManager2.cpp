/****************************************************************************
** Meta object code from reading C++ file 'LandsatManager2.h'
**
** Created: Wed Jul 12 17:29:39 2006
**      by: The Qt Meta Object Compiler version 59 (Qt 4.1.4)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "LandsatManager2.h"
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'LandsatManager2.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 59
#error "This file was generated using the moc from 4.1.4. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

static const uint qt_meta_data_OpenStreetMap__LandsatManager2[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       2,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      45,   32,   31,   31, 0x0a,
      87,   76,   31,   31, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_OpenStreetMap__LandsatManager2[] = {
    "OpenStreetMap::LandsatManager2\0\0response,dim\0"
    "dataReceived(QByteArray,void*)\0response,t\0"
    "newDataReceived(QByteArray,void*)\0"
};

const QMetaObject OpenStreetMap::LandsatManager2::staticMetaObject = {
    { &QObject::staticMetaObject, qt_meta_stringdata_OpenStreetMap__LandsatManager2,
      qt_meta_data_OpenStreetMap__LandsatManager2, 0 }
};

const QMetaObject *OpenStreetMap::LandsatManager2::metaObject() const
{
    return &staticMetaObject;
}

void *OpenStreetMap::LandsatManager2::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_OpenStreetMap__LandsatManager2))
	return static_cast<void*>(const_cast<LandsatManager2*>(this));
    return QObject::qt_metacast(_clname);
}

int OpenStreetMap::LandsatManager2::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: dataReceived((*reinterpret_cast< const QByteArray(*)>(_a[1])),(*reinterpret_cast< void*(*)>(_a[2]))); break;
        case 1: newDataReceived((*reinterpret_cast< const QByteArray(*)>(_a[1])),(*reinterpret_cast< void*(*)>(_a[2]))); break;
        }
        _id -= 2;
    }
    return _id;
}
