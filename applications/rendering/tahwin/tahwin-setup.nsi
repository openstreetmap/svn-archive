; This program is free software: you can redistribute it and/or modify
; it under the terms of the GNU General Public License as published by
; the Free Software Foundation, either version 3 of the License, or
; (at your option) any later version.
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
; GNU General Public License for more details.
;
; You should have received a copy of the GNU General Public License
; along with this program.  If not, see <http://www.gnu.org/licenses/>.
;
; Check http://www.osm-tools.org/ for updates.
;
; Copyright 2009-2011 Stephan Knauss <osm@stephans-server.de>

; This installer is based on the ideas of the abandoned installer hosted on sourceforge
; http://sourceforge.net/projects/wintah/
; Thanks to Eddi di Pieri for creating the initial version and providing some windows scripts!

!include WinMessages.nsh
!include "StrFunc.nsh"
!include "LogicLib.nsh"
!include "WordFunc.nsh"
!insertmacro WordFind
!insertmacro WordFind2x
;  !insertmacro WordReplace
!include MUI2.nsh

${StrRep}

; used plugins are included with the script for a convenient build of installer. Set directories or 3rd party stuff here
!AddIncludeDir "include"
!AddPluginDir "plugins"

!include FontReg.nsh
!include FontName.nsh


; ===== Installer Attributes, compiler settings and version information =====
; AddBrandingImage
; BrandingText
; DirText
;InstallDir $PROGRAMFILES\TilesAtHome  probleme mit spaces und svn update
InstallDir C:\TilesAtHome
; InstallDirRegKey    verwenden um update beim späteren install. Install muss Pfad in registry schreiben
; InstType      klappt damit die Umschaltung zwischen 32bit und 64 bit?
LicenseForceSelection checkbox
Name "Tiles@Home for Windows"
!define VERSION 1.0.1.3
OutFile "tahwin-setup_${VERSION}.exe"
RequestExecutionLevel admin
ShowInstDetails show
ShowUninstDetails show
XPStyle on

SetCompressor /SOLID /FINAL lzma

VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "Tiles@Home for Windows"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "Tiles@Home for Windows setup"
VIAddVersionKey /LANG=${LANG_ENGLISH} "Comments" "download and install all components needed to run Tiles@Home on a windows system."
VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "GPL installer, contact osm@stephans-server.de"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" "${VERSION}"
VIProductVersion "${VERSION}"


!define ppmInstall "!insertmacro ppmInstall"
!macro ppmInstall Target Ppd
  Push ${Target}
  Push ${Ppd}
  Call ppmInstall
!macroend

!define fileDownload "!insertmacro fileDownload"
!macro fileDownload Target Url FileName Md5
  Push ${Target}
  Push ${Url}
  Push ${FileName}
  Push ${Md5}
  Call fileDownload
!macroend

; use script http://nsis.sourceforge.net/Java_Runtime_Environment_Dynamic_Installer for java runtime detection and installation
!define JRE_VERSION "1.6"
; to get download link, browse http://java.com/en/download/manual.jsp 
; http://javadl.sun.com/webapps/download/AutoDL?BundleId=44457   JRE-6u23 
; http://javadl.sun.com/webapps/download/AutoDL?BundleId=49024   JRE-6u26
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=49024"
!include "JREDyna.nsh"

VAR HTTPPROXY
VAR HTTPSPROXY
VAR IPADDRESS1
VAR PORT1
VAR IPADDRESS2
VAR PORT2

VAR TAHDL

; store the silent mode requested by user (/S command line for silent)
VAR SILENTMODE


; ===== Pages =====
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Header\orange.bmp" ; optional
!define MUI_ABORTWARNING
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install-full.ico"

