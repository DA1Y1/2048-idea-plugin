package com.daie.game;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

/**
 * @Created by DAIE
 * @Date 2021/2/2 11:20
 * @Description 2048 GAME
 */
public class Game2048 extends JPanel implements ToolWindowFactory {

    private final String fontStyle = "SansSerif";

    //枚举：开始，获胜，正在进行游戏，游戏结束
    enum State {
        start, won, running, over
    }

    final Color[] colorTable = {
            new Color(0x701710), new Color(0xFFE4C3), new Color(0xfff4d3),
            new Color(0xffdac3), new Color(0xe7b08e), new Color(0xe7bf8e),
            new Color(0xffc4c3), new Color(0xE7948e), new Color(0xbe7e56),
            new Color(0xbe5e56), new Color(0x9c3931), new Color(0x701710)};

    final static int target = 2048;//游戏最终目标

    static int highestNum;//最高数

    static int score;

    private final Color gridColor = new Color(0xBBADA0);//网格颜色
    private final Color emptyColor = new Color(0xCDC1B4);//没有图块的格子的颜色
    private final Color startColor = new Color(0xFFEBCD);//开始界面框的颜色

    private Random rand = new Random();
    private Tile[][] tiles = new Tile[4][4];
    private final int side = 4;
    private State gameState = State.start;
    private boolean checkingAvailableMoves;

