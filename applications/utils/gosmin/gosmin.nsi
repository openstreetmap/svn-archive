;
; gosmin.nsi
;

; Set the compression mechanism first.
SetCompressor /SOLID lzma

; ============================================================================
; Header configuration
; ============================================================================
; The name and version of the installer
!define PROGRAM_NAME "OSM Karten"
!define VERSION "0.0.4"

Name "${PROGRAM_NAME}"

; The file to write
OutFile "gosmin.exe"

XPStyle on


; ============================================================================
; log output
; ============================================================================
!include "FileFunc.nsh"
Var LogFile
!define LogOut `!insertmacro _LogOut`
!macro _LogOut Text
FileWrite $LogFile "${Text}$\r$\n"
!macroend


!addplugindir "3rdparty"

!include "3rdparty\XML.nsh"

!include "MUI.nsh"
!include "FileFunc.nsh"
!include "LogicLib.nsh"
!include "WordFunc.nsh"

!include "pages\WelcomePage.nsh"
!include "pages\PreparationsPage.nsh"
!include "pages\DevicePage.nsh"
!include "pages\DestinationPage.nsh"
!include "pages\SourcePage.nsh"

; ============================================================================
; Modern UI
; ============================================================================

!include "MUI.nsh"

!define MUI_ICON "gosmin.ico"

!define MUI_WELCOMEFINISHPAGE_BITMAP "images\brand\brand.bmp" 
!define MUI_HEADERIMAGE 
!define MUI_HEADERIMAGE_RIGHT
!define MUI_HEADERIMAGE_BITMAP "images\brand\header.bmp"

!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_WELCOMEPAGE_TEXT "Einfache Installation einer OpenStreetMap Karte auf einem Garmin GPS Gerät.\r\n\r\nVersion: ${VERSION}\r\n\r\nKlicken Sie auf 'Weiter' um fortzufahren."
!define MUI_FINISHPAGE_TEXT "ACHTUNG: Die Karte muß im Garmin Gerät unter: Einstellungen / Karte / Karteninfo eingeschaltet werden!!!"
;!define MUI_FINISHPAGE_LINK "Hinweise zum Umgang mit OSM Karten!"
;!define MUI_FINISHPAGE_LINK_LOCATION "http://wiki.openstreetmap.org/wiki/Garmin"

!define MUI_PAGE_CUSTOMFUNCTION_PRE WelcomePageSetupLinkPre
!define MUI_PAGE_CUSTOMFUNCTION_SHOW myShowCallback

; ============================================================================
; MUI Pages
; ============================================================================

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "pages\LicensePage.txt"
Page custom preparationsPageDisplay
Page custom devicePageDisplay
Page custom destinationPageDisplay
Page custom sourcePageDisplay
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

; ============================================================================
; MUI Languages
; ============================================================================

!insertmacro MUI_LANGUAGE "german"

; ============================================================================
; Reserve Files
; ============================================================================

  ;Things that need to be extracted on first (keep these lines before any File command!)
  ;Only useful for BZIP2 compression

  ReserveFile "pages\devices.ini"
  ReserveFile "pages\maps.ini"
  !insertmacro MUI_RESERVEFILE_INSTALLOPTIONS

; ============================================================================
; Install page configuration
; ============================================================================
ShowInstDetails show

; ============================================================================
; Functions and macros
; ============================================================================

Function myShowCallback

  FileOpen $LogFile "$PLUGINSDIR\gosmin.log" w
  ${LogOut} "myShowCallback"

  SetOutPath '$PLUGINSDIR'
  File "pages\maps.ini"
  File "pages\devices.ini"

  Call WelcomePageInit  
  Call devicePageInit
  Call sourcePageInit

FunctionEnd

!macro FileSize fileName fileSize
  ${GetParent} "${fileName}" $R7
  ${GetFileName} "${fileName}" $R8
  ${GetSize} "$R7" "/M=$R8 /S=0M /G=0" ${fileSize} $R5 $R6
!macroend

; ============================================================================
; Installation execution commands
; ============================================================================

Section "-Required"
;-------------------------------------------

SetOutPath '$PLUGINSDIR'

File 3rdparty\7za.exe

Var /GLOBAL installFile

Var /GLOBAL installSizeMB


; ============================================================================
; "calculate" local filename to install
; ============================================================================
StrCpy $installFile $sourcePageLocalFile
${If} $sourcePageSelect == "download"
  StrCpy $installSizeMB $sourcePageMapFileSizeMB
${Else}
  !insertmacro FileSize $installFile $1
  StrCpy $installSizeMB $1
${EndIf}
DetailPrint "Verwende Datei: $installFile ($installSizeMB MB)"
DetailPrint "Freier Platz auf Ziellaufwerk: $destinationFreeMB MB"

; ============================================================================
; check if file will fit on target space
; ============================================================================
IntCmp $destinationFreeMB $installSizeMB 0 0 enoughFreeSpace
    DetailPrint "$installFile ($installSizeMB MB) ist zu groß für $destinationDrive ($destinationFreeMB MB)"
    MessageBox MB_OK|MB_ICONSTOP "$installFile ($installSizeMB MB) ist zu groß für $destinationDrive ($destinationFreeMB MB)"
    Abort
