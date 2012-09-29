;*******************************************************************************
; This script is compiled with the Nullsoft Scriptable Install System (NSIS)
; to produce a windows installer.
;*******************************************************************************

;*******************************************************************************
; ATTRIBUTES
;*******************************************************************************

Name                 "Falcon 4.0 Allied Force Pit Trainer"
OutFile              "..\..\..\PitTrainer-Windows-installer.exe"
InstallDir           "C:\Program Files\PitTrainer"
BrandingText         " "

CRCCheck             force
SetCompress          force
SetCompressor        lzma

ShowInstDetails      show
ShowUnInstDetails    show

LicenseData          readme.rtf
LicenseText          Overview Continue
LicenseBkColor       /gray

InstallColors        /windows
XPStyle              on

Page                 license
Page                 directory
Page                 instfiles

UninstPage           uninstConfirm
UninstPage           instfiles

;*******************************************************************************
; INSTALLER
;*******************************************************************************

Section ""
   SetOutPath        $INSTDIR\bin
   File              "..\..\bin\PitTrainer.jar"
   File              "..\com\gpl\pittrainer\images\superpitsicon1.ico"

   SetOutPath        $INSTDIR
   File /r           "..\..\thirdpty"
   
   SetOutPath        $INSTDIR\doc
   File              "..\..\doc\HISTORY.txt"
   File              "..\..\doc\README.txt"
   File              "..\..\doc\LICENSE.rtf"

   SetOutPath        $INSTDIR\scripts
   File              "..\..\scripts\run.bat"

   SetOutPath        $INSTDIR\scripts
   CreateShortcut    "$DESKTOP\Pit Trainer.lnk" "$INSTDIR\scripts\run.bat" "" "$INSTDIR\bin\superpitsicon1.ico"
      
   WriteUninstaller  Uninst.exe
   WriteRegStr       HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\PitTrainer" "DisplayName" "Pit Trainer"
   WriteRegStr       HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\PitTrainer" "UninstallString" "$INSTDIR\Uninst.exe"
SectionEnd

;*******************************************************************************
; UNINSTALLER
;*******************************************************************************

Section "Uninstall"
   RMDir             /r $INSTDIR
   Delete            "$DESKTOP\Pit Trainer.lnk"
   DeleteRegKey      HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\PitTrainer"
SectionEnd
