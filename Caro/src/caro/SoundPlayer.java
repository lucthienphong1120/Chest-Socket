package caro;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.swing.JOptionPane;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class SoundPlayer {

    private FileInputStream FIS;
    private Player myPlayer;

    public void playSound(String path) {
        try {
            FIS = new FileInputStream(path);
            myPlayer = new Player(FIS);
            new Thread(
                    new Runnable() {
                @Override
                public void run() {
                    try {
                        myPlayer.play();
                    } catch (JavaLayerException e) {
                    }
                }
            }
            ).start();
        } catch (FileNotFoundException | JavaLayerException e) {
            JOptionPane.showInputDialog(e.getMessage());
        }
    }
}
