
; ============================================================================
; vars available to the outside
; ============================================================================

Var /GLOBAL destinationDrive

Var /GLOBAL destinationDir

Var /GLOBAL destinationFreeMB

; ============================================================================
; internally used stuff
; ============================================================================

Var DriveCurrent
Var DriveSmall
Var DriveEmpty
Var DriveGmapsupp
Var DriveGarmin

Var yDestinationPagePos
Var yDestinationPageStep
Var destinationPageDialog
Var destinationPageLabel
Var destinationPageDirRadio
Var destinationPageDirName
Var destinationPageDirButton

; ============================================================================
; build the "free MB" display string
; ============================================================================
!macro FreeMBDisplay DriveLetter FreeString
  ${DriveSpace} "${DriveLetter}" "/D=F /S=M" ${FreeString}

  IfFileExists "${DriveLetter}\Garmin\gmapsupp.img" 0 nogmapsupp3
    ${GetSize} "${DriveLetter}Garmin" "/M=gmapsupp.img /S=0M /G=0" $4 $5 $6
    ;MessageBox MB_OK "$9Garmin Size: $4!"
    IntOp ${FreeString} ${FreeString} + $4
  nogmapsupp3:
!macroend


; ============================================================================
; build the "removable drive" display string
; ============================================================================
!macro FddDisplay DriveLetter DisplayString
  ; get free space of this drive
  ${DriveSpace} "${DriveLetter}" "/D=T /S=M" $1
  ${DriveSpace} "${DriveLetter}" "/D=F /S=M" $2
  
  ;  drive label
  push ${DriveLetter}
  call GetDriveLabel
  pop $3
  ;MessageBox MB_OK "Label: $3"
  
  ; different label in autorun.inf?
  ReadIniStr $0 "${DriveLetter}Autorun.inf" "Autorun" "Label"
  ;MessageBox MB_OK "${DriveLetter}Autorun.inf: $0"
  ${If} $0 != ''
    StrCpy $3 $0
  ${EndIf}

  ; if this drive completly empty?
  StrCpy $R0 ""
  ${If} $1 == $2
    StrCpy $R0 ", Leer"
  ${EndIf}
  
  ; gmapsupp.img installed?
  StrCpy $R1 ""
  StrCpy $7 $2
  IfFileExists "${DriveLetter}\Garmin\gmapsupp.img" 0 nogmapsupp2
    ${GetSize} "${DriveLetter}Garmin" "/M=gmapsupp.img /S=0M /G=0" $4 $5 $6
    ;MessageBox MB_OK "$9Garmin Size: $4!"
    IntOp $7 $7 + $4
    StrCpy $R1 ", Inst. Karte: $4 MB"
  nogmapsupp2:

  ; Garmin dir existing?
  StrCpy $R2 ""
  IfFileExists "${DriveLetter}\Garmin" 0 nogarmindir2
    ${GetSize} "${DriveLetter}Garmin" "/S=0M" $4 $5 $6
    ${If} $4 == ""
      StrCpy $R2 ", Garmin Verz.: 0 MB"
    ${Else}
      StrCpy $R2 ", Garmin Verz.: $4 MB"
    ${EndIf}
  nogarmindir2:
  
  ${If} $1 == ""
    StrCpy ${DisplayString} "$9  Leer"
  ${Else}
    StrCpy ${DisplayString} "$9  $3 $1 MB, Frei: $2 MB$R0$R2$R1"
  ${EndIf}
!macroend


