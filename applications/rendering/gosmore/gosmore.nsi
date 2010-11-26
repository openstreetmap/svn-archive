SetCompressor /SOLID lzma
Page license
LicenseData README

Page directory
DirText "Directory for the executables and ALL the data.$\n$\r\
$\n$\r\
Note that there are more than 50 map files of 400MB each."

InstallDir "$PROGRAMFILES\Gosmore"

Page instfiles
UninstPage instfiles

Section "Gosmore"
  SetOutPath $INSTDIR
  File "gosmore.exe"
  File "elemstyles.xml"
  File "icons.csv"
  File "libgcc_s_dw2-1.dll"
  File "libstdc++-6.dll"
  File "libxml2-2.dll"
  File "default.pak"
  File "gosmore.opt"
  File "keepleft.wav"
  File "round1.wav"
  File "round3.wav"
  File "round5.wav"
  File "round7.wav"
  File "stop.wav"
  File "turnright.wav"
  File "keepright.wav"
  File "round2.wav"
  File "round4.wav"
  File "round6.wav"
  File "round8.wav"
  File "turnleft.wav"
  File "uturn.wav"
  File "7z.exe"
SectionEnd
Section "Start Menu Shortcuts"
  CreateShortCut "$SMPROGRAMS\Gosmore.lnk" "$INSTDIR\gosmore.exe"
SectionEnd

OutFile "Install Gosmore.exe"