; Pages in the order to be displayed by the installer
!insertmacro MUI_PAGE_LICENSE "license.txt"
!insertmacro MUI_PAGE_COMPONENTS
!define MUI_DIRECTORYPAGE_TEXT_DESTINATION "Installing in a different directory is not yet supported. Plese don't change it!"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro CUSTOM_PAGE_JREINFO
!insertmacro MUI_PAGE_INSTFILES
;!insertmacro MUI_PAGE_FINISH

; Pages in the order to be displayed by the uninstaller
!insertmacro MUI_UNPAGE_CONFIRM
; TODO ask to uninstall fonts !insertmacro MUI_UNPAGE_COMPONENTS
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"


; ===== Sections =====
SectionGroup /e "Main Program"

Section "Core Components" SecCoreComponents

  SectionIn RO

  ; add the bytes we download and install
  AddSize 140000

  ; Set output path to the installation directory.
  SetOutPath $INSTDIR

  ; Put file there
  File tah.ico
  File tah.pl

  SetOutPath $INSTDIR\pngnq
  File /r /x .svn binary-source\pngnq\*.*

  SetOutPath $INSTDIR\svn
  File /r /x .svn binary-source\svn\*.*

  SetOutPath $INSTDIR\xmlstarlet
  File /r /x .svn binary-source\xmlstarlet\*.*

  SetOutPath $INSTDIR\optipng
  File /r /x .svn binary-source\optipng\*.*

  SetOutPath $INSTDIR\pngcrush
  File /r /x .svn binary-source\pngcrush\*.*

  SetOutPath $INSTDIR\zip
  File /r /x .svn binary-source\zip\*.*

  SetOutPath $INSTDIR

  StrCpy $TAHDL "$EXEDIR\tahwin-setup"
  CreateDirectory $TAHDL

  Call ConnectInternet

  ;Font installer need local repository of fonts... so I can't download before
  ;NSISdl::download http://surfnet.dl.sourceforge.net/sourceforge/dejavu/dejavu-fonts-ttf-2.25.zip $INSTDIR\binary-source\fonts
  ;goto xmlstarlet_done
                                                                                                                                                                                                                                                                                
