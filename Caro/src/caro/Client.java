package caro;

import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/*
 * Client gửi các tin nhắn đến server: 
    MOVE
    QUIT
 * Client tiếp nhận thông tin từ Server: 
    MESSAGE
    VICTORY / DEFEAT /TIE
 */
public class Client {

    /*
     * Các thuộc tính đồ họa của game
     */
    private JLabel messageLabel = new JLabel("");
    private String URLIcon;
    private String URLOpenentICon;
    public ImagePanel background = new ImagePanel("img/main.png", 0, 0, 800, 600);
    public ImagePanel waitingPicture = new ImagePanel("img/waiting2.png", 500, 350, 300, 225);
    public ImagePanel attackPicture = new ImagePanel("img/attack1.png", 500, 350, 300, 225);
    public ImagePanel waitingConnectPicture = new ImagePanel("img/WaitingConnect.png", 500, 350, 300, 225);
    private ImagePanel[][] board = new ImagePanel[16][16];
    private ImagePanel currentSquare;
    public static BackButton myBackButton;

    public SoundPlayer mySoundPlayer = new SoundPlayer();
    /*
      Các thuộc tính kết nối server:
     */
    private static int PORT = 8901;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    /*
     * Các thuộc tính xét trạng thái game
     */
    public static boolean isStartGame;
    char mark;

