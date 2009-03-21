;;
;;  german.nsh
;;
;;  German language strings for the Windows JOSM NSIS installer.
;;  Windows Code page: 1252
;;
;;  Author: Bjoern Voigt <bjoern@cs.tu-berlin.de>, 2003.
;;  Version 2
;;

!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_WELCOME_TEXT "Diese Installationshilfe wird Sie durch den Installationsvorgang des JAVA OpenStreetMap Editors (JOSM) f�hren.\r\n\r\nBevor Sie die Installation starten, stellen Sie bitte sicher das JOSM nicht bereits l�uft.\r\n\r\nAuf 'Weiter' klicken um fortzufahren."
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_DIR_TEXT "Bitte das Verzeichnis ausw�hlen, in das JOSM installiert werden soll."

!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_FULL_INSTALL "JOSM (Komplettinstallation)"
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SEC_JOSM "JOSM"
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SEC_PLUGINS_GROUP "Plugins"
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SEC_AGPIFOJ_PLUGIN  "AgPifoJ"
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SEC_VALIDATOR_PLUGIN  "Validator"
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SEC_WMS_PLUGIN  "WMS"
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SEC_STARTMENU  "Startmen� Eintrag"
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SEC_DESKTOP_ICON  "Desktop Icon"
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SEC_QUICKLAUNCH_ICON  "Schnellstartleiste Icon"
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SEC_FILE_EXTENSIONS  "Dateiendungen"
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SECDESC_JOSM "JOSM ist der JAVA OpenStreetMap Editor f�r .osm Dateien."
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SECDESC_PLUGINS_GROUP "Eine Auswahl an n�tzlichen JOSM Plugins."
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SECDESC_AGPIFOJ_PLUGIN  "Bringt GPS Tracks mit Fotos in �bereinstimmung oder importiert EXIF Fotos"
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SECDESC_VALIDATOR_PLUGIN  "Validatiert ge�nderte Daten ob diese mit den �blichen Ratschl�gen �bereinstimmen."
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SECDESC_WMS_PLUGIN  "Hintergrundbilder von Web Map Service (WMS) Quellen."
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SECDESC_STARTMENU  "F�gt JOSM zum Startmen� hinzu."
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SECDESC_DESKTOP_ICON  "F�gt ein JOSM Icon zum Desktop hinzu."
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SECDESC_QUICKLAUNCH_ICON  "F�gt ein JOSM Icon zur Schnellstartleiste (Quick Launch) hinzu."
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_SECDESC_FILE_EXTENSIONS  "F�gt JOSM Dateiendungen f�r .osm and .gpx Dateien hinzu."

!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_UPDATEICONS_ERROR1 "Kann die Bibliothek 'shell32.dll' nicht finden. Das Update der Icons ist nicht m�glich"
!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_UPDATEICONS_ERROR2 "Sie sollten die kostenlose 'Microsoft Layer for Unicode' installieren um die Icons updaten zu k�nnen"

!insertmacro JOSM_MACRO_DEFAULT_STRING JOSM_LINK_TEXT "JAVA OpenStreetMap - Editor"

!insertmacro JOSM_MACRO_DEFAULT_STRING un.JOSM_UNCONFIRMPAGE_TEXT_TOP "Die folgende JAVA OpenStreetMap editor (JOSM) Installation wird deinstalliert. Auf 'Weiter' klicken um fortzufahren."
!insertmacro JOSM_MACRO_DEFAULT_STRING un.JOSM_DEFAULT_UNINSTALL "Default (pers�nliche Einstellungen und Plugins behalten)"
!insertmacro JOSM_MACRO_DEFAULT_STRING un.JOSM_FULL_UNINSTALL "Alles (alles entfernen)"

!insertmacro JOSM_MACRO_DEFAULT_STRING un.JOSM_IN_USE_ERROR "Achtung: josm.exe konnte nicht entfernt werden, m�glicherweise wird es noch benutzt!"
!insertmacro JOSM_MACRO_DEFAULT_STRING un.JOSM_INSTDIR_ERROR "Achtung: Das Verzeichnis $INSTDIR konnte nicht entfernt werden!"

!insertmacro JOSM_MACRO_DEFAULT_STRING un.JOSM_SEC_UNINSTALL "JOSM" 
!insertmacro JOSM_MACRO_DEFAULT_STRING un.JOSM_SEC_PERSONAL_SETTINGS "Pers�nliche Einstellungen" 
!insertmacro JOSM_MACRO_DEFAULT_STRING un.JOSM_SEC_PLUGINS "Pers�nliche Plugins" 

!insertmacro JOSM_MACRO_DEFAULT_STRING un.JOSM_SECDESC_UNINSTALL "Deinstalliere JOSM."
!insertmacro JOSM_MACRO_DEFAULT_STRING un.JOSM_SECDESC_PERSONAL_SETTINGS  "Deinstalliere pers�nliche Einstellungen von Ihrem Profil: $PROFILE."
