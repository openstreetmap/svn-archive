/****************************************************************************
** Meta object code from reading C++ file 'WayDialogue.h'
**
** Created: Wed Jul 12 17:29:45 2006
**      by: The Qt Meta Object Compiler version 59 (Qt 4.1.4)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "WayDialogue.h"
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'WayDialogue.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 59
#error "This file was generated using the moc from 4.1.4. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

static const uint qt_meta_data_OpenStreetMap__WayDialogue[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       1,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      28,   27,   27,   27, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_OpenStreetMap__WayDialogue[] = {
    "OpenStreetMap::WayDialogue\0\0changeWA(QString)\0"
};

const QMetaObject OpenStreetMap::WayDialogue::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_OpenStreetMap__WayDialogue,
      qt_meta_data_OpenStreetMap__WayDialogue, 0 }
};

const QMetaObject *OpenStreetMap::WayDialogue::metaObject() const
{
    return &staticMetaObject;
}

void *OpenStreetMap::WayDialogue::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_OpenStreetMap__WayDialogue))
	return static_cast<void*>(const_cast<WayDialogue*>(this));
    return QDialog::qt_metacast(_clname);
}

int OpenStreetMap::WayDialogue::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QDialog::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: changeWA((*reinterpret_cast< const QString(*)>(_a[1]))); break;
        }
        _id -= 1;
    }
    return _id;
}
