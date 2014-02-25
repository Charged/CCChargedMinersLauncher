package com.chargedminers.launcher;

import com.chargedminers.shared.SharedUpdaterCode;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ChargedMinersSettings {

    private static final boolean FULLSCREEN_DEFAULT = false,
            AUTOSIZE_DEFAULT = true;
    private static final int WIDTH_DEFAULT = 800,
            HEIGHT_DEFAULT = 600;
    public static boolean fullscreen, autoSize;
    public static int width, height;

    public static void reset() {
        fullscreen = FULLSCREEN_DEFAULT;
        autoSize = AUTOSIZE_DEFAULT;
        width = WIDTH_DEFAULT;
        height = HEIGHT_DEFAULT;
    }

    private static File findFile() throws IOException {
        return new File(SharedUpdaterCode.getDataDir(), PathUtil.OPTIONS_FILE_NAME);
    }

    public static void load() throws IOException {
        LogUtil.getLogger().log(Level.INFO, "Loading settings from {0}", findFile());
        SettingsFile settings = new SettingsFile();
        settings.load(findFile());
        fullscreen = settings.getBoolean("fullscreen", FULLSCREEN_DEFAULT);
        autoSize = settings.getBoolean("fullscreenAutoSize", AUTOSIZE_DEFAULT);
        width = settings.getInt("w", 800);
        height = settings.getInt("h", 600);
    }

    public static void save() throws IOException {
        LogUtil.getLogger().log(Level.INFO, "Saving settings to {0}", findFile());
        SettingsFile settings = new SettingsFile();
        settings.load(findFile());
        settings.setBoolean("fullscreen", fullscreen);
        settings.setBoolean("fullscreenAutoSize", autoSize);
        settings.setInt("w", width);
        settings.setInt("h", height);
        settings.save(findFile());
    }
}
