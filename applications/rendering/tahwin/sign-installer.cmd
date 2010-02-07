@echo off
REM this is usign SIGNTOOL from platform SDK to sign installer. A certificate suitable for code signing has to be installed
SET SIGN_APP="C:\Program Files\Microsoft SDKs\Windows\v7.0\Bin\signtool.exe"
SET SIGN_DESC="Tiles@Home for Windows installation"
SET SIGN_DESCURL="http://wiki.openstreetmap.org/wiki/Windows%%40home"
SET SIGN_TSTAMP="http://timestamp.verisign.com/scripts/timstamp.dll"
%SIGN_APP% sign /a /d %SIGN_DESC% /du %SIGN_DESCURL% /t %SIGN_TSTAMP% /v tahwin-setup_*.exe
