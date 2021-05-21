
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
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
    private static String Chat = "Chat", stateUser = "Username", len = "Array Length", turn = "Player Turn"
            ,coordinate = "Send coordiante", timeOut = "Time Out", isWin = "Who Win", clearPaint = "Clear Painting"
            ,disconnect = "Disconnected";
    /**
     * Creates new form clientFrame
     */

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9090;
    
    private ArrayList<String> users;
    private int usersOnline;
    
    String username, typingText, paintColor, serverColor;
    private ArrayList<String> ansLst, cluLst;
    private static String clueWord, ansWord;
    PrintWriter output;
    Socket socket;    
    Boolean isConnected = false, isIt, isDraw = true, isWaitPlayer = true, startGame = false;
    
    Graphics g;
    int currentX = 0, currentY= 0, oldX= 0, oldY= 0, counter;
    
    int serverX = 0, serverY = 0;
    
    int counters = 20, oldRand;
    Random rand;
    
    Font font;
    Color color;
    int xx,yy;
    
    Timer T = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            counters--;
            timeLebel.setText(""+counters);
            if(isDraw) {
                clientType.setEditable(false);
            } else if(!isDraw) {
                clientType.setEditable(true);
            }
            switch (counters) {
                case -1:
                    drawScreen.repaint();
                    g.setColor(Color.black);
                    clientArea.append("The answer word is '"+ansWord+"'\n");
                    counters = 20;
                    if(isDraw) {
                        output.println("time out"+","+timeOut);
                    }
         
                    timeLebel.setText(""+counters);
                    break;
                case 15:
                    if(!isDraw) {
                        rand = new Random();
                        oldRand = rand.nextInt(ansLst.size()-1);
                        swap(ansLst,cluLst,oldRand);
                        ansLebel.setText(arrToString(cluLst));
                    }
                    break;
                case 10:
                    if(!isDraw) {  
                        rand = new Random();
                        int randNum = rand.nextInt(ansLst.size()-1);
                        while(oldRand == randNum) {
                            randNum = rand.nextInt(ansLst.size()-1);
                        }
                        swap(ansLst,cluLst,randNum);
                        ansLebel.setText(arrToString(cluLst));
                    }
                    break;
                default:
                    break;
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
                    ansWord = temp1[1];
                    counters = 20;

                    ansLst = splitString(ansWord);    
                    clueWord = repeat(ansWord.length(), "_");
                    cluLst = splitString(clueWord);
                    
                    clientArea.append(temp1[0]+" Turn To Draw\n");
                    waitLebel.setText("");

                    startGame = true;
                    T.start();

                    if (username.equals(temp1[0])){
                        ansLebel.setText(ansWord);                   
                        isDraw = true;                    
                    } else {
                        ansLebel.setText(arrToString(cluLst));   
                        isDraw = false;
                    }
                }
                
                else if(temp1[lastIndex].equals(coordinate)) {
                    serverX = Integer.parseInt(temp1[0]);
                    serverY = Integer.parseInt(temp1[1]);
                    serverColor = temp1[2];
                    serverDraw(serverX,serverY,serverColor);

                }
                
                else if(temp1[lastIndex].equals(isWin)) {
                    clientArea.append(temp1[0] + temp1[1] +"\n");
                    clientArea.append("The answer word is '"+ansWord+"'\n");
                }
                
                else if(temp1[lastIndex].equals(clearPaint)) {
                    repaintDraw();                    
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
    
    /** Convert Array To String **/
    public static String arrToString(ArrayList<String> arr) {
        String tempString = "";
        for (String s : arr) {
            tempString += s + " ";
        }
        return tempString;
    }

    /** Split String To ArrayLists **/
    public static ArrayList<String> splitString(String word) {
        String[] tempArr;
        tempArr = word.split("");
        java.util.List<String> fixedLenghtList = Arrays.asList(tempArr);
        ArrayList<String> arrLst = new ArrayList<>(fixedLenghtList);
        return arrLst;

    }
    
    public static String repeat(int count, String with) {
        return new String(new char[count]).replace("\0", with);
    }
    
    
    public static void swap(ArrayList<String> a,ArrayList<String> b, int num) {
        String tempA = a.get(num);
        String tempB = b.get(num);
        a.remove(num);
        a.add(num, tempB);
        b.remove(num);
        b.add(num, tempA);  
    }
    public void serverDraw(int serverX, int serverY,String serverColor) {
        try{
            if(!isDraw) {
             try {
                Field field = Color.class.getField(serverColor);
                color = (Color)field.get(null);
            } catch (Exception e) {
                color = null; // Not defined
            }       
            g.setColor(color);
            g.fillOval(serverX, serverY, 10, 10);
            
            }
            
        } catch(Exception e) {
            
        }
    }


    public clientFrame() throws IOException{
        initComponents();

        clientArea.setEditable(false);
        setFont();     
        isDraw = false;
        users = new ArrayList<>();
        clientType.setText("");
        g = drawScreen.getGraphics();
        clientType.setEditable(false);

    }
    

    
    public void setFont() {
        font = new Font("Verdana",Font.BOLD,12);
        clientArea.setFont(font);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        drawScreen = new javax.swing.JPanel();
        waitLebel = new javax.swing.JLabel();
        timeLebel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        clientArea = new javax.swing.JTextArea();
        titleBar = new javax.swing.JPanel();
        titleExit = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        clock = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        userField = new javax.swing.JTextField();
        connectB = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        ansLebel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        garbageB = new javax.swing.JLabel();
        blueB = new javax.swing.JButton();
        cyanB = new javax.swing.JButton();
        magnetaB = new javax.swing.JButton();
        greenB = new javax.swing.JButton();
        yellowB = new javax.swing.JButton();
        orangeB = new javax.swing.JButton();
        pinkB = new javax.swing.JButton();
        redB = new javax.swing.JButton();
        blackB = new javax.swing.JButton();
        dGB = new javax.swing.JButton();
        lightGrayB = new javax.swing.JButton();
        clientType = new javax.swing.JTextField();
        bg = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 0));
        setUndecorated(true);
        setResizable(false);
        addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                formPropertyChange(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

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

        waitLebel.setFont(new java.awt.Font("Leelawadee", 0, 36)); // NOI18N
        waitLebel.setText("Waiting For Player...");

        javax.swing.GroupLayout drawScreenLayout = new javax.swing.GroupLayout(drawScreen);
        drawScreen.setLayout(drawScreenLayout);
        drawScreenLayout.setHorizontalGroup(
            drawScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, drawScreenLayout.createSequentialGroup()
                .addContainerGap(253, Short.MAX_VALUE)
                .addComponent(waitLebel, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(203, 203, 203))
        );
        drawScreenLayout.setVerticalGroup(
            drawScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, drawScreenLayout.createSequentialGroup()
                .addContainerGap(264, Short.MAX_VALUE)
                .addComponent(waitLebel)
                .addGap(232, 232, 232))
        );

        getContentPane().add(drawScreen, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, -1, 540));

        timeLebel.setFont(new java.awt.Font("Kristen ITC", 0, 28)); // NOI18N
        timeLebel.setText("     ");
        timeLebel.setPreferredSize(new java.awt.Dimension(30, 30));
        getContentPane().add(timeLebel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, 40, -1));

        clientArea.setColumns(20);
        clientArea.setRows(5);
        jScrollPane2.setViewportView(clientArea);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(899, 137, 400, 550));

        titleBar.setBackground(new java.awt.Color(12, 52, 132));
        titleBar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        titleBar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                titleBarMouseDragged(evt);
            }
        });
        titleBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                titleBarMousePressed(evt);
            }
        });

        titleExit.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        titleExit.setForeground(new java.awt.Color(255, 102, 102));
        titleExit.setText("X");
        titleExit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        titleExit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                titleExitMouseClicked(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tempus Sans ITC", 1, 45)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("sketch.io");

        javax.swing.GroupLayout titleBarLayout = new javax.swing.GroupLayout(titleBar);
        titleBar.setLayout(titleBarLayout);
        titleBarLayout.setHorizontalGroup(
            titleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titleBarLayout.createSequentialGroup()
                .addGap(545, 545, 545)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 563, Short.MAX_VALUE)
                .addComponent(titleExit)
                .addContainerGap())
        );
        titleBarLayout.setVerticalGroup(
            titleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titleBarLayout.createSequentialGroup()
                .addGroup(titleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleExit)
                    .addComponent(jLabel1))
                .addGap(3, 3, 3))
        );

        getContentPane().add(titleBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1319, 50));

        clock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon.png"))); // NOI18N
        getContentPane().add(clock, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        jPanel1.setBackground(new java.awt.Color(12, 52, 132));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1320, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 750, 1320, 30));

        jPanel2.setBackground(new java.awt.Color(102, 153, 255));
        jPanel2.setForeground(new java.awt.Color(51, 153, 255));

        userField.setFont(new java.awt.Font("Leelawadee", 0, 18)); // NOI18N
        userField.setToolTipText("");
        userField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userFieldActionPerformed(evt);
            }
        });

        connectB.setText("Connect");
        connectB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectBActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tempus Sans ITC", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(51, 51, 51));
        jLabel2.setText("Username");

        ansLebel.setFont(new java.awt.Font("Leelawadee", 0, 40)); // NOI18N
        ansLebel.setForeground(new java.awt.Color(51, 51, 51));
        ansLebel.setLabelFor(this);
        ansLebel.setText("            ");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(316, Short.MAX_VALUE)
                .addComponent(ansLebel, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(187, 187, 187)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(connectB)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(connectB, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(ansLebel)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 80, 1250, 50));

        jPanel3.setBackground(new java.awt.Color(102, 153, 255));

        garbageB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/garbage.png"))); // NOI18N
        garbageB.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        garbageB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                garbageBMouseClicked(evt);
            }
        });

        blueB.setBackground(java.awt.Color.blue);
        blueB.setForeground(java.awt.Color.blue);
        blueB.setText("      ");
        blueB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blueBActionPerformed(evt);
            }
        });

        cyanB.setBackground(java.awt.Color.cyan);
        cyanB.setForeground(java.awt.Color.gray);
        cyanB.setText("      ");
        cyanB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cyanBActionPerformed(evt);
            }
        });

        magnetaB.setBackground(java.awt.Color.magenta);
        magnetaB.setForeground(java.awt.Color.gray);
        magnetaB.setText("      ");
        magnetaB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                magnetaBActionPerformed(evt);
            }
        });

        greenB.setBackground(java.awt.Color.green);
        greenB.setForeground(java.awt.Color.gray);
        greenB.setText("      ");
        greenB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                greenBActionPerformed(evt);
            }
        });

        yellowB.setBackground(java.awt.Color.yellow);
        yellowB.setForeground(java.awt.Color.gray);
        yellowB.setText("      ");
        yellowB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yellowBActionPerformed(evt);
            }
        });

        orangeB.setBackground(java.awt.Color.orange);
        orangeB.setForeground(java.awt.Color.gray);
        orangeB.setText("      ");
        orangeB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orangeBActionPerformed(evt);
            }
        });

        pinkB.setBackground(java.awt.Color.pink);
        pinkB.setForeground(java.awt.Color.pink);
        pinkB.setText("      ");
        pinkB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pinkBActionPerformed(evt);
            }
        });

        redB.setBackground(java.awt.Color.red);
        redB.setForeground(new java.awt.Color(255, 0, 0));
        redB.setText("      ");
        redB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redBActionPerformed(evt);
            }
        });

        blackB.setBackground(java.awt.Color.black);
        blackB.setText("      ");
        blackB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blackBActionPerformed(evt);
            }
        });

        dGB.setBackground(java.awt.Color.darkGray);
        dGB.setForeground(java.awt.Color.darkGray);
        dGB.setText("      ");
        dGB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dGBActionPerformed(evt);
            }
        });

        lightGrayB.setBackground(java.awt.Color.gray);
        lightGrayB.setForeground(java.awt.Color.gray);
        lightGrayB.setText("      ");
        lightGrayB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lightGrayBActionPerformed(evt);
            }
        });

        clientType.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                clientTypeKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(350, 350, 350)
                                .addComponent(greenB))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(400, 400, 400)
                                .addComponent(magnetaB))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(300, 300, 300)
                                .addComponent(yellowB))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(200, 200, 200)
                                .addComponent(pinkB))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(100, 100, 100)
                                .addComponent(blackB))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(dGB))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(250, 250, 250)
                                .addComponent(orangeB))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(150, 150, 150)
                                .addComponent(redB))
                            .addComponent(lightGrayB))
                        .addGap(49, 49, 49)
                        .addComponent(blueB))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(450, 450, 450)
                        .addComponent(cyanB)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 267, Short.MAX_VALUE)
                .addComponent(garbageB)
                .addGap(30, 30, 30)
                .addComponent(clientType, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(garbageB)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(clientType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(greenB)
                    .addComponent(magnetaB)
                    .addComponent(yellowB)
                    .addComponent(pinkB)
                    .addComponent(blackB)
                    .addComponent(dGB)
                    .addComponent(orangeB)
                    .addComponent(redB)
                    .addComponent(lightGrayB)
                    .addComponent(blueB)
                    .addComponent(cyanB))
                .addContainerGap())
        );

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 690, 1280, 40));

        bg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bg.jpg"))); // NOI18N
        getContentPane().add(bg, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, -1, 730));

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
//            sendB.setEnabled(true);
                     
            clientArea.append("My name " + username +"\n");
            new Thread(connection).start();

        } catch (IOException ex) {
            Logger.getLogger(clientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

       
    }//GEN-LAST:event_connectBActionPerformed
    
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
        paintColor = "red";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color)field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }       
        g.setColor(color);
    }//GEN-LAST:event_redBActionPerformed

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
              
              output.println(currentX + "," + currentY + ","+ paintColor + "," + coordinate);
              
              oldX = currentX;
              oldY = currentY;
              
          }  
        }
        catch(Exception e) {
            
        }
        
    }//GEN-LAST:event_drawScreenMouseDragged
    

    
    private void formPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_formPropertyChange
        // TODO add your handling code here:
        if(startGame) {
           T.start(); 
        }
        
        
        
    }//GEN-LAST:event_formPropertyChange

    private void blueBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blueBActionPerformed
        // TODO add your handling code here:
        paintColor = "blue";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color)field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }       
        g.setColor(color);
    }//GEN-LAST:event_blueBActionPerformed

    private void titleBarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_titleBarMousePressed
        // TODO add your handling code here:
        xx = evt.getX();
        yy = evt.getY();
    }//GEN-LAST:event_titleBarMousePressed

    private void titleBarMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_titleBarMouseDragged

        // Drag Window Program
        int x = evt.getXOnScreen();
        int y = evt.getYOnScreen();
        this.setLocation(x-xx,y-yy);
    }//GEN-LAST:event_titleBarMouseDragged

    private void titleExitMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_titleExitMouseClicked
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_titleExitMouseClicked

    private void lightGrayBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lightGrayBActionPerformed
        // TODO add your handling code here:
        paintColor = "gray";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color)field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }       
        g.setColor(color);
    }//GEN-LAST:event_lightGrayBActionPerformed

    private void blackBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blackBActionPerformed
        // TODO add your handling code here:
        paintColor = "black";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color)field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }       
        g.setColor(color);
    }//GEN-LAST:event_blackBActionPerformed

    private void pinkBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pinkBActionPerformed
        // TODO add your handling code here:
        paintColor = "pink";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color)field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }       
        g.setColor(color);
    }//GEN-LAST:event_pinkBActionPerformed

    private void orangeBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orangeBActionPerformed
        // TODO add your handling code here:
        paintColor = "orange";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color)field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }       
        g.setColor(color);
    }//GEN-LAST:event_orangeBActionPerformed

    private void yellowBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yellowBActionPerformed
        // TODO add your handling code here:
        paintColor = "yellow";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color)field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }       
        g.setColor(color);
    }//GEN-LAST:event_yellowBActionPerformed

    private void greenBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_greenBActionPerformed
        // TODO add your handling code here:
        paintColor = "green";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color)field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }       
        g.setColor(color);
    }//GEN-LAST:event_greenBActionPerformed

    private void magnetaBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_magnetaBActionPerformed
        // TODO add your handling code here:
        paintColor = "magenta";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color)field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }       
        g.setColor(color);
    }//GEN-LAST:event_magnetaBActionPerformed

    private void cyanBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cyanBActionPerformed
        // TODO add your handling code here:
        paintColor = "cyan";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color)field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }       
        g.setColor(color);
    }//GEN-LAST:event_cyanBActionPerformed

    private void dGBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dGBActionPerformed
        // TODO add your handling code here:
        paintColor = "darkGray";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color)field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }       
        g.setColor(color);
    }//GEN-LAST:event_dGBActionPerformed

    private void garbageBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_garbageBMouseClicked
        // TODO add your handling code here:
        if(isDraw) {
          repaintDraw();
          output.println("piant ja" +","+clearPaint);  
        }
        
        
    }//GEN-LAST:event_garbageBMouseClicked
    
    public void repaintDraw(){
       drawScreen.repaint(); 
    }
    
    
    private void sendData_chat() {
            typingText = clientType.getText();
            output.println(username + "," + typingText + "," + Chat);
            clientType.setText("");
            try{
                if(typingText.equals(ansWord)){
                    
                    output.println(username+","+isWin);
                    output.println("time out"+","+timeOut); 
                }               
            }catch(Exception e) {
                
            }
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
    private javax.swing.JLabel ansLebel;
    private javax.swing.JLabel bg;
    private javax.swing.JButton blackB;
    private javax.swing.JButton blueB;
    private javax.swing.JTextArea clientArea;
    private javax.swing.JTextField clientType;
    private javax.swing.JLabel clock;
    private javax.swing.JButton connectB;
    private javax.swing.JButton cyanB;
    private javax.swing.JButton dGB;
    private javax.swing.JPanel drawScreen;
    private javax.swing.JLabel garbageB;
    private javax.swing.JButton greenB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton lightGrayB;
    private javax.swing.JButton magnetaB;
    private javax.swing.JButton orangeB;
    private javax.swing.JButton pinkB;
    private javax.swing.JButton redB;
    private javax.swing.JLabel timeLebel;
    private javax.swing.JPanel titleBar;
    private javax.swing.JLabel titleExit;
    private javax.swing.JTextField userField;
    private javax.swing.JLabel waitLebel;
    private javax.swing.JButton yellowB;
    // End of variables declaration//GEN-END:variables
}
