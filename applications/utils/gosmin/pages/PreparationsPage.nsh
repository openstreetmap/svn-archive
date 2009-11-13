
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

  ExecShell "open" "http://wiki.openstreetmap.org/index.php?title=DE:OSM_Map_On_Garmin/Mass_Storage_Mode&uselang=de"

FunctionEnd

Var yCPPos
Var yCPSpacer

; ============================================================================
; display the page
; ============================================================================
Function preparationsPageDisplay
    !insertmacro MUI_HEADER_TEXT "Vorbereitungen" "Bitte folgendes beachten."
    
    StrCpy $yCPPos 0
    StrCpy $yCPSpacer 20
    
    ; create the new dialog
	nsDialogs::Create 1018
	Pop $preparationsPageDialog
	${If} $preparationsPageDialog == error
		Abort
	${EndIf}

    ; Gerät anschliessen
	${NSD_CreateGroupBox} 0 $yCPPos 100% 22u "Garmin anschliessen"
    Pop $0
    
    IntOp $1 $yCPPos + 15
	${NSD_CreateLabel} 10 $1 435 10u "Bitte jetzt das Garmin Gerät an den Computer anschliessen, falls nicht bereits geschehen."
	Pop $preparationsPageLabel    

    IntOp $yCPPos $yCPPos + 22
    IntOp $yCPPos $yCPPos + $yCPSpacer
    
    ; Massenspeichermodus
	${NSD_CreateGroupBox} 0 $yCPPos 100% 55u "Massenspeichermodus"
    Pop $0
    
    IntOp $1 $yCPPos + 15
	${NSD_CreateLabel} 10 $1 435 25u "Dieser Installer unterstützt nur Garmin Geräte im Massenspeichermodus (die als Laufwerke im Windows Explorer erscheinen). Evtl. muss dieser Modus im Gerätemenu aktiviert werden. Proprietäre Garmin (USB / serielle) Verbindungen werden nicht unterstützt."
	Pop $preparationsPageLabel    

	IntOp $1 $yCPPos + 55
    ${NSD_CreateButton} 260 $1 180 15u "Was ist der Massenspeichermodus?"
    Pop $preparationsPageMSMLink
    ${NSD_OnClick} $preparationsPageMSMLink onClickMSMLink
    
    IntOp $yCPPos $yCPPos + 55
    IntOp $yCPPos $yCPPos + $yCPSpacer

    ; show page (stays in there)
	nsDialogs::Show
    
FunctionEnd

