package com.chargedminers.launcher.gui;

import com.chargedminers.launcher.AccountManager;
import com.chargedminers.launcher.ChargedMinersSettings;
import com.chargedminers.launcher.DiagnosticInfoUploader;
import com.chargedminers.launcher.GameServiceType;
import com.chargedminers.launcher.LogUtil;
import com.chargedminers.launcher.Prefs;
import com.chargedminers.launcher.SessionManager;
import com.chargedminers.launcher.UpdateMode;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.border.EmptyBorder;

final class PreferencesScreen extends javax.swing.JDialog {
    // =============================================================================================
    //                                                                                INITIALIZATION
    // =============================================================================================

    PreferencesScreen(final JFrame parent) {
        super(parent, "Preferences", true);
        final JRootPane root = getRootPane();
        root.setBorder(new EmptyBorder(8, 8, 8, 8));
        initComponents();

        root.setDefaultButton(bSave);

        // tweak BG colors
        root.setBackground(new Color(247, 247, 247));
        getContentPane().setBackground(new Color(247, 247, 247));

        // match save and cancel buttons' sizes
        bSave.setPreferredSize(bCancel.getSize());

        // fix for ugly spinner border
        nWidth.getEditor().setOpaque(false);
        nHeight.getEditor().setOpaque(false);

        // Pack and center
        pack();
        setLocationRelativeTo(parent);

        // Fill in the values
        loadPreferences();
        checkIfForgetButtonsShouldBeEnabled();
    }

    void checkIfForgetButtonsShouldBeEnabled() {
        AccountManager curManager = SessionManager.getAccountManager();
        AccountManager otherManager;
        if (SessionManager.getSession().getServiceType() == GameServiceType.ClassiCubeNetService) {
            otherManager = new AccountManager(GameServiceType.MinecraftNetService);
        } else {
            otherManager = new AccountManager(GameServiceType.ClassiCubeNetService);
        }
        boolean hasUsers = curManager.hasAccounts() || otherManager.hasAccounts();
        boolean hasPasswords = hasUsers && (curManager.hasPasswords() || otherManager.hasPasswords());
        boolean hasResume;
        try {
            hasResume = SessionManager.hasAnyResumeInfo() || (Prefs.getRememberedExternalIPs().keys().length > 0);
        } catch (BackingStoreException ex) {
            LogUtil.getLogger().log(Level.WARNING, "Error checking stored external IPs", ex);
            hasResume = SessionManager.hasAnyResumeInfo();
        }

        this.bForgetUsers.setEnabled(hasUsers);
        this.bForgetPasswords.setEnabled(hasPasswords);
        this.bForgetServers.setEnabled(hasResume);
    }

    void checkIfSettingsShouldBeEnabled() {
        xAutoSize.setEnabled(xFullscreen.isSelected());
        nWidth.setEnabled(!(xFullscreen.isSelected() && xAutoSize.isSelected()));
        nHeight.setEnabled(!(xFullscreen.isSelected() && xAutoSize.isSelected()));
    }

    // =============================================================================================
    //                                                                         LOADING/STORING PREFS
    // =============================================================================================
    private void loadPreferences() {
        loadUpdateMode(Prefs.getUpdateMode());
        xRememberPasswords.setSelected(Prefs.getRememberPasswords());
        xRememberUsers.setSelected(Prefs.getRememberUsers()); // should be loaded AFTER password
        xRememberServer.setSelected(Prefs.getRememberServer());
        xDebugMode.setSelected(Prefs.getDebugMode());
        xKeepOpen.setSelected(Prefs.getKeepOpen());

        try {
            ChargedMinersSettings.load();
        } catch (IOException ex) {
            ChargedMinersSettings.reset();
            ErrorScreen.show("Error loading game settings",
                    "Game settings could not be loaded due to an unexpected error.", ex);
        }
        loadCMPreferences();
    }

