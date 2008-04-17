TEMPLATE = app
TARGET = merkaartor

QT += network xml core gui
CONFIG += release
CONFIG += debug
CONFIG += yahoo

isEmpty(OUTPUT_DIR) {
    CONFIG(release):OUTPUT_DIR=$$PWD/binaries/release
    CONFIG(debug):OUTPUT_DIR=$$PWD/binaries/debug
}
DESTDIR = $$OUTPUT_DIR/bin

VERSION="0.11"
DEFINES += VERSION=\"\\\"$$VERSION\\\"\"

INCLUDEPATH += .
DEPENDPATH += .
MOC_DIR += tmp
OBJECTS_DIR += obj
UI_DIR += tmp

TRANSLATIONS += \
	merkaartor_fr.ts \ 
	merkaartor_de.ts 

BINTRANSLATIONS += \
	merkaartor_de.qm \
	merkaartor_fr.qm

#Include file(s)
include(Merkaartor.pri)
include(QMapControl.pri)
include(ImportExport.pri)

unix {
    target.path = /usr/local/bin
    # Prefix: base instalation directory
    count( PREFIX, 1 ) {
        target.path = $${PREFIX}/bin

        isEmpty(TRANSDIR_MERKAARTOR) {
            TRANSDIR_MERKAARTOR = $${PREFIX}/share/merkaartor/translations
        }
        isEmpty(TRANSDIR_SYSTEM) {
            TRANSDIR_SYSTEM = $${PREFIX}/share/qt4/translations
        }

    }
    INSTALLS += target
}

win32-msvc* {
    DEFINES += _USE_MATH_DEFINES
}

count(TRANSDIR_MERKAARTOR, 1) {
    translations.path =  $${TRANSDIR_MERKAARTOR}
    translations.files = $${BINTRANSLATIONS}
    DEFINES += TRANSDIR_MERKAARTOR=\"\\\"$$translations.path\\\"\"
    INSTALLS += translations
}

count(TRANSDIR_SYSTEM, 1) {
    DEFINES += TRANSDIR_SYSTEM=\"\\\"$${TRANSDIR_SYSTEM}\\\"\"
}

osmarender {
    !win32-g++ {
        include(Render.pri)
    }
}

yahoo {
    DEFINES += YAHOO
    SOURCES += QMapControl/yahoolegalmapadapter.cpp QMapControl/browserimagemanager.cpp
    HEADERS += QMapControl/yahoolegalmapadapter.h QMapControl/browserimagemanager.h

    include(webkit/WebKit.pri)
}

