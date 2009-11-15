
; ============================================================================
; vars available to the outside
; ============================================================================

; "download" or local "file"?
Var /GLOBAL sourcePageSelect

; URL of map download
Var /GLOBAL sourcePageMapUrl

Var /GLOBAL sourcePageMapFileSizeMB

; local file
Var /GLOBAL sourcePageLocalFile


; ============================================================================
; internally used stuff
; ============================================================================
; the currently selected map "Map 1"
Var /GLOBAL sourcePageCurrentMap
; Name of map "Deutschland - All in One ..."
Var /GLOBAL sourcePageMapName

; ============================================================================
; convsersion of user visible strings
; ============================================================================
!macro sourcePageMapNameDisplay mapName mapDisplayName
  ReadINIStr $5 "$PLUGINSDIR\Maps.ini" "${mapName}" "Boundary:de"
  ReadINIStr $6 "$PLUGINSDIR\Maps.ini" "${mapName}" "Name:de"
  ReadINIStr $7 "$PLUGINSDIR\Maps.ini" "${mapName}" "FileSizeMB"
  StrCpy ${mapDisplayName} "$5 - $6 (~ $7 MB)"
  ;MessageBox MB_OK ${mapDisplayName}
!macroend

!macro sourcePageRoutingDisplay mapName RoutingDisplay
  ReadINIStr $5 "$PLUGINSDIR\Maps.ini" "${mapName}" "Routing"
  ;MessageBox MB_OK $5
  ${If} $5 == "Yes"
    StrCpy ${RoutingDisplay} "Ja"
  ${ElseIf} $5 == "No"
    StrCpy ${RoutingDisplay} "Nein"
  ${Else}
    StrCpy ${RoutingDisplay} "Unbekannt"
  ${EndIf}
  ;MessageBox MB_OK ${RoutingDisplay}
!macroend

