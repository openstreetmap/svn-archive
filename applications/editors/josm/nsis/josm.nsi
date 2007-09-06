;
; josm.nsi
;


; Set the compression mechanism first.
; As of NSIS 2.07, solid compression which makes installer about 1MB smaller
; is no longer the default, so use the /SOLID switch.
; This unfortunately is unknown to NSIS prior to 2.07 and creates an error.
; So if you get an error here, please update to at least NSIS 2.07!
SetCompressor /SOLID lzma

; work with JAVA ini strings
!include "INIStrNS.nsh"

!define DEST "josm"

InstType "JOSM (full install)"

InstType "un.Default (keep Personal Settings and plugins)"
InstType "un.All (remove all)"

; Used to refresh the display of file association
!define SHCNE_ASSOCCHANGED 0x08000000
!define SHCNF_IDLIST 0

; Used to add associations between file extensions and JOSM
!define OSM_ASSOC "josm-file"

; ============================================================================
; Header configuration
; ============================================================================
; The name of the installer
!define PROGRAM_NAME "JOSM"

Name "${PROGRAM_NAME} ${VERSION}"

; The file to write
OutFile "${DEST}-setup-${VERSION}.exe"

; Uninstall stuff (NSIS 2.08: "\r\n" don't work here)
!define MUI_UNCONFIRMPAGE_TEXT_TOP "The following JAVA OpenStreetMap editor (JOSM) installation will be uninstalled. Click 'Next' to continue."

XPStyle on



; ============================================================================
; Modern UI
; ============================================================================

!include "MUI.nsh"
;!addplugindir ".\Plugins"

; Icon of installer and uninstaller
!define MUI_ICON "logo.ico"
!define MUI_UNICON "logo.ico"

!define MUI_COMPONENTSPAGE_SMALLDESC
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_UNFINISHPAGE_NOAUTOCLOSE
!define MUI_WELCOMEPAGE_TEXT "This wizard will guide you through the installation of the JAVA OpenStreetMap editor (JOSM).\r\n\r\nBefore starting the installation, make sure any JOSM applications are not running.\r\n\r\nClick 'Next' to continue."
;!define MUI_FINISHPAGE_LINK "Install WinPcap to be able to capture packets from a network!"
;!define MUI_FINISHPAGE_LINK_LOCATION "http://www.winpcap.org"

; NSIS shows Readme files by opening the Readme file with the default application for
; the file's extension. "README.win32" won't work in most cases, because extension "win32"
; is usually not associated with an appropriate text editor. We should use extension "txt"
; for a text file or "html" for an html README file.
;!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\NEWS.txt"
;!define MUI_FINISHPAGE_SHOWREADME_TEXT "Show News"
;!define MUI_FINISHPAGE_SHOWREADME_NOTCHECKED
!define MUI_FINISHPAGE_RUN "$INSTDIR\josm.exe"
;!define MUI_FINISHPAGE_RUN_NOTCHECKED



;!define MUI_PAGE_CUSTOMFUNCTION_SHOW myShowCallback

; ============================================================================
; MUI Pages
; ============================================================================

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "downloads\LICENSE"
!insertmacro MUI_PAGE_COMPONENTS
;Page custom DisplayAdditionalTasksPage
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_COMPONENTS
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

; ============================================================================
; MUI Languages
; ============================================================================

!insertmacro MUI_LANGUAGE "English"

; ============================================================================
; Reserve Files
; ============================================================================

  ;Things that need to be extracted on first (keep these lines before any File command!)
  ;Only useful for BZIP2 compression

;  ReserveFile "AdditionalTasksPage.ini"
!insertmacro MUI_RESERVEFILE_INSTALLOPTIONS

; ============================================================================
; Section macros
; ============================================================================
!include "Sections.nsh"

; ========= Macro to unselect and disable a section =========

!macro DisableSection SECTION

  Push $0
    SectionGetFlags "${SECTION}" $0
    IntOp $0 $0 & ${SECTION_OFF}
    IntOp $0 $0 | ${SF_RO}
    SectionSetFlags "${SECTION}" $0
  Pop $0

!macroend

; ========= Macro to enable (unreadonly) a section =========
!define SECTION_ENABLE   0xFFFFFFEF
!macro EnableSection SECTION

  Push $0
    SectionGetFlags "${SECTION}" $0
    IntOp $0 $0 & ${SECTION_ENABLE}
    SectionSetFlags "${SECTION}" $0
  Pop $0

!macroend

; ============================================================================
; Command Line
; ============================================================================
!include "FileFunc.nsh"

;!insertmacro GetParameters
;!insertmacro GetOptions

; ============================================================================
; Directory selection page configuration
; ============================================================================
; The text to prompt the user to enter a directory
DirText "Choose a directory in which to install OpenStreeMap."

; The default installation directory
InstallDir $PROGRAMFILES\JOSM\

; See if this is an upgrade; if so, use the old InstallDir as default
InstallDirRegKey HKEY_LOCAL_MACHINE SOFTWARE\JOSM "InstallDir"


; ============================================================================
; Install page configuration
; ============================================================================
ShowInstDetails show
ShowUninstDetails show

; ============================================================================
; Functions and macros
; ============================================================================

; update file extension icons
!macro UpdateIcons
	Push $R0
  	Push $R1
  	Push $R2

	!define UPDATEICONS_UNIQUE ${__LINE__}

	IfFileExists "$SYSDIR\shell32.dll" UpdateIcons.next1_${UPDATEICONS_UNIQUE} UpdateIcons.error1_${UPDATEICONS_UNIQUE}
UpdateIcons.next1_${UPDATEICONS_UNIQUE}:
	GetDllVersion "$SYSDIR\shell32.dll" $R0 $R1
	IntOp $R2 $R0 / 0x00010000
	IntCmp $R2 4 UpdateIcons.next2_${UPDATEICONS_UNIQUE} UpdateIcons.error2_${UPDATEICONS_UNIQUE}
UpdateIcons.next2_${UPDATEICONS_UNIQUE}:
	System::Call 'shell32.dll::SHChangeNotify(i, i, i, i) v (${SHCNE_ASSOCCHANGED}, ${SHCNF_IDLIST}, 0, 0)'
	Goto UpdateIcons.quit_${UPDATEICONS_UNIQUE}

UpdateIcons.error1_${UPDATEICONS_UNIQUE}:
	MessageBox MB_OK|MB_ICONSTOP  "Can't find 'shell32.dll' library. Impossible to update icons"
	Goto UpdateIcons.quit_${UPDATEICONS_UNIQUE}
UpdateIcons.error2_${UPDATEICONS_UNIQUE}:
	MessageBox MB_OK|MB_ICONINFORMATION "You should install the free 'Microsoft Layer for Unicode' to update JOSM file icons"
	Goto UpdateIcons.quit_${UPDATEICONS_UNIQUE}
UpdateIcons.quit_${UPDATEICONS_UNIQUE}:
	!undef UPDATEICONS_UNIQUE
	Pop $R2
	Pop $R1
  	Pop $R0

!macroend

; associate a file extension to an icon
Function Associate
	; $R0 should contain the prefix to associate to JOSM
	Push $R1

	ReadRegStr $R1 HKCR $R0 ""
	StrCmp $R1 "" Associate.doRegister
	Goto Associate.end
Associate.doRegister:
	;The extension is not associated to any program, we can do the link
	WriteRegStr HKCR $R0 "" ${OSM_ASSOC}
Associate.end:
	pop $R1
FunctionEnd

; disassociate a file extension from an icon
Function un.unlink
	; $R0 should contain the prefix to unlink
	Push $R1

	ReadRegStr $R1 HKCR $R0 ""
	StrCmp $R1 ${OSM_ASSOC} un.unlink.doUnlink
	Goto un.unlink.end
un.unlink.doUnlink:
	; The extension is associated with JOSM so, we must destroy this!
	DeleteRegKey HKCR $R0
un.unlink.end:
	pop $R1
FunctionEnd

Function .onInit
  ;Extract InstallOptions INI files
;  !insertmacro MUI_INSTALLOPTIONS_EXTRACT "AdditionalTasksPage.ini"
FunctionEnd

;Function DisplayAdditionalTasksPage
;  !insertmacro MUI_HEADER_TEXT "Select Additional Tasks" "Which additional tasks should be done?"
;  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "AdditionalTasksPage.ini"
;FunctionEnd

; ============================================================================
; Installation execution commands
; ============================================================================

Section "-Required"
;-------------------------------------------

;
; Install for every user
;
SectionIn 1 2 RO
SetShellVarContext all

SetOutPath $INSTDIR

; Write the uninstall keys for Windows
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\OSM" "DisplayVersion" "${VERSION}"
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\OSM" "DisplayName" "JOSM ${VERSION}"
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\OSM" "UninstallString" '"$INSTDIR\uninstall.exe"'
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\OSM" "Publisher" "The OpenStreetMap developer community, http://www.openstreetmap.org/"
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\OSM" "HelpLink" "mailto:newbies@openstreetmap.org."
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\OSM" "URLInfoAbout" "http://www.openstreetmap.org/"
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\OSM" "URLUpdateInfo" "http://wiki.openstreetmap.org/index.php/JOSM"
WriteRegDWORD HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\OSM" "NoModify" 1
WriteRegDWORD HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\OSM" "NoRepair" 1
WriteUninstaller "uninstall.exe"

; Write an entry for ShellExecute
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\App Paths\josm.exe" "" '$INSTDIR\josm.exe'
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\App Paths\josm.exe" "Path" '$INSTDIR'

; Create start menu entries (depending on additional tasks page)
;ReadINIStr $0 "$PLUGINSDIR\AdditionalTasksPage.ini" "Field 2" "State"
;StrCmp $0 "0" SecRequired_skip_StartMenu
; To qoute "http://msdn.microsoft.com/library/default.asp?url=/library/en-us/dnwue/html/ch11d.asp":
; "Do not include Readme, Help, or Uninstall entries on the Programs menu."
CreateShortCut "$SMPROGRAMS\JOSM.lnk" "$INSTDIR\josm.exe" "" "$INSTDIR\josm.exe" 0 "" "" "JAVA OpenStreetMap - Editor"
;SecRequired_skip_StartMenu:

; is command line option "/desktopicon" set?
;${GetParameters} $R0
;${GetOptions} $R0 "/desktopicon=" $R1
;StrCmp $R1 "no" SecRequired_skip_DesktopIcon
;StrCmp $R1 "yes" SecRequired_install_DesktopIcon

; Create desktop icon (depending on additional tasks page and command line option)
;ReadINIStr $0 "$PLUGINSDIR\AdditionalTasksPage.ini" "Field 3" "State"
;StrCmp $0 "0" SecRequired_skip_DesktopIcon
;SecRequired_install_DesktopIcon:
CreateShortCut "$DESKTOP\JOSM.lnk" "$INSTDIR\josm.exe" "" "$INSTDIR\josm.exe" 0 "" "" "JAVA OpenStreetMap - Editor"
;SecRequired_skip_DesktopIcon:

; is command line option "/quicklaunchicon" set?
;${GetParameters} $R0
;${GetOptions} $R0 "/quicklaunchicon=" $R1
;StrCmp $R1 "no" SecRequired_skip_QuickLaunchIcon
;StrCmp $R1 "yes" SecRequired_install_QuickLaunchIcon

; Create quick launch icon (depending on additional tasks page and command line option)
;ReadINIStr $0 "$PLUGINSDIR\AdditionalTasksPage.ini" "Field 4" "State"
;StrCmp $0 "0" SecRequired_skip_QuickLaunchIcon
;SecRequired_install_QuickLaunchIcon:
CreateShortCut "$QUICKLAUNCH\JOSM.lnk" "$INSTDIR\josm.exe" "" "$INSTDIR\josm.exe" 0 "" "" "JAVA OpenStreetMap - Editor"
;SecRequired_skip_QuickLaunchIcon:

; Create File Extensions (depending on additional tasks page)
;ReadINIStr $0 "$PLUGINSDIR\AdditionalTasksPage.ini" "Field 6" "State"
;StrCmp $0 "0" SecRequired_skip_FileExtensions
WriteRegStr HKCR ${OSM_ASSOC} "" "OpenStreetMap data"
WriteRegStr HKCR "${OSM_ASSOC}\Shell\open\command" "" '"$INSTDIR\josm.exe" "%1"'
WriteRegStr HKCR "${OSM_ASSOC}\DefaultIcon" "" '"$INSTDIR\josm.exe",0'
push $R0
	StrCpy $R0 ".osm"
  	Call Associate
	StrCpy $R0 ".gpx"
  	Call Associate
; if somethings added here, add it also to the uninstall section and the AdditionalTask page
pop $R0
!insertmacro UpdateIcons
;SecRequired_skip_FileExtensions:

SectionEnd ; "Required"


Section "JOSM" SecJosm
;-------------------------------------------
SectionIn 1
SetOutPath $INSTDIR
File "josm.exe"

; XXX - should be provided/done by josm.jar itself and not here!
SetShellVarContext current
SetOutPath "$APPDATA\JOSM"

; don't overwrite existing bookmarks
IfFileExists preferences dont_overwrite_bookmarks
File "bookmarks"
dont_overwrite_bookmarks:

; don't overwrite existing de_streets.xml file
IfFileExists de-streets.xml dont_overwrite_de_streets
File "de-streets.xml"
dont_overwrite_de_streets:

; write reasonable defaults for some preferences
; XXX - some of this should be done in JOSM itself
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "laf" "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "download.osm" "true"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "layerlist.visible" "true"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "commandstack.visible" "true"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "propertiesdialog.visible" "true"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "validator.visible" "true"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "draw.segment.direction" "true"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "projection" "org.openstreetmap.josm.data.projection.Epsg4326"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "osm-server.url" "http://www.openstreetmap.org/api"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "taggingpreset.sources" "$APPDATA/JOSM/de-streets.xml"
SectionEnd


SectionGroup /e "Plugins" SecPluginsGroup

Section "mappaint" SecMappaintPlugin
;-------------------------------------------
SectionIn 1 2
SetShellVarContext all
SetOutPath $APPDATA\JOSM\plugins
File "downloads\mappaint.jar"
SectionEnd

Section "osmarender" SecOsmarenderPlugin
;-------------------------------------------
SectionIn 1 2
SetShellVarContext all
SetOutPath $APPDATA\JOSM\plugins
File "downloads\osmarender.jar"
; XXX - should be done inside the plugin and not here!
SetShellVarContext current
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "osmarender.firefox" "$PROGRAMFILES\Mozilla Firefox\firefox.exe"
SectionEnd

Section "WMS" SecWMSPlugin
;-------------------------------------------
SectionIn 1 2
SetShellVarContext all
SetOutPath $APPDATA\JOSM\plugins
File "downloads\wmsplugin.jar"
SectionEnd

Section "namefinder" SecNamefinderPlugin
;-------------------------------------------
SectionIn 1 2
SetShellVarContext all
SetOutPath $APPDATA\JOSM\plugins
File "downloads\namefinder.jar"
SectionEnd

Section "validator" SecValidatorPlugin
;-------------------------------------------
SectionIn 1 2
SetShellVarContext all
SetOutPath $APPDATA\JOSM\plugins
File "downloads\validator.jar"
SectionEnd

Section "tways" SecTWaysPlugin
;-------------------------------------------
SectionIn 1 2
SetShellVarContext all
SetOutPath $APPDATA\JOSM\plugins
File "downloads\tways-0.2.jar"
SectionEnd

SectionGroupEnd	; "Plugins"

Section "-PluginSetting"
;-------------------------------------------
;MessageBox MB_OK "PluginSetting!" IDOK 0
; XXX - should better be handled inside JOSM (recent plugin manager is going in the right direction)
SetShellVarContext current
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "plugins" "mappaint,osmarender,wmsplugin,namefinder,validator,tways-0.2"
SectionEnd


Section "Uninstall" un.SecUinstall
;-------------------------------------------

;
; UnInstall for every user
;
SectionIn 1 2
SetShellVarContext all

Delete "$INSTDIR\josm.exe"
Delete "$INSTDIR\uninstall.exe"
Delete "$INSTDIR\plugins\wmsplugin.jar"
Delete "$INSTDIR\plugins\osmarender.jar"
Delete "$INSTDIR\plugins\mappaint.jar"
Delete "$INSTDIR\plugins\namefinder.jar"
Delete "$INSTDIR\plugins\validator.jar"
Delete "$INSTDIR\plugins\tways-0.2.jar"
IfErrors 0 NoJOSMErrorMsg
	MessageBox MB_OK "Please note: josm.exe could not be removed, it's probably in use!" IDOK 0 ;skipped if josm.exe removed
	Abort "Please note: josm.exe could not be removed, it's probably in use! Abort uninstall process!"
NoJOSMErrorMsg:

DeleteRegKey HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\OSM"
DeleteRegKey HKEY_LOCAL_MACHINE "Software\josm.exe"
DeleteRegKey HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\App Paths\josm.exe"

push $R0
	StrCpy $R0 ".osm"
  	Call un.unlink
	StrCpy $R0 ".gpx"
  	Call un.unlink
pop $R0

DeleteRegKey HKCR ${OSM_ASSOC}
DeleteRegKey HKCR "${OSM_ASSOC}\Shell\open\command"
DeleteRegKey HKCR "${OSM_ASSOC}\DefaultIcon"
!insertmacro UpdateIcons

Delete "$SMPROGRAMS\josm.lnk"
Delete "$DESKTOP\josm.lnk"
Delete "$QUICKLAUNCH\josm.lnk"

RMDir "$INSTDIR"

SectionEnd ; "Uinstall"

Section /o "Un.Personal Settings" un.SecPersonalSettings
;-------------------------------------------
SectionIn 2
SetShellVarContext current
Delete "$APPDATA\JOSM\preferences"
Delete "$APPDATA\JOSM\bookmarks"
Delete "$APPDATA\JOSM\de-streets.xml"
RMDir "$APPDATA\JOSM"
RMDir "$APPDATA\JOSM\plugins\mappaint"
SectionEnd

Section /o "Un.Personal Plugins" un.SecPlugins
;-------------------------------------------
SectionIn 2
SetShellVarContext current
Delete "$APPDATA\JOSM\plugins\wmsplugin.jar"
Delete "$APPDATA\JOSM\plugins\osmarender.jar"
Delete "$APPDATA\JOSM\plugins\osmarender\*.*"
Delete "$APPDATA\JOSM\plugins\mappaint.jar"
Delete "$APPDATA\JOSM\plugins\namefinder.jar"
Delete "$APPDATA\JOSM\plugins\validator\*.*"
Delete "$APPDATA\JOSM\plugins\validator.jar"
Delete "$APPDATA\JOSM\plugins\tways-0.2.jar"
RMDir "$APPDATA\JOSM\plugins\osmarender"
RMDir "$APPDATA\JOSM\plugins\validator"
RMDir "$APPDATA\JOSM\plugins"
RMDir "$APPDATA\JOSM"
SectionEnd


Section "-Un.Finally"
;-------------------------------------------
SectionIn 1 2
; this test must be done after all other things uninstalled (e.g. Global Settings)
IfFileExists "$INSTDIR" 0 NoFinalErrorMsg
    MessageBox MB_OK "Please note: The directory $INSTDIR could not be removed!" IDOK 0 ; skipped if dir doesn't exist
NoFinalErrorMsg:
SectionEnd


; ============================================================================
; PLEASE MAKE SURE, THAT THE DESCRIPTIVE TEXT FITS INTO THE DESCRIPTION FIELD!
; ============================================================================
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SecJosm} "JOSM is the JAVA OpenStreetMap editor for .osm files."
  !insertmacro MUI_DESCRIPTION_TEXT ${SecPluginsGroup} "Various JOSM plugins."
  !insertmacro MUI_DESCRIPTION_TEXT ${SecMappaintPlugin} "An alternative renderer for the map with colouring, line thickness, icons after tags."
  !insertmacro MUI_DESCRIPTION_TEXT ${SecOsmarenderPlugin} "Displays the current screen as nicely rendered SVG graphics in FireFox."
  !insertmacro MUI_DESCRIPTION_TEXT ${SecWMSPlugin} "Display background images from Web Map Service (WMS) sources."
  !insertmacro MUI_DESCRIPTION_TEXT ${SecNamefinderPlugin} "Add a 'Find places by their name' tab to the download dialog."
  !insertmacro MUI_DESCRIPTION_TEXT ${SecValidatorPlugin} "Validates edited data if it conforms to common suggestions."
  !insertmacro MUI_DESCRIPTION_TEXT ${SecTwaysPlugin} "Mass wayfication of segments."
!insertmacro MUI_FUNCTION_DESCRIPTION_END

!insertmacro MUI_UNFUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${un.SecUinstall} "Uninstall JOSM."
  !insertmacro MUI_DESCRIPTION_TEXT ${un.SecPersonalSettings} "Uninstall personal settings like your preferences and bookmarks from your profile: $PROFILE."
  !insertmacro MUI_DESCRIPTION_TEXT ${un.SecPlugins} "Uninstall all plugins."
!insertmacro MUI_UNFUNCTION_DESCRIPTION_END

; ============================================================================
; Callback functions
; ============================================================================

