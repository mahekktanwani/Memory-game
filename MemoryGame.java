import javax.swing.*;
import java.awt.*;
import java.util.*;
import javax.sound.sampled.*;
import java.io.*;

public class MemoryGame extends JFrame {

    CardLayout cl = new CardLayout();
    JPanel main = new JPanel(cl);

    JButton[] cards = new JButton[36];
    ImageIcon[] icons = new ImageIcon[36];
    ImageIcon[] baseImages = new ImageIcon[8];

    ArrayList<Point> particles = new ArrayList<>();
    javax.swing.Timer bgTimer;

    int first = -1, second = -1;
    boolean canClick = true;
    int matched = 0;
    int moves = 0;
    int timeLeft = 60;
    int streak = 0;

    JLabel timerLabel = new JLabel("Time:60");
    JLabel moveLabel = new JLabel("Moves: 0");
    JLabel playerLabel = new JLabel("");

    javax.swing.Timer timer;
    Clip bgMusic;

    String player = "PLAYER";
    boolean bossMode = false;
    boolean soundOn = true;

    JPanel gameContainer = new JPanel(new BorderLayout());

    public MemoryGame() {

        setTitle("MemoryBoost");
        setSize(720, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        setIconImage(Toolkit.getDefaultToolkit().getImage("gameicon.ico"));

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                cleanExit();
            }
        });

        preloadImages();

        main.add(menuPage(), "menu");
        main.add(levelPage(), "level");
        main.add(gameContainer, "game");

        add(main);
        setVisible(true);

        cl.show(main, "menu");
    }

    void cleanExit() {
        if (timer != null) timer.stop();
        if (bgTimer != null) bgTimer.stop();
        dispose();
        System.exit(0);
    }

    void preloadImages() {
        for (int i = 1; i <= 8; i++) {
            ImageIcon img = new ImageIcon("images/img" + i + ".jpg");
            Image im = img.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            baseImages[i - 1] = new ImageIcon(im);
        }
    }

    JButton btn(String t) {
        JButton b = new JButton(t);
        b.setFocusPainted(false);
        b.setBackground(Color.BLACK);
        b.setForeground(Color.CYAN);
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    JPanel menuPage() {
        JPanel p = new JPanel(null);
        p.setBackground(Color.BLACK);

        JLabel title = new JLabel("⚡ MEMORY BOSS ⚡");
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(Color.CYAN);
        title.setBounds(160, 120, 500, 60);
        p.add(title);

        JTextField name = new JTextField();
        name.setBounds(260, 230, 200, 45);
        p.add(name);

        JButton start = btn("START");
        start.setBounds(260, 300, 200, 50);
        p.add(start);

        JButton leader = btn("LEADERBOARD");
        leader.setBounds(260, 370, 200, 50);
        p.add(leader);

        JButton exit = btn("EXIT");
        exit.setBounds(260, 440, 200, 50);
        p.add(exit);

        start.addActionListener(e -> {
            player = name.getText().trim();
            if (player.equals("")) player = "PLAYER";
            cl.show(main, "level");
        });

        leader.addActionListener(e -> showScore());
        exit.addActionListener(e -> cleanExit());

        return p;
    }

    JPanel levelPage() {
        JPanel p = new JPanel(null);
        p.setBackground(Color.BLACK);

        JLabel l = new JLabel("SELECT LEVEL");
        l.setForeground(Color.CYAN);
        l.setFont(new Font("Arial", Font.BOLD, 35));
        l.setBounds(230, 120, 400, 60);
        p.add(l);

        JButton easy = btn("EASY");
        easy.setBounds(260, 250, 200, 50);
        p.add(easy);

        JButton hard = btn("HARD");
        hard.setBounds(260, 320, 200, 50);
        p.add(hard);

        JButton boss = btn("BOSS 6x6 🔥");
        boss.setBounds(260, 390, 200, 50);
        p.add(boss);

        JButton back = btn("BACK");
        back.setBounds(260, 460, 200, 50);
        p.add(back);

        easy.addActionListener(e -> { timeLeft = 80; bossMode = false; startGameReal(); });
        hard.addActionListener(e -> { timeLeft = 60; bossMode = false; startGameReal(); });
        boss.addActionListener(e -> { timeLeft = 50; bossMode = true; startGameReal(); });

        back.addActionListener(e -> cl.show(main, "menu"));

        return p;
    }

    void startGameReal() {

        if (timer != null) timer.stop();

        reset();
        loadImages();
        playerLabel.setText("Player: " + player);

        int size = bossMode ? 6 : 4;

        gameContainer.removeAll();
        gameContainer.setBackground(Color.BLACK);

        JPanel top = new JPanel();
        top.setBackground(Color.BLACK);

        timerLabel.setForeground(Color.CYAN);
        moveLabel.setForeground(Color.CYAN);
        playerLabel.setForeground(Color.GREEN);

        JButton menuBtn = btn("MENU");
        menuBtn.addActionListener(e -> {
            timer.stop();
            cl.show(main, "menu");
        });

        JButton backBtn = btn("BACK");
        backBtn.addActionListener(e -> {
            timer.stop();
            cl.show(main, "level");
        });

        top.add(timerLabel);
        top.add(moveLabel);
        top.add(playerLabel);
        top.add(menuBtn);
        top.add(backBtn);

        JPanel grid = new JPanel(new GridLayout(size, size, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        grid.setBackground(Color.BLACK);

        for (int i = 0; i < size * size; i++) {
            JButton b = new JButton();
            b.setBackground(Color.BLACK);
            b.setBorder(BorderFactory.createLineBorder(
                    bossMode ? Color.RED : Color.CYAN, 2));
            int idx = i;
            b.addActionListener(e -> click(idx));
            cards[i] = b;
            grid.add(b);
        }

        gameContainer.add(top, BorderLayout.NORTH);
        gameContainer.add(grid, BorderLayout.CENTER);

        gameContainer.revalidate();
        gameContainer.repaint();

        cl.show(main, "game");
        startTimer();
    }

    void startTimer() {
        timerLabel.setText("Time: " + timeLeft);
        timer = new javax.swing.Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Time: " + timeLeft);
            if (timeLeft <= 0) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "TIME OVER");
                cl.show(main, "menu");
            }
        });
        timer.start();
    }

    void loadImages() {
        ArrayList<ImageIcon> temp = new ArrayList<>();
        int pairs = bossMode ? 18 : 8;

        for (int i = 0; i < pairs; i++) {
            ImageIcon img = baseImages[i % 8];
            temp.add(img);
            temp.add(img);
        }

        Collections.shuffle(temp);
        for (int i = 0; i < temp.size(); i++)
            icons[i] = temp.get(i);
    }

    void click(int i) {

        if (!canClick) return;
        if (cards[i].getIcon() != null) return;

        cards[i].setIcon(icons[i]);

        if (first == -1) {
            first = i;
            return;
        }

        second = i;
        canClick = false;
        moves++;
        moveLabel.setText("Moves: " + moves);

        if (icons[first] == icons[second]) {
            matched++;
            streak++;
            first = -1;
            second = -1;
            canClick = true;

            int total = bossMode ? 18 : 8;
            if (matched == total) {
                timer.stop();
                JOptionPane.showMessageDialog(this,
                        "🔥 YOU WIN\nStreak: " + streak);
                cl.show(main, "menu");
            }

        } else {
            streak = 0;
            javax.swing.Timer t = new javax.swing.Timer(600, e -> {
                cards[first].setIcon(null);
                cards[second].setIcon(null);
                first = -1;
                second = -1;
                canClick = true;
            });
            t.setRepeats(false);
            t.start();
        }
    }

    void reset() {
        first = -1;
        second = -1;
        matched = 0;
        moves = 0;
        streak = 0;
        moveLabel.setText("Moves: 0");
    }

    void showScore() {
        JOptionPane.showMessageDialog(this,
                "🔥 Leaderboard\n\nPlayer: " + player +
                        "\nMoves: " + moves +
                        "\nBest Streak: " + streak);
    }

    public static void main(String[] args) {
        new MemoryGame();
    }
}