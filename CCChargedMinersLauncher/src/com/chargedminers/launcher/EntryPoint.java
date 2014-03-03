package com.chargedminers.launcher;

import com.chargedminers.launcher.gui.DebugWindow;
import com.chargedminers.launcher.gui.ErrorScreen;
import com.chargedminers.launcher.gui.Resources;
import com.chargedminers.launcher.gui.SignInScreen;
import com.chargedminers.shared.SharedUpdaterCode;
import java.io.IOException;

// Contains initialization code for the whole launcher
public final class EntryPoint {
    // This is also called by CCChargedMinersSelfUpdater

    public static void main(final String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");

        // Create launcher's data dir and init logger
        try {
            SharedUpdaterCode.getDataDir();
            LogUtil.init();

        } catch (final IOException ex) {
            ErrorScreen.show("Error starting Charged-Miners",
                    "Could not create data directory.", ex);
            System.exit(0);
        }

        // initialize shared code
        GameSession.initCookieHandling();

        // set look-and-feel to Numbus
        Resources.setLookAndFeel();

        if (Prefs.getDebugMode()) {
            DebugWindow.showWindow();
            DebugWindow.setWindowTitle("Launcher Running");
        }

        // display the form
        new SignInScreen().setVisible(true);

        // begin the update process
        UpdateTask.getInstance().execute();

        // begin looking up our external IP address
        GetExternalIPTask.getInstance().execute();
    }
}