    private void loadCMPreferences() {
        xFullscreen.setSelected(ChargedMinersSettings.fullscreen);
        xAutoSize.setSelected(ChargedMinersSettings.autoSize);
        nWidth.setValue(ChargedMinersSettings.width);
        nHeight.setValue(ChargedMinersSettings.height);
        checkIfSettingsShouldBeEnabled();
    }

    private void loadUpdateMode(final UpdateMode val) {
        final JRadioButton btn;
        switch (val) {
            case DISABLED:
                btn = rUpdateDisabled;
                break;
            case AUTOMATIC:
                btn = rUpdateAutomatic;
                break;
            default: // NOTIFY
                btn = rUpdateNotify;
                break;
        }
        rgUpdateMode.setSelected(btn.getModel(), true);
    }

    private void loadDefaults() {
        loadUpdateMode(Prefs.UpdateModeDefault);
        xRememberUsers.setSelected(Prefs.RememberUsersDefault);
        xRememberPasswords.setSelected(Prefs.RememberPasswordsDefault);
        xRememberServer.setSelected(Prefs.RememberServerDefault);
        xDebugMode.setSelected(Prefs.DebugModeDefault);
        xKeepOpen.setSelected(Prefs.KeepOpenDefault);

        ChargedMinersSettings.reset();
        loadCMPreferences();
    }

    private void storePreferences() {
        Prefs.setUpdateMode(storeUpdateMode());
        Prefs.setRememberUsers(xRememberUsers.isSelected());
        Prefs.setRememberPasswords(xRememberPasswords.isSelected());
        Prefs.setRememberServer(xRememberServer.isSelected());
        Prefs.setDebugMode(xDebugMode.isSelected());
        Prefs.setKeepOpen(xKeepOpen.isSelected());

        ChargedMinersSettings.fullscreen = xFullscreen.isSelected();
        ChargedMinersSettings.autoSize = xAutoSize.isSelected();
        ChargedMinersSettings.width = (int)nWidth.getValue();
        ChargedMinersSettings.height = (int)nHeight.getValue();
        try {
            ChargedMinersSettings.save();
        } catch (IOException ex) {
            ChargedMinersSettings.reset();
            ErrorScreen.show("Error saving game settings",
                    "Game settings could not be saved due to an unexpected error.", ex);
        }
    }

    private UpdateMode storeUpdateMode() {
        final UpdateMode val;
        if (rUpdateDisabled.isSelected()) {
            val = UpdateMode.DISABLED;
        } else if (rUpdateAutomatic.isSelected()) {
            val = UpdateMode.AUTOMATIC;
        } else {
            val = UpdateMode.NOTIFY;
        }
        return val;
    }

