package com.chargedminers.launcher;

import com.chargedminers.shared.SharedUpdaterCode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

final class PathUtil {

    public static final String LOG_FILE_NAME = "launcher.log",
            LOG_OLD_FILE_NAME = "launcher.old.log",
            CLIENT_LOG_FILE_NAME = "log.txt",
            OPTIONS_FILE_NAME = "settings.ini",
            SELF_UPDATER_LOG_FILE_NAME = "selfupdater.log";

    // Safely replace contents of destFile with sourceFile.
    public synchronized static void replaceFile(final File sourceFile, final File destFile)
            throws IOException {
        if (sourceFile == null) {
            throw new NullPointerException("sourceFile");
        }
        if (destFile == null) {
            throw new NullPointerException("destFile");
        }
        Path sourcePath = Paths.get(sourceFile.getAbsolutePath());
        Path destPath = Paths.get(destFile.getAbsolutePath());
        try {
            Files.move(sourcePath, destPath, FileReplaceOptions);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(sourcePath, destPath, FallbackFileReplaceOptions);
        }
    }

    private static final CopyOption[] FileReplaceOptions = new CopyOption[]{
        StandardCopyOption.ATOMIC_MOVE,
        StandardCopyOption.REPLACE_EXISTING
    };

    private static final CopyOption[] FallbackFileReplaceOptions = new CopyOption[]{
        StandardCopyOption.REPLACE_EXISTING
    };

    public static void copyStreamToFile(InputStream inStream, File file) throws IOException {
        try (ReadableByteChannel in = Channels.newChannel(inStream)) {
            try (FileOutputStream outStream = new FileOutputStream(file)) {
                FileChannel out = outStream.getChannel();
                long offset = 0;
                long quantum = 1024 * 1024;
                long count;
                while ((count = out.transferFrom(in, offset, quantum)) > 0) {
                    offset += count;
                }
            }
        }
    }

    static String getBinaryName(boolean primary) {
        boolean isX64 = System.getProperty("os.arch").contains("64");
        String architecture;
        if (isX64 && primary) {
            architecture = "x86_64";
        } else if (!isX64 && !primary) {
            return null;
        } else {
            architecture = "i386";
        }

        final String osSuffix;
        switch (SharedUpdaterCode.OperatingSystem.detect()) {
            case WINDOWS:
                osSuffix = "exe";
                break;
            case MACOS:
                osSuffix = "MacOSX";
                break;
            case NIX:
                osSuffix = "Linux";
                break;
            default:
                throw new RuntimeException("Unsupported operating system.");
        }

        return "Charge." + architecture + "." + osSuffix;
    }

    private PathUtil() {
    }
}