; !!BATIK  ${FileDownload} "$TAHDL" "http://inkscape.modevia.com/win32"                         "inkscape-0.46.win32.7z"                        "ff2d2c7950a7747c77b19ac7cf2a7c6a"
; !!BATIK   RMDir /r "$INSTDIR\inkscape"
; !!BATIK   nsExec::ExecToLog '"$INSTDIR\zip\7za" "x" "-o$INSTDIR" "$TAHDL\$0"'
; !!BATIK   Pop $0
; !!BATIK   DetailPrint '"7za" "x" "$TAHDL\$0" returned $0'
; !!BATIK   RMDir /r "$INSTDIR\packaging"
; !!BATIK   RMDir /r "$INSTDIR\src"


  ; Activestate does not allow redistribution of their software. So download is needed 
  ${FileDownload} "$TAHDL" "http://downloads.activestate.com/ActivePerl/releases/5.14.1.1401"   "ActivePerl-5.14.1.1401-MSWin32-x86-294969.zip" "b787066279f5f5aa1288faae3403e6b0"
    RMDir /r "$INSTDIR\perl"
    ZipDLL::extractall $TAHDL\$0 $INSTDIR\tmp
    Rename $INSTDIR\tmp\ActivePerl-5.14.1.1401-MSWin32-x86-294969\perl $INSTDIR\perl
    RMDir /r $INSTDIR\tmp
    ${StrRep} $0 "$INSTDIR\perl" "\" "\\"
    nsExec::ExecToLog '"$INSTDIR\perl\bin\wperl.exe" "$INSTDIR\perl\bin\reloc_perl" "$0"'
    Pop $0
    DetailPrint "reloc_perl returned $0"
    ${If} $0 != 0
        Abort
    ${EndIf}

  ${fileDownload} "$TAHDL" "http://www.bribes.org/perl/ppm"                                             "IPC-Run.ppd"                "222E0B3EF0EAD5B51CFFE11432D95648"
  ${fileDownload} "$TAHDL" "http://www.bribes.org/perl/ppm"                                             "IPC-Run-0.89-PPM514.tar.gz" "4bb2f08bd90abdfb77c40c016d37b5ab"
  
  ${ppmInstall}   "$TAHDL" "$TAHDL\IPC-Run.ppd"
  ${ppmInstall}   "$TAHDL" "Win32-GUI"
  ${ppmInstall}   "$TAHDL" "AppConfig"
  ${ppmInstall}   "$TAHDL" "Math-Vec"
  ${ppmInstall}   "$TAHDL" "libxml-perl"
  ${ppmInstall}   "$TAHDL" "XML-XPath"
  ${ppmInstall}   "$TAHDL" "Set-Object"
  ${ppmInstall}   "$TAHDL" "XML-Writer"
  ${ppmInstall}   "$TAHDL" "Error"
  ${ppmInstall}   "$TAHDL" "Module-Pluggable"

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ; Download Tiles@home using svn
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  Call GetProxySettings
  DetailPrint "Proxy-host $IPADDRESS1"
  DetailPrint "Proxy-port $PORT1"
  ${If} $IPADDRESS1 != ""
    CreateDirectory $APPDATA\Subversion
    FileOpen $9 $APPDATA\Subversion\servers w ;Opens a Empty File an fills it
    FileWrite $9 "[global]$\r$\n"
    FileWrite $9 "http-proxy-host = $IPADDRESS1$\r$\n"
    FileWrite $9 "http-proxy-port = $PORT1$\r$\n"
    FileClose $9 ;Closes the filled file
  ${EndIf}
  SetOutPath $INSTDIR
  nsExec::ExecToLog '"$INSTDIR\svn\bin\svn" "co" "http://svn.openstreetmap.org/applications/rendering/tilesAtHome-dev/tags/Vizag" tilesAtHome'
  Pop $0
  DetailPrint "svn checkout returned $0"
  RMDir /r $APPDATA\Subversion
  ${If} $0 != 0
        Abort
  ${EndIf}

;  DetailPrint "Adjusting settings in config file"
;  Push Rasterizer=Inkscape #text to be replaced
;  Push Rasterizer=Batik #replace with
;  Push all #replace all occurrences
;  Push all #replace all occurrences
;  Push $INSTDIR\tilesAtHome\tilesAtHome.conf.windows #file to replace in
;  Call AdvReplaceInFile

  CreateDirectory $INSTDIR\tmp
  SetOutPath $INSTDIR

  ; Start Menu creation
  CreateDirectory $SMPROGRAMS\TilesAtHome
  CreateShortCut "$SMPROGRAMS\TilesAtHome\TilesAtHome GUI.lnk" "$INSTDIR\perl\bin\perl.exe" '"$INSTDIR\tah.pl"' "$INSTDIR\tah.ico" 0

;Store installation folder
;  WriteRegStr HKCU "Software\Modern UI Test" "" $INSTDIR

  ; http://nsis.sourceforge.net/Add_uninstall_information_to_Add/Remove_Programs
  ; uninstaller creation and registration
  WriteUninstaller "uninstall.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome" \
                 "DisplayName" "$(^Name)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome" \
                 "DisplayIcon" "$\"$INSTDIR\tah.ico$\""
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome" \
                 "DisplayVersion" "${VERSION}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome" \
                 "Publisher" "T@H for OSM"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome" \
                 "Contact" "osm@stephans-server.de"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome" \
                 "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome" \
                 "QuietUninstallString" "$\"$INSTDIR\uninstall.exe$\" /S"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome" \
                 "InstallLocation" "$\"$INSTDIR\$\""
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome" \
                 "NoModify" "1"
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome" \
                 "NoRepair" "1"
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome" \
                 "EstimatedSize" "165000"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome" \
                 "Comments" "Tiles@Home for Windows: collaborative rendering for the OSM map"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome" \
                 "URLInfoAbout" "http://wiki.openstreetmap.org/wiki/Windows%40home"
