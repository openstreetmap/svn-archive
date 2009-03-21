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
!define MUI_WELCOMEFINISHPAGE_BITMAP "josm-nsis-brand.bmp"
!define MUI_WELCOMEPAGE_TEXT $(JOSM_WELCOME_TEXT) 
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
!insertmacro MUI_PAGE_LICENSE "..\core\LICENSE"
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

  ;Remember the installer language
  !define MUI_LANGDLL_REGISTRY_ROOT "HKLM" 
  !define MUI_LANGDLL_REGISTRY_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\OSM" 
  !define MUI_LANGDLL_REGISTRY_VALUENAME "Installer Language"
  
  ;; English goes first because its the default. The rest are
  ;; in alphabetical order (at least the strings actually displayed
  ;; will be).

  !insertmacro MUI_LANGUAGE "English"
  !insertmacro MUI_LANGUAGE "German"

;--------------------------------
;Translations

  !define JOSM_DEFAULT_LANGFILE "locale\english.nsh"

  !include "langmacros.nsh"
  
  !insertmacro JOSM_MACRO_INCLUDE_LANGFILE "ENGLISH" "locale\english.nsh"
  !insertmacro JOSM_MACRO_INCLUDE_LANGFILE "GERMAN" "locale\german.nsh"

; Uninstall stuff (NSIS 2.08: "\r\n" don't work here)
!define MUI_UNCONFIRMPAGE_TEXT_TOP ${un.JOSM_UNCONFIRMPAGE_TEXT_TOP}

; ============================================================================
; Installation types
; ============================================================================

InstType "$(JOSM_FULL_INSTALL)"

InstType "un.$(un.JOSM_DEFAULT_UNINSTALL)"
InstType "un.$(un.JOSM_FULL_UNINSTALL)"

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
DirText $(JOSM_DIR_TEXT)

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
	MessageBox MB_OK|MB_ICONSTOP $(JOSM_UPDATEICONS_ERROR1)
	Goto UpdateIcons.quit_${UPDATEICONS_UNIQUE}
UpdateIcons.error2_${UPDATEICONS_UNIQUE}:
	MessageBox MB_OK|MB_ICONINFORMATION $(JOSM_UPDATEICONS_ERROR2)
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
  !insertmacro MUI_LANGDLL_DISPLAY
FunctionEnd

Function un.onInit

  !insertmacro MUI_UNGETLANGUAGE
  
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

SectionEnd ; "Required"


Section $(JOSM_SEC_JOSM) SecJosm
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

; write reasonable defaults for some preferences
; XXX - some of this should be done in JOSM itself, see also JOSM core, data\Preferences.java function resetToDefault()
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "projection" "org.openstreetmap.josm.data.projection.Epsg4326"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "draw.segment.direction" "true"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "layerlist.visible" "true"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "selectionlist.visible" "true"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "commandstack.visible" "true"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "propertiesdialog.visible" "true"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "osm-server.url" "http://www.openstreetmap.org/api"
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "laf" "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"

${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "validator.visible" "true"

SectionEnd


SectionGroup $(JOSM_SEC_PLUGINS_GROUP) SecPluginsGroup

;Section "osmarender" SecOsmarenderPlugin
; osmarender needs Firefox (which isn't available on all machines)
; and often provides clipped SVG graphics - therefore it's ommited by default
;-------------------------------------------
;SectionIn 1 2
;SetShellVarContext all
;SetOutPath $APPDATA\JOSM\plugins
;File "downloads\osmarender.jar"
; XXX - should be done inside the plugin and not here!
;SetShellVarContext current
;${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "osmarender.firefox" "$PROGRAMFILES\Mozilla Firefox\firefox.exe"
;SectionEnd

Section $(JOSM_SEC_WMS_PLUGIN) SecWMSPlugin
;-------------------------------------------
SectionIn 1 2
SetShellVarContext all
SetOutPath $APPDATA\JOSM\plugins
File "..\dist\wmsplugin.jar"
SetOutPath $INSTDIR\webkit-image\imageformats
File "webkit-image\imageformats\qjpeg4.dll"
SetOutPath $INSTDIR\webkit-image
File "webkit-image\mingwm10.dll"
File "webkit-image\QtCore4.dll"
File "webkit-image\QtGui4.dll"
File "webkit-image\QtNetwork4.dll"
File "webkit-image\QtWebKit4.dll"
File "webkit-image\webkit-image.exe"
SectionEnd

Section $(JOSM_SEC_VALIDATOR_PLUGIN) SecValidatorPlugin
;-------------------------------------------
SectionIn 1 2
SetShellVarContext all
SetOutPath $APPDATA\JOSM\plugins
File "..\dist\validator.jar"
SectionEnd

SectionGroupEnd	; "Plugins"

Section $(JOSM_SEC_STARTMENU) SecStartMenu
;-------------------------------------------
SectionIn 1 2
; Create start menu entries (depending on additional tasks page)
;ReadINIStr $0 "$PLUGINSDIR\AdditionalTasksPage.ini" "Field 2" "State"
;StrCmp $0 "0" SecRequired_skip_StartMenu
; To qoute "http://msdn.microsoft.com/library/default.asp?url=/library/en-us/dnwue/html/ch11d.asp":
; "Do not include Readme, Help, or Uninstall entries on the Programs menu."
CreateShortCut "$SMPROGRAMS\JOSM.lnk" "$INSTDIR\josm.exe" "" "$INSTDIR\josm.exe" 0 "" "" $(JOSM_LINK_TEXT)
;SecRequired_skip_StartMenu:
SectionEnd

Section $(JOSM_SEC_DESKTOP_ICON) SecDesktopIcon
;-------------------------------------------
; SectionIn 1 2
; is command line option "/desktopicon" set?
;${GetParameters} $R0
;${GetOptions} $R0 "/desktopicon=" $R1
;StrCmp $R1 "no" SecRequired_skip_DesktopIcon
;StrCmp $R1 "yes" SecRequired_install_DesktopIcon

; Create desktop icon (depending on additional tasks page and command line option)
;ReadINIStr $0 "$PLUGINSDIR\AdditionalTasksPage.ini" "Field 3" "State"
;StrCmp $0 "0" SecRequired_skip_DesktopIcon
;SecRequired_install_DesktopIcon:
CreateShortCut "$DESKTOP\JOSM.lnk" "$INSTDIR\josm.exe" "" "$INSTDIR\josm.exe" 0 "" "" $(JOSM_LINK_TEXT)
;SecRequired_skip_DesktopIcon:
SectionEnd

Section $(JOSM_SEC_QUICKLAUNCH_ICON) SecQuickLaunchIcon
;-------------------------------------------
SectionIn 1 2
; is command line option "/quicklaunchicon" set?
;${GetParameters} $R0
;${GetOptions} $R0 "/quicklaunchicon=" $R1
;StrCmp $R1 "no" SecRequired_skip_QuickLaunchIcon
;StrCmp $R1 "yes" SecRequired_install_QuickLaunchIcon

; Create quick launch icon (depending on additional tasks page and command line option)
;ReadINIStr $0 "$PLUGINSDIR\AdditionalTasksPage.ini" "Field 4" "State"
;StrCmp $0 "0" SecRequired_skip_QuickLaunchIcon
;SecRequired_install_QuickLaunchIcon:
CreateShortCut "$QUICKLAUNCH\JOSM.lnk" "$INSTDIR\josm.exe" "" "$INSTDIR\josm.exe" 0 "" "" $(JOSM_LINK_TEXT)
;SecRequired_skip_QuickLaunchIcon:
SectionEnd

Section $(JOSM_SEC_FILE_EXTENSIONS) SecFileExtensions
;-------------------------------------------
SectionIn 1 2
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
SectionEnd


Section "-PluginSetting"
;-------------------------------------------
SectionIn 1 2
;MessageBox MB_OK "PluginSetting!" IDOK 0
; XXX - should better be handled inside JOSM (recent plugin manager is going in the right direction)
SetShellVarContext current
!include LogicLib.nsh
${WriteINIStrNS} $R0 "$APPDATA\JOSM\preferences" "plugins" "wmsplugin;validator"
SectionEnd


Section "un.$(un.JOSM_SEC_UNINSTALL)" un.SecUinstall
;-------------------------------------------

;
; UnInstall for every user
;
SectionIn 1 2
SetShellVarContext all

Delete "$INSTDIR\josm.exe"
IfErrors 0 NoJOSMErrorMsg
	MessageBox MB_OK $(un.JOSM_IN_USE_ERROR) IDOK 0 ;skipped if josm.exe removed
	Abort $(un.JOSM_IN_USE_ERROR)
NoJOSMErrorMsg:
Delete "$INSTDIR\uninstall.exe"
Delete "$APPDATA\JOSM\plugins\wmsplugin.jar"
Delete "$APPDATA\JOSM\plugins\namefinder.jar"
Delete "$APPDATA\JOSM\plugins\validator.jar"
RMDir "$APPDATA\JOSM\plugins"
RMDir "$APPDATA\JOSM"

DeleteRegKey HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\OSM"
DeleteRegKey HKEY_LOCAL_MACHINE "Software\josm.exe"
DeleteRegKey HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\App Paths\josm.exe"

; Remove Language preference info
DeleteRegKey HKCU "Software/JOSM" ;${MUI_LANGDLL_REGISTRY_ROOT} ${MUI_LANGDLL_REGISTRY_KEY}

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

Section /o "un.$(un.JOSM_SEC_PERSONAL_SETTINGS)" un.SecPersonalSettings
;-------------------------------------------
SectionIn 2
SetShellVarContext current
Delete "$APPDATA\JOSM\preferences"
Delete "$APPDATA\JOSM\bookmarks"
;Delete "$APPDATA\JOSM\de-streets.xml"
RMDir "$APPDATA\JOSM"
SectionEnd

Section /o "un.$(un.JOSM_SEC_PLUGINS)"un.SecPlugins
;-------------------------------------------
SectionIn 2
SetShellVarContext current
Delete "$APPDATA\JOSM\plugins\wmsplugin.jar"
;Delete "$APPDATA\JOSM\plugins\osmarender.jar"
;Delete "$APPDATA\JOSM\plugins\osmarender\*.*"
Delete "$APPDATA\JOSM\plugins\namefinder.jar"
Delete "$APPDATA\JOSM\plugins\validator\*.*"
Delete "$APPDATA\JOSM\plugins\validator.jar"
;RMDir "$APPDATA\JOSM\plugins\osmarender"
RMDir "$APPDATA\JOSM\plugins\validator"
RMDir "$APPDATA\JOSM\plugins"
RMDir "$APPDATA\JOSM"
SectionEnd


Section "-Un.Finally"
;-------------------------------------------
SectionIn 1 2
; this test must be done after all other things uninstalled (e.g. Global Settings)
IfFileExists "$INSTDIR" 0 NoFinalErrorMsg
    MessageBox MB_OK $(un.JOSM_INSTDIR_ERROR) IDOK 0 ; skipped if dir doesn't exist
NoFinalErrorMsg:
SectionEnd


; ============================================================================
; PLEASE MAKE SURE, THAT THE DESCRIPTIVE TEXT FITS INTO THE DESCRIPTION FIELD!
; ============================================================================
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SecJosm} $(JOSM_SECDESC_JOSM)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecPluginsGroup} $(JOSM_SECDESC_PLUGINS_GROUP)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecWMSPlugin} $(JOSM_SECDESC_WMS_PLUGIN)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecValidatorPlugin} $(JOSM_SECDESC_VALIDATOR_PLUGIN)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecStartMenu} $(JOSM_SECDESC_STARTMENU)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecDesktopIcon} $(JOSM_SECDESC_DESKTOP_ICON)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecQuickLaunchIcon} $(JOSM_SECDESC_QUICKLAUNCH_ICON) 
  !insertmacro MUI_DESCRIPTION_TEXT ${SecFileExtensions} $(JOSM_SECDESC_FILE_EXTENSIONS)
  

!insertmacro MUI_FUNCTION_DESCRIPTION_END

!insertmacro MUI_UNFUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${un.SecUinstall} $(un.JOSM_SECDESC_UNINSTALL)
  !insertmacro MUI_DESCRIPTION_TEXT ${un.SecPersonalSettings} $(un.JOSM_SECDESC_PERSONAL_SETTINGS)
  !insertmacro MUI_DESCRIPTION_TEXT ${un.SecPlugins} $(un.JOSM_SECDESC_PLUGINS)
!insertmacro MUI_UNFUNCTION_DESCRIPTION_END

; ============================================================================
; Callback functions
; ============================================================================

