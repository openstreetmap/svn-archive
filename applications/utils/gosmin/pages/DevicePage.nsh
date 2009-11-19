
; ============================================================================
; vars available to the outside
; ============================================================================

; the selected series "eTrex"
Var /GLOBAL devicePageSelectedSeries

; the currently selected device "eTrex Legend HCx"
Var /GLOBAL devicePageSelectedDevice

; ============================================================================
; internally used stuff
; ============================================================================

; ============================================================================
; fill "series" drop list
; ============================================================================
!macro devicePageFillListOfSeries HWND
  ${LogOut} "devicePageFillListOfSeries"
  StrCpy $4 "1"
  SendMessage ${HWND} ${CB_RESETCONTENT} 0 0
  
enumSeriesDevices:
  ; get next device, finish if we don't have any more
  ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $4" "Series"
  ;${LogOut} "Device[$4]: $5"
  ${If} $5 == ""
    Goto enumSeriesDevicesFinished
  ${EndIf}
  
  ; add the series to the ComboBox if its not already in there (avoid duplicates)
  SendMessage ${HWND} ${CB_FINDSTRINGEXACT} -1 "STR:$5" $0
  ${If} $0 == -1
    ${LogOut} "Series[$4]: $5"
    SendMessage ${HWND} ${CB_ADDSTRING} 0 "STR:$5"
  ${EndIf}  
  IntOp $4 $4 + 1
  Goto enumSeriesDevices
enumSeriesDevicesFinished:
!macroend

; ============================================================================
; fill "types" drop list
; ============================================================================
!macro devicePageFillListOfTypes Series HWND FirstItem
  ${LogOut} "devicePageFillListOfTypes"
  StrCpy ${FirstItem} ""
  StrCpy $4 "1"
  SendMessage ${HWND} ${CB_RESETCONTENT} 0 0

enumDevices:
  ; get next device, finish if we don't have anymore
  ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $4" "Series"
  ${If} $5 == ""
    Goto enumDevicesFinished
  ${EndIf}

  ; add the type to the ComboBox, if its part of the requested series
  ${If} $5 == ${Series}
    ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $4" "Name"
    ${LogOut} "Device[$4]: $5"
    ${If} ${FirstItem} == ""
      ${LogOut} "First: $5"
      StrCpy ${FirstItem} $5
    ${EndIf}
    SendMessage ${HWND} ${CB_ADDSTRING} 0 "STR:$5"
  ${EndIf}  
  
  ;MessageBox MB_OK "$4: $5 (${ListItemsOfTypes})"
  IntOp $4 $4 + 1
  Goto enumDevices
enumDevicesFinished:
!macroend


; ============================================================================
; get device index in ini file from device name 
; ============================================================================
!macro devicePageGetDeviceIndexFromName Name Index
  ; reset
  StrCpy $4 "1"
  ${LogOut} "devicePageGetDeviceIndexFromName"

enumDevices:
  ; get next device, finish if we don't have anymore
  ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $4" "Name"
  ${If} $5 == ""
    Goto enumDevicesFinished
  ${EndIf}

  ; is this the one we are searching for?
  ${If} $5 == ${Name}
    StrCpy ${Index} $4
    ${LogOut} "Device found: ${Index}"    
    Goto enumDevicesFinished
  ${EndIf}  
  
  ;MessageBox MB_OK "$4: $5 (${ListItemsOfTypes})"
  IntOp $4 $4 + 1
  Goto enumDevices
enumDevicesFinished:
!macroend


Var Dialog
Var SeriesLabel
Var SeriesList
Var TypesLabel
Var TypesList
Var Image
Var ImageHandle
Var ImageLabel
Var ImageSD
Var ImageSDHandle
Var MapDisplayLabel
Var MapDisplayStateLabel
Var ConnectionLabel
Var ConnectionStateLabel
Var MemoryLabel
Var MemoryStateLabel
Var MemoryCardLabel
Var MemoryCardStateLabel
Var MemoryCardSDLabel
Var MemoryCardSDStateLabel
Var MemoryCardSDHCLabel
Var MemoryCardSDHCStateLabel
Var HelpButton
Var FirmwareInstalledLabel
Var FirmwareInstalledStateLabel
Var FirmwareAvailableLabel
Var FirmwareAvailableStateLabel

Var xPosHeader
Var xPosItem
Var xPosValues
Var yPos
Var yStep
Var ySpacer

