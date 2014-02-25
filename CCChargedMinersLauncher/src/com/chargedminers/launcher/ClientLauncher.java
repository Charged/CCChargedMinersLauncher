package com.chargedminers.launcher;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import com.chargedminers.launcher.gui.DebugWindow;
import com.chargedminers.launcher.gui.ErrorScreen;
import com.chargedminers.shared.SharedUpdaterCode;
import java.io.FileNotFoundException;
import org.apache.commons.lang3.StringUtils;

// Handles launching the client process.
final public class ClientLauncher {

    public static void launchClient(final ServerJoinInfo joinInfo) {
        LogUtil.getLogger().info("launchClient");
        if (joinInfo == null) {
            throw new NullPointerException("joinInfo");
        }

        try {
            // Find the game binary
            File binaryFile = new File(SharedUpdaterCode.getDataDir(), PathUtil.getBinaryName(false));
            if (!binaryFile.exists()) {
                binaryFile = new File(SharedUpdaterCode.getDataDir(), PathUtil.getBinaryName(true));
            }
            if (!binaryFile.exists()) {
                throw new FileNotFoundException("Could not find the game executable at " + binaryFile.getAbsolutePath());
            }

            // Chmod +x, if needed
            if (!binaryFile.canExecute()) {
                binaryFile.setExecutable(true);
            }

            // Pack joinInfo's information into an mc:// URI
            String mcUrl = String.format("mc://%s:%d/%s/%s",
                    joinInfo.address.getHostAddress(), joinInfo.port, joinInfo.playerName,
                    (joinInfo.pass != null ? joinInfo.pass : ""));

            final ProcessBuilder processBuilder = new ProcessBuilder(
                    binaryFile.getAbsolutePath(), mcUrl
            //, "SKIN_SERVER=" + SessionManager.getSession().getSkinUrl() // TODO: when CM supports it
            );

            processBuilder.directory(SharedUpdaterCode.getDataDir());

            // log the command used to launch client
            String cmdLineToLog = StringUtils.join(processBuilder.command(), ' ');
            if (joinInfo.pass != null && joinInfo.pass.length() > 16) {
                // sanitize mppass -- we don't want it logged.
                cmdLineToLog = cmdLineToLog.replace(joinInfo.pass, "########");
            }
            LogUtil.getLogger().log(Level.INFO, cmdLineToLog);

            if (Prefs.getDebugMode()) {
                processBuilder.redirectErrorStream(true);
                try {
                    final Process p = processBuilder.start();
                    DebugWindow.setWindowTitle("Game Running");

                    // capture output from the client, redirect to DebugWindow
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Scanner input = new Scanner(p.getInputStream());
                                while (true) {
                                    DebugWindow.writeLine(input.nextLine());
                                }
                            } catch (NoSuchElementException ex) {
                                DebugWindow.writeLine("(client closed)");
                                DebugWindow.setWindowTitle("Client Closed");
                            }
                        }
                    }.start();

                } catch (IOException ex) {
                    LogUtil.getLogger().log(Level.SEVERE, "Error launching client", ex);
                }
            } else {
                processBuilder.start();
                if (!Prefs.getKeepOpen()) {
                    System.exit(0);
                }
            }

        } catch (final Exception ex) {
            ErrorScreen.show("Could not launch the game",
                    "Error launching the client:<br>" + ex.getMessage(), ex);
        }
    }

    private ClientLauncher() {
    }
}