    public Client(String serverAddress) {
        try {
            isStartGame = false;
            // Setup networking
            socket = new Socket(serverAddress, PORT);
            input = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception ex) {
        }

        // Layout GUI
        ImagePanel messagePanel = new ImagePanel("img/message.png", 20, 500, 480, 40);
        messageLabel.setBounds(20, 4, 300, 30);
        messagePanel.add(messageLabel);
        background.add(messagePanel);

        myBackButton = new BackButton("towLanPlayerPanel");
        myBackButton.setEnabled(false);
        background.add(myBackButton);
        background.repaint();
        // thêm vào waitting picture 

        background.add(waitingConnectPicture);

        ImagePanel boardPanel = new ImagePanel("img/table.png", 20, 20, 480, 480);

        boardPanel.setLayout(null);
        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 16; col++) {
                final int k = row;
                final int l = col;
                board[row][col] = new ImagePanel("img/khung.png", col * 30, row * 30, 30, 30);
                board[row][col].repaint();
                background.repaint();
                board[row][col].addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // play sound 
                        if (GamePanel.canPlaySound) {
                            mySoundPlayer.playSound("sound/kick.mp3");
                        }
                        currentSquare = board[k][l];
                        System.out.println("Client : isStartGame = " + isStartGame);
                        if (isStartGame) {
                            System.out.println("MOVE " + (k * 16 + l));
                            output.println("MOVE " + (k * 16 + l));
                        }
                    }
                });
                boardPanel.add(board[row][col]);
                board[row][col].repaint();
                background.repaint();
            }
        }
        background.add(boardPanel);
    }

    /* phương thức của Client để lắng nghe tin nhắn từ Server.
     *Tin nhắn đầu tiên là "WELCOME" và đồng thời nhận được kí tự đánh dấu.
      *Sử dụng vòng lặp vô hạn để lắng nghe các tin nhắn "VALID_MOVE",
      "OPPONENT_MOVED", "VICTORY", "DEFEAT", "TIE", "OPPONENT_QUIT hoặc "MESSAGE".
     *Các tin "VICTORY", "DEFEAT" và "TIE" là các tính hiệu kết thúc game. Khi đó, 
    chương trình hỏi người chơi có muốn chơi lại hay không. Nếu không thì Server
    gửi tin nhắn "QUIT" để thoát game và "OPPONENT_QUIT" để đối thủ cũng thoát game.       
     */
    public void play() {
        String response;
        try {
            response = input.readLine();
            if (response.startsWith("WELCOME")) {
                mark = response.charAt(8);
                URLIcon = (mark == 'X' ? "img/khung1.png" : "img/khung2.png");
                URLOpenentICon = (mark == 'X' ? "img/khung2.png" : "img/khung1.png");

                if (mark == 'X') {
                    waitingPicture.setPicture("img/waiting2.png");
                    attackPicture.setPicture("img/attack1.png");
                } else if (mark == 'O') {
                    waitingPicture.setPicture("img/waiting1.png");
                    attackPicture.setPicture("img/attack2.png");

                    isStartGame = true;
                    background.remove(waitingConnectPicture);
                    background.add(waitingPicture);
                }
            }
            while (true) {
                response = input.readLine();
                background.repaint();
                Main.myFrame.repaint();
                if (response.startsWith("VALID_MOVE")) {
                    currentSquare.setPicture(URLIcon);
                    background.remove(attackPicture);
                    background.add(waitingPicture);

                    messageLabel.setText("Bạn đã đi, đợi tí");
                    background.repaint();
                    Main.myFrame.repaint();

                } else if (response.startsWith("OPPONENT_MOVED")) {
                    background.remove(waitingPicture);
                    background.add(attackPicture);

                    int location = Integer.parseInt(response.substring(15));
                    int row = location / 16;
                    int col = location % 16;
                    board[row][col].setPicture(URLOpenentICon);
                    background.repaint();
                    Main.myFrame.repaint();
                    myBackButton.setEnabled(true); // kích hoạt nút thoát ra 
                    messageLabel.setText("Đối thủ đã đi, đến lượt bạn");

                } else if (response.startsWith("VICTORY")) {
                    // âm thanh 
                    if (GamePanel.canPlaySound) {
                        mySoundPlayer.playSound("sound/win.mp3");
                    }

                    // bảng thông báo
                    Icon myIcon = new ImageIcon(getClass().getResource("img/winner" + (mark == 'X' ? '1' : '2') + ".gif"));
                    JOptionPane.showMessageDialog(null, null, "Chiến thắng!", JOptionPane.INFORMATION_MESSAGE, myIcon);

                    messageLabel.setText("Bạn giành được thắng lợi");
                    Main.myFrame.repaint();
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("Bạn đã bị đánh bại");
                    // âm thanh 
                    if (GamePanel.canPlaySound) {
                        mySoundPlayer.playSound("sound/GameOver.mp3");
                    }

                    // bảng thông báo
                    Icon myIcon = new ImageIcon(getClass().getResource("img/loser" + (mark == 'X' ? '1' : '2') + ".gif"));
                    JOptionPane.showMessageDialog(null, null, "Thua!", JOptionPane.INFORMATION_MESSAGE, myIcon);

                    Main.myFrame.repaint();
                    break;
                } else if (response.startsWith("TIE")) {
                    messageLabel.setText("Hòa nhau");
                    Main.myFrame.repaint();
                    break;
                } else if (response.startsWith("MESSAGE")) {

                    switch (response) {
                        case "MESSAGE First turn":
                            background.remove(waitingConnectPicture);
                            background.add(attackPicture);
                            messageLabel.setText("Game đã bắt đầu. Bạn hãy đi một ô");
                            break;
                        case "MESSAGE All is connected":
                            isStartGame = true;
                            messageLabel.setText("Tất cả người chơi đã kết nối");
                            break;
                        case "MESSAGE This is not your turn":
                            messageLabel.setText("Chưa đến lượt chơi của bạn. Đợi tí");
                            break;
                        case "MESSAGE Wait another connects":
                            messageLabel.setText("Đợi người chơi khác kết nối");
                            break;
                        default:
                            messageLabel.setText(response.substring(8));
                            break;
                    }
                    Main.myFrame.repaint();
                }
            }
            output.println("QUIT");
        } catch (HeadlessException | IOException | NumberFormatException ex) {

        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
            }
        }
    }

    /*
     * The methord: Do you want restart game?
     */
    public boolean wantsToPlayAgain() {
        int response = JOptionPane.showConfirmDialog(null,
                "Bạn muốn chơi lại lần nữa không ?",
                "Game Carô",
                JOptionPane.YES_NO_OPTION);
        isStartGame = true;
        return response == JOptionPane.YES_OPTION;
    }

    /**
     * Runs the client as an application.
     */
}
