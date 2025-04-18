/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package schoolzone.gui;
/**
 *
 * @author ardau
 */
public class MainServiceLauncher extends javax.swing.JFrame {

    /**
     * Creates new form MainServiceLauncher
     */
    public MainServiceLauncher() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelTitle = new javax.swing.JLabel();
        btnTrafficService = new javax.swing.JButton();
        labelSubtitle = new javax.swing.JLabel();
        btnPedestrianService = new javax.swing.JButton();
        btnSpeedService = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        labelTitle.setText("  SchoolZone Control Panel");

        btnTrafficService.setText("Traffic Signal Service");
        btnTrafficService.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTrafficServiceActionPerformed(evt);
            }
        });

        labelSubtitle.setText("Select a service to start:");

        btnPedestrianService.setText("Pedestrian Crossing");
        btnPedestrianService.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPedestrianServiceActionPerformed(evt);
            }
        });

        btnSpeedService.setText("Speed Enforcement");
        btnSpeedService.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSpeedServiceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(105, 105, 105)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(labelTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                    .addComponent(btnTrafficService, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnPedestrianService, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSpeedService, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelSubtitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(143, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(labelTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelSubtitle)
                .addGap(18, 18, 18)
                .addComponent(btnTrafficService)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnPedestrianService)
                .addGap(12, 12, 12)
                .addComponent(btnSpeedService)
                .addContainerGap(132, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTrafficServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTrafficServiceActionPerformed
        // TODO add your handling code here:
        new TrafficSignalServiceGUI().setVisible(true);
    }//GEN-LAST:event_btnTrafficServiceActionPerformed

    private void btnPedestrianServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPedestrianServiceActionPerformed
        // TODO add your handling code here:
        new PedestrianCrossingServiceGUI().setVisible(true);
    }//GEN-LAST:event_btnPedestrianServiceActionPerformed

    private void btnSpeedServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSpeedServiceActionPerformed
        // TODO add your handling code here:
        new SpeedEnforcementServiceGUI().setVisible(true);
    }//GEN-LAST:event_btnSpeedServiceActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainServiceLauncher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MainServiceLauncher().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnPedestrianService;
    private javax.swing.JButton btnSpeedService;
    private javax.swing.JButton btnTrafficService;
    private javax.swing.JLabel labelSubtitle;
    private javax.swing.JLabel labelTitle;
    // End of variables declaration//GEN-END:variables
}