enoughFreeSpace:

; ============================================================================
; check if garmin dir exists on target (otherwise create it)
; ============================================================================
IfFileExists "$destinationDir" garmindirexists
  CreateDirectory $destinationDir
garmindirexists:

IfFileExists "$destinationDir" garmindirexists2
  MessageBox MB_OK|MB_ICONSTOP "Konnte Verzeichnis $destinationDir nicht erzeugen!"
  Quit
garmindirexists2:
  
; ============================================================================
; if map file already exists on device, ask user to delete
; ============================================================================
IfFileExists "$destinationDir\gmapsupp.img" 0 mapcopy
  !insertmacro FileSize $destinationDir\gmapsupp.img $0
  MessageBox MB_OKCANCEL|MB_ICONQUESTION "Eine Kartendatei $destinationDir\gmapsupp.img ($0 MB) ist bereits installiert. Diese Datei ersetzen?" IDOK deleteoldfile
  Abort
deleteoldfile:
  DetailPrint "User acknowledged to delete old gmapsupp.img file!"
  Delete "$destinationDir\gmapsupp.img"
mapcopy:

; ============================================================================
; download map file
; ============================================================================
${If} $sourcePageSelect == "download"
  ; TODO: check, if file already existing
  DetailPrint "Download: $sourcePageMapUrl"
  ;NSISdl::download $sourcePageMapUrl $installFile
  metadl::download $sourcePageMapUrl $installFile
  Pop $R0 ;Get the return value
  ${If} $R0 != "success"
    DetailPrint "Download failed: $R0"
    MessageBox MB_OK|MB_ICONSTOP "Download failed: $R0"
    Abort
  ${EndIf}
  !insertmacro FileSize $installFile $1
  DetailPrint "Download finished: $installFile ($1 MB)"
${EndIf}

; ============================================================================
; check if downloaded file really exists now
; ============================================================================
IfFileExists "$installFile" downloadedok
  DetailPrint "Couldn't find $installFile!"
  MessageBox MB_OK|MB_ICONSTOP "Couldn't find $installFile!"
  Abort
downloadedok:

; ============================================================================
; unzip map file
; ============================================================================

;  detect from file extension, if unzip is necessary

${GetFileExt} $installFile $0
;MessageBox MB_OK "Extension: $0"

StrCpy $1 "plain"

${If} $0 == "bz2"
  StrCpy $1 "unzip"
${EndIf}
${If} $0 == "zip"
  StrCpy $1 "unzip"
${EndIf}
${If} $0 == "gz"
  StrCpy $1 "unzip"
${EndIf}
${If} $0 == "7z"
  StrCpy $1 "unzip"
${EndIf}

${If} $1 == "unzip"
  DetailPrint "Entpacke Datei: $installFile ..."
  ExecWait '"$PLUGINSDIR\7za.exe" e "$installFile"' $0
  ${Switch} $0
    ${Case} 0
    ${Break}
    ${Case} 1
    DetailPrint "7zip: Warning"
    Abort
    ${Break}
    ${Case} 2
    DetailPrint "7zip: Fatal error"
    Abort
    ${Break}
    ${Case} 7
    DetailPrint "7zip: Command line error"
    Abort
    ${Break}
    ${Case} 8
    DetailPrint "7zip: Not enough memory"
    Abort
    ${Break}
    ${Case} 255
    DetailPrint "7zip: User stopped process"
    Abort
    ${Break}
  ${EndSwitch}
  !insertmacro FileSize "$PLUGINSDIR\gmapsupp.img" $1
  DetailPrint "Datei $installFile entpackt (gmapsupp.img $1 MB)!"
${EndIf}

; ============================================================================
; check if unzipped file really exists now
; ============================================================================
IfFileExists gmapsupp.img unzippedok
  DetailPrint "Couldn't find gmapsupp.img!"
  MessageBox MB_OK|MB_ICONSTOP "Couldn't find gmapsupp.img!"
  Abort
unzippedok:

!insertmacro FileSize "$PLUGINSDIR\gmapsupp.img" $1
IntCmp $destinationFreeMB $1 0 0 unzippedEnoughFreeSpace
    DetailPrint "gmapsupp.img ($1 MB) ist zu groß für Ziel ($destinationFreeMB MB)"
    MessageBox MB_OK|MB_ICONSTOP "gmapsupp.img ($1 MB) ist zu groß für Ziel ($destinationFreeMB MB)"
    Abort
unzippedEnoughFreeSpace:
  
; ============================================================================
; finally copy map file to device
; ============================================================================
DetailPrint "Kopiere $PLUGINSDIR\gmapsupp.img nach $destinationDir!"
ClearErrors
CopyFiles $PLUGINSDIR\gmapsupp.img $destinationDir
IfErrors 0 copyok
  MessageBox MB_OK|MB_ICONSTOP "Konnte Datei $PLUGINSDIR\gmapsupp.img nicht nach $destinationDir kopieren!"
  Abort
copyok:

MessageBox MB_OK|MB_ICONEXCLAMATION "Fertig! Im Garmin Gerät bitte noch die Karte unter: Einstellungen / Karte / Karteninfo einschalten!!!"

SectionEnd ; "Required"