;  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome" \
;                 "Readme" "http://wiki.openstreetmap.org/wiki/Windows%40home"
                 
                 
                 
SectionEnd ; end the section

; The stuff to install
Section "Fonts" SecFonts
  SectionIn RO

  StrCpy $FONT_DIR $FONTS

  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSans-Bold.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSans-BoldOblique.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSans-ExtraLight.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSans-Oblique.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSans.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSansCondensed-Bold.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSansCondensed-BoldOblique.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSansCondensed-Oblique.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSansCondensed.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSansMono-Bold.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSansMono-BoldOblique.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSansMono-Oblique.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSansMono.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSerif-Bold.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSerif-BoldItalic.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSerif-Italic.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSerif.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSerifCondensed-Bold.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSerifCondensed-BoldItalic.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSerifCondensed-Italic.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\DejaVuSerifCondensed.ttf'
  !insertmacro InstallTTFFont 'binary-source\fonts\unifont.ttf'

  SendMessage ${HWND_BROADCAST} ${WM_FONTCHANGE} 0 0 /TIMEOUT=5000

SectionEnd

Section "Batik Renderer" SecBatik
   SectionIn RO

   AddSize 21000

   ; force java install to be always silent. restore previous state afterwards
   ; TODO: java installer not silent. lib setting wrong argument. Disabled for now. 
   ;SetSilent silent
   call DownloadAndInstallJREIfNecessary
   StrCmp $SILENTMODE "normal" 0 +2
   SetSilent normal
   

   SetOutPath $INSTDIR\batik
   File /r /x .svn binary-source\batik\*.*

SectionEnd

Section "Downloaded Cache" SecDownload
   SectionIn RO
   AddSize 50000
SectionEnd

Section "-PostInstall" SecPostInstall

; $::HKEY_CURRENT_USER->Open("SOFTWARE\\openstreetmap.org\\tah", $tahreg)
; $tahreg->QueryValueEx("http_proxy_enable", $type, $http_proxy_enabled);
; $tahreg->QueryValueEx("http_proxy_exceptions", $type, $http_proxy_exceptions);
; $tahreg->QueryValueEx("http_proxy_host", $type, $http_proxy_host);
; $tahreg->QueryValueEx("http_proxy_port", $type, $http_proxy_port);
; $tahreg->QueryValueEx("http_proxy_username", $type, $http_proxy_username);
; $tahreg->QueryValueEx("http_proxy_password", $type, $http_proxy_password);
; $tahreg->QueryValueEx("authentication_username", $type, $authentication_username);
; $tahreg->QueryValueEx("authentication_password", $type, $authentication_password);
; $tahreg->QueryValueEx("console_visible", $type, $console_visible);

  WriteRegDWORD HKCU "Software\openstreetmap.org\tah" "batikRasterizer" 0x1


SectionEnd

SectionGroupEnd

Section "Uninstall"

  ; Remove registry keys
  ;DeleteRegKey HKLM SOFTWARE\NSIS_Example2

  ; Remove files and uninstaller
  Delete $INSTDIR\tah.ico
  Delete $INSTDIR\tah.pl
  Delete $INSTDIR\uninstall.exe

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\TilesAtHome\*.*"

  ; Remove directories used
  RMDir "$SMPROGRAMS\TilesAtHome"
  RMDir /r "$INSTDIR\TilesAtHome"
  RMDir /r "$INSTDIR\perl"
  RMDir /r "$INSTDIR\zip"
  RMDir /r "$INSTDIR\pngcrush"
  RMDir /r "$INSTDIR\pngnq"
  RMDir /r "$INSTDIR\optipng"
  RMDir /r "$INSTDIR\xmlstarlet"
  RMDir /r "$INSTDIR\batik"