; ============================================================================
; get label of drive (http://nsis.sourceforge.net/DVD_functions)
; ============================================================================
Function GetDriveLabel
  Exch $R0
  Push $R1
  Push $R2
 
  ;leave function if no parameter was supplied
  StrCmp $R0 "" break
 
  ;get the label of the drive (return to $R1)
  System::Call /NOUNLOAD 'Kernel32::GetVolumeInformation(t r10,t.r11,i ${NSIS_MAX_STRLEN},*i,*i,*i,t.r12,i ${NSIS_MAX_STRLEN})i.r10'
  StrCmp $R0 0 0 +3
    StrCpy $R0 ""
    goto +2
  StrCpy $R0 $R1
  break:
 
  Pop $R2
  Pop $R1
  Exch $R0
FunctionEnd

Var hwnd

!macro DetectDevice GarminDevice
  ;MessageBox MB_OK "Trying to detect device at: ${GarminDevice"
  ${xml::LoadFile} "${GarminDevice}\Garmin\GarminDevice.xml" $0
  ${if} $0 == -1
    Goto GarminDeviceFileEnd    
  ${EndIf}
  ${xml::DeclarationVersion} $0 $1
  ;MessageBox MB_OK "${GarminDeviceFile}: Version: $0 Success: $1"
  ${xml::GotoPath} "/Device/Model/Description" $0
  ${xml::GetText} $1 $0
  ${xml::GotoPath} "/Device/Model/SoftwareVersion" $0
  ${xml::GetText} $2 $0
  MessageBox MB_OK "Laufwerk: ${GarminDevice} Beschreibung: $1 SoftwareVersion: $2"
GarminDeviceFileEnd:
  ${xml::Unload}
!macroend

; no current SD card is larger than 64GB, so probably a USB connected hard drive
!define MAX_DRIVE_SIZE 64000

; ============================================================================
; callback for GetDrives (fdd = removable drive)
; ============================================================================
Function FddDriveCallback
  ; get free space of this drive
  ${DriveSpace} "$9" "/D=T /S=M" $1
  ${DriveSpace} "$9" "/D=F /S=M" $2

  ; add radio button to page
  !insertmacro FddDisplay $9 $0
  ${NSD_CreateRadioButton} 0 $yDestinationPagePos 100% 10u "$0"
  Pop $hwnd
  nsDialogs::SetUserData $hwnd $9
  ;MessageBox MB_OK "Drive: $9"
  ${NSD_OnClick} $hwnd DestinationDriveChanged
  IntOp $yDestinationPagePos $yDestinationPagePos + $yDestinationPageStep
  
  ; if this drive completly empty?
  ${If} $1 == $2
    ${If} $1 != ""
      ${If} $1 < ${MAX_DRIVE_SIZE}
        StrCpy $DriveEmpty $hwnd
      ${EndIf}
    ${EndIf}
  ${EndIf}
  
  ; gmapsupp.img installed?
  StrCpy $7 $2
  IfFileExists "$9\Garmin\gmapsupp.img" 0 nogmapsupp
    ${GetSize} "$9Garmin" "/M=gmapsupp.img /S=0M /G=0" $4 $5 $6
    ;MessageBox MB_OK "$9Garmin Size: $4!"
    IntOp $7 $7 + $4
    ${If} $1 < ${MAX_DRIVE_SIZE}
      StrCpy $DriveGmapsupp $hwnd
    ${EndIf}
  nogmapsupp:

  ; Garmin dir existing?
  IfFileExists "$9\Garmin" 0 nogarmindir
    !insertmacro DetectDevice $9
    ${If} $1 < ${MAX_DRIVE_SIZE}
      StrCpy $DriveGarmin $hwnd
    ${EndIf}  
  nogarmindir:
  
  ${If} $1 < ${MAX_DRIVE_SIZE}
    StrCpy $DriveCurrent $hwnd
  ${EndIf}  
  
  StrCpy $DriveSmall $hwnd

  ; continue drive enumeration  
  Push $0
FunctionEnd

; ============================================================================
; user clicked one of the destination drive radio buttons
; ============================================================================
Function DestinationDriveChanged
  Pop $hwnd
  nsDialogs::GetUserData $hwnd
  Pop $0
  StrCpy $destinationDrive $0
  StrCpy $destinationDir "$destinationDriveGarmin"
  
  ; disable dir requester
  EnableWindow $destinationPageDirName 0
  EnableWindow $destinationPageDirButton 0

  ; check if dir exists
  ${DirState} "$destinationDrive" $R0
  GetDlgItem $1 $HWNDPARENT 1
  ${If} $R0 == -1
    EnableWindow $1 0
  ${Else}
    EnableWindow $1 1
  ${EndIf}
  
  ;MessageBox MB_OK "Drive Radio changed: Drive: $destinationDrive Dir: $destinationDir"
FunctionEnd

; ============================================================================
; user clicked the destination dir radio button
; ============================================================================
Function DestinationDirChanged
  Pop $hwnd
  ; enable dir requester
  EnableWindow $destinationPageDirName 1
  EnableWindow $destinationPageDirButton 1
  
  ${NSD_GetText} $destinationPageDirName $destinationDir
  
  ; derive drive from dir name
  ${GetRoot} $destinationDir $destinationDrive
  StrCpy $destinationDrive "$destinationDrive\"

  ; check if dir exists
  ${DirState} "$destinationDir" $R0
  GetDlgItem $1 $HWNDPARENT 1
  ${If} $R0 == -1
    EnableWindow $1 0
  ${Else}
    EnableWindow $1 1
  ${EndIf}    
  
  ;MessageBox MB_OK "Dir Radio changed: Drive: $destinationDrive Dir: $destinationDir"
FunctionEnd

; ============================================================================
; user clicked the dir browse button
; ============================================================================
Function OnDirBrowseButton
  Pop $R0

  ${NSD_GetText} $destinationPageDirName $R4

  nsDialogs::SelectFolderDialog "Title" $R4
  Pop $R3

  ${If} $R3 != error
    SendMessage $destinationPageDirName ${WM_SETTEXT} 0 STR:$R3
  ${EndIf}
FunctionEnd

!macro SelectDrive SelectedDrive
  SendMessage ${SelectedDrive} ${BM_SETCHECK} ${BST_CHECKED} 0
  nsDialogs::GetUserData ${SelectedDrive}
  Pop $0
  StrCpy $destinationDrive $0
  StrCpy $destinationDir "$destinationDriveGarmin"
  ;MessageBox MB_OK "SelectedDrive: ${SelectedDrive}"
!macroend

; ============================================================================
; display the page
; ============================================================================
Function destinationPageDisplay

    !insertmacro MUI_HEADER_TEXT "Ziel auswählen" "Bitte warten, die Liste wird aufgebaut ..."

    StrCpy $yDestinationPagePos 0
    StrCpy $yDestinationPageStep 18
    
    ; create the new dialog
    nsDialogs::Create 1018
    Pop $destinationPageDialog
    ${If} $destinationPageDialog == error
        Abort
    ${EndIf}

    ; static header label
    ${NSD_CreateLabel} 0 $yDestinationPagePos 100 10u "Ziellaufwerk:"
    Pop $destinationPageLabel
    IntOp $yDestinationPagePos $yDestinationPagePos + 20

    ; list of removable drives
    StrCpy $DriveCurrent ""
    StrCpy $DriveSmall ""
    StrCpy $DriveEmpty ""
    StrCpy $DriveGmapsupp ""
    StrCpy $DriveGarmin ""
    ${GetDrives} "FDD" "FddDriveCallback"
    
    ; dir requester
    ${NSD_CreateRadioButton} 0 160 100% 10u "Zielverzeichnis:"
    Pop $destinationPageDirRadio
    ${NSD_OnClick} $destinationPageDirRadio DestinationDirChanged
    ${NSD_CreateDirRequest} 15 180 380 12u "C:\"
    Pop $destinationPageDirName
    ${NSD_SetText} $destinationPageDirName $EXEDIR
    ${NSD_OnChange} $destinationPageDirName DestinationDirChanged
    ${NSD_CreateBrowseButton} 400 179 25 22 "..."
    Pop $destinationPageDirButton
    ${NSD_OnClick} $destinationPageDirButton OnDirBrowseButton

    EnableWindow $destinationPageDirName 0
    EnableWindow $destinationPageDirButton 0
    
    ; try to guess which drive to install to ...
    ${If} $DriveEmpty != ""
      ; completely empty drive
      !insertmacro SelectDrive $DriveEmpty
    ${ElseIf} $DriveGmapsupp != ""
      ; drive with \garmin\gmapsupp.img installed
      !insertmacro SelectDrive $DriveGmapsupp
    ${ElseIf} $DriveGarmin != ""
      ; drive with \garmin dir
      !insertmacro SelectDrive $DriveGarmin
    ${ElseIf} $DriveSmall != ""
      ; any removable drive
      !insertmacro SelectDrive $DriveSmall
    ${ElseIf} $DriveCurrent != ""
      ; any removable drive
      !insertmacro SelectDrive $DriveCurrent
    ${Else}
      ; no removable drive
      ${NSD_CreateLabel} 10 $yDestinationPagePos 100% 10u "Keine Wechsellaufwerke gefunden"
      Pop $0
      EnableWindow $0 0
    
      SendMessage $destinationPageDirRadio ${BM_SETCHECK} ${BST_CHECKED} 0
      Push $0
      Call DestinationDirChanged
    ${EndIf}

    !insertmacro MUI_HEADER_TEXT "Ziel auswählen" "Wohin soll installiert werden?"
    
    ; show page (stays in there)
    nsDialogs::Show    
    
    !insertmacro FreeMBDisplay $destinationDrive $destinationFreeMB

    ;MessageBox MB_OK "Drive: $destinationDrive Free: $destinationFreeMB MB Dir: $destinationDir"
FunctionEnd