; ============================================================================
; init vars and page values
; ============================================================================
Function devicePageInit
  ${LogOut} "devicePageInit"
  File "images\devices\eTrex_h.bmp"
  File "images\devices\eTrex_Legend.bmp"
  File "images\devices\eTrex_Legend_HCx.bmp"
  File "images\devices\eTrex_Summit.bmp"
  File "images\devices\eTrex_Summit_HC.bmp"
  File "images\devices\eTrex_Venture_HC.bmp"
  File "images\devices\etrex_Vista.bmp"
  File "images\devices\Forerunner_205.bmp"
  File "images\devices\Geko_101.bmp"
  File "images\devices\Geko_201.bmp"
  File "images\devices\Geko_301.bmp"
  File "images\devices\GPS_12.bmp"
  File "images\devices\GPS_60.bmp"
  File "images\devices\GPS_V_IIIplus.bmp"
  File "images\devices\GPSMap_60CSx.bmp"
  File "images\devices\GPSMap_76CS.bmp"
  File "images\devices\nuevi_200w.bmp"
  File "images\devices\Oregon_400t.bmp"
  File "images\devices\zumo_550.bmp"
  File "images\SecureDigital.bmp"
  File "images\SecureDigitalMicro.bmp"
  File "images\SecureDigitalNone.bmp"
  File "images\SecureDigitalUnknown.bmp"

  ;StrCpy $devicePageSelectedSeries "Oregon"
  ;StrCpy $devicePageSelectedDevice "Oregon 300"
  StrCpy $devicePageSelectedSeries "Bitte auswaehlen!"
  StrCpy $devicePageSelectedDevice "Bitte auswaehlen!"
FunctionEnd


