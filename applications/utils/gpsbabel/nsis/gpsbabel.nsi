;
; gpsbabel.nsi
;
; $Id


; get this from *outside*!
!define VERSION "1.3.4"


; Set the compression mechanism first.
; As of NSIS 2.07, solid compression which makes installer about 1MB smaller
; is no longer the default, so use the /SOLID switch.
; This unfortunately is unknown to NSIS prior to 2.07 and creates an error.
; So if you get an error here, please update to at least NSIS 2.07!
SetCompressor /SOLID lzma

InstType "GPSBabel"

InstType "un.All (remove all)"

; Used to refresh the display of file association
!define SHCNE_ASSOCCHANGED 0x08000000
!define SHCNF_IDLIST 0

; Used to add associations between file extensions and GPSBabel
!define GPSBABEL_ASSOC "gpsbabel-file"

; ============================================================================
; Header configuration
; ============================================================================
; The name of the installer
!define PROGRAM_NAME "GPSBabel"

Name "${PROGRAM_NAME} ${VERSION}"

; The file to write
OutFile "GPSBabel-setup-${VERSION}.exe"

; Icon of installer and uninstaller
Icon "GPSBabelGUI.ico"
UninstallIcon "GPSBabelGUI.ico"

; Uninstall stuff (NSIS 2.08: "\r\n" don't work here)
!define MUI_UNCONFIRMPAGE_TEXT_TOP "The following GPSBabel installation will be uninstalled. Click 'Next' to continue."
; Uninstall stuff (this text isn't used with the MODERN_UI!)
;UninstallText "This will uninstall Gpsbabel.\r\nBefore starting the uninstallation, make sure Gpsbabel is not running.\r\nClick 'Next' to continue."

XPStyle on



; ============================================================================
; Modern UI
; ============================================================================
; The modern user interface will look much better than the common one.

!include "MUI.nsh"

!define MUI_ICON "GPSBabelGUI.ico"
!define MUI_UNICON "GPSBabelGUI.ico"

!define MUI_COMPONENTSPAGE_SMALLDESC
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_UNFINISHPAGE_NOAUTOCLOSE
!define MUI_WELCOMEPAGE_TEXT "This wizard will guide you through the installation of GPSBabel.\r\n\r\nBefore starting the installation, make sure GPSBabel is not running.\r\n\r\nClick 'Next' to continue."
;!define MUI_FINISHPAGE_LINK "Install WinPcap to be able to capture packets from a network!"
;!define MUI_FINISHPAGE_LINK_LOCATION "http://www.winpcap.org"

; NSIS shows Readme files by opening the Readme file with the default application for
; the file's extension. "README.win32" won't work in most cases, because extension "win32"
; is usually not associated with an appropriate text editor. We should use extension "txt"
; for a text file or "html" for an html README file.
;!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\NEWS.txt"
;!define MUI_FINISHPAGE_SHOWREADME_TEXT "Show News"
!define MUI_FINISHPAGE_SHOWREADME_NOTCHECKED
!define MUI_FINISHPAGE_RUN "$INSTDIR\GPSBabelGui.exe"
!define MUI_FINISHPAGE_RUN_NOTCHECKED


; ============================================================================
; MUI Pages
; ============================================================================

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "gpsbabel-${VERSION}\COPYING"
!insertmacro MUI_PAGE_COMPONENTS
Page custom DisplayAdditionalTasksPage
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
;!insertmacro MUI_UNPAGE_COMPONENTS
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

  ReserveFile "AdditionalTasksPage.ini"
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

!insertmacro GetParameters
!insertmacro GetOptions

; ============================================================================
; License page configuration
; ============================================================================
LicenseText "GPSBabel is distributed under the GNU General Public License."
LicenseData "gpsbabel-${VERSION}\COPYING"

; ============================================================================
; Component page configuration
; ============================================================================
ComponentText "The following components are available for installation."

; ============================================================================
; Directory selection page configuration
; ============================================================================
; The text to prompt the user to enter a directory
DirText "Choose a directory in which to install GPSBabel."

; The default installation directory
InstallDir $PROGRAMFILES\GPSBabel\

; See if this is an upgrade; if so, use the old InstallDir as default
InstallDirRegKey HKEY_LOCAL_MACHINE SOFTWARE\GPSBabel "InstallDir"


; ============================================================================
; Install page configuration
; ============================================================================
ShowInstDetails show
ShowUninstDetails show

; ============================================================================
; Functions and macros
; ============================================================================
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
	MessageBox MB_OK|MB_ICONINFORMATION "You should install the free 'Microsoft Layer for Unicode' to update GPSBabel file icons"
	Goto UpdateIcons.quit_${UPDATEICONS_UNIQUE}
UpdateIcons.quit_${UPDATEICONS_UNIQUE}:
	!undef UPDATEICONS_UNIQUE
	Pop $R2
	Pop $R1
  	Pop $R0

!macroend

Function Associate
	; $R0 should contain the prefix to associate to GPSBabel
	Push $R1

	ReadRegStr $R1 HKCR $R0 ""
	StrCmp $R1 "" Associate.doRegister
	Goto Associate.end
Associate.doRegister:
	;The extension is not associated to any program, we can do the link
	WriteRegStr HKCR $R0 "" ${GPSBABEL_ASSOC}
Associate.end:
	pop $R1
FunctionEnd

Function un.unlink
	; $R0 should contain the prefix to unlink
	Push $R1

	ReadRegStr $R1 HKCR $R0 ""
	StrCmp $R1 ${GPSBABEL_ASSOC} un.unlink.doUnlink
	Goto un.unlink.end
un.unlink.doUnlink:
	; The extension is associated with GPSBabel so, we must destroy this!
	DeleteRegKey HKCR $R0
un.unlink.end:
	pop $R1
FunctionEnd

Function .onInit
  ;Extract InstallOptions INI files
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT "AdditionalTasksPage.ini"
FunctionEnd

Function DisplayAdditionalTasksPage
  !insertmacro MUI_HEADER_TEXT "Select Additional Tasks" "Which additional tasks should be done?"
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "AdditionalTasksPage.ini"
FunctionEnd

; ============================================================================
; Installation execution commands
; ============================================================================

Section "!GPSBabel" SecGPSBabel
;-------------------------------------------
;
; Install for every user
;
SectionIn 1 2 RO
SetShellVarContext all


SetOutPath $INSTDIR
File "gpsbabel-${VERSION}\AUTHORS"
File "gpsbabel-${VERSION}\COPYING"
File "gpsbabel-${VERSION}\gpsbabel.exe"
File "gpsbabel-${VERSION}\gpsbabel.html"
File "gpsbabel-${VERSION}\GPSBabelGUI.exe"
File "gpsbabel-${VERSION}\libexpat.dll"


; Write the uninstall keys for Windows
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\GPSBabel" "DisplayVersion" "${VERSION}"
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\GPSBabel" "DisplayName" "GPSBabel ${VERSION}"
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\GPSBabel" "UninstallString" '"$INSTDIR\uninstall.exe"'
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\GPSBabel" "Publisher" "The GPSBabel developer community, http://www.gpsbabel.org"
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\GPSBabel" "HelpLink" "http://www.gpsbabel.org/lists.html"
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\GPSBabel" "URLInfoAbout" "http://www.gpsbabel.org"
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\GPSBabel" "URLUpdateInfo" "http://www.gpsbabel.org/download.html"
WriteRegDWORD HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\GPSBabel" "NoModify" 1
WriteRegDWORD HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\GPSBabel" "NoRepair" 1
WriteUninstaller "uninstall.exe"

; Write an entry for ShellExecute
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\App Paths\GPSBabelGUI.exe" "" '$INSTDIR\GPSBabelGUI.exe'
WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\App Paths\GPSBabelGUI.exe" "Path" '$INSTDIR'

; Create start menu entries (depending on additional tasks page)
ReadINIStr $0 "$PLUGINSDIR\AdditionalTasksPage.ini" "Field 2" "State"
StrCmp $0 "0" SecRequired_skip_StartMenu
SetOutPath $PROFILE
CreateDirectory "$SMPROGRAMS\GPSBabel"
; To qoute "http://msdn.microsoft.com/library/default.asp?url=/library/en-us/dnwue/html/ch11d.asp":
; "Do not include Readme, Help, or Uninstall entries on the Programs menu."
CreateShortCut "$SMPROGRAMS\GPSBabelGUI.lnk" "$INSTDIR\GPSBabelGUI.exe" "" "$INSTDIR\GPSBabelGUI.exe" 0 "" "" "The GPSBabel converter"
SecRequired_skip_StartMenu:

; is command line option "/desktopicon" set?
${GetParameters} $R0
${GetOptions} $R0 "/desktopicon=" $R1
StrCmp $R1 "no" SecRequired_skip_DesktopIcon
StrCmp $R1 "yes" SecRequired_install_DesktopIcon

; Create desktop icon (depending on additional tasks page and command line option)
ReadINIStr $0 "$PLUGINSDIR\AdditionalTasksPage.ini" "Field 3" "State"
StrCmp $0 "0" SecRequired_skip_DesktopIcon
SecRequired_install_DesktopIcon:
CreateShortCut "$DESKTOP\GPSBabelGUI.lnk" "$INSTDIR\GPSBabelGUI.exe" "" "$INSTDIR\GPSBabelGUI.exe" 0 "" "" "The GPSBabel converter"
SecRequired_skip_DesktopIcon:

; is command line option "/quicklaunchicon" set?
${GetParameters} $R0
${GetOptions} $R0 "/quicklaunchicon=" $R1
StrCmp $R1 "no" SecRequired_skip_QuickLaunchIcon
StrCmp $R1 "yes" SecRequired_install_QuickLaunchIcon

; Create quick launch icon (depending on additional tasks page and command line option)
ReadINIStr $0 "$PLUGINSDIR\AdditionalTasksPage.ini" "Field 4" "State"
StrCmp $0 "0" SecRequired_skip_QuickLaunchIcon
SecRequired_install_QuickLaunchIcon:
CreateShortCut "$QUICKLAUNCH\GPSBabelGUI.lnk" "$INSTDIR\GPSBabelGUI.exe" "" "$INSTDIR\GPSBabelGUI.exe" 0 "" "" "The GPSBabel converter"
SecRequired_skip_QuickLaunchIcon:

SectionEnd ; "Required"


SectionGroup /e "File Extensions" SecFileExtensions

Section "GPX" SecFileExtensionsGPX
;-------------------------------------------
WriteRegStr HKCR ${GPSBABEL_ASSOC} "" "GPSBabel file"
WriteRegStr HKCR "${GPSBABEL_ASSOC}\Shell\open\command" "" '"$INSTDIR\GPSBabelGUI.exe" "%1"'
WriteRegStr HKCR "${GPSBABEL_ASSOC}\DefaultIcon" "" '"$INSTDIR\GPSBabelGUI.exe",1'
push $R0
	StrCpy $R0 ".gpx"
  	Call Associate
pop $R0
!insertmacro UpdateIcons

SectionEnd

; if you add new file extensions here, add it also to the uninstall section

SectionGroupEnd


Section "Uninstall" un.SecUinstall
;-------------------------------------------

;
; UnInstall for every user
;
SectionIn 1 2
SetShellVarContext all

Delete "$INSTDIR\GPSBabel.exe"
IfErrors 0 NoGPSBabelErrorMsg
	MessageBox MB_OK "Please note: gpsbabel.exe could not be removed, it's probably in use!" IDOK 0 ;skipped if gpsbabel.exe removed
	Abort "Please note: gpsbabel.exe could not be removed, it's probably in use! Abort uninstall process!"
NoGPSBabelErrorMsg:

Delete "$INSTDIR\GPSBabelGUI.exe"
IfErrors 0 NoGPSBabelGUIErrorMsg
	MessageBox MB_OK "Please note: GPSBabelGUI.exe could not be removed, it's probably in use!" IDOK 0 ;skipped if GPSBabelGUI.exe removed
	Abort "Please note: GPSBabelGUI.exe could not be removed, it's probably in use! Abort uninstall process!"
NoGPSBabelGUIErrorMsg:



DeleteRegKey HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\GPSBabel"
DeleteRegKey HKEY_LOCAL_MACHINE "Software\GPSBabel"
DeleteRegKey HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\App Paths\GPSBabelGUI.exe"

push $R0
	StrCpy $R0 ".gpx"
  	Call un.unlink
pop $R0

DeleteRegKey HKCR ${GPSBABEL_ASSOC}
DeleteRegKey HKCR "${GPSBABEL_ASSOC}\Shell\open\command"
DeleteRegKey HKCR "${GPSBABEL_ASSOC}\DefaultIcon"
!insertmacro UpdateIcons


Delete "$INSTDIR\AUTHORS"
Delete "$INSTDIR\COPYING"
Delete "$INSTDIR\gpsbabel.exe"
Delete "$INSTDIR\gpsbabel.html"
Delete "$INSTDIR\GPSBabelGUI.exe"
Delete "$INSTDIR\libexpat.dll"

Delete "$INSTDIR\uninstall.exe"
Delete "$SMPROGRAMS\GPSBabelGUI.lnk"
Delete "$DESKTOP\GPSBabelGUI.lnk"
Delete "$QUICKLAUNCH\GPSBabelGUI.lnk"

RMDir "$SMPROGRAMS\GPSBabel"
RMDir "$INSTDIR\GPSBabel"
RMDir "$INSTDIR"

SectionEnd ; "Uinstall"


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
  !insertmacro MUI_DESCRIPTION_TEXT ${SecGPSBabel} "GPSBabel GUI frontend and command line version."
  !insertmacro MUI_DESCRIPTION_TEXT ${SecFileExtensions} "Associate file extension(s) to GPSBabel."
  !insertmacro MUI_DESCRIPTION_TEXT ${SecFileExtensionsGPX} "Associate .gpx (GPS Exchange Format) to GPSBabel."
!insertmacro MUI_FUNCTION_DESCRIPTION_END

!insertmacro MUI_UNFUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${un.SecUinstall} "Uninstall all GPSBabel components."
!insertmacro MUI_UNFUNCTION_DESCRIPTION_END

