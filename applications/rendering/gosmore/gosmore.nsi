Page directory
DirText "Welcome to the Gosmore Installer" "Please select the install directory. Note that$\n \
there are over 60 maps, each of being approximately 400MB. So$\n\
it is important that you have sufficient disk space."

Page instfiles
UninstPage instfiles

Section "Gosmore"
  SetOutPath $INSTDIR
  File "gosmore.exe"
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
SectionEnd
Section "Start Menu Shortcuts"
  CreateShortCut "$SMPROGRAMS\Gosmore.lnk" "$INSTDIR\gosmore.exe"
SectionEnd

OutFile "Install Gosmore.exe"