; ============================================================================
; display the page
; ============================================================================
Function devicePageDisplay

    ${LogOut} "devicePageDisplay"

    ; init
    !insertmacro MUI_HEADER_TEXT "Ger�t ausw�hlen" "Auf welches Garmin Ger�t soll installiert werden?"
    
    ; create the new dialog
	nsDialogs::Create 1018
	Pop $Dialog
	${If} $Dialog == error
		Abort
	${EndIf}

    ; series list
	${NSD_CreateLabel} 0 0 100% 10u "Ger�teserie:"
	Pop $SeriesLabel
    ${NSD_CreateDropList} 5 20 130 10u ""
    Pop $SeriesList
    !insertmacro devicePageFillListOfSeries $SeriesList
    ${NSD_OnChange} $SeriesList SeriesListChanged

    ; type list
	${NSD_CreateLabel} 0 45 100% 12u "Typ:"
	Pop $TypesLabel
    ${NSD_CreateDropList} 5 65 130 12u ""
    Pop $TypesList
    !insertmacro devicePageFillListOfTypes $devicePageSelectedSeries $TypesList $0
    ${NSD_OnChange} $TypesList TypesListChanged

    ; image
    ${NSD_CreateBitmap} 10 90 120 120 ""
	Pop $Image
	${NSD_SetImage} $Image "$PLUGINSDIR\zumo 550.bmp" $ImageHandle
	${NSD_CreateLabel} 28 215 100 10u ""
	Pop $ImageLabel

    StrCpy $xPosHeader "185"
    StrCpy $xPosItem "200"
    StrCpy $xPosValues "330"
    StrCpy $yPos "0"
    StrCpy $yStep "14"
    StrCpy $ySpacer "3"
        
    ;does this device display a map?    
	${NSD_CreateLabel} $xPosHeader $yPos 100% 10u "Kartendarstellung:"
	Pop $0
    IntOp $yPos $yPos + $yStep
    
	${NSD_CreateLabel} $xPosItem $yPos 100% 10u "Display:"
	Pop $MapDisplayLabel
	${NSD_CreateLabel} $xPosValues $yPos 100% 10u ""
	Pop $MapDisplayStateLabel
    IntOp $yPos $yPos + $yStep
    
    
    IntOp $yPos $yPos + $ySpacer
	${NSD_CreateLabel} $xPosHeader $yPos 100% 10u "Computer-Anschlu�:"
	Pop $0
    IntOp $yPos $yPos + $yStep

	${NSD_CreateLabel} $xPosItem $yPos 100% 10u "Schnittstelle:"
	Pop $ConnectionLabel
	${NSD_CreateLabel} $xPosValues $yPos 100% 10u ""
	Pop $ConnectionStateLabel
    IntOp $yPos $yPos + $yStep

    
    IntOp $yPos $yPos + $ySpacer
	${NSD_CreateLabel} $xPosHeader $yPos 100% 10u "Speicher:"
	Pop $0
    IntOp $yPos $yPos + $yStep

    ; SD card image
    ${NSD_CreateBitmap} 410 80 37 50 ""
	Pop $ImageSD
	${NSD_SetImage} $ImageSD "" $ImageSDHandle
    
	${NSD_CreateLabel} $xPosItem $yPos 100% 10u "Interner Speicher:"
	Pop $MemoryLabel
	${NSD_CreateLabel} $xPosValues $yPos 100% 10u ""
	Pop $MemoryStateLabel
    IntOp $yPos $yPos + $yStep
    
	${NSD_CreateLabel} $xPosItem $yPos 100% 10u "Speicherkarte:"
	Pop $MemoryCardLabel
	${NSD_CreateLabel} $xPosValues $yPos 100% 10u ""
	Pop $MemoryCardStateLabel
    IntOp $yPos $yPos + $yStep
    
	${NSD_CreateLabel} $xPosItem $yPos 100% 10u "   SD max. Gr��e:"
	Pop $MemoryCardSDLabel
	${NSD_CreateLabel} $xPosValues $yPos 100% 10u ""
	Pop $MemoryCardSDStateLabel
    IntOp $yPos $yPos + $yStep
    
	${NSD_CreateLabel} $xPosItem $yPos 100% 10u "   SDHC max. Gr��e:"
	Pop $MemoryCardSDHCLabel
	${NSD_CreateLabel} $xPosValues $yPos 100% 10u ""
	Pop $MemoryCardSDHCStateLabel
    IntOp $yPos $yPos + $yStep

    
    IntOp $yPos $yPos + $ySpacer
	${NSD_CreateLabel} $xPosHeader $yPos 100% 10u "Firmware:"
	Pop $0
    IntOp $yPos $yPos + $yStep

	${NSD_CreateLabel} $xPosItem $yPos 100% 10u "installiert:"
	Pop $FirmwareInstalledLabel
	${NSD_CreateLabel} $xPosValues $yPos 100% 10u ""
	Pop $FirmwareInstalledStateLabel
    IntOp $yPos $yPos + $yStep

	${NSD_CreateLabel} $xPosItem $yPos 100% 10u "verf�gbar:"
	Pop $FirmwareAvailableLabel
	${NSD_CreateLabel} $xPosValues $yPos 100% 10u ""
	Pop $FirmwareAvailableStateLabel
    IntOp $yPos $yPos + $yStep

	${NSD_CreateButton} 240 205 80u 15u "Ger�tedetails Online"
    Pop $HelpButton
    ${NSD_OnClick} $HelpButton onClickWikiLink
    ToolTips::Classic $HelpButton "Wiki Seite �ber dieses Ger�t im Browser anzeigen" 0x0066FBF2 0x00000000 "Comic Sans Ms" 9
    
    ; init controls
    StrCpy $R6 $devicePageSelectedDevice
    SendMessage $SeriesList ${CB_SELECTSTRING} 0 "STR:$devicePageSelectedSeries"
    Call SeriesListChanged
    SendMessage $TypesList ${CB_SELECTSTRING} 0 "STR:$R6"
    Call TypesListChanged

    ${LogOut} "devicePageDisplay: Show"
    
    ; show page (stays in there)
	nsDialogs::Show
    ; page is closed now
    
    ${LogOut} "devicePageDisplay: Finish"
    
	${NSD_FreeImage} $ImageHandle
	${NSD_FreeImage} $ImageSDHandle
    
FunctionEnd

; ============================================================================
; user clicked on Wiki button
; ============================================================================
Function onClickWikiLink
  Pop $0 ; don't forget to pop HWND of the stack

  ; get the link corresponding to the currently selected device
  !insertmacro devicePageGetDeviceIndexFromName $devicePageSelectedDevice $0
  ReadINIStr $0 "$PLUGINSDIR\devices.ini" "Device $0" "Help"

  ${LogOut} "onClickWikiLink($devicePageSelectedDevice): $0"    

  ; open the page
  ExecShell "open" "$0"

FunctionEnd

; ============================================================================
; the series selection changed
; ============================================================================
Function SeriesListChanged
	SendMessage $SeriesList ${CB_GETCURSEL} 0 0 $1
	System::Call 'user32::SendMessage(i $SeriesList, i ${CB_GETLBTEXT}, i $1, t .s)'
	Pop $1

    ${LogOut} "SeriesListChanged: $1"
    StrCpy $devicePageSelectedSeries $1
    
    ;MessageBox MB_OK "New Series: $1"
    !insertmacro devicePageFillListOfTypes $1 $TypesList $0
    ;MessageBox MB_OK "New Series: $1 new Type: $0"
    SendMessage $TypesList ${CB_SELECTSTRING} 0 "STR:$0"
    Call TypesListChanged
