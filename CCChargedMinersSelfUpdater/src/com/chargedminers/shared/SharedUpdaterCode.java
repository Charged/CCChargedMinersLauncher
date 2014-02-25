package com.chargedminers.shared;

import java.io.File;
import java.io.IOException;

// Code shared between Launcher and SelfUpdater. The two source files are identical.
// These two classes cannot be combined into one because SelfUpdater must be able to
// run without referencing the Launcher, and vice versa.
public class SharedUpdaterCode {

    public static final String BASE_URL = "http://cm-cdn.fcraft.net/",
            DATA_DIR_NAME = "charge",
            MAC_PATH_SUFFIX = "Library/Application Support",
            NIX_PATH_SUFFIX = ".config",
            LAUNCHER_NEW_JAR_NAME = "launcher.jar.new";
    private static File launcherPath,
            appDataPath;

    public static synchronized File getDataDir() throws IOException {
        if (launcherPath == null) {
            final File userDir = getAppDataDir();
            launcherPath = new File(userDir, DATA_DIR_NAME);
            if (!launcherPath.exists() && !launcherPath.mkdirs()) {
                throw new IOException("Unable to create directory " + launcherPath);
            }
        }
        return launcherPath;
    }

    public static synchronized File getAppDataDir() {
        if (appDataPath == null) {
            final String home = System.getProperty("user.home", ".");
            final OperatingSystem os = OperatingSystem.detect();

            switch (os) {
                case WINDOWS:
                    final String appData = System.getenv("APPDATA");
                    if (appData != null) {
                        appDataPath = new File(appData);
                    } else {
                        appDataPath = new File(home);
                    }
                    break;

                case MACOS:
                    appDataPath = new File(home, MAC_PATH_SUFFIX);
                    break;

                case NIX:
                    appDataPath = new File(home, NIX_PATH_SUFFIX);
                    break;

                default:
                    appDataPath = new File(home);
            }
        }
        return appDataPath;
    }

    public enum OperatingSystem {

        NIX,
        SOLARIS,
        WINDOWS,
        MACOS,
        UNKNOWN;

        private final static String osName = System.getProperty("os.name").toLowerCase();

        public static OperatingSystem detect() {
            if (osName.contains("win")) {
                return OperatingSystem.WINDOWS;
            } else if (osName.contains("mac")) {
                return OperatingSystem.MACOS;
            } else if (osName.contains("solaris") || osName.contains("sunos")) {
                return OperatingSystem.SOLARIS;
            } else if (osName.contains("linux") || osName.contains("unix")) {
                return OperatingSystem.NIX;
            } else {
                return OperatingSystem.UNKNOWN;
            }
        }
    }

    private SharedUpdaterCode() {
    }
}
