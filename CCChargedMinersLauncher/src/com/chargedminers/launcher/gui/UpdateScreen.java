package com.chargedminers.launcher.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.border.EmptyBorder;
import com.chargedminers.launcher.ClientLauncher;
import com.chargedminers.launcher.LogUtil;
import com.chargedminers.launcher.Prefs;
import com.chargedminers.launcher.ServerJoinInfo;
import com.chargedminers.launcher.SessionManager;
import com.chargedminers.launcher.UpdateMode;
import com.chargedminers.launcher.UpdateTask;

public final class UpdateScreen extends JFrame {
    // =============================================================================================
    //                                                                            FIELDS & CONSTANTS
    // =============================================================================================

    private static final String RELEASE_NOTES_URL = "http://www.classicube.net/forum/viewpost/ir/latest/#bottom_post";
    private Desktop desktop;
    private final ServerJoinInfo joinInfo;

    // =============================================================================================
    //                                                                                INITIALIZATION
    // =============================================================================================
    // Returns 'true' if the calling screen should keep itself open.
    public static boolean createAndShow(final ServerJoinInfo joinInfo) {
        if (UpdateTask.getUpdateFinished()) {
            // Skip the update screen
            ClientLauncher.launchClient(joinInfo);
            return Prefs.getKeepOpen();
        } else {
            UpdateScreen sc = new UpdateScreen(joinInfo);
            sc.setVisible(true);
            UpdateTask.getInstance().registerUpdateScreen(sc);
            return false;
        }
    }

    private UpdateScreen(final ServerJoinInfo joinInfo) {
        this.joinInfo = joinInfo;
        final JRootPane root = getRootPane();
        root.setBorder(new EmptyBorder(8, 8, 8, 8));

        initComponents();

        // center the form on screen (initially)
        setLocationRelativeTo(null);

        // tweak the UI for auto/notify preference
        switch (Prefs.getUpdateMode()) {
            case AUTOMATIC:
                lNotice.setText("The game will start as soon as updates are complete.");
                bPlay.setVisible(false);
                break;
            case NOTIFY:
                lNotice.setText("Please wait: a game update is being installed.");
                root.setDefaultButton(bPlay);
                this.desktop = (Desktop.isDesktopSupported() ? Desktop.getDesktop() : null);
                if (this.desktop != null && !this.desktop.isSupported(Desktop.Action.BROWSE)) {
                    this.desktop = null;
                }
                break;
            case DISABLED:
                lNotice.setText("The game will start as soon as required files are downloaded.");
                bPlay.setVisible(false);
                bViewChanges.setVisible(false);
                break;
        }
        pack();
    }

    // =============================================================================================
    //                                                                                      UPDATING
    // =============================================================================================
    public void setStatus(final UpdateTask.ProgressUpdate dl) {
        if (dl.progress < 0) {
            this.progress.setIndeterminate(true);
        } else {
            this.progress.setIndeterminate(false);
            this.progress.setValue(dl.progress);
        }
        this.lStats.setText(dl.statusString);
    }

    public void onUpdateDone(final boolean updatesApplied) {
        LogUtil.getLogger().info("onUpdateDone");
        try {
            // wait for updater to finish (if still running)
            UpdateTask.getInstance().get();
            UpdateTask.setUpdateFinished(true);

        } catch (final InterruptedException | ExecutionException ex) {
            LogUtil.getLogger().log(Level.SEVERE, "Error during the download/update process.", ex);
            ErrorScreen.show("Error updating",
                    "The game cannot be started because an error occured during the download/update process.",
                    ex);
            System.exit(3);
            return;
        }

        if (!updatesApplied || Prefs.getUpdateMode() != UpdateMode.NOTIFY) {
            launchClient();
        } else {
            this.lNotice.setText(" ");
            this.bPlay.setEnabled(true);
            this.bPlay.setVisible(true);
            pack();
        }
    }

    private void launchClient() {
        dispose();
        ClientLauncher.launchClient(this.joinInfo);
        if (Prefs.getKeepOpen()) {
            if (SessionManager.getSession().isSignedIn()) {
                // If a user is is signed in, [Connect] must've been clicked,
                // so we return to the server list
                new ServerListScreen().setVisible(true);
            } else {
                // Else, [Singleplayer] or [Direct] must've been clicked,
                // so we return to sign-in screen
                new SignInScreen().setVisible(true);
            }
        }
    }

    // =============================================================================================
    //                                                                           GUI EVENT LISTENERS
    // =============================================================================================
    private void bPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bPlayActionPerformed
        launchClient();
    }//GEN-LAST:event_bPlayActionPerformed

    private void bViewChangesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bViewChangesActionPerformed
        if (this.desktop != null) {
            try {
                this.desktop.browse(new URI(RELEASE_NOTES_URL));
            } catch (final IOException | URISyntaxException | UnsupportedOperationException | SecurityException | IllegalArgumentException ex) {
                LogUtil.getLogger().log(Level.WARNING, "Error opening release notes URL", ex);
                showReleaseNotesUrl();
            }
        } else {
            showReleaseNotesUrl();
        }
    }//GEN-LAST:event_bViewChangesActionPerformed

    void showReleaseNotesUrl() {
        PromptScreen.show("Release notes link",
                "You can find a list of changes in this game update at this URL:",
                RELEASE_NOTES_URL, false);
    }

    // =============================================================================================
    //                                                                            GENERATED GUI CODE
    // =============================================================================================
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lStats = new javax.swing.JLabel();
        progress = new javax.swing.JProgressBar();
        lNotice = new javax.swing.JLabel();
        bViewChanges = new com.chargedminers.launcher.gui.JNiceLookingButton();
        bPlay = new com.chargedminers.launcher.gui.JNiceLookingButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Game Update");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        lStats.setForeground(new java.awt.Color(255, 255, 255));
        lStats.setText("Preparing to update...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        getContentPane().add(lStats, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        getContentPane().add(progress, gridBagConstraints);

        lNotice.setForeground(new java.awt.Color(255, 255, 255));
        lNotice.setText("<notice>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        getContentPane().add(lNotice, gridBagConstraints);

        bViewChanges.setText("View Changes");
        bViewChanges.setToolTipText("Check out list of changes included in this update (opens a web browser).");
        bViewChanges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bViewChangesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 16);
        getContentPane().add(bViewChanges, gridBagConstraints);

        bPlay.setText("Play >");
        bPlay.setEnabled(false);
        bPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bPlayActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
        getContentPane().add(bPlay, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.chargedminers.launcher.gui.JNiceLookingButton bPlay;
    private com.chargedminers.launcher.gui.JNiceLookingButton bViewChanges;
    private javax.swing.JLabel lNotice;
    private javax.swing.JLabel lStats;
    private javax.swing.JProgressBar progress;
    // End of variables declaration//GEN-END:variables
}