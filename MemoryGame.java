import javax.swing.*;
import java.awt.*;
import java.util.*;
import javax.sound.sampled.*;
import java.io.*;

public class MemoryGame extends JFrame {

    CardLayout cl = new CardLayout();
    JPanel main = new JPanel(cl);

    JButton[] cards = new JButton[16];
    ImageIcon[] icons = new ImageIcon[16];

    int first=-1, second=-1;
    boolean canClick=true;
    int matched=0;
    int moves=0;
    int timeLeft=60;

    JLabel timerLabel=new JLabel("Time:60");
    JLabel moveLabel=new JLabel("Moves:0");
    JLabel scoreLabel=new JLabel("Score:0");

    javax.swing.Timer timer;
    Clip bgMusic;

    String player="PLAYER";

    public MemoryGame(){
        setTitle("MEMORY BOSS ULTRA 🔥");
        setSize(720,760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        main.add(menuPage(),"menu");
        main.add(levelPage(),"level");
        main.add(gamePage(),"game");

        add(main);
        setVisible(true);

        playBG();
        cl.show(main,"menu");
    }

    // ========= SOUND =========
    void play(String path){
        try{
            AudioInputStream a=AudioSystem.getAudioInputStream(new File(path));
            Clip c=AudioSystem.getClip();
            c.open(a);
            c.start();
        }catch(Exception e){System.out.println("sound err "+path);}
    }

    void playBG(){
        try{
            AudioInputStream a=AudioSystem.getAudioInputStream(new File("sounds/bg.wav"));
            bgMusic=AudioSystem.getClip();
            bgMusic.open(a);
            bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }catch(Exception e){System.out.println("bg music missing");}
    }

    // ========= MENU =========
    JPanel menuPage(){
        JPanel p=new JPanel(null){
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g;
                GradientPaint gp=new GradientPaint(0,0,Color.BLACK,getWidth(),getHeight(),new Color(30,0,60));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };

        JLabel title=new JLabel("MEMORY BOSS");
        title.setFont(new Font("Arial",Font.BOLD,46));
        title.setForeground(Color.CYAN);
        title.setBounds(210,120,400,60);
        p.add(title);

        JTextField name=new JTextField();
        name.setBounds(260,220,200,40);
        p.add(name);

        JButton start=btn("START");
        start.setBounds(260,300,200,50);
        p.add(start);

        JButton leader=btn("LEADERBOARD");
        leader.setBounds(260,370,200,50);
        p.add(leader);

        JButton exit=btn("EXIT");
        exit.setBounds(260,440,200,50);
        p.add(exit);

        JLabel messy=new JLabel("by messy");
        messy.setForeground(Color.GRAY);
        messy.setBounds(600,660,100,30);
        p.add(messy);

        start.addActionListener(e->{
            player=name.getText();
            cl.show(main,"level");
        });

        leader.addActionListener(e->showScore());
        exit.addActionListener(e->System.exit(0));

        return p;
    }

    // ========= LEVEL PAGE =========
    JPanel levelPage(){
        JPanel p=new JPanel(null);
        p.setBackground(Color.BLACK);

        JLabel l=new JLabel("SELECT LEVEL");
        l.setForeground(Color.CYAN);
        l.setFont(new Font("Arial",Font.BOLD,40));
        l.setBounds(220,80,400,60);
        p.add(l);

        JButton easy=btn("EASY");
        easy.setBounds(260,200,200,50);
        p.add(easy);

        JButton hard=btn("HARD");
        hard.setBounds(260,270,200,50);
        p.add(hard);

        JButton boss=btn("BOSS 🔥");
        boss.setBounds(260,340,200,50);
        p.add(boss);

        JButton back=btn("BACK");
        back.setBounds(260,430,200,50);
        p.add(back);

        easy.addActionListener(e->{timeLeft=80; startGame();});
        hard.addActionListener(e->{timeLeft=60; startGame();});
        boss.addActionListener(e->{timeLeft=40; startGame();});
        back.addActionListener(e->cl.show(main,"menu"));

        return p;
    }

    // ========= GAME =========
    JPanel gamePage(){
        JPanel container=new JPanel(new BorderLayout());
        container.setBackground(Color.BLACK);

        JPanel top=new JPanel();
        top.setBackground(Color.BLACK);

        timerLabel.setForeground(Color.CYAN);
        moveLabel.setForeground(Color.CYAN);
        scoreLabel.setForeground(Color.YELLOW);

        JButton back=btn("MENU");
        back.addActionListener(e->cl.show(main,"menu"));

        top.add(timerLabel);
        top.add(moveLabel);
        top.add(scoreLabel);
        top.add(back);

        container.add(top,BorderLayout.NORTH);

        JPanel grid=new JPanel(new GridLayout(4,4,10,10));
        grid.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        grid.setBackground(Color.BLACK);

        ArrayList<ImageIcon> temp=new ArrayList<>();

        for(int i=1;i<=8;i++){
            ImageIcon img=new ImageIcon("images/img"+i+".jpg");
            Image im=img.getImage().getScaledInstance(100,100,Image.SCALE_SMOOTH);
            img=new ImageIcon(im);
            temp.add(img);
            temp.add(img);
        }

        Collections.shuffle(temp);
        for(int i=0;i<16;i++) icons[i]=temp.get(i);

        for(int i=0;i<16;i++){
            JButton b=new JButton();
            b.setBackground(new Color(20,20,20));
            b.setBorder(BorderFactory.createLineBorder(Color.CYAN,2));
            int idx=i;

            b.addActionListener(e->click(idx));
            cards[i]=b;
            grid.add(b);
        }

        container.add(grid,BorderLayout.CENTER);
        return container;
    }

    void startGame(){
        reset();
        cl.show(main,"game");
        startTimer();
    }

    void startTimer(){
        timerLabel.setText("Time:"+timeLeft);
        timer=new javax.swing.Timer(1000,e->{
            timeLeft--;
            timerLabel.setText("Time:"+timeLeft);

            if(timeLeft<=0){
                timer.stop();
                JOptionPane.showMessageDialog(this,"TIME OVER");
                saveScore();
                cl.show(main,"menu");
            }
        });
        timer.start();
    }

    void click(int i){
        if(!canClick) return;
        if(cards[i].getIcon()!=null) return;

        play("sounds/click.wav");
        cards[i].setIcon(icons[i]);

        if(first==-1){first=i; return;}
        second=i;
        canClick=false;
        moves++;
        moveLabel.setText("Moves:"+moves);

        if(icons[first].toString().equals(icons[second].toString())){
            matched++;
            first=-1; second=-1;
            canClick=true;

            if(matched==8){
                timer.stop();
                play("sounds/win.wav");
                JOptionPane.showMessageDialog(this,"YOU WIN "+player);
                saveScore();
                cl.show(main,"menu");
            }

        }else{
            javax.swing.Timer t=new javax.swing.Timer(600,e->{
                cards[first].setIcon(null);
                cards[second].setIcon(null);
                first=-1; second=-1;
                canClick=true;
            });
            t.setRepeats(false);
            t.start();
        }
    }

    void reset(){
        first=-1; second=-1; matched=0; moves=0;
        moveLabel.setText("Moves:0");
    }

    void saveScore(){
        try{
            FileWriter fw=new FileWriter("score.txt",true);
            fw.write(player+" - Moves:"+moves+"\n");
            fw.close();
        }catch(Exception e){}
    }

    void showScore(){
        try{
            BufferedReader br=new BufferedReader(new FileReader("score.txt"));
            String s="",line;
            while((line=br.readLine())!=null) s+=line+"\n";
            br.close();
            JOptionPane.showMessageDialog(this,s.equals("")?"No score":s);
        }catch(Exception e){}
    }

    JButton btn(String t){
        JButton b=new JButton(t);
        b.setFocusPainted(false);
        b.setBackground(Color.BLACK);
        b.setForeground(Color.CYAN);
        b.setFont(new Font("Arial",Font.BOLD,16));
        b.setBorder(BorderFactory.createLineBorder(Color.CYAN,2));

        b.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseEntered(java.awt.event.MouseEvent e){
                b.setBackground(new Color(0,40,40));
            }
            public void mouseExited(java.awt.event.MouseEvent e){
                b.setBackground(Color.BLACK);
            }
        });
        return b;
    }

    public static void main(String[] args){
        new MemoryGame();
    }
}
