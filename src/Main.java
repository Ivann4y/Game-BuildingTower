import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Nusantara Tower");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        NusantaraTower gamePanel = new NusantaraTower();
        frame.add(gamePanel);

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH    );
        frame.setVisible(true);

        frame.validate();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gamePanel.stopGame();
            }
        });
    }
}
