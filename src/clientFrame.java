
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
// Mansea
public class clientFrame extends javax.swing.JFrame {
    private static String Chat = "Chat", stateUser = "Username", len = "Array Length", turn = "Player Turn",
            coordinate = "Send coordiante", timeOut = "Time Out", isWin = "Who Win", clearPaint = "Clear Painting",
            defaultColor = "black", Exit = "Exit";
    /**
     * Creates new form clientFrame
     */

    private String SERVER_IP = "";
    private static final int SERVER_PORT = 9090;

    private ArrayList<String> users;
    private int usersOnline;

    String username, typingText, paintColor, serverColor;
    private ArrayList<String> ansLst, cluLst;
    private static String clueWord, ansWord;
    PrintWriter output;
    Socket socket;
    Boolean isConnected = false, isIt, isDraw = true, isWaitPlayer = true, startGame = false, 
            disconnect = false, spectetor = false;

    Graphics g;
    int currentX = 0, currentY = 0, oldX = 0, oldY = 0, counter;

    int serverX = 0, serverY = 0;

    int counters = 60, clue_word1, clue_word2;
    Random rand;

    Font font;
    Color color;
    int xx, yy;

    /** Countdown Timer **/
    Timer T = new Timer(1001, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            counters--;
            timeLebel.setText("" + counters);
            if(usersOnline < 2) {
                isWaitPlayer = true;
                timeLebel.setText("");
                ansLebel.setText("");
                waitLebel.setText("Waiting For Player...");
            }
            switch (counters) {
                case -1:
                    counters = 60;
                    drawScreen.repaint();
                    g.setColor(Color.black);
                    

                    
                    if (isConnected && !spectetor) {
                        clientArea.append("The answer word is '" + ansWord + "'\n");
                    }
                    if (isDraw) {
                        output.println("time out" + "," + timeOut);
                    }
                    timeLebel.setText("" + counters);
                    break;
                case 40:
                    if (!isDraw) {
                        swap(ansLst, cluLst, clue_word1);
                        ansLebel.setText(arrToString(cluLst));
                    }
                    break;
                case 15:
                    if (!isDraw) {
                        swap(ansLst, cluLst, clue_word2);
                        ansLebel.setText(arrToString(cluLst));
                    }
                    break;
                default:
                    break;
            }
        }
    });

    /** Client use for connect to server **/
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

            output.println(username + "," + stateUser);

            try {
                while ((serverResponse = input.readLine()) != null) {
                    String temp1[] = serverResponse.split(",");
                    int lastIndex = temp1.length - 1;

                    if (temp1[lastIndex].equals(Chat)) {
                        clientArea.append(temp1[0] + ": " + temp1[1] + "\n");
                    }

                    else if (temp1[lastIndex].equals(stateUser)) {
                        if (temp1[0].equals(username)) {
                            isConnected = true;
                        } else {
                            clientArea.append(temp1[0] + " has joined\n");
                        }
                        

                    }

                    else if (temp1[lastIndex].equals(len)) {
                        usersOnline = Integer.parseInt(temp1[0]);
                        onlineLebel.setText(temp1[0]);

                        if (ansWord == null && usersOnline > 2) {
                            waitLebel.setText("Waiting For The Next Turn..");
                            spectetor = true;
                        }

                    }

                    else if (temp1[lastIndex].equals(turn) && usersOnline >= 2) {
                        counters = 60;
                        if(spectetor) {
                            spectetor = false;
                        }
                        g.setColor(Color.black);                       
                        repaintDraw();
                        turnLebel.setText(temp1[0] + "'s Turn");
                        ansWord = temp1[1];

                        ansLst = splitString(ansWord);
                        clueWord = repeat(ansWord.length(), "_");
                        cluLst = splitString(clueWord);
                        clue_word1 = Integer.parseInt(temp1[2]);
                        clue_word2 = Integer.parseInt(temp1[3]);
                        clientArea.append(temp1[0] + " Turn To Draw\n");
                        waitLebel.setText("");

                        startGame = true;
                        
                        T.start();

                        if (username.equals(temp1[0])) {
                            ansLebel.setText(ansWord);
                            isDraw = true;
                        } else {
                            ansLebel.setText(arrToString(cluLst));
                            isDraw = false;
                        }
                    }

                    else if (temp1[lastIndex].equals(coordinate)) {
                        serverX = Integer.parseInt(temp1[0]);
                        serverY = Integer.parseInt(temp1[1]);
                        serverColor = temp1[2];
                        
                        if(!spectetor) {
                          serverDraw(serverX, serverY, serverColor);  
                        }                        

                    }

                    else if (temp1[lastIndex].equals(isWin)) {
                        if (!disconnect) {
                            clientArea.append("The answer word is '" + ansWord + "'\n");
                            clientArea.append(temp1[0] + temp1[1] + "\n");

                        }

                    }

                    else if (temp1[lastIndex].equals(clearPaint)) {
                        repaintDraw();
                    }

                    else if (temp1[lastIndex].equals(defaultColor)) {
                        paintColor = defaultColor;
                    }

                    else if (temp1[lastIndex].equals(Exit)) {
                        disconnect = true;
                        clientArea.append(temp1[0] + " has left\n");
                    }

                }
            } catch (IOException e) {

            } finally {
                try {
                    input.close();
                } catch (IOException e) {

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

    /** Set string **/
    public static String repeat(int count, String with) {
        return new String(new char[count]).replace("\0", with);
    }

    /** Swap Index **/
    public static void swap(ArrayList<String> a, ArrayList<String> b, int num) {
        String tempA = a.get(num);
        String tempB = b.get(num);
        a.remove(num);
        a.add(num, tempB);
        b.remove(num);
        b.add(num, tempA);
    }

    /** Receive x,y from server to draw in drawScreen **/
    public void serverDraw(int serverX, int serverY, String serverColor) {
        try {
            if (!isDraw) {
                try {
                    Field field = Color.class.getField(serverColor);
                    color = (Color) field.get(null);
                } catch (Exception e) {
                    color = null; // Not defined
                }
                g.setColor(color);
                g.fillOval(serverX, serverY, 10, 10);

            }

        } catch (Exception e) {

        }
    }

    public clientFrame() throws IOException {
        initComponents();
        setIcon();
        
        clientArea.setEditable(false);
        setFont();       
        isDraw = false;
        users = new ArrayList<>();
        clientType.setText("");
        g = drawScreen.getGraphics();
        clientType.setEditable(false);

    }
    
    /** Set Icon **/
    public void setIcon() {
        setIconImage(Toolkit.getDefaultToolkit().getImage((getClass().getResource("icon3.png"))));
    }

    /** Set Font In Chat **/
    public void setFont() {
        font = new Font("Verdana", Font.BOLD, 12);
        clientArea.setFont(font);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollBar1 = new javax.swing.JScrollBar();
        jScrollBar2 = new javax.swing.JScrollBar();
        jComboBox1 = new javax.swing.JComboBox<>();
        jPanel6 = new javax.swing.JPanel();
        connectB = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        userField = new javax.swing.JTextField();
        ipLebel = new javax.swing.JLabel();
        ipField = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        turnLebel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        onlineLebel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
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
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
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
        eraserB = new javax.swing.JLabel();
        bg = new javax.swing.JLabel();

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

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

        jPanel6.setBackground(new java.awt.Color(102, 153, 255));

        connectB.setText("Connect");
        connectB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectBActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tempus Sans ITC", 1, 23)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(51, 51, 51));
        jLabel2.setText("Username");

        userField.setFont(new java.awt.Font("Leelawadee", 0, 18)); // NOI18N
        userField.setToolTipText("");
        userField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userFieldActionPerformed(evt);
            }
        });

        ipLebel.setFont(new java.awt.Font("Tempus Sans ITC", 1, 23)); // NOI18N
        ipLebel.setForeground(new java.awt.Color(51, 51, 51));
        ipLebel.setText("IP Address");

        ipField.setFont(new java.awt.Font("Leelawadee", 0, 18)); // NOI18N
        ipField.setToolTipText("");
        ipField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(16, 16, 16)
                        .addComponent(userField)
                        .addGap(13, 13, 13)
                        .addComponent(connectB, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(ipLebel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ipField, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(16, Short.MAX_VALUE))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ipLebel)
                    .addComponent(ipField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addContainerGap(17, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(connectB, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8))))
        );

        getContentPane().add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 80, 370, 100));

        jPanel4.setBackground(new java.awt.Color(102, 153, 255));

        turnLebel.setFont(new java.awt.Font("Tempus Sans ITC", 1, 24)); // NOI18N
        turnLebel.setForeground(new java.awt.Color(51, 51, 51));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(turnLebel, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(68, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(turnLebel, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7))
        );

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 200, 280, 50));

        jPanel5.setBackground(new java.awt.Color(254, 220, 86));

        onlineLebel.setFont(new java.awt.Font("Tempus Sans ITC", 0, 30)); // NOI18N
        onlineLebel.setText(" ");

        jLabel3.setFont(new java.awt.Font("Tempus Sans ITC", 1, 24)); // NOI18N
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/greenbutt.png"))); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addComponent(onlineLebel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(onlineLebel))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel3)))
                .addContainerGap())
        );

        getContentPane().add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(1210, 200, 90, 50));

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
        waitLebel.setText("Waiting For Connect...");

        javax.swing.GroupLayout drawScreenLayout = new javax.swing.GroupLayout(drawScreen);
        drawScreen.setLayout(drawScreenLayout);
        drawScreenLayout.setHorizontalGroup(
            drawScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, drawScreenLayout.createSequentialGroup()
                .addContainerGap(248, Short.MAX_VALUE)
                .addComponent(waitLebel, javax.swing.GroupLayout.PREFERRED_SIZE, 632, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        drawScreenLayout.setVerticalGroup(
            drawScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, drawScreenLayout.createSequentialGroup()
                .addContainerGap(265, Short.MAX_VALUE)
                .addComponent(waitLebel)
                .addGap(231, 231, 231))
        );

        getContentPane().add(drawScreen, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 890, 540));

        timeLebel.setFont(new java.awt.Font("Kristen ITC", 0, 28)); // NOI18N
        timeLebel.setText("     ");
        timeLebel.setPreferredSize(new java.awt.Dimension(30, 30));
        getContentPane().add(timeLebel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, 40, -1));

        clientArea.setColumns(20);
        clientArea.setRows(5);
        jScrollPane2.setViewportView(clientArea);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(929, 267, 370, 410));

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

        titleExit.setFont(new java.awt.Font("Tahoma", 1, 40)); // NOI18N
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
                .addGap(546, 546, 546)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 560, Short.MAX_VALUE)
                .addComponent(titleExit)
                .addContainerGap())
        );
        titleBarLayout.setVerticalGroup(
            titleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titleBarLayout.createSequentialGroup()
                .addGroup(titleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleExit)
                    .addComponent(jLabel1))
                .addGap(11, 11, 11))
        );

        getContentPane().add(titleBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1319, 60));

        clock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon.png"))); // NOI18N
        getContentPane().add(clock, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        jPanel1.setBackground(new java.awt.Color(12, 52, 132));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Originality by Mansea & Wonyus");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 750, 1320, 30));

        jPanel2.setBackground(new java.awt.Color(102, 153, 255));
        jPanel2.setForeground(new java.awt.Color(51, 153, 255));

        ansLebel.setFont(new java.awt.Font("Leelawadee", 0, 40)); // NOI18N
        ansLebel.setForeground(new java.awt.Color(51, 51, 51));
        ansLebel.setLabelFor(this);
        ansLebel.setText("            ");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(280, Short.MAX_VALUE)
                .addComponent(ansLebel, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(230, 230, 230))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(ansLebel)
                .addGap(0, 1, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 80, 860, 50));

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

        eraserB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/eraser.png"))); // NOI18N
        eraserB.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        eraserB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                eraserBMouseClicked(evt);
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(eraserB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(garbageB)
                .addGap(18, 18, 18)
                .addComponent(clientType, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(garbageB)
                    .addComponent(eraserB))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 690, 1280, 40));

        bg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bg.jpg"))); // NOI18N
        getContentPane().add(bg, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, -1, 730));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void eraserBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_eraserBMouseClicked
        // TODO add your handling code here:
        paintColor = "white";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color) field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }
        g.setColor(color);
    }//GEN-LAST:event_eraserBMouseClicked

    private void ipFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipFieldActionPerformed

    private void userFieldActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_userFieldActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_userFieldActionPerformed

    // Connect to server
    private void connectBActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_connectBActionPerformed
        // TODO add your handling code here:
        clientArea.append("Connecting to server...\n");
        try {
            SERVER_IP = ipField.getText();
            socket = new Socket(SERVER_IP, SERVER_PORT);
            output = new PrintWriter(socket.getOutputStream(), true);
            ServerConnection connection = new ServerConnection(socket);

            username = userField.getText();            
            ipField.setEditable(false);
            userField.setEditable(false);
            connectB.setEnabled(false);
            clientType.setEditable(true);
            waitLebel.setText("Waiting For Player...");
            clientArea.append("My name " + username + "\n");                     
            new Thread(connection).start();

        } catch (IOException ex) {
            clientArea.append("Server is not responding...\n");

        }

    }

    private void clientTypeKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_clientTypeKeyPressed
        // TODO add your handling code here:
        if (clientType.getText() != "") {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                sendData_chat();

            }

        }
    }

    private void redBActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_redBActionPerformed
        // TODO add your handling code here:
        paintColor = "red";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color) field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }
        g.setColor(color);
    }

    private void drawScreenMousePressed(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_drawScreenMousePressed
        // TODO add your handling code here:
        oldX = evt.getX();
        oldY = evt.getY();

    }

    private void drawScreenMouseDragged(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_drawScreenMouseDragged
        // TODO add your handling code here:
        currentX = evt.getX();
        currentY = evt.getY();

        try {
            if (g != null && isDraw && startGame) {
                g.fillOval(oldX, oldY, 10, 10);

                output.println(currentX + "," + currentY + "," + paintColor + "," + coordinate);

                oldX = currentX;
                oldY = currentY;

            }
        } catch (Exception e) {

        }

    }

    private void formPropertyChange(java.beans.PropertyChangeEvent evt) {// GEN-FIRST:event_formPropertyChange
        // TODO add your handling code here:
        if (startGame) {
            T.start();
        }
    }

    private void blueBActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_blueBActionPerformed
        // TODO add your handling code here:
        paintColor = "blue";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color) field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }
        g.setColor(color);
    }

    private void titleBarMousePressed(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_titleBarMousePressed
        // TODO add your handling code here:
        xx = evt.getX();
        yy = evt.getY();
    }

    private void titleBarMouseDragged(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_titleBarMouseDragged

        // Drag Window Program
        int x = evt.getXOnScreen();
        int y = evt.getYOnScreen();
        this.setLocation(x - xx, y - yy);
    }

    private void titleExitMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_titleExitMouseClicked
        // TODO add your handling code here:

        if (startGame) {
            try {
                if(isDraw) {
                    output.println("time out" + "," + timeOut);
                }
                output.println(username + "," + Exit);
                output.close();
                socket.close();
                System.exit(0);
            } catch (IOException e) {
                // TODO Auto-generated catch block
//                e.printStackTrace();
            }
        } else if (isConnected) {
            output.println(username + "," + Exit);
            System.exit(0);
        } else {
            System.exit(0);
        }

    }

    private void lightGrayBActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_lightGrayBActionPerformed
        // TODO add your handling code here:
        paintColor = "gray";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color) field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }
        g.setColor(color);
    }

    private void blackBActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_blackBActionPerformed
        // TODO add your handling code here:
        paintColor = "black";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color) field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }
        g.setColor(color);
    }

    private void pinkBActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_pinkBActionPerformed
        // TODO add your handling code here:
        paintColor = "pink";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color) field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }
        g.setColor(color);
    }

    private void orangeBActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_orangeBActionPerformed
        // TODO add your handling code here:
        paintColor = "orange";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color) field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }
        g.setColor(color);
    }

    private void yellowBActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_yellowBActionPerformed
        // TODO add your handling code here:
        paintColor = "yellow";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color) field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }
        g.setColor(color);
    }

    private void greenBActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_greenBActionPerformed
        // TODO add your handling code here:
        paintColor = "green";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color) field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }
        g.setColor(color);
    }

    private void magnetaBActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_magnetaBActionPerformed
        // TODO add your handling code here:
        paintColor = "magenta";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color) field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }
        g.setColor(color);
    }

    private void cyanBActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cyanBActionPerformed
        // TODO add your handling code here:
        paintColor = "cyan";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color) field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }
        g.setColor(color);
    }

    private void dGBActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_dGBActionPerformed
        // TODO add your handling code here:
        paintColor = "darkGray";
        try {
            Field field = Color.class.getField(paintColor);
            color = (Color) field.get(null);
        } catch (Exception e) {
            color = null; // Not defined
        }
        g.setColor(color);
    }

    private void garbageBMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_garbageBMouseClicked
        // TODO add your handling code here:
        if (isDraw) {
            repaintDraw();
            output.println("piant ja" + "," + clearPaint);
        }

    }

    /** Clear all Paint **/
    public void repaintDraw() {
        drawScreen.repaint();
    }

    /** Receive x,y from server to draw in drawScreen **/
    private void sendData_chat() {
        typingText = clientType.getText();
        output.println(username + "," + typingText + "," + Chat);
        clientType.setText("");
        try {
            if (typingText.equals(ansWord) && !(isDraw)) {

                output.println(username + "," + isWin);
                output.println("time out" + "," + timeOut);
            }
        } catch (Exception e) {

        }
        clientType.requestFocus();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(clientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null,
                    ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(clientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null,
                    ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(clientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null,
                    ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(clientFrame.class.getName()).log(java.util.logging.Level.SEVERE, null,
                    ex);
        }
        // </editor-fold>

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
    private javax.swing.JLabel eraserB;
    private javax.swing.JLabel garbageB;
    private javax.swing.JButton greenB;
    private javax.swing.JTextField ipField;
    private javax.swing.JLabel ipLebel;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollBar jScrollBar1;
    private javax.swing.JScrollBar jScrollBar2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton lightGrayB;
    private javax.swing.JButton magnetaB;
    private javax.swing.JLabel onlineLebel;
    private javax.swing.JButton orangeB;
    private javax.swing.JButton pinkB;
    private javax.swing.JButton redB;
    private javax.swing.JLabel timeLebel;
    private javax.swing.JPanel titleBar;
    private javax.swing.JLabel titleExit;
    private javax.swing.JLabel turnLebel;
    private javax.swing.JTextField userField;
    private javax.swing.JLabel waitLebel;
    private javax.swing.JButton yellowB;
    // End of variables declaration//GEN-END:variables
}