    public Game2048() {
        setPreferredSize(new Dimension(900, 700));
        setBackground(new Color(0xFAF8EF));
        setFont(new Font(fontStyle, Font.BOLD, 48));
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startGame();
                repaint();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        moveUp();
                        break;
                    case KeyEvent.VK_DOWN:
                        moveDown();
                        break;
                    case KeyEvent.VK_LEFT:
                        moveLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                        moveRight();
                        break;
                }
                repaint();
            }
        });
    }

    //复写JPanel里面的paintComponent方法，创建一个我们自己想要的界面
    @Override
    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        drawGrid(g);
    }

    //开始游戏
    void startGame() {
        if (gameState != State.running) {
            score = 0;
            highestNum = 0;
            gameState = State.running;
            tiles = new Tile[side][side];
            //执行两次，生成两个随机数
            addRandomTile();
            addRandomTile();
        }
    }

    //生成框及里面内容
    void drawGrid(Graphics2D g) {
        g.setColor(gridColor);
        g.fillRoundRect(200, 100, 499, 499, 15, 15);
        if (gameState == State.running) {
            for (int r = 0; r < side; r++) {
                for (int c = 0; c < side; c++) {
                    if (tiles[r][c] == null) {
                        g.setColor(emptyColor);
                        g.fillRoundRect(215 + c * 121, 115 + r * 121, 106, 106, 7, 7);
                    } else {
                        drawTile(g, r, c);
                    }
                }
            }
        } else {
            g.setColor(startColor);
            g.fillRoundRect(215, 115, 469, 469, 7, 7);
            g.setColor(gridColor.darker());//设置一个比当前颜色深一级的Color
            g.setFont(new Font(fontStyle, Font.BOLD, 128));
            g.drawString("2048", 310, 270);
            g.setFont(new Font(fontStyle, Font.BOLD, 20));
            if (gameState == State.won) {
                g.drawString("YOU WIN!", 390, 470);
            } else if (gameState == State.over)
                g.drawString("FAIL! PLEASE RESATRT", 330, 350);
            g.setColor(gridColor);
            g.drawString("START GAME", 390, 470);
            g.drawString("use keyborad to move the block", 290, 530);
        }
    }

    void drawTile(Graphics2D g, int r, int c) {
        int value = tiles[r][c].getValue();
        g.setColor(colorTable[(int) (Math.log(value) / Math.log(2)) + 1]);
        g.fillRoundRect(215 + c * 121, 115 + r * 121, 106, 106, 7, 7);
        String s = String.valueOf(value);
        g.setColor(value < 128 ? colorTable[0] : colorTable[1]);
        FontMetrics fm = g.getFontMetrics();
        int asc = fm.getAscent();
        int dec = fm.getDescent();
        int x = 215 + c * 121 + (106 - fm.stringWidth(s)) / 2;
        int y = 115 + r * 121 + (asc + (106 - (asc + dec)) / 2);
        g.drawString(s, x, y);
    }

    private void addRandomTile() {
        int pos = rand.nextInt(side * side);
        int row, col;
        do {
            pos = (pos + 1) % (side * side);
            row = pos / side;
            col = pos % side;
        } while (tiles[row][col] != null);
        int val = rand.nextInt(10) == 0 ? 4 : 2;
        tiles[row][col] = new Tile(val);
    }

    private boolean move(int countDownFrom, int yIncr, int xIncr) {
        boolean moved = false;
        for (int i = 0; i < side * side; i++) {
            int j = Math.abs(countDownFrom - i);
            int r = j / side;
            int c = j % side;
            if (tiles[r][c] == null)
                continue;
            int nextR = r + yIncr;
            int nextC = c + xIncr;
            while (nextR >= 0 && nextR < side && nextC >= 0 && nextC < side) {
                Tile next = tiles[nextR][nextC];
                Tile curr = tiles[r][c];
                if (next == null) {
                    if (checkingAvailableMoves)
                        return true;
                    tiles[nextR][nextC] = curr;
                    tiles[r][c] = null;
                    r = nextR;
                    c = nextC;
                    nextR += yIncr;
                    nextC += xIncr;
                    moved = true;
                } else if (next.canMergeWith(curr)) {
                    if (checkingAvailableMoves)
                        return true;
                    int value = next.mergeWith(curr);
                    if (value > highestNum)
                        highestNum = value;
                    score += value;
                    tiles[r][c] = null;
                    moved = true;
                    break;
                } else
                    break;
            }
        }
        if (moved) {
            if (highestNum < target) {
                clearMerged();
                addRandomTile();
                if (!movesAvailable()) {//如果不能再移动图块，则则把游戏状态变成游戏失败
                    gameState = State.over;
                }
            } else if (highestNum == target)//如果最高数=2048，则把游戏状态变成游戏成功
                gameState = State.won;
        }
        return moved;
    }

    boolean moveUp() {
        return move(0, -1, 0);
    }

    boolean moveDown() {
        return move(side * side - 1, 1, 0);
    }

    boolean moveLeft() {
        return move(0, 0, -1);
    }

    boolean moveRight() {
        return move(side * side - 1, 0, 1);
    }

    void clearMerged() {
        for (Tile[] row : tiles)
            for (Tile tile : row)
                if (tile != null)
                    tile.setMerged(false);
    }

    //判断是否还能继续移动图块
    boolean movesAvailable() {
        checkingAvailableMoves = true;
        boolean hasMoves = moveUp() || moveDown() || moveLeft() || moveRight();
        checkingAvailableMoves = false;
        return hasMoves;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setTitle("2048");
            f.setResizable(true);
            f.add(new Game2048(), BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }

    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool window
     */
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Game2048 game2048 = new Game2048();
        //按键监听事件
        game2048.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        moveUp();
                        break;
                    case KeyEvent.VK_DOWN:
                        moveDown();
                        break;
                    case KeyEvent.VK_LEFT:
                        moveLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                        moveRight();
                        break;
                }
                repaint();
            }
        });

        //todo 添加restart按钮
        //JButton restartButtion = new JButton("restart");
        //game2048.add(restartButtion);
        //restartButtion.addMouseListener(new MouseAdapter() {
        //    @Override
        //    public void mousePressed(MouseEvent e) {
        //    }
        //});

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(game2048, "", false);
        toolWindow.getContentManager().addContent(content);


    }
}

class Tile {
    private boolean merged;
    private int value;

    Tile(int val) {
        value = val;
    }

    boolean canMergeWith(Tile other) {
        return !merged && other != null && !other.merged && value == other.getValue();
    }

    int mergeWith(Tile other) {
        if (canMergeWith(other)) {
            value *= 2;
            merged = true;
            return value;
        }
        return -1;
    }

    //set get
    int getValue() {
        return value;
    }

    void setMerged(boolean m) {
        merged = m;
    }
}
