Falcon 4.0 Allied Force
Pit Trainer
http://pittrainer.sourceforge.net
================================================================================

See the LICENSE.rtf file in the docs directory for details on the license.

USER INFO
--------------------------------------------------------------------------------

OVERVIEW

   - provides an interactive cockpit training environment for Falcon 4.0 Allied
     Force
   - use the cursor keys and numeric pad to move the view around the cockpit or
     just move the mouse to the edge and click just as in the sim; mouse over 
     the various controls to see more info
   - provides textual info on the controls and in some cases images to go along
     with the controls

KNOWN ISSUES

   - Enabling anti-aliasing in your video card settings can prevent Pit Trainer
     from rendering; the result will be a big gray window with nothing in it or
     the impression that you are unable to interact with the application; Make
     sure to turn off anti-aliasing (and possibly other custom video card
     profile settings) if you experience this problem.

SYSTEM REQUIREMENTS

   - any platform with the Java v1.4.2 or greater runtime
   - for Windows users using the graphical installer, you don't need to
     install Java as the graphical installer provides an embedded Java runtime
     as part of the Pit Trainer install; this runtime will not interfere with
     whatever Java you may already have installed; therefore, Windows users
     using the graphical installer don't really need to know or care about
     the Java install
   - for non-Windows platforms, if you don't have the Java runtime installed,
     you can use the below link to get it.  On the download page, look for
     the "J2SE Java Runtime Environment (JRE)" download.
         http://java.sun.com/j2se/1.4.2/download.html
   - if your platform is not listed on the downloads page, you'll need to
     go to your operating system provider's web site to see if they provide a
     Java v1.4.2 runtime.

RUNNING

   - on Windows, download and run the installer and then use the desktop
     shortcut ("Pit Trainer")
   - on other platforms, extract the .zip file and then use the run.sh
     script in the extracted directory (make sure the current directory
     is set to the directory where the run.sh script is located)
   - if errors occur, a log file will be created named PitTrainer_log.txt
     in the scripts directory


DEVELOPER INFO
--------------------------------------------------------------------------------

OVERVIEW

   - the Windows graphical installer only installs the files necessary to run,
     if you want to compile, you should download the full .zip file and then
     use the below instructions

SYSTEM REQUIREMENTS

   - any platform with the Java v1.4.2 or greater developer's kit (JDK)   
   - if you don't have the JDK installed, you can use the below link
     to get it.  On the download page, look for the "J2SE Software Development
     Kit (SDK)" download.
         http://java.sun.com/j2se/1.4.2/download.html
   - if your platform is not listed on the downloads page, you'll need to
     go to your operating system provider's web site to see if they provide a
     Java v1.4.2 developer's kit.

BUILDING FROM SOURCE

   - on Windows, double-click scripts\build.bat
   - on other platforms, cd into the scripts directory and run build.sh

CREATING THE WINDOWS GRAPHICAL INSTALLER

   - once you've built you can create a Windows graphical installer
     from the built code using these instructions provided you're running
     on a Windows platform
   - download and install NSIS (http://nsis.sourceforge.net)
   - right-click on src\installer\installer.nsi and select
     "Compile NSIS Script"
   - this will create PitTrainer-installer.exe in the parent directory
     of the PitTrainer directory

NOTES

   - If you provide "-devmode" as a command line argument when running the
     application, it will start in developer's mode.  In this mode, if you
     left-click drag and release, it will write the code necessary for
     adding a rectangular tip at the indicated location to standard output.
     If you right click multiple places and then hit the SPACEBAR, it will
     write the code necessary for adding a polygon tip to include the specified
     points to standard output. If you middle-click, drag and release, it
     will write the code necessary for a circular tip area to standard
     output.  This was added to facilitate adding the tip areas.
     