FunctionEnd

; ============================================================================
; the type selection changed
; ============================================================================
Function TypesListChanged
    ${LogOut} "TypesListChanged"
    ; get the name of the currently selected device
	SendMessage $TypesList ${CB_GETCURSEL} 0 0 $1
	System::Call 'user32::SendMessage(i $TypesList, i ${CB_GETLBTEXT}, i $1, t .s)'
	Pop $devicePageSelectedDevice
    ${LogOut} "TypesListChanged: New Type: $devicePageSelectedDevice"
    !insertmacro devicePageGetDeviceIndexFromName $devicePageSelectedDevice $0
    ${LogOut} "TypesListChanged: New Index: $0"
    ${If} $0 != -1
        SendMessage $TypesList ${CB_GETCURSEL} 0 0 $2
        IntOp $2 $2 + 1
        ${NSD_FreeImage} $ImageHandle
        ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $0" "Picture"
        ${NSD_SetImage} $Image "$PLUGINSDIR\$5" $ImageHandle
        
        ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $0" "PictureSimilar"
        ${If} $5 == "Yes"
          SendMessage $ImageLabel ${WM_SETTEXT} 0 "STR:(Abbildung �hnlich?)"
        ${Else}
          SendMessage $ImageLabel ${WM_SETTEXT} 0 "STR:"
        ${EndIf}        
        
        ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $0" "MapDisplay"
        ${If} $5 == "No"
          SendMessage $MapDisplayStateLabel ${WM_SETTEXT} 0 "STR:Nein"
        ${ElseIf} $5 == "Mono"
          SendMessage $MapDisplayStateLabel ${WM_SETTEXT} 0 "STR:Graustufen"
        ${ElseIf} $5 == "Color"
          SendMessage $MapDisplayStateLabel ${WM_SETTEXT} 0 "STR:Farbig"
        ${Else}
          SendMessage $MapDisplayStateLabel ${WM_SETTEXT} 0 "STR:?"
        ${EndIf}
        
        ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $0" "Connection"
        ${If} $5 == "No"
          SendMessage $ConnectionStateLabel ${WM_SETTEXT} 0 "STR:Nein"
        ${ElseIf} $5 == "USB"
          SendMessage $ConnectionStateLabel ${WM_SETTEXT} 0 "STR:USB (1.1 oder 2.0?)"
        ${ElseIf} $5 == "USB 1.1"
          SendMessage $ConnectionStateLabel ${WM_SETTEXT} 0 "STR:USB 1.1 (nur 12MBit/s)"
        ${ElseIf} $5 == "USB 2.0"
          SendMessage $ConnectionStateLabel ${WM_SETTEXT} 0 "STR:USB 2.0 (480MBit/s)"
        ${ElseIf} $5 == "Serial"
          SendMessage $ConnectionStateLabel ${WM_SETTEXT} 0 "STR:Seriell"
        ${Else}
          SendMessage $ConnectionStateLabel ${WM_SETTEXT} 0 "STR:?"
        ${EndIf}
        
        ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $0" "MemoryMB"
        ${If} $5 == "No"
          SendMessage $MemoryStateLabel ${WM_SETTEXT} 0 "STR:Nein"
        ${ElseIf} $5 == ""
          SendMessage $MemoryStateLabel ${WM_SETTEXT} 0 "STR:?"
        ${Else}
          SendMessage $MemoryStateLabel ${WM_SETTEXT} 0 "STR:$5 MB"
        ${EndIf}

        ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $0" "MemoryCard"
        ${If} $5 == "No"
          SendMessage $MemoryCardStateLabel ${WM_SETTEXT} 0 "STR:Nein"
          ${NSD_SetImage} $ImageSD "$PLUGINSDIR\SecureDigitalNone.bmp" $ImageSDHandle
          ToolTips::Classic $ImageSD "Ger�t unterst�tzt keine Speicherkarten" 0x0066FBF2 0x00000000 "Comic Sans Ms" 9
        ${ElseIf} $5 == "SD"
          SendMessage $MemoryCardStateLabel ${WM_SETTEXT} 0 "STR:SD Karte"
          ${NSD_SetImage} $ImageSD "$PLUGINSDIR\SecureDigital.bmp" $ImageSDHandle
          ToolTips::Classic $ImageSD "Ger�t unterst�tzt SD Speicherkarten" 0x0066FBF2 0x00000000 "Comic Sans Ms" 9
        ${ElseIf} $5 == "microSD"
          SendMessage $MemoryCardStateLabel ${WM_SETTEXT} 0 "STR:microSD Karte"
          ${NSD_SetImage} $ImageSD "$PLUGINSDIR\SecureDigitalMicro.bmp" $ImageSDHandle
          ToolTips::Classic $ImageSD "Ger�t unterst�tzt microSD Speicherkarten" 0x0066FBF2 0x00000000 "Comic Sans Ms" 9
        ${ElseIf} $5 == "Yes"
          SendMessage $MemoryCardStateLabel ${WM_SETTEXT} 0 "STR:Ja (Bauart unbekannt)"
          ${NSD_SetImage} $ImageSD "$PLUGINSDIR\SecureDigitalUnknown.bmp" $ImageSDHandle
          ToolTips::Classic $ImageSD "Ger�t unterst�tzt Speicherkarten, Bauart noch nicht bekannt, bitte mithelfen!" 0x0066FBF2 0x00000000 "Comic Sans Ms" 9
        ${Else}
          SendMessage $MemoryCardStateLabel ${WM_SETTEXT} 0 "STR:?"
          ${NSD_SetImage} $ImageSD "$PLUGINSDIR\SecureDigitalUnknown.bmp" $ImageSDHandle
          ToolTips::Classic $ImageSD "Ob Ger�t Speicherkarten unterst�tzt ist unbekannt, bitte mithelfen!" 0x0066FBF2 0x00000000 "Comic Sans Ms" 9
        ${EndIf}
       
        ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $0" "MemorySDMaxGB"
        ${If} $5 == "No"
          SendMessage $MemoryCardSDStateLabel ${WM_SETTEXT} 0 "STR:Nein"
        ${ElseIf} $5 == ""
          SendMessage $MemoryCardSDStateLabel ${WM_SETTEXT} 0 "STR:?"
        ${Else}
          SendMessage $MemoryCardSDStateLabel ${WM_SETTEXT} 0 "STR:$5 GB"
        ${EndIf}
        
        ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $0" "MemorySDHCMaxGB"
        ${If} $5 == "No"
          SendMessage $MemoryCardSDHCStateLabel ${WM_SETTEXT} 0 "STR:Nein"
        ${ElseIf} $5 == ""
          SendMessage $MemoryCardSDHCStateLabel ${WM_SETTEXT} 0 "STR:?"
        ${Else}
          SendMessage $MemoryCardSDHCStateLabel ${WM_SETTEXT} 0 "STR:$5 GB"
        ${EndIf}
        
        ; TODO: get info from xml file
        ;ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $0" "Firmware"
        StrCpy $5 ""
        ${If} $5 == ""
          SendMessage $FirmwareInstalledStateLabel ${WM_SETTEXT} 0 "STR:?"
        ${Else}
          SendMessage $FirmwareInstalledStateLabel ${WM_SETTEXT} 0 "STR:$5"
        ${EndIf}
        
        ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $0" "Firmware"
        ${If} $5 == ""
          SendMessage $FirmwareAvailableStateLabel ${WM_SETTEXT} 0 "STR:?"
        ${Else}
          SendMessage $FirmwareAvailableStateLabel ${WM_SETTEXT} 0 "STR:$5"
        ${EndIf}

        ; wiki button
        ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $0" "Help"
        ${LogOut} "TypesListChanged: WikiButton: $5"
        ${If} $5 == ""
          EnableWindow $HelpButton 0
        ${Else}
          EnableWindow $HelpButton 1
        ${EndIf}

        ; en-/disable Next button        
        GetDlgItem $1 $HWNDPARENT 1
        ReadINIStr $5 "$PLUGINSDIR\devices.ini" "Device $0" "MapDisplay"
        ${LogOut} "TypesListChanged: NextButton: $5"
        ${If} $5 == "No"
          EnableWindow $1 0
        ${Else}
          EnableWindow $1 1
        ${EndIf}
    ${Else}
        ${LogOut} "TypesListChanged: Unknown index of $devicePageSelectedDevice"
        ${NSD_FreeImage} $ImageHandle
        ${NSD_SetImage} $Image "$PLUGINSDIR\EmptyImage.bmp" $ImageHandle
        ${NSD_FreeImage} $ImageSDHandle
        ${NSD_SetImage} $ImageSD "$PLUGINSDIR\EmptyImage.bmp" $ImageSDHandle
    ${EndIf}

FunctionEnd
