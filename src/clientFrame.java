
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author acer
 */
public class clientFrame extends javax.swing.JFrame {
    private static String Chat = "Chat", Game = "Game", draw = "Draw", stateUser = "Username", len = "Array Length", turn = "Player Turn"
            ,coordinate = "Send coordiante", timeOut = "Time Out";
    /**
     * Creates new form clientFrame
     */

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9090;
    
    private ArrayList<String> users;
    private int usersOnline;
    
    String username, typingText;
    PrintWriter output;
    Socket socket;    
    Boolean isConnected = false, isIt, isDraw, isWaitPlayer = true, startGame = false;
    
    Graphics g;
    int currentX = 0, currentY= 0, oldX= 0, oldY= 0, counter;
    
    int serverX = 0, serverY = 0;
    
    int counters = 5;
    
    Timer T = new Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
              counters--;  
               timeLebel.setText(""+counters);          
               if(counters == -1 ) {
                   drawScreen.repaint();
                   counters = 5;
                   if(isDraw) {
                     output.println("time out"+","+timeOut);    
                   }                                 
                   timeLebel.setText(""+counters);
               }
        }
    });
    public class ServerConnection implements Runnable {
    private BufferedReader input;
    private String serverResponse;

    public ServerConnection(Socket s) throws IOException {
        socket = s;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        output.println(username +","+ stateUser);
        try {
            while ((serverResponse = input.readLine()) != null) {
                String temp1[] = serverResponse.split(",");
                int lastIndex = temp1.length - 1;
                
                if(temp1[lastIndex].equals(Chat)){
                    clientArea.append(temp1[0] + ": " + temp1[1] + "\n");
                }
                else if(temp1[lastIndex].equals(stateUser)) {                
                    clientArea.append(temp1[0]+" has joined\n");
           
                }
                
                else if(temp1[lastIndex].equals(len)){
                    usersOnline = Integer.parseInt(temp1[0]);
                    
                } else if(temp1[lastIndex].equals(turn) && usersOnline > 1 ) {
                    clientArea.append(temp1[0]+" turn\n");
                    waitLebel.setText("");
                    ansWord.setText(temp1[1]);
                    startGame = true;
                    T.start();
                    if (username.equals(temp1[0])){
                        clientArea.append("is Draw\n");                      
                        isDraw = true;                    
                    } else {
                        clientArea.append("not Draw\n");    
                        isDraw = false;
                    }
                }
                
                else if(temp1[lastIndex].equals(coordinate)) {
                    serverX = Integer.parseInt(temp1[0]);
                    serverY = Integer.parseInt(temp1[1]);
                    serverDraw(serverX,serverY);

                }
                

                
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
}
    public void serverDraw(int serverX, int serverY) {
        try{
            if(!isDraw) {
            g.fillOval(serverX, serverY, 10, 10);
            }
            
        } catch(Exception e) {
            
        }
    }


    public clientFrame() throws IOException{
        initComponents();
        isDraw = false;
        users = new ArrayList<>();
        clientType.setText("");
        g = drawScreen.getGraphics();
        clientType.setEditable(false);
        sendB.setEnabled(false);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        clientSP = new javax.swing.JScrollPane();
        clientArea = new javax.swing.JTextArea();
        userField = new javax.swing.JTextField();
        enterUser = new javax.swing.JLabel();
        connectB = new javax.swing.JButton();
        clientType = new javax.swing.JTextField();
        sendB = new javax.swing.JButton();
        drawScreen = new javax.swing.JPanel();
        waitLebel = new javax.swing.JLabel();
        redB = new javax.swing.JButton();
        clearB = new javax.swing.JButton();
        timeLebel = new javax.swing.JLabel();
        ansWord = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                formPropertyChange(evt);
            }
        });

        clientArea.setColumns(20);
        clientArea.setRows(5);
        clientSP.setViewportView(clientArea);

        userField.setToolTipText("");
        userField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userFieldActionPerformed(evt);
            }
        });

        enterUser.setText("Enter Username: ");

        connectB.setText("Connect To Server");
        connectB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectBActionPerformed(evt);
            }
        });

        clientType.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                clientTypeKeyPressed(evt);
            }
        });

        sendB.setText("Send");
        sendB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendBActionPerformed(evt);
            }
        });

        drawScreen.setBackground(new java.awt.Color(255, 255, 255));
        drawScreen.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                drawScreenMouseDragged(evt);
            }
        });
        drawScreen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                drawScreenMousePressed(evt);
            }
        });
        drawScreen.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                drawScreenPropertyChange(evt);
            }
        });

        waitLebel.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        waitLebel.setText("Waitting For Player...");

        javax.swing.GroupLayout drawScreenLayout = new javax.swing.GroupLayout(drawScreen);
        drawScreen.setLayout(drawScreenLayout);
        drawScreenLayout.setHorizontalGroup(
            drawScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, drawScreenLayout.createSequentialGroup()
                .addContainerGap(264, Short.MAX_VALUE)
                .addComponent(waitLebel, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(246, 246, 246))
        );
        drawScreenLayout.setVerticalGroup(
            drawScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(drawScreenLayout.createSequentialGroup()
                .addGap(247, 247, 247)
                .addComponent(waitLebel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        redB.setText("Red");
        redB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redBActionPerformed(evt);
            }
        });

        clearB.setText("Clear");
        clearB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBActionPerformed(evt);
            }
        });

        timeLebel.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        timeLebel.setText("15");

        ansWord.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        ansWord.setText("Word...");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(drawScreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(redB)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(clearB)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(clientType)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sendB))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(clientSP, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(enterUser)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(connectB))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(timeLebel)
                        .addGap(337, 337, 337)
                        .addComponent(ansWord, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(enterUser)
                            .addComponent(connectB)
                            .addComponent(ansWord)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(timeLebel)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(drawScreen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(clientSP, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(clientType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sendB)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(redB)
                            .addComponent(clearB))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void userFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_userFieldActionPerformed
    
    // Connect to server
    private void connectBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectBActionPerformed
        // TODO add your handling code here:
        clientArea.append("Connecting to server..\n");
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            output = new PrintWriter(socket.getOutputStream(), true);
            ServerConnection connection = new ServerConnection(socket);
                
            username = userField.getText();
                                   
            userField.setEditable(false);
            connectB.setEnabled(false);
            clientType.setEditable(true);
            sendB.setEnabled(true);
                     
            clientArea.append("My name " + username +"\n");
            new Thread(connection).start();

        } catch (IOException ex) {
            Logger.getLogger(clientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

       
    }//GEN-LAST:event_connectBActionPerformed
    
    // Send Button here
    private void sendBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendBActionPerformed
        // TODO add your handling code here:
            sendData_chat();
    }//GEN-LAST:event_sendBActionPerformed

    private void clientTypeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clientTypeKeyPressed
        // TODO add your handling code here:
        if(clientType.getText() != "") {
            if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                sendData_chat();

            }
            
        }
    }//GEN-LAST:event_clientTypeKeyPressed

    private void redBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redBActionPerformed
        // TODO add your handling code here:
        g.setColor(Color.red);
    }//GEN-LAST:event_redBActionPerformed

    private void clearBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBActionPerformed
        // TODO add your handling code here:
        drawScreen.repaint();
    }//GEN-LAST:event_clearBActionPerformed

    private void drawScreenMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_drawScreenMousePressed
        // TODO add your handling code here:
        oldX = evt.getX();
        oldY = evt.getY();
        
        
    }//GEN-LAST:event_drawScreenMousePressed
    
    private void drawScreenMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_drawScreenMouseDragged
        // TODO add your handling code here:
        currentX = evt.getX();
        currentY = evt.getY();
        
        try{
            if(g != null && isDraw && startGame) {       
              g.fillOval(oldX, oldY, 10, 10);
              
              output.println(currentX + "," + currentY + "," + coordinate);
              
              oldX = currentX;
              oldY = currentY;
          }  
        }
        catch(Exception e) {
            
        }
        
    }//GEN-LAST:event_drawScreenMouseDragged
    

    
    private void drawScreenPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_drawScreenPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_drawScreenPropertyChange

    private void formPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_formPropertyChange
        // TODO add your handling code here:
        if(startGame) {
           T.start(); 
        }
        
        
        
    }//GEN-LAST:event_formPropertyChange
    
    
    
    private void sendData_chat() {
            typingText = clientType.getText();
            output.println(username + "," + typingText + "," + Chat);
            clientType.setText("");
            clientType.requestFocus();
    }
    
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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(clientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(clientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(clientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(clientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new clientFrame().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(clientFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ansWord;
    private javax.swing.JButton clearB;
    private javax.swing.JTextArea clientArea;
    private javax.swing.JScrollPane clientSP;
    private javax.swing.JTextField clientType;
    private javax.swing.JButton connectB;
    private javax.swing.JPanel drawScreen;
    private javax.swing.JLabel enterUser;
    private javax.swing.JButton redB;
    private javax.swing.JButton sendB;
    private javax.swing.JLabel timeLebel;
    private javax.swing.JTextField userField;
    private javax.swing.JLabel waitLebel;
    // End of variables declaration//GEN-END:variables
}