; !!BATIK  RMDir /r "$INSTDIR\inkscape"
  RMDir /r "$INSTDIR\tmp"
  RMDIR /r "$INSTDIR\svn"
  RMDir "$INSTDIR"

  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\TilesAtHome"

; TODO remove tah keys (maybe ask user if deep clean is desired (remind update might uninstall first

; TODO ask user if also remove fonts

SectionEnd

Section "un.Fonts"
 ; TODO uninstall fonts. nullsoft wiki talks about problems with RemoveTTF
SectionEnd

 ;Language strings
LangString DESC_SecFonts ${LANG_ENGLISH} "DejaVu fonts needed for renderer"
LangString DESC_SecDownload ${LANG_ENGLISH} "Cached download of 3rd party components"
LangString DESC_SecCoreComponents ${LANG_ENGLISH} "Tiles@Home core components"
LangString DESC_SecBatik ${LANG_ENGLISH} "Batik rasterizer, requires Java"
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SecFonts} $(DESC_SecFonts)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecDownload} $(DESC_SecDownload)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecCoreComponents} $(DESC_SecCoreComponents)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecBatik} $(DESC_SecBatik)
!insertmacro MUI_FUNCTION_DESCRIPTION_END


Function .onInit

   IfSilent 0 +3
      StrCpy $SILENTMODE "silent"
      goto +2
      StrCpy $SILENTMODE "normal"

FunctionEnd

Function ConnectInternet

  	Push $R0
    ClearErrors
    Dialer::AttemptConnect
    IfErrors noie3

    Pop $R0
    StrCmp $R0 "online" connected
      	MessageBox MB_OK|MB_ICONSTOP "Cannot connect to the internet."
      	Quit

    noie3:
    	; IE3 not installed
    	MessageBox MB_OK|MB_ICONINFORMATION "Please connect to the internet now."

    connected:
	Pop $R0
FunctionEnd

; TODO still needed? check download plugin. Consider reading from registry previous installation
Function GetProxySettings
	ReadRegDWORD $R0 HKCU "Software\Microsoft\Windows\CurrentVersion\Internet Settings" ProxyEnable ;Check registry to see if proxy is enabled
	${If} $R0 = 1
		ReadRegStr $R0 HKCU "Software\Microsoft\Windows\CurrentVersion\Internet Settings" ProxyServer ;Read the proxy string from the reg.
		${WordFind2X} "$R0" "http=" ";" "E+1" $3 ;Get http proxy data. if error, set to 1
		${If} $3 = 1
			StrCpy $HTTPPROXY $R0
			${WordFind} "$HTTPPROXY" ":" "+1" $IPADDRESS1 	
			${WordFind} "$HTTPPROXY" ":" "-1" $PORT1
		${EndIf}
		${If} $3 != 1 ;If there is no error, do the following
			StrCpy $HTTPPROXY $3
			${WordFind} "$HTTPPROXY" ":" "+1" $IPADDRESS1 	
			${WordFind} "$HTTPPROXY" ":" "-1" $PORT1
		${EndIf}
		${WordFind2X} "$R0" "https=" ";" "E+1" $3 ;Get https proxy data. if error, set to 1
		${If} $3 != 1 ;If there is no error, do the follwing:
			StrCpy $HTTPSPROXY $3
			${WordFind} "$HTTPSPROXY" ":" "+1" $IPADDRESS2   ;Split it up
			${WordFind} "$HTTPSPROXY" ":" "-1" $PORT2
		${EndIf}
;	${ElseIf} $R0 = 0
;		MessageBox MB_OK "No Proxy Data Found. Assuming Direct Connection to Internet"
	${EndIf}
FunctionEnd

