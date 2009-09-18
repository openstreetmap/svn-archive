#NoTrayIcon
#include <GUIConstants.au3>

Global $s_DatFile = @ScriptDir & '\' & 'garmin-mapsource-installer.ini'
Global $s_MapSourceDir = RegRead('HKLM\Software\Garmin\MapSource\Products', 'InstallDir')
Global $s_OpenStreetMapDir = $s_MapSourceDir & "OpenStreetMap"

_LoadUpdateData()

Opt("GuiResizeMode", $GUI_DOCKALL)
$gui_Main = GuiCreate($t_application_name, 350, 90)

;; A <hr>
GuiCtrlCreateLabel('', 0, 0, 350, 2, $SS_SUNKEN)

$gr_Instal_Details=GuiCtrlCreateGroup($t_installation_path, 5, 5, 340, 50)
$hFile = GUICtrlCreateEdit($s_OpenStreetMapDir, 10,  25, 250, 20, $ES_READONLY, $WS_EX_STATICEDGE)
GUICtrlSetCursor(-1, 2) 
GUICtrlSetState(-1, $GUI_ACCEPTFILES)
$bt_Path_Selector = GUICtrlCreateButton($t_path_edit, 265,  25, 75, 20)

GUIStartGroup()

$bt_Install = GuiCtrlCreateButton($t_install, 5, 60, 340, 25)

;
; Start application
;

; Show main window
GuiSetState(@SW_SHOW, $gui_Main)

While 1
	$a_GMsg = GUIGetMsg(1)

	If $a_GMsg[1] = $gui_Main Then
		Select
            Case $a_GMsg[0] = $bt_Path_Selector
                $sTmpFile = FileSelectFolder("Select a custom install location:", "", 1 + 2 + 4, $s_MapSourceDir)
                If @error Then ContinueLoop
                GUICtrlSetData($hFile, $sTmpFile)


			; Install
			Case $a_GMsg[0] = $bt_Install
				_Install()

            Case $a_GMsg[0] = $GUI_EVENT_CLOSE
                Exit
        EndSelect
    EndIf
Wend

Func _LoadUpdateData()
    ;; ==================
    ;; Get global section
    ;; ==================

	Global $t_global_language = IniRead($s_DatFile, 'Global', 'language', '%language%')
	Global $t_global_version = IniRead($s_DatFile, 'Global', 'version', '%version%')

    ;; =======================
    ;; Get i18n from .ini file
    ;; =======================

    ;; Find out what language we're using to get those translation strings
    Local $lang_ini_section = 'Language_' & $t_global_language

    ;; Get i18n strings
    Global $t_application_name = IniRead($s_DatFile, $lang_ini_section, 'application_name', '%application_name%')
    Global $t_application_description = IniRead($s_DatFile, $lang_ini_section, 'application_description', '%application_description%')
    Global $t_help_menu = IniRead($s_DatFile, $lang_ini_section, 'help_menu', '%help_menu%')
    Global $t_help_menu_website = IniRead($s_DatFile, $lang_ini_section, 'help_menu_website', '%help_menu_website%')
    Global $t_help_menu_website_url = IniRead($s_DatFile, $lang_ini_section, 'help_menu_website_url', '%help_menu_website_url%')
    Global $t_help_menu_about = IniRead($s_DatFile, $lang_ini_section, 'help_menu_about', '%help_menu_about%')
    Global $t_help_menu_about_title = IniRead($s_DatFile, $lang_ini_section, 'help_menu_about_title', '%help_menu_about_title%')
    Global $t_help_menu_about_text = IniRead($s_DatFile, $lang_ini_section, 'help_menu_about_text', '%help_menu_about_text%')
    Global $t_help_menu_about_contact = IniRead($s_DatFile, $lang_ini_section, 'help_menu_about_contact', '%help_menu_about_contact%')
    Global $t_help_menu_about_website = IniRead($s_DatFile, $lang_ini_section, 'help_menu_about_website', '%help_menu_about_website%')

    Global $t_install = IniRead($s_DatFile, $lang_ini_section, 'install', '%install%')
    Global $t_installation_path = IniRead($s_DatFile, $lang_ini_section, 'installation_path', '%installation_path%')
    Global $t_path_edit = IniRead($s_DatFile, $lang_ini_section, 'path_edit', '%path_edit%')
EndFunc

Func _Install()

    Local $family = IniRead($s_DatFile, 'Map', 'Family', '')
    Local $id = IniRead($s_DatFile, 'Map', 'ID', '')
    Local $bmap = IniRead($s_DatFile, 'Map', 'BMAP', '')
    Local $tdb = IniRead($s_DatFile, 'Map', 'TDB', '')
    Local $other = IniRead($s_DatFile, 'Map', 'other', '')

	If Not RegWrite('HKLM\Software\Garmin\MapSource\Families\' & $family, "ID", "REG_BINARY", $id) Then
        MsgBox(1, "Arghl", "Failed to set family key")
    EndIf
    RegWrite('HKLM\Software\Garmin\MapSource\Families\' & $family & '\1', "BMAP", "REG_SZ", $s_OpenStreetMapDir & '\' & $bmap)
    RegWrite('HKLM\Software\Garmin\MapSource\Families\' & $family & '\1', "TDB", "REG_SZ", $s_OpenStreetMapDir & '\' & $tdb)
    RegWrite('HKLM\Software\Garmin\MapSource\Families\' & $family & '\1', "LOC", "REG_SZ", $s_OpenStreetMapDir & '\')

    If Not DirCreate($s_OpenStreetMapDir) Then
        MsgBox(1, "Arghl", "Failed to create install dir")
    EndIf

    FileCopy(@ScriptDir & '\' & $bmap, $s_OpenStreetMapDir, 1)
    FileCopy(@ScriptDir & '\' & $tdb, $s_OpenStreetMapDir, 1)
    FileCopy(@ScriptDir & '\' & $other, $s_OpenStreetMapDir, 1)

    MsgBox(1, 'Yay', 'Your map was successfully installed, you can now exit the application')
EndFunc
