package com.chargedminers.launcher.gui;

import com.chargedminers.launcher.LogUtil;
import com.chargedminers.launcher.Prefs;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import javax.swing.border.EmptyBorder;

public class SameIPScreen extends JDialog {

    private static final String DIALOG_TITLE = "Local server detected";

    public static InetAddress show(InetAddress serverAddress, int port) {
        String fullHostname = serverAddress.getHostAddress() + ":" + port;
        Preferences ipList = Prefs.getRememberedExternalIPs();
        String ipRemapString = ipList.get(fullHostname, "");
        if (!ipRemapString.isEmpty()) {
            try {
                return InetAddress.getByName(ipRemapString);
            } catch (UnknownHostException ex) {
                LogUtil.getLogger().log(Level.SEVERE, "Error parsing remembered external-IP remapping.", ex);
            }
        }
        SameIPScreen screen = new SameIPScreen(serverAddress);
        screen.setVisible(true);
        
        // Save user's preference
        if (screen.chosenAddress != null && screen.xRememberChoice.isSelected()) {
            ipList.put(fullHostname, screen.chosenAddress.getHostAddress());
        }
        return screen.chosenAddress;
    }

    InetAddress originalAddress;
    InetAddress chosenAddress;

    private SameIPScreen(final InetAddress serverAddress) {
        // set title, add border
        super((Frame) null, DIALOG_TITLE, true);
        originalAddress = serverAddress;
        sharedInitCode();
    }

    private void sharedInitCode() {
        // set background
        final ImagePanel bgPanel = new ImagePanel(null, true);
        bgPanel.setGradient(true);
        bgPanel.setImage(Resources.getClassiCubeBackground());
        bgPanel.setGradientColor(Resources.colorGradient);
        bgPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        setContentPane(bgPanel);

        initComponents();

        // focus & highlight [Continue]
        getRootPane().setDefaultButton(bContinue);

        // Show GridBagLayout who's boss.
        this.imgErrorIcon.setImage(Resources.getInfoIcon());
        this.imgErrorIcon.setMinimumSize(new Dimension(64, 64));
        this.imgErrorIcon.setPreferredSize(new Dimension(64, 64));
        this.imgErrorIcon.setSize(new Dimension(64, 64));

        // Set windows icon, size, and location
        this.setIconImages(Resources.getWindowIcons());
        this.setPreferredSize(new Dimension(450, 200));
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        bgChoice = new javax.swing.ButtonGroup();
        imgErrorIcon = new com.chargedminers.launcher.gui.ImagePanel();
        lMessage = new javax.swing.JLabel();
        rLocalhost = new javax.swing.JRadioButton();
        rLocalNetwork = new javax.swing.JRadioButton();
        rNoChange = new javax.swing.JRadioButton();
        xRememberChoice = new javax.swing.JCheckBox();
        bContinue = new com.chargedminers.launcher.gui.JNiceLookingButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setType(java.awt.Window.Type.UTILITY);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        imgErrorIcon.setMaximumSize(new java.awt.Dimension(64, 64));
        imgErrorIcon.setMinimumSize(new java.awt.Dimension(64, 64));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        getContentPane().add(imgErrorIcon, gridBagConstraints);

        lMessage.setForeground(new java.awt.Color(255, 255, 255));
        lMessage.setText("<html><b>Are you trying to connect to a server that is hosted on your home network (LAN)?");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        getContentPane().add(lMessage, gridBagConstraints);

        bgChoice.add(rLocalhost);
        rLocalhost.setSelected(true);
        rLocalhost.setText("Yes, server is hosted on this computer.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 4, 0);
        getContentPane().add(rLocalhost, gridBagConstraints);

        bgChoice.add(rLocalNetwork);
        rLocalNetwork.setText("Yes, server is on another computer on this network.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 4, 0);
        getContentPane().add(rLocalNetwork, gridBagConstraints);

        bgChoice.add(rNoChange);
        rNoChange.setText("No, neither.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 4, 0);
        getContentPane().add(rNoChange, gridBagConstraints);

        xRememberChoice.setSelected(true);
        xRememberChoice.setText("Remember my choice.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        getContentPane().add(xRememberChoice, gridBagConstraints);

        bContinue.setText("Continue");
        bContinue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bContinueActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        getContentPane().add(bContinue, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bContinueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bContinueActionPerformed
        if (this.rLocalhost.isSelected()) {
            // Yes, server is hosted on this computer:
            // Use localhost (127.0.0.1) in place of server's IP.
            chosenAddress = InetAddress.getLoopbackAddress();

        } else if (this.rLocalNetwork.isSelected()) {
            // Yes, server is on another computer on this network:
            // Ask player for a local IP address.
            this.setEnabled(false);
            final String baseMessage = "Please enter the local address (192.168.x.x) of the computer on which the server is hosted.";

            while (true) {
                String givenString = PromptScreen.show(DIALOG_TITLE, baseMessage, "192.168.", true);
                if (givenString == null) {
                    // User left the address blank or pressed [Cancel]. Abort!
                    this.setEnabled(true);
                    return;
                }

                // try parsing the given string
                try {
                    if (givenString.equals("localhost")) {
                        chosenAddress = InetAddress.getLoopbackAddress();
                    } else {
                        chosenAddress = InetAddress.getByName(givenString);
                        if (!chosenAddress.isSiteLocalAddress()) {
                            // save the trouble of doing a DNS lookup in case of blatantly invalid IP
                            throw new UnknownHostException();
                        }
                    }
                    break;
                } catch (UnknownHostException ex) {
                    String errorMsg = "The given address is not a valid local address: \""
                            + givenString.replace("<", "&gt;")
                            + "\"<br>Expected \"localhost\" or an IPv4 address like 192.168.x.x";
                    ErrorScreen.show("Error", errorMsg, null);
                }
                this.setEnabled(true);
            }
        } else {
            // No, neither:
            // Return server's original address, and hope it works
            chosenAddress = originalAddress;
        }
        this.dispose();
    }//GEN-LAST:event_bContinueActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.chargedminers.launcher.gui.JNiceLookingButton bContinue;
    private javax.swing.ButtonGroup bgChoice;
    private com.chargedminers.launcher.gui.ImagePanel imgErrorIcon;
    private javax.swing.JLabel lMessage;
    private javax.swing.JRadioButton rLocalNetwork;
    private javax.swing.JRadioButton rLocalhost;
    private javax.swing.JRadioButton rNoChange;
    private javax.swing.JCheckBox xRememberChoice;
    // End of variables declaration//GEN-END:variables
}
