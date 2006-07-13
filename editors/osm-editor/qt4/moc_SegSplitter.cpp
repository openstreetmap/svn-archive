/****************************************************************************
** Meta object code from reading C++ file 'SegSplitter.h'
**
** Created: Wed Jul 12 17:29:44 2006
**      by: The Qt Meta Object Compiler version 59 (Qt 4.1.4)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "SegSplitter.h"
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'SegSplitter.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 59
#error "This file was generated using the moc from 4.1.4. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

static const uint qt_meta_data_OpenStreetMap__SegSplitter[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       7,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // signals: signature, parameters, type, tag, flags
      28,   27,   27,   27, 0x05,
      35,   27,   27,   27, 0x05,

 // slots: signature, parameters, type, tag, flags
      52,   50,   27,   27, 0x0a,
      80,   50,   27,   27, 0x0a,
     112,   50,   27,   27, 0x0a,
     143,  139,   27,   27, 0x0a,
     174,  172,   27,   27, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_OpenStreetMap__SegSplitter[] = {
    "OpenStreetMap::SegSplitter\0\0done()\0error(QString)\0,\0"
    "nodeAdded(QByteArray,void*)\0splitSegAdded(QByteArray,void*)\0"
    "finished(QByteArray,void*)\0i,e\0handleHttpError(int,QString)\0e\0"
    "handleError(QString)\0"
};

const QMetaObject OpenStreetMap::SegSplitter::staticMetaObject = {
    { &QObject::staticMetaObject, qt_meta_stringdata_OpenStreetMap__SegSplitter,
      qt_meta_data_OpenStreetMap__SegSplitter, 0 }
};

const QMetaObject *OpenStreetMap::SegSplitter::metaObject() const
{
    return &staticMetaObject;
}

void *OpenStreetMap::SegSplitter::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_OpenStreetMap__SegSplitter))
	return static_cast<void*>(const_cast<SegSplitter*>(this));
    return QObject::qt_metacast(_clname);
}

int OpenStreetMap::SegSplitter::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: done(); break;
        case 1: error((*reinterpret_cast< const QString(*)>(_a[1]))); break;
        case 2: nodeAdded((*reinterpret_cast< const QByteArray(*)>(_a[1])),(*reinterpret_cast< void*(*)>(_a[2]))); break;
        case 3: splitSegAdded((*reinterpret_cast< const QByteArray(*)>(_a[1])),(*reinterpret_cast< void*(*)>(_a[2]))); break;
        case 4: finished((*reinterpret_cast< const QByteArray(*)>(_a[1])),(*reinterpret_cast< void*(*)>(_a[2]))); break;
        case 5: handleHttpError((*reinterpret_cast< int(*)>(_a[1])),(*reinterpret_cast< const QString(*)>(_a[2]))); break;
        case 6: handleError((*reinterpret_cast< const QString(*)>(_a[1]))); break;
        }
        _id -= 7;
    }
    return _id;
}

// SIGNAL 0
void OpenStreetMap::SegSplitter::done()
{
    QMetaObject::activate(this, &staticMetaObject, 0, 0);
}

// SIGNAL 1
void OpenStreetMap::SegSplitter::error(const QString & _t1)
{
    void *_a[] = { 0, const_cast<void*>(reinterpret_cast<const void*>(&_t1)) };
    QMetaObject::activate(this, &staticMetaObject, 1, _a);
}
