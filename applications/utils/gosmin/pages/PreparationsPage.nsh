
; ============================================================================
; vars available to the outside
; ============================================================================

; ============================================================================
; internally used stuff
; ============================================================================

!include "nsDialogs.nsh"

!include "MUI.nsh"


Var preparationsPageDialog
Var preparationsPageLabel
Var preparationsPageMSMLink

; ============================================================================
; user clicked on MSM button
; ============================================================================
Function onClickMSMLink
  Pop $0 ; don't forget to pop HWND of the stack

  ${LogOut} "onClickMSMLink"
  
  ExecShell "open" "http://wiki.openstreetmap.org/index.php?title=DE:OSM_Map_On_Garmin/Mass_Storage_Mode&uselang=de"

FunctionEnd

Var yCPPos
Var yCPSpacer

; ============================================================================
; display the page
; ============================================================================
Function preparationsPageDisplay
    ${LogOut} "preparationsPageDisplay"
    !insertmacro MUI_HEADER_TEXT "Vorbereitungen" "Bitte folgendes beachten."
    
    StrCpy $yCPPos 0
    StrCpy $yCPSpacer 10
    
    ; create the new dialog
	nsDialogs::Create 1018
	Pop $preparationsPageDialog
	${If} $preparationsPageDialog == error
		Abort
	${EndIf}

    ; attach memory card
	${NSD_CreateGroupBox} 0 $yCPPos 100% 65 "Speicherkarte anschliessen"
    Pop $0
    
    IntOp $1 $yCPPos + 15
	${NSD_CreateLabel} 10 $1 435 30u "Die Verwendung einer (micro)SD Speicherkarte wird empfohlen. Falls nicht bereits geschehen, bitte diese an den Computer anschliessen. Notfalls kann die Karte auch in den Garmin gesteckt werden, oder der (meist begrenzte) interne Speicher verwendet werden."
	Pop $preparationsPageLabel    

    IntOp $yCPPos $yCPPos + 65
    IntOp $yCPPos $yCPPos + $yCPSpacer
    
    ; attach device
	${NSD_CreateGroupBox} 0 $yCPPos 100% 38 "Garmin anschliessen"
    Pop $0
    
    IntOp $1 $yCPPos + 15
	${NSD_CreateLabel} 10 $1 435 10u "Bitte das Garmin Gerät an den Computer anschliessen, falls nicht bereits geschehen."
	Pop $preparationsPageLabel    

    IntOp $yCPPos $yCPPos + 38
    IntOp $yCPPos $yCPPos + $yCPSpacer
    
    ; mass storage mode
	${NSD_CreateGroupBox} 0 $yCPPos 100% 88 "Massenspeichermodus aktivieren"
    Pop $0
    
    IntOp $1 $yCPPos + 15
	${NSD_CreateLabel} 10 $1 435 25u "Aktuellere Garmin Geräte bieten den USB Massenspeichermodus (die dann als Laufwerke im Windows Explorer erscheinen). Evtl. muss der Massenspeichermodus im Garmin über das Menu aktiviert werden."
	Pop $preparationsPageLabel    

	IntOp $1 $yCPPos + 55
    ${NSD_CreateButton} 260 $1 180 15u "Was ist der Massenspeichermodus?"
    Pop $preparationsPageMSMLink
    ${NSD_OnClick} $preparationsPageMSMLink onClickMSMLink
    ToolTips::Classic $preparationsPageMSMLink "Wiki Seite über den Massenspeichermodus im Browser anzeigen" 0x0066FBF2 0x00000000 "Comic Sans Ms" 9
    
    IntOp $yCPPos $yCPPos + 88
    IntOp $yCPPos $yCPPos + $yCPSpacer

    ; show page (stays in there)
	nsDialogs::Show
    
FunctionEnd