Function fileDownload
  	ClearErrors
  	Pop $4
  	Pop $3
  	Pop $2
  	Pop $1
  	Push $3
	md5dll::GetMD5File "$1\$3"
	Pop $0
	${If} $0 == $4
		DetailPrint "MD5 $3 Ok"
	${Else}
		DetailPrint "Wrong MD5 $0... Downloading..."
  		NSISdl::download $2/$3 $1\$3
  		Pop $0
		${If} $0 == "success"
			DetailPrint "Download $3 finished!!"
			md5dll::GetMD5File "$1\$3"
                        Pop $0
                        ${If} $0 != $4
				DetailPrint "Downloaded MD5 differs. Using file with new MD5: $0"
                        ${EndIf}
		${Else}
  			SetDetailsView show
  			DetailPrint "Download $3 failed: $0"
  			Abort
  		${EndIf}
	${EndIf}
	Pop $0
FunctionEnd	

Function ppmInstall
	ClearErrors
	Pop $2
	Pop $1
	SetOutPath $1
    DetailPrint 'Installing perl module $2 ...'
    nsExec::ExecToLog '"$INSTDIR\perl\bin\wperl.exe" "$INSTDIR\perl\bin\ppm" "install" "$2"'
    Pop $0
    DetailPrint '... "ppm install $2" returned $0'
    ${If} $0 != 0
    	Abort
    ${EndIf}
FunctionEnd

; http://nsis.sourceforge.net/More_advanced_replace_text_in_file
Function AdvReplaceInFile
Exch $0 ;file to replace in
Exch
Exch $1 ;number to replace after
Exch
Exch 2
Exch $2 ;replace and onwards
Exch 2
Exch 3
Exch $3 ;replace with
Exch 3
Exch 4
Exch $4 ;to replace
Exch 4
Push $5 ;minus count
Push $6 ;universal
Push $7 ;end string
Push $8 ;left string
Push $9 ;right string
Push $R0 ;file1
Push $R1 ;file2
Push $R2 ;read
Push $R3 ;universal
Push $R4 ;count (onwards)
Push $R5 ;count (after)
Push $R6 ;temp file name
 
  GetTempFileName $R6
  FileOpen $R1 $0 r ;file to search in
  FileOpen $R0 $R6 w ;temp file
   StrLen $R3 $4
   StrCpy $R4 -1
   StrCpy $R5 -1
 
loop_read:
 ClearErrors
 FileRead $R1 $R2 ;read line
 IfErrors exit
 
   StrCpy $5 0
   StrCpy $7 $R2
 
loop_filter:
   IntOp $5 $5 - 1
   StrCpy $6 $7 $R3 $5 ;search
   StrCmp $6 "" file_write2
   StrCmp $6 $4 0 loop_filter
 
StrCpy $8 $7 $5 ;left part
IntOp $6 $5 + $R3
IntCmp $6 0 is0 not0
is0:
StrCpy $9 ""
Goto done
not0:
StrCpy $9 $7 "" $6 ;right part
done:
StrCpy $7 $8$3$9 ;re-join
 
IntOp $R4 $R4 + 1
StrCmp $2 all file_write1
StrCmp $R4 $2 0 file_write2
IntOp $R4 $R4 - 1
 
IntOp $R5 $R5 + 1
StrCmp $1 all file_write1
StrCmp $R5 $1 0 file_write1
IntOp $R5 $R5 - 1
Goto file_write2
 
file_write1:
 FileWrite $R0 $7 ;write modified line
Goto loop_read
 
file_write2:
 FileWrite $R0 $R2 ;write unmodified line
Goto loop_read
 
exit:
  FileClose $R0
  FileClose $R1
 
   SetDetailsPrint none
  Delete $0
  Rename $R6 $0
  Delete $R6
   SetDetailsPrint both
 
Pop $R6
Pop $R5
Pop $R4
Pop $R3
Pop $R2
Pop $R1
Pop $R0
Pop $9
Pop $8
Pop $7
Pop $6
Pop $5
Pop $0
Pop $1
Pop $2
Pop $3
Pop $4
FunctionEnd

