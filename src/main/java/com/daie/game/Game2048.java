package com.daie.game;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

/**
 * @author daiyi
 * @date 2021/2/2
 */
public class Game2048 extends JPanel {

    private final String fontStyle = "SansSerif";

    enum State {
        /**
         * 开始
         */
        start,
        /**
         * 正在进行游戏
         */
        running,
        /**
         * 游戏结束
         */
        over
    }

    final Color[] colorTable = {
            new Color(0x701710), new Color(0xFFE4C3), new Color(0xfff4d3),
            new Color(0xffdac3), new Color(0xe7b08e), new Color(0xe7bf8e),
            new Color(0xffc4c3), new Color(0xE7948e), new Color(0xbe7e56),
            new Color(0xbe5e56), new Color(0x9c3931), new Color(0x701710)
    };


    /**
     * 最高数
     */
    static int highestNum;

    static int score;

    private final Color gridColor = new Color(0xBBADA0);//网格颜色
    private final Color emptyColor = new Color(0xCDC1B4);//没有图块的格子的颜色
    private final Color startColor = new Color(0xFFEBCD);//开始界面框的颜色

    private Random rand = new Random();
    private Tile[][] tiles = new Tile[4][4];
    private final int side = 4;
    private State gameState = State.start;
    private boolean checkingAvailableMoves;


    public Game2048(Project project, ToolWindow toolWindow, GameWindowFactory gameWindowFactory) {
        // setPreferredSize(new Dimension(900, 700));
        setFont(new Font(fontStyle, Font.BOLD, 48));
        setFocusable(true);

        // 添加restart按钮
        JButton restartButtion = new JButton("Restart");
        this.add(restartButtion);
        restartButtion.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                gameWindowFactory.createToolWindowContent(project, toolWindow);
            }
        });

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

    /**
     * 复写JPanel里面的paintComponent方法，创建一个我们自己想要的界面
     *
     * @param gg 图
     */
    @Override
    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        drawGrid(g);
    }


    /**
     * 开始游戏
     */
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


    /**
     * 生成框及里面内容
     *
     * @param g 图
     */
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
            g.drawString("2048", 280, 270);
            g.setFont(new Font(fontStyle, Font.BOLD, 20));
            if (gameState == State.over) {
                // todo 这里展示所有的最高成绩
                g.drawString("Game over! Highest score: " + highestNum, 300, 350);
            }
            g.setColor(gridColor);
            g.drawString("Click to start", 390, 470);
            g.drawString("Use ↑↓←→ to move the block", 290, 530);
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
            if (tiles[r][c] == null) {
                continue;
            }
            int nextR = r + yIncr;
            int nextC = c + xIncr;
            while (nextR >= 0 && nextR < side && nextC >= 0 && nextC < side) {
                Tile next = tiles[nextR][nextC];
                Tile curr = tiles[r][c];
                if (next == null) {
                    if (checkingAvailableMoves) {
                        return true;
                    }
                    tiles[nextR][nextC] = curr;
                    tiles[r][c] = null;
                    r = nextR;
                    c = nextC;
                    nextR += yIncr;
                    nextC += xIncr;
                    moved = true;
                } else if (next.canMergeWith(curr)) {
                    if (checkingAvailableMoves) {
                        return true;
                    }
                    int value = next.mergeWith(curr);
                    if (value > highestNum) {
                        highestNum = value;
                    }
                    score += value;
                    tiles[r][c] = null;
                    moved = true;
                    break;
                } else {
                    break;
                }
            }
        }
        if (moved) {
            clearMerged();
            addRandomTile();
            if (!movesAvailable()) {
                //如果不能再移动图块，则则把游戏状态变成游戏失败
                gameState = State.over;
            }
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
        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                if (tile != null) {
                    tile.setMerged(false);
                }
            }
        }
    }


    /**
     * 判断是否还能继续移动图块
     *
     * @return true|false
     */
    boolean movesAvailable() {
        checkingAvailableMoves = true;
        boolean hasMoves = moveUp() || moveDown() || moveLeft() || moveRight();
        checkingAvailableMoves = false;
        return hasMoves;
    }

    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(() -> {
    //         JFrame f = new JFrame();
    //         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //         f.setTitle("2048");
    //         f.setResizable(true);
    //         f.add(new Game2048(), BorderLayout.CENTER);
    //         f.pack();
    //         f.setLocationRelativeTo(null);
    //         f.setVisible(true);
    //     });
    // }

}

