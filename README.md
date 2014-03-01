Charged-Miners launcher
=======================

Based on ClassiCube launcher. Notable changes made in this fork:

Namespaces
----------
All <code>net.classicube.\*</code> namespaces have been renamed to <code>com.chargedminers.*</code>



Added classes
-------------
#####.launcher.ChargedMinersSettings
> New in CCCML: Encapsulates Charged-Miners settings from "settings.ini".
  Uses *.launcher.SettingsFile for saving/loading. Used by PreferencesScreen.

#####.launcher.SettingsFile
> New in CCCML: Represents contents of a CM-formatted .ini file.



Notably changed classes
-----------------------
#####.launcher.ClientLauncher
> Charged-Miners is a self-contained executable, and does not need JVM.
  It's launched differently, and has different command-line arguments.
  Executable's name varies between platforms, and it has to be chmod'ed.

#####.launcher.DiagnosticInfoUploader
> Client log file is named differently. Launcher's and game's files are
  co-located in DataDir, instead of split between LauncherDir and ClientDir.

#####.launcher.PathUtil
> Changed many directory and file names. Added getBinaryName().
  Removed getClientDir() (replaced by SharedUpdaterCode.getDataDir()).
  Removed getJavaPath().

#####.launcher.Prefs
> Removed Fullscreen/WindowSize, JavaArgs, and MaxMemory keys.

#####.launcher.UpdateTask
> Almost completely rewritten. Different file set, different hashing method,
  no more natives-extraction, primary/alternative download based on platform,
  no more resources-download, different version-file format and URL.

#####.launcher.gui.ErrorScreen
> Changed support URL to point to CCChargedMinersLauncher's repo issues.

#####.launcher.gui.JNiceLookingRenderer
> Added textColor property for graying disabled text.
  Changed all the colors.

#####.launcher.gui.PreferencesScreen
> Removed ClassiCube-specific Fullscreen/JavaArgs/MaxMemory settings.
  Added Charged-Miners-specific AutoSize/Width/Height settings.
  Rearranged the form elements a bit.

#####.launcher.gui.SignInScreen
> Removed [SinglePlayer] option.

#####.shared.SharedUpdaterCode
> Changed some file and directory names. Replaced getLauncherDir() with getDataDir(),
  since launcher and client files are co-located for Charged-Miners.
  Removed processDownload(), unpack200(), and all LZMA-related functionality.

#####.selfupdater.Program
> Changed URLs and launcher class name. Removed LZMA-related test and download.


Misc changes
------------
* Window icon and embedded images (under <code>CCChargedMinersLauncher/src/images/</code> have been re-branded.
* Updated Launch4j parameters (<code>launch4j.xml</code> and <code>wrapper.manifest</code> under <code>CCChargedMinersSelfUpdater/</code>)

