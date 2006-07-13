/****************************************************************************
** Meta object code from reading C++ file 'MainWindow2.h'
**
** Created: Wed Jul 12 17:29:41 2006
**      by: The Qt Meta Object Compiler version 59 (Qt 4.1.4)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "MainWindow2.h"
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'MainWindow2.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 59
#error "This file was generated using the moc from 4.1.4. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

static const uint qt_meta_data_OpenStreetMap__MainWindow2[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
      50,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // signals: signature, parameters, type, tag, flags
      28,   27,   27,   27, 0x05,

 // slots: signature, parameters, type, tag, flags
      46,   27,   27,   27, 0x0a,
      53,   27,   27,   27, 0x0a,
      60,   27,   27,   27, 0x0a,
      69,   27,   27,   27, 0x0a,
      79,   27,   27,   27, 0x0a,
      89,   27,   27,   27, 0x0a,
      96,   27,   27,   27, 0x0a,
     112,   27,   27,   27, 0x0a,
     129,   27,   27,   27, 0x0a,
     141,   27,   27,   27, 0x0a,
     153,   27,   27,   27, 0x0a,
     160,   27,   27,   27, 0x0a,
     173,   27,   27,   27, 0x0a,
     187,   27,   27,   27, 0x0a,
     201,   27,   27,   27, 0x0a,
     206,   27,   27,   27, 0x0a,
     213,   27,   27,   27, 0x0a,
     220,   27,   27,   27, 0x0a,
     228,   27,   27,   27, 0x0a,
     238,   27,   27,   27, 0x0a,
     249,   27,   27,   27, 0x0a,
     262,   27,   27,   27, 0x0a,
     275,   27,   27,   27, 0x0a,
     289,   27,   27,   27, 0x0a,
     298,   27,   27,   27, 0x0a,
     318,   27,   27,   27, 0x0a,
     335,   27,   27,   27, 0x0a,
     347,   27,   27,   27, 0x0a,
     370,   27,   27,   27, 0x0a,
     397,  390,   27,   27, 0x0a,
     431,  390,   27,   27, 0x0a,
     463,  461,   27,   27, 0x0a,
     496,   27,   27,   27, 0x0a,
     516,  461,   27,   27, 0x0a,
     551,  545,   27,   27, 0x0a,
     579,   27,   27,   27, 0x0a,
     592,   27,   27,   27, 0x0a,
     604,   27,   27,   27, 0x0a,
     620,   27,   27,   27, 0x0a,
     639,   27,   27,   27, 0x0a,
     660,   27,   27,   27, 0x0a,
     677,  390,   27,   27, 0x0a,
     709,   27,   27,   27, 0x0a,
     724,   27,   27,   27, 0x0a,
     743,   27,   27,   27, 0x0a,
     757,   27,   27,   27, 0x0a,
     775,  545,   27,   27, 0x0a,
     801,  545,   27,   27, 0x0a,
     827,   27,   27,   27, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_OpenStreetMap__MainWindow2[] = {
    "OpenStreetMap::MainWindow2\0\0newNodeAddedSig()\0open()\0save()\0"
    "saveAs()\0saveGPX()\0readGPS()\0quit()\0toggleLandsat()\0"
    "toggleContours()\0toggleOSM()\0toggleGPX()\0undo()\0setMode(int)\0"
    "toggleNodes()\0grabLandsat()\0up()\0down()\0left()\0right()\0magnify()\0"
    "screenUp()\0screenDown()\0screenLeft()\0screenRight()\0shrink()\0"
    "loginToLiveUpdate()\0grabOSMFromNet()\0uploadOSM()\0"
    "logoutFromLiveUpdate()\0removeTrackPoints()\0array,\0"
    "newSegmentAdded(QByteArray,void*)\0newWayAdded(QByteArray,void*)\0,\0"
    "loadComponents(QByteArray,void*)\0deleteSelectedSeg()\0"
    "handleHttpError(int,QString)\0error\0handleNetCommError(QString)\0"
    "toggleWays()\0uploadWay()\0doaddseg(void*)\0changeSerialPort()\0"
    "uploadNewWaypoints()\0grabGPXFromNet()\0loadOSMTracks(QByteArray,void*)\0"
    "splitterDone()\0changeWayDetails()\0batchUpload()\0batchUploadDone()\0"
    "batchUploadError(QString)\0segSplitterError(QString)\0"
    "toggleSegmentColours()\0"
};

const QMetaObject OpenStreetMap::MainWindow2::staticMetaObject = {
    { &QMainWindow::staticMetaObject, qt_meta_stringdata_OpenStreetMap__MainWindow2,
      qt_meta_data_OpenStreetMap__MainWindow2, 0 }
};

const QMetaObject *OpenStreetMap::MainWindow2::metaObject() const
{
    return &staticMetaObject;
}

void *OpenStreetMap::MainWindow2::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_OpenStreetMap__MainWindow2))
	return static_cast<void*>(const_cast<MainWindow2*>(this));
    if (!strcmp(_clname, "DrawSurface"))
	return static_cast<DrawSurface*>(const_cast<MainWindow2*>(this));
    return QMainWindow::qt_metacast(_clname);
}

int OpenStreetMap::MainWindow2::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QMainWindow::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: newNodeAddedSig(); break;
        case 1: open(); break;
        case 2: save(); break;
        case 3: saveAs(); break;
        case 4: saveGPX(); break;
        case 5: readGPS(); break;
        case 6: quit(); break;
        case 7: toggleLandsat(); break;
        case 8: toggleContours(); break;
        case 9: toggleOSM(); break;
        case 10: toggleGPX(); break;
        case 11: undo(); break;
        case 12: setMode((*reinterpret_cast< int(*)>(_a[1]))); break;
        case 13: toggleNodes(); break;
        case 14: grabLandsat(); break;
        case 15: up(); break;
        case 16: down(); break;
        case 17: left(); break;
        case 18: right(); break;
        case 19: magnify(); break;
        case 20: screenUp(); break;
        case 21: screenDown(); break;
        case 22: screenLeft(); break;
        case 23: screenRight(); break;
        case 24: shrink(); break;
        case 25: loginToLiveUpdate(); break;
        case 26: grabOSMFromNet(); break;
        case 27: uploadOSM(); break;
        case 28: logoutFromLiveUpdate(); break;
        case 29: removeTrackPoints(); break;
        case 30: newSegmentAdded((*reinterpret_cast< const QByteArray(*)>(_a[1])),(*reinterpret_cast< void*(*)>(_a[2]))); break;
        case 31: newWayAdded((*reinterpret_cast< const QByteArray(*)>(_a[1])),(*reinterpret_cast< void*(*)>(_a[2]))); break;
        case 32: loadComponents((*reinterpret_cast< const QByteArray(*)>(_a[1])),(*reinterpret_cast< void*(*)>(_a[2]))); break;
        case 33: deleteSelectedSeg(); break;
        case 34: handleHttpError((*reinterpret_cast< int(*)>(_a[1])),(*reinterpret_cast< const QString(*)>(_a[2]))); break;
        case 35: handleNetCommError((*reinterpret_cast< const QString(*)>(_a[1]))); break;
        case 36: toggleWays(); break;
        case 37: uploadWay(); break;
        case 38: doaddseg((*reinterpret_cast< void*(*)>(_a[1]))); break;
        case 39: changeSerialPort(); break;
        case 40: uploadNewWaypoints(); break;
        case 41: grabGPXFromNet(); break;
        case 42: loadOSMTracks((*reinterpret_cast< const QByteArray(*)>(_a[1])),(*reinterpret_cast< void*(*)>(_a[2]))); break;
        case 43: splitterDone(); break;
        case 44: changeWayDetails(); break;
        case 45: batchUpload(); break;
        case 46: batchUploadDone(); break;
        case 47: batchUploadError((*reinterpret_cast< const QString(*)>(_a[1]))); break;
        case 48: segSplitterError((*reinterpret_cast< const QString(*)>(_a[1]))); break;
        case 49: toggleSegmentColours(); break;
        }
        _id -= 50;
    }
    return _id;
}

// SIGNAL 0
void OpenStreetMap::MainWindow2::newNodeAddedSig()
{
    QMetaObject::activate(this, &staticMetaObject, 0, 0);
}
