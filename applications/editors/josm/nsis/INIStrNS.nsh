;
; Write to INI style file with no section headings
;
; http://nsis.sourceforge.net/Write_to_INI_style_file_with_no_section_headings
;
; some more at: http://nsis.sourceforge.net/Category:INI%2C_CSV_%26_Registry_Functions

Function WriteINIStrNS
 Exch $R0 ; new value
 Exch
 Exch $R1 ; key
 Exch 2
 Exch $R2 ; ini file
 Exch 2
 Push $R3
 Push $R4
 Push $R5
 Push $R6
 Push $R7
 Push $R8
 Push $R9
 
  StrCpy $R9 0
 
  FileOpen $R3 $R2 r
  GetTempFileName $R4
  FileOpen $R5 $R4 w
 
  LoopRead:
   ClearErrors
   FileRead $R3 $R6
   IfErrors End
 
   StrCpy $R7 -1
   LoopGetVal:
    IntOp $R7 $R7 + 1
    StrCpy $R8 $R6 1 $R7
    StrCmp $R8 "" LoopRead
    StrCmp $R8 = 0 LoopGetVal
 
     StrCpy $R8 $R6 $R7
     StrCmp $R8 $R1 0 +4
 
      FileWrite $R5 "$R1=$R0$\r$\n"
      StrCpy $R9 1
      Goto LoopRead
 
    FileWrite $R5 $R6
    Goto LoopRead
 
  End:
   StrCmp $R9 1 +2
   FileWrite $R5 "$R1=$R0$\r$\n"
 
  FileClose $R5
  FileClose $R3
 
  SetDetailsPrint none
  Delete $R2
  Rename $R4 $R2
  SetDetailsPrint both
 
 Pop $R9
 Pop $R8
 
 Pop $R7
 Pop $R6
 Pop $R5
 Pop $R4
 Pop $R3
 Pop $R2
 Pop $R1
 Pop $R0
FunctionEnd
 
!define WriteINIStrNS "!insertmacro WriteINIStrNS"
!macro WriteINIStrNS Var File Key Value
 Push "${File}"
 Push "${Key}"
 Push "${Value}"
  Call WriteINIStrNS
 Pop "${Var}"
!macroend


