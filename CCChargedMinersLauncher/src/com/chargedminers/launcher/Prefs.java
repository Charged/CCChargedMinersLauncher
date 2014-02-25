package com.chargedminers.launcher;

import java.util.prefs.Preferences;

public final class Prefs {

    // Key names
    private static final String keyUpdateMode = "UpdateMode",
            keyRememberUsers = "RememberUsers",
            keyRememberPasswords = "RememberPasswords",
            keyRememberServer = "RememberServer",
            keySelectedGameService = "SelectedGameService",
            keyDebugMode = "DebugMode",
            keyRememberedExternalIPs = "RememberedExternalIPs",
            keyKeepOpen = "KeepOpen";

    // Defaults
    public final static UpdateMode UpdateModeDefault = UpdateMode.NOTIFY;
    public final static boolean RememberUsersDefault = true,
            RememberPasswordsDefault = true,
            RememberServerDefault = true,
            DebugModeDefault = false,
            KeepOpenDefault = false;
    public final static GameServiceType SelectedGameServiceDefault = GameServiceType.ClassiCubeNetService;

    // Getters
    public static UpdateMode getUpdateMode() {
        try {
            return UpdateMode.valueOf(getPrefs().get(keyUpdateMode, UpdateModeDefault.name()));
        } catch (final IllegalArgumentException ex) {
            return UpdateModeDefault;
        }
    }

    public static boolean getRememberUsers() {
        return getPrefs().getBoolean(keyRememberUsers, RememberUsersDefault);
    }

    public static boolean getRememberPasswords() {
        return getPrefs().getBoolean(keyRememberPasswords, RememberPasswordsDefault);
    }

    public static boolean getRememberServer() {
        return getPrefs().getBoolean(keyRememberServer, RememberServerDefault);
    }

    public static boolean getDebugMode() {
        return getPrefs().getBoolean(keyDebugMode, DebugModeDefault);
    }

    public static GameServiceType getSelectedGameService() {
        try {
            final String val = getPrefs().get(keySelectedGameService, SelectedGameServiceDefault.name());
            return GameServiceType.valueOf(val);
        } catch (final IllegalArgumentException ex) {
            return SelectedGameServiceDefault;
        }
    }
    
    public static boolean getKeepOpen() {
        return getPrefs().getBoolean(keyKeepOpen, KeepOpenDefault);
    }

    // Setters
    public static void setUpdateMode(final UpdateMode val) {
        getPrefs().put(keyUpdateMode, val.name());
    }

    public static void setRememberUsers(final boolean val) {
        getPrefs().putBoolean(keyRememberUsers, val);
    }

    public static void setRememberPasswords(final boolean val) {
        getPrefs().putBoolean(keyRememberPasswords, val);
    }

    public static void setRememberServer(final boolean val) {
        getPrefs().putBoolean(keyRememberServer, val);
    }

    public static void setDebugMode(final boolean val) {
        getPrefs().putBoolean(keyDebugMode, val);
    }

    public static void setSelectedGameService(final GameServiceType val) {
        getPrefs().put(keySelectedGameService, val.name());
    }
    
    public static void setKeepOpen(final boolean val) {
        getPrefs().putBoolean(keyKeepOpen, val);
    }

    // Etc
    private static Preferences getPrefs() {
        return Preferences.userNodeForPackage(Prefs.class);
    }
    
    public static Preferences getRememberedExternalIPs() {
        return getPrefs().node(keyRememberedExternalIPs);
    }

    private Prefs() {
    }
}