    // =============================================================================================
    //                                                                                    FORGETTING
    // =============================================================================================
    private void bForgetUsersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bForgetUsersActionPerformed
        if (ConfirmScreen.show("Warning", "Really erase all stored user information?")) {
            LogUtil.getLogger().log(Level.INFO, "[Forget Users]");
            SessionManager.getAccountManager().clear();
            checkIfForgetButtonsShouldBeEnabled();
        }
    }//GEN-LAST:event_bForgetUsersActionPerformed

    private void bForgetPasswordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bForgetPasswordsActionPerformed
        if (ConfirmScreen.show("Warning", "Really erase all stored user passwords?")) {
            LogUtil.getLogger().log(Level.INFO, "[Forget Passwords]");
            SessionManager.getAccountManager().clearPasswords();
            checkIfForgetButtonsShouldBeEnabled();
        }
    }//GEN-LAST:event_bForgetPasswordsActionPerformed

    private void bForgetServersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bForgetServersActionPerformed
        if (ConfirmScreen.show("Warning", "Really erase stored information about the servers you joined?")) {
            LogUtil.getLogger().log(Level.INFO, "[Forget Servers]");
            SessionManager.clearAllResumeInfo();
            try {
                Prefs.getRememberedExternalIPs().clear();
            } catch (BackingStoreException ex) {
                LogUtil.getLogger().log(Level.SEVERE, "Error erasing preferences.", ex);
            }
            checkIfForgetButtonsShouldBeEnabled();
        }
    }//GEN-LAST:event_bForgetServersActionPerformed

    // =============================================================================================
    //                                                                           GUI EVENT LISTENERS
    // =============================================================================================
    private void bCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCancelActionPerformed
        LogUtil.getLogger().log(Level.FINE, "[Cancel]");
        dispose();
    }//GEN-LAST:event_bCancelActionPerformed

    private void bDefaultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bDefaultsActionPerformed
        LogUtil.getLogger().log(Level.FINE, "[Defaults]");
        loadDefaults();
    }//GEN-LAST:event_bDefaultsActionPerformed

    private void bSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSaveActionPerformed
        LogUtil.getLogger().log(Level.FINE, "[Save]");
        storePreferences();
        if (!this.xRememberUsers.isSelected()) {
            SessionManager.getAccountManager().clear();
        }
        if (!this.xRememberPasswords.isSelected()) {
            SessionManager.getAccountManager().clearPasswords();
        }
        if (!this.xRememberServer.isSelected()) {
            SessionManager.clearAllResumeInfo();
        }
        dispose();
    }//GEN-LAST:event_bSaveActionPerformed

    private void xRememberUsersItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_xRememberUsersItemStateChanged
        if (evt.getStateChange() == ItemEvent.DESELECTED) {
            this.xRememberPasswords.setEnabled(false);
            this.xRememberPasswords.setSelected(false);
            this.bForgetPasswords.setEnabled(false);
            this.bForgetUsers.setEnabled(false);
        } else {
            this.xRememberPasswords.setEnabled(true);
            this.bForgetUsers.setEnabled(true);
            this.bForgetPasswords.setEnabled(xRememberPasswords.isSelected());
        }
    }//GEN-LAST:event_xRememberUsersItemStateChanged

    private void xRememberPasswordsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_xRememberPasswordsItemStateChanged
        if (evt.getStateChange() == ItemEvent.DESELECTED) {
            this.bForgetPasswords.setEnabled(false);
        } else {
            this.bForgetPasswords.setEnabled(true);
        }
    }//GEN-LAST:event_xRememberPasswordsItemStateChanged

    private void bSubmitDiagInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSubmitDiagInfoActionPerformed
        LogUtil.getLogger().log(Level.FINE, "[Submit diagnostic information]");
        String url = DiagnosticInfoUploader.uploadToGist();
        PromptScreen.show("Diagnostic information submitted!",
                "Please provide this link to the ClassiCube developers.",
                url, false);
    }//GEN-LAST:event_bSubmitDiagInfoActionPerformed

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

        rgUpdateMode = new javax.swing.ButtonGroup();
        javax.swing.JLabel lResolution = new javax.swing.JLabel();
        nWidth = new javax.swing.JSpinner();
        javax.swing.JLabel lX = new javax.swing.JLabel();
        nHeight = new javax.swing.JSpinner();
        xAutoSize = new javax.swing.JCheckBox();
        xFullscreen = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JLabel lUpdateMode = new javax.swing.JLabel();
        rUpdateDisabled = new javax.swing.JRadioButton();
        rUpdateNotify = new javax.swing.JRadioButton();
        rUpdateAutomatic = new javax.swing.JRadioButton();
        javax.swing.JSeparator jSeparator2 = new javax.swing.JSeparator();
        xRememberUsers = new javax.swing.JCheckBox();
        bForgetUsers = new com.chargedminers.launcher.gui.JNiceLookingButton();
        xRememberPasswords = new javax.swing.JCheckBox();
        bForgetPasswords = new com.chargedminers.launcher.gui.JNiceLookingButton();
        xRememberServer = new javax.swing.JCheckBox();
        bForgetServers = new com.chargedminers.launcher.gui.JNiceLookingButton();
        javax.swing.JSeparator jSeparator3 = new javax.swing.JSeparator();
        javax.swing.JSeparator jSeparator4 = new javax.swing.JSeparator();
        bDefaults = new com.chargedminers.launcher.gui.JNiceLookingButton();
        bSave = new com.chargedminers.launcher.gui.JNiceLookingButton();
        bCancel = new com.chargedminers.launcher.gui.JNiceLookingButton();
        xDebugMode = new javax.swing.JCheckBox();
        bSubmitDiagInfo = new com.chargedminers.launcher.gui.JNiceLookingButton();
        xKeepOpen = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        lResolution.setText("Game resolution:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        getContentPane().add(lResolution, gridBagConstraints);

        nWidth.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(320), Integer.valueOf(320), null, Integer.valueOf(1)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        getContentPane().add(nWidth, gridBagConstraints);

        lX.setText("×");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        getContentPane().add(lX, gridBagConstraints);

        nHeight.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(240), Integer.valueOf(240), null, Integer.valueOf(1)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        getContentPane().add(nHeight, gridBagConstraints);

        xAutoSize.setText("Auto-size");
        xAutoSize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                xAutoSizeStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        getContentPane().add(xAutoSize, gridBagConstraints);

        xFullscreen.setText("Start the game in fullscreen");
        xFullscreen.setToolTipText("<html>Choose whether ClassiCube games should start in fullscreen mode.<br>\nYou can also toggle fullscreen mode in-game by pressing <b>F11</b>.<br>\nDefault is OFF.");
        xFullscreen.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                xFullscreenStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        getContentPane().add(xFullscreen, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        getContentPane().add(jSeparator1, gridBagConstraints);

        lUpdateMode.setText("Install game updates...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        getContentPane().add(lUpdateMode, gridBagConstraints);

        rgUpdateMode.add(rUpdateDisabled);
        rUpdateDisabled.setText("Disable");
        rUpdateDisabled.setToolTipText("<html><b>Disable</b>: No game updates will ever be downloaded or installed.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        getContentPane().add(rUpdateDisabled, gridBagConstraints);

        rgUpdateMode.add(rUpdateNotify);
        rUpdateNotify.setText("Enable (notify me)");
        rUpdateNotify.setToolTipText("<html><b>Enable (notify me)</b>: Game updates will be downloaded and installed.<br>\nYou will be notified when that happens, and you'll have an option to review changes in the latest update.<br>\nThis is the default option.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        getContentPane().add(rUpdateNotify, gridBagConstraints);

        rgUpdateMode.add(rUpdateAutomatic);
        rUpdateAutomatic.setText("Enable (automatic)");
        rUpdateAutomatic.setToolTipText("<html><b>Enable (automatic)</b>: Game updates will be installed automatically and silently.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        getContentPane().add(rUpdateAutomatic, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        getContentPane().add(jSeparator2, gridBagConstraints);

        xRememberUsers.setText("Remember usernames");
        xRememberUsers.setToolTipText("<html>Choose whether the launcher should remember usernames of players who sign in.<br>\nWhen enabled (default), most-recently-used name is filled in when the launcher starts,<br>\nand names of other accounts are available from a drop-down menu. When disabled,<br>\nyou will have to re-enter both username and password every time you sign in.");
        xRememberUsers.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                xRememberUsersItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(xRememberUsers, gridBagConstraints);

        bForgetUsers.setText("Forget all users");
        bForgetUsers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bForgetUsersActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(bForgetUsers, gridBagConstraints);

        xRememberPasswords.setText("Remember passwords");
        xRememberPasswords.setToolTipText("<html>Choose whether the launcher should remember passwords of players who sign in.<br>\nWhen enabled, selecting a previously-used username will fill in the password field.<br>\nWhen disabled (default), you will have to re-enter the password every time you sign in.<br>\nNote that entered passwords are stored on your PC in plain text.");
        xRememberPasswords.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                xRememberPasswordsItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(xRememberPasswords, gridBagConstraints);

        bForgetPasswords.setText("Forget all passwords");
        bForgetPasswords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bForgetPasswordsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(bForgetPasswords, gridBagConstraints);

        xRememberServer.setText("Remember last-joined server");
        xRememberServer.setToolTipText("<html>Choose whether the launcher should remember last-joined server.<br>\nWhen enabled, the [Resume] button will become available, which will reconnect<br>\nyou to the most-recently-joined server using the same username/credentials as last time.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        getContentPane().add(xRememberServer, gridBagConstraints);

        bForgetServers.setText("Forget servers");
        bForgetServers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bForgetServersActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(bForgetServers, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        getContentPane().add(jSeparator3, gridBagConstraints);

        jSeparator4.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 8, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        getContentPane().add(jSeparator4, gridBagConstraints);

        bDefaults.setText("Defaults");
        bDefaults.setToolTipText("Reset all preferences to their default values.");
        bDefaults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bDefaultsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        getContentPane().add(bDefaults, gridBagConstraints);

        bSave.setText("Save");
        bSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSaveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
        getContentPane().add(bSave, gridBagConstraints);

        bCancel.setText("Cancel");
        bCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
        getContentPane().add(bCancel, gridBagConstraints);

        xDebugMode.setText("Debug mode");
        xDebugMode.setToolTipText("Enables debug console (requires launcher restart).");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        getContentPane().add(xDebugMode, gridBagConstraints);

        bSubmitDiagInfo.setText("Submit diagnostic information");
        bSubmitDiagInfo.setToolTipText("<html>Upload information needed for diagnosing problems in ClassiCube software.<br>\nInformation includes log files, your preferences, some basic system information<br>\n(Java version, operating system, etc), and a list of files in your client's directory.<br>\n<b>Logs may contain your username, but NOT your password or any other personal info.");
        bSubmitDiagInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSubmitDiagInfoActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        getContentPane().add(bSubmitDiagInfo, gridBagConstraints);

        xKeepOpen.setText("Keep launcher open after launching the game");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(xKeepOpen, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void xFullscreenStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_xFullscreenStateChanged
        checkIfSettingsShouldBeEnabled();
    }//GEN-LAST:event_xFullscreenStateChanged

    private void xAutoSizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_xAutoSizeStateChanged
        checkIfSettingsShouldBeEnabled();
    }//GEN-LAST:event_xAutoSizeStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.chargedminers.launcher.gui.JNiceLookingButton bCancel;
    private com.chargedminers.launcher.gui.JNiceLookingButton bDefaults;
    private com.chargedminers.launcher.gui.JNiceLookingButton bForgetPasswords;
    private com.chargedminers.launcher.gui.JNiceLookingButton bForgetServers;
    private com.chargedminers.launcher.gui.JNiceLookingButton bForgetUsers;
    private com.chargedminers.launcher.gui.JNiceLookingButton bSave;
    private com.chargedminers.launcher.gui.JNiceLookingButton bSubmitDiagInfo;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSpinner nHeight;
    private javax.swing.JSpinner nWidth;
    private javax.swing.JRadioButton rUpdateAutomatic;
    private javax.swing.JRadioButton rUpdateDisabled;
    private javax.swing.JRadioButton rUpdateNotify;
    private javax.swing.ButtonGroup rgUpdateMode;
    private javax.swing.JCheckBox xAutoSize;
    private javax.swing.JCheckBox xDebugMode;
    private javax.swing.JCheckBox xFullscreen;
    private javax.swing.JCheckBox xKeepOpen;
    private javax.swing.JCheckBox xRememberPasswords;
    private javax.swing.JCheckBox xRememberServer;
    private javax.swing.JCheckBox xRememberUsers;
    // End of variables declaration//GEN-END:variables
}