; ============================================================================
; init page values (only once, so "back" is working as expected
; ============================================================================
Function sourcePageInit
    File "images\maps\AllInOne.bmp"
    File "images\maps\Computerteddy.bmp"
    File "images\maps\Radkarte.bmp"
    
    StrCpy $sourcePageSelect "download"
    StrCpy $sourcePageCurrentMap "Map 1"
    !insertmacro sourcePageMapNameDisplay $sourcePageCurrentMap $sourcePageMapName
    
    StrCpy $sourcePageLocalFile $EXEDIR
FunctionEnd

; ============================================================================
; page vars
; ============================================================================

; placement of controls
Var xSPPosLabels
Var xSPPosValues
Var ySPPos
Var ySPStep

; HWND of controls
Var sourcePageDialog

Var MapImage
Var MapImageHandle

Var sourcePageDownloadRadio
Var sourcePageMapList
Var sourcePageRoutingLabel
Var sourcePageRoutingValue
Var sourcePageUpdateLabel
Var sourcePageUpdateValue
Var SourceHelpButton
Var sourcePageCommentLabel
Var sourcePageCommentValue
Var sourcePageFileSizeLabel
Var sourcePageFileSizeValue

Var sourcePageLocalRadio
Var sourcePageFileName
Var sourcePageFileButton


; ============================================================================
; fill list of maps
; ============================================================================
!macro sourcePageFillListOfMaps HWND

  ; prepare map DropDown ListItems
  IntOp $8 0 + 0
MapNext2:
  IntOp $8 $8 + 1
  ReadINIStr $0 "$PLUGINSDIR\Maps.ini" "Map $8" "Name:de"
  StrCmp $0 "" MapEnd2
  !insertmacro sourcePageMapNameDisplay "Map $8" $0
    SendMessage ${HWND} ${CB_ADDSTRING} 0 "STR:$0"
  Goto MapNext2
MapEnd2:
!macroend

; ============================================================================
; map list drop down selection changed
; ============================================================================
Function MapListChanged
Pop $0

  ; get the selected map name into $3
  SendMessage $0 ${CB_GETCURSEL} 0 0 $1
  System::Call 'user32::SendMessage(i $0, i ${CB_GETLBTEXT}, i $1, t .s)'
  Pop $3

  ; which map is now selected?
  IntOp $8 0 + 0
MapNext5:
  IntOp $8 $8 + 1
  ReadINIStr $0 "$PLUGINSDIR\Maps.ini" "Map $8" "Name:de"
  StrCmp $0 "" MapEnd5
  !insertmacro sourcePageMapNameDisplay "Map $8" $0
  ${If} $3 == $0
  ${If} $sourcePageMapName != $0
    ;MessageBox MB_OK "Map changed to $8"
    StrCpy $sourcePageCurrentMap "Map $8"
    !insertmacro sourcePageMapNameDisplay $sourcePageCurrentMap $sourcePageMapName
  ${EndIf}
  ${EndIf}
  Goto MapNext5
MapEnd5:

  ; update info value fields
  !insertmacro sourcePageRoutingDisplay $sourcePageCurrentMap $0
  SendMessage $sourcePageRoutingValue ${WM_SETTEXT} 1 'STR:$0'
  
  ReadINIStr $0 "$PLUGINSDIR\Maps.ini" "$sourcePageCurrentMap" "Updated:de"
  SendMessage $sourcePageUpdateValue ${WM_SETTEXT} 1 'STR:$0'
  
  ReadINIStr $0 "$PLUGINSDIR\Maps.ini" "$sourcePageCurrentMap" "Comment:de"
  SendMessage $sourcePageCommentValue ${WM_SETTEXT} 1 'STR:$0'
  
  ReadINIStr $0 "$PLUGINSDIR\Maps.ini" "$sourcePageCurrentMap" "DownloadSizeMB"
  ReadINIStr $1 "$PLUGINSDIR\Maps.ini" "$sourcePageCurrentMap" "FileSizeMB"
  SendMessage $sourcePageFileSizeValue ${WM_SETTEXT} 1 'STR:~$1 MB (Download: ~$0 MB)'
  
  ReadINIStr $sourcePageMapUrl "$PLUGINSDIR\Maps.ini" $sourcePageCurrentMap "DownloadUrl"
  ReadINIStr $sourcePageMapFileSizeMB "$PLUGINSDIR\Maps.ini" $sourcePageCurrentMap "FileSizeMB"
  
  ${NSD_FreeImage} $MapImageHandle
  ReadINIStr $5 "$PLUGINSDIR\maps.ini" "$sourcePageCurrentMap" "Picture"
  ${NSD_SetImage} $MapImage "$PLUGINSDIR\$5" $MapImageHandle
FunctionEnd

; ============================================================================
; change enable/disable label states
; ============================================================================
!macro changeLabelStates enable
    ;MessageBox MB_OK "radio changed: ${enable}"
    EnableWindow $sourcePageMapList ${enable}
    EnableWindow $sourcePageRoutingLabel ${enable}
    EnableWindow $sourcePageRoutingValue ${enable}
    EnableWindow $sourcePageUpdateLabel ${enable}
    EnableWindow $sourcePageUpdateValue ${enable}
    EnableWindow $SourceHelpButton ${enable}
    EnableWindow $sourcePageCommentLabel ${enable}
    EnableWindow $sourcePageCommentValue ${enable}
    EnableWindow $sourcePageFileSizeLabel ${enable}
    EnableWindow $sourcePageFileSizeValue ${enable}

    ${If} ${enable} == "1"
        EnableWindow $sourcePageFileName 0
        EnableWindow $sourcePageFileButton 0
    ${Else}
        EnableWindow $sourcePageFileName 1
        EnableWindow $sourcePageFileButton 1
    ${EndIf}
!macroend

; ============================================================================
; download radio button clicked
; ============================================================================
Function sourcePageDownloadRadioClicked
    Pop $0

    ;MessageBox MB_OK "Changed to download"
    StrCpy $sourcePageSelect "download"
    !insertmacro changeLabelStates 1
    
    ; update map picture
    ${NSD_FreeImage} $MapImageHandle
    ReadINIStr $5 "$PLUGINSDIR\maps.ini" "$sourcePageCurrentMap" "Picture"
    ${NSD_SetImage} $MapImage "$PLUGINSDIR\$5" $MapImageHandle
FunctionEnd

; ============================================================================
; local file radio button clicked
; ============================================================================
Function sourcePageLocalRadioClicked
    Pop $0

    ;MessageBox MB_OK "Changed to local file"
    StrCpy $sourcePageSelect "file"
    !insertmacro changeLabelStates 0

    ; update map picture
    ${NSD_FreeImage} $MapImageHandle
    ${NSD_SetImage} $MapImage "$PLUGINSDIR\EmptyImage.bmp" $MapImageHandle
FunctionEnd

;; ============================================================================
; local file entry changed
; ============================================================================
Function sourcePageLocalFileChanged
    Pop $0

    ${NSD_GetText} $sourcePageFileName $sourcePageLocalFile
    ;MessageBox MB_OK "Changed local file name to: $sourcePageLocalFile"

    ; TODO: disable Next button, if file doesn't exist
FunctionEnd

; ============================================================================
; user clicked the file browse button
; ============================================================================
Function OnSourcePageFileBrowseButton
  Pop $R0

  ${NSD_GetText} $sourcePageFileName $R4

  ; TODO: add a suitable file filter here?
  nsDialogs::SelectFileDialog open $R4
  Pop $R3

  ${If} $R3 != error
  ${If} $R3 != ""
    ;MessageBox MB_OK "Return: $R3"
    SendMessage $sourcePageFileName ${WM_SETTEXT} 0 STR:$R3
  ${EndIf}
  ${EndIf}
FunctionEnd

; ============================================================================
; user clicked on Wiki button
; ============================================================================
Function onClickSourceWikiLink
  Pop $0 ; don't forget to pop HWND of the stack

  ; get the link corresponding to the currently selected device
  ReadINIStr $0 "$PLUGINSDIR\Maps.ini" "$sourcePageCurrentMap" "HelpUrl"

  ;MessageBox MB_OK "Map: $sourcePageCurrentMap Link: $0"

  ; open the page
  ExecShell "open" "$0"

FunctionEnd

; ============================================================================
; display the source page
; ============================================================================
Function sourcePageDisplay
    ; prepare switch values
    StrCpy $xSPPosLabels "10"
    StrCpy $xSPPosValues "90"
    StrCpy $ySPPos "45"
    StrCpy $ySPStep "15"
    
    !insertmacro MUI_HEADER_TEXT "Quelle auswählen" "Von welcher Quelle soll installiert werden?"

    ; create the new dialog
	nsDialogs::Create 1018
	Pop $sourcePageDialog
	${If} $sourcePageDialog == error
		Abort
	${EndIf}

    
    ; image
    ${NSD_CreateBitmap} 300 5 150u 150u ""
	Pop $MapImage
	${NSD_SetImage} $MapImage "$PLUGINSDIR\Computerteddy.bmp" $MapImageHandle

    
    ${NSD_CreateRadioButton} 0 0 100% 10u "Download:"
    Pop $sourcePageDownloadRadio
    ${NSD_OnClick} $sourcePageDownloadRadio sourcePageDownloadRadioClicked
    
    ${NSD_CreateDropList} 5 20 270 10u ""
    Pop $sourcePageMapList
    !insertmacro sourcePageFillListOfMaps $sourcePageMapList
    ${NSD_OnChange} $sourcePageMapList MapListChanged

	${NSD_CreateButton} $xSPPosLabels 110 80u 15u "Kartendetails Online"
    Pop $SourceHelpButton
    ${NSD_OnClick} $SourceHelpButton onClickSourceWikiLink
    ToolTips::Classic $SourceHelpButton "Wiki Seite über diese Karte im Browser anzeigen" 0x0066FBF2 0x00000000 "Comic Sans Ms" 9    
    
	${NSD_CreateLabel} $xSPPosLabels $ySPPos 100% 10u "Kommentar:"
	Pop $sourcePageCommentLabel
	${NSD_CreateLabel} $xSPPosValues $ySPPos 100% 10u ""
	Pop $sourcePageCommentValue
    IntOp $ySPPos $ySPPos + $ySPStep
    
	${NSD_CreateLabel} $xSPPosLabels $ySPPos 100% 10u "Routingfähig:"
	Pop $sourcePageRoutingLabel
	${NSD_CreateLabel} $xSPPosValues $ySPPos 100% 10u ""
	Pop $sourcePageRoutingValue
    IntOp $ySPPos $ySPPos + $ySPStep
    
	${NSD_CreateLabel} $xSPPosLabels $ySPPos 100% 10u "Update:"
	Pop $sourcePageUpdateLabel
	${NSD_CreateLabel} $xSPPosValues $ySPPos 100% 10u ""
	Pop $sourcePageUpdateValue
    IntOp $ySPPos $ySPPos + $ySPStep
    
	${NSD_CreateLabel} $xSPPosLabels $ySPPos 100% 10u "Dateigröße:"
	Pop $sourcePageFileSizeLabel
	${NSD_CreateLabel} $xSPPosValues $ySPPos 100% 10u ""
	Pop $sourcePageFileSizeValue
    IntOp $ySPPos $ySPPos + $ySPStep

    
    ${NSD_CreateRadioButton} 0 175 100% 10u "Lokale Datei:"
    Pop $sourcePageLocalRadio
    ${NSD_OnClick} $sourcePageLocalRadio sourcePageLocalRadioClicked

    ${NSD_CreateDirRequest} 15 195 380 12u ""
    Pop $sourcePageFileName
    ${NSD_SetText} $sourcePageFileName $sourcePageLocalFile
    ${NSD_OnChange} $sourcePageFileName sourcePageLocalFileChanged
    ${NSD_CreateBrowseButton} 400 194 25 22 "..."
    Pop $sourcePageFileButton
    ${NSD_OnClick} $sourcePageFileButton OnSourcePageFileBrowseButton

    
    ; init control states
    ${If} $sourcePageSelect == "download"
      SendMessage $sourcePageDownloadRadio ${BM_SETCHECK} ${BST_CHECKED} 0
      Push $0
      Call sourcePageDownloadRadioClicked
    ${Else}
      SendMessage $sourcePageLocalRadio ${BM_SETCHECK} ${BST_CHECKED} 0
      Push $0
      Call sourcePageLocalRadioClicked
    ${EndIf}
    SendMessage $sourcePageMapList ${CB_SELECTSTRING} 0 "STR:$sourcePageMapName"
    Push $0
    Call MapListChanged
    
    ; show page (stays in there)
	nsDialogs::Show
    
	${NSD_FreeImage} $MapImageHandle
    
    ${If} $sourcePageSelect == "download"
      ;MessageBox MB_OK "Download: URL: $sourcePageMapUrl FileSize: $sourcePageMapFileSizeMB MB"
    ${Else}
      ;MessageBox MB_OK "Local file: $sourcePageLocalFile"
    ${EndIf}
FunctionEnd

