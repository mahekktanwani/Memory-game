import javax.swing.*;
import java.awt.*;
import java.util.*;
import javax.sound.sampled.*;
import java.io.File;

public class MemoryGame extends JFrame {

    CardLayout cardLayout = new CardLayout();
    JPanel mainPanel = new JPanel(cardLayout);

    JButton[] buttons = new JButton[16];
    ImageIcon[] images = new ImageIcon[16];

    int first = -1, second = -1;
    boolean canClick = true;
    int matched = 0;

    JLabel timerLabel = new JLabel("Time: 60");
    JLabel movesLabel = new JLabel("Moves: 0");

    int timeLeft = 60;
    int moves = 0;
    javax.swing.Timer gameTimer;

    Clip bgMusic;

    public MemoryGame() {
        setTitle("Memory Game Pro 🔥");
        setSize(650, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        mainPanel.add(loadingPage(), "loading");
        mainPanel.add(menuPage(), "menu");
        mainPanel.add(gamePage(), "game");

        add(mainPanel);
        setVisible(true);

        playBGMusic(); // start music
        showLoadingThenMenu();
    }

    // ================= SOUND =================
    private void playSound(String path){
        try{
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(path));
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        }catch(Exception e){
            System.out.println("Sound error: "+path);
        }
    }

    private void playBGMusic(){
        try{
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File("sounds/bg.wav"));
            bgMusic = AudioSystem.getClip();
            bgMusic.open(audio);
            bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }catch(Exception e){
            System.out.println("BG music not found");
        }
    }

    // ================= LOADING =================
    private JPanel loadingPage() {
        JPanel p = new JPanel(){
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                GradientPaint gp = new GradientPaint(0,0,Color.BLACK,getWidth(),getHeight(),Color.MAGENTA);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        p.setLayout(new GridBagLayout());

        JLabel title = new JLabel("🎮 MEMORY GAME");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 40));
        p.add(title);

        return p;
    }

    private void showLoadingThenMenu() {
        javax.swing.Timer t = new javax.swing.Timer(2500, e -> {
            cardLayout.show(mainPanel, "menu");
        });
        t.setRepeats(false);
        t.start();
    }

    // ================= MENU =================
    private JPanel menuPage() {
        JPanel panel = new JPanel(){
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                GradientPaint gp = new GradientPaint(0,0,new Color(10,10,40),
                        getWidth(),getHeight(),new Color(120,0,120));
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15,15,15,15);

        JLabel title = new JLabel("MEMORY GAME");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 42));

        JButton start = stylishButton("START GAME");
        JButton leader = stylishButton("LEADERBOARD");
        JButton settings = stylishButton("SETTINGS");

        start.addActionListener(e -> {
            resetGame();
            cardLayout.show(mainPanel,"game");
        });

        leader.addActionListener(e ->
                JOptionPane.showMessageDialog(this,"No scores yet 😎"));

        settings.addActionListener(e ->
                JOptionPane.showMessageDialog(this,"Sound: ON 🔊"));

        gbc.gridy=0; panel.add(title,gbc);
        gbc.gridy=1; panel.add(start,gbc);
        gbc.gridy=2; panel.add(leader,gbc);
        gbc.gridy=3; panel.add(settings,gbc);

        return panel;
    }

    private JButton stylishButton(String text){
        JButton b = new JButton(text);
        b.setFont(new Font("Arial",Font.BOLD,18));
        b.setBackground(new Color(255,70,150));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(220,50));

        b.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseEntered(java.awt.event.MouseEvent e){
                b.setBackground(new Color(255,20,120));
            }
            public void mouseExited(java.awt.event.MouseEvent e){
                b.setBackground(new Color(255,70,150));
            }
        });
        return b;
    }

    // ================= GAME =================
    private JPanel gamePage() {

        JPanel container = new JPanel(new BorderLayout());

        JPanel top = new JPanel();
        top.setBackground(Color.BLACK);

        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Arial",Font.BOLD,20));

        movesLabel.setForeground(Color.WHITE);
        movesLabel.setFont(new Font("Arial",Font.BOLD,20));

        JButton back = new JButton("MENU");
        back.addActionListener(e->{
            gameTimer.stop();
            cardLayout.show(mainPanel,"menu");
        });

        top.add(timerLabel);
        top.add(movesLabel);
        top.add(back);
        container.add(top,BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(4,4,10,10));
        grid.setBackground(new Color(20,20,20));
        grid.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        ArrayList<ImageIcon> temp = new ArrayList<>();

        for(int i=1;i<=8;i++){
            ImageIcon img = new ImageIcon("images/img"+i+".jpg");
            Image im = img.getImage().getScaledInstance(90,90,Image.SCALE_SMOOTH);
            img = new ImageIcon(im);
            temp.add(img);
            temp.add(img);
        }

        Collections.shuffle(temp);
        for(int i=0;i<16;i++) images[i]=temp.get(i);

        for(int i=0;i<16;i++){
            JButton btn = new JButton();
            btn.setBackground(Color.WHITE);
            btn.setFocusable(false);
            int index=i;

            btn.addActionListener(e->cardClick(index));
            buttons[i]=btn;
            grid.add(btn);
        }

        container.add(grid,BorderLayout.CENTER);
        startTimer();
        return container;
    }

    private void startTimer(){
        timeLeft=60;
        timerLabel.setText("Time: 60");

        gameTimer = new javax.swing.Timer(1000,e->{
            timeLeft--;
            timerLabel.setText("Time: "+timeLeft);

            if(timeLeft<=0){
                gameTimer.stop();
                JOptionPane.showMessageDialog(this,"Time Over!");
                cardLayout.show(mainPanel,"menu");
            }
        });
        gameTimer.start();
    }

    private void cardClick(int i){
        if(!canClick) return;
        if(buttons[i].getIcon()!=null) return;

        playSound("sounds/click.wav");

        buttons[i].setIcon(images[i]);

        if(first==-1){
            first=i;
        }else{
            second=i;
            canClick=false;
            moves++;
            movesLabel.setText("Moves: "+moves);

            if(images[first].toString().equals(images[second].toString())){
                matched++;
                first=-1; second=-1;
                canClick=true;

                if(matched==8){
                    gameTimer.stop();
                    playSound("sounds/win.wav");
                    JOptionPane.showMessageDialog(this,"YOU WIN 🔥");
                    cardLayout.show(mainPanel,"menu");
                }

            }else{
                javax.swing.Timer t=new javax.swing.Timer(600,e->{
                    buttons[first].setIcon(null);
                    buttons[second].setIcon(null);
                    first=-1; second=-1;
                    canClick=true;
                });
                t.setRepeats(false);
                t.start();
            }
        }
    }

    private void resetGame(){
        first=-1; second=-1; matched=0;
        moves=0;
        movesLabel.setText("Moves: 0");
    }

    public static void main(String[] args){
        new MemoryGame();
    }
}
