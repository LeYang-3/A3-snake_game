package snake;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PFont;

import java.util.*;

public class App extends PApplet {

    // game UI window
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    public static final int SPRITESIZE = 20;
    public static final int TOPBAR = 80;
    public static final int NUMROWS = (HEIGHT - TOPBAR) / SPRITESIZE;
    public static final int NUMCOLS = WIDTH / SPRITESIZE;

    // game clock
    public static final int FPS = 60;
    public static Random random = new Random();
    public static final int POWERDURATION = 8; // 8 seconds;

    // game status
    private boolean gameOver = false;
    private boolean win = false;

    // goal, score
    // when catching a black square, gain UNITSOCRE
    private int UNITSCORE;
    // when score reaches SCORETHRESHOLD,
    // moving speed will be doubled, i.e., MOVECOUNT will be halved
    private int SCORETHRESHOLD;
    private boolean harder;
    private int goal;
    private int score;

    // black square to form the snake
    private boolean hasBlackSquare;
    private int blackCount;
    private int blackTimer;
    private int blackPos;  // int pos <=> (row = pos/NUMROWS, col = pos%NUMROWS) <=> (x = col*SPRITESIZE, y = TOPBAR + row*SPRITESIZE)

    // red power square to slow down the snake
    private boolean hasPower;
    private int powerCount;
    private int powerTimer;
    private int powerPos;
    private boolean showPower;
    private int remainingSeconds;

    // snake moving
    private int moveCount;
    private int moveTimer;


    // snake body
    private List<Integer> body;
    private int direction;  // 0 -- right; 1 -- down; 2 -- left; 3 -- up

    // text and image
    private PImage powerImage;
    private PImage blackSquareImage;
    private PImage wallImage;

    private PFont font;

    public App() {
    }

    /**
     * Initialise the setting of the window size.
     */
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements of snake game.
     */
    public void setup() {
        frameRate(FPS);

        // initialize the score
        goal = 300;
        score = 0;
        UNITSCORE = 10;
        SCORETHRESHOLD = 100;
        harder = false;

        // black square
        hasBlackSquare = false;
        blackCount = 1 * FPS;
        blackTimer = 0;
        blackPos = 0;

        // power: red square
        hasPower = false;
        powerCount = 20 * FPS; // initial 10 seconds
        powerTimer = 0;
        remainingSeconds = 0;
        powerPos = 0;
        showPower = false;

        // snake move
        moveCount = SPRITESIZE;
        moveTimer = 0;

        // snake body, first body of the snake
        body = new LinkedList<>();
        body.add(random.nextInt(NUMROWS * NUMCOLS));

        // 0 -- right; 1 -- down; 2 -- left; 3 -- up
        direction = 0;

        // Load images during setup
        this.blackSquareImage = loadImage(this.getClass().getResource("black_square.png").getPath());
        this.powerImage = loadImage(this.getClass().getResource("power.png").getPath());
        this.wallImage = loadImage(this.getClass().getResource("wall.png").getPath());

        // status bar font
        font = createFont("Arial", 16, true);
    }

    /**
     * Draw all elements in the game by current frame.
     */
    public void draw() {
        background(0x654321);

        for (int i=0; i<NUMCOLS; i++) {
            image(wallImage, i * SPRITESIZE, TOPBAR - SPRITESIZE);
        }
        // show top bar
        if(gameOver) {
            textFont(font, 50);
            fill(255);
            text("Game over", 500, 50);
        }
        else if(win) {
            textFont(font, 50);
            fill(255);
            text("Victory", 500, 50);
        }
        else {
            textFont(font, 30);
            fill(255);
            text("Goal: " + goal, 100, 40);
            text("Score: " + score, 300, 40);

            if(hasPower && !showPower) {
                textFont(font, 20);
                text("Powerup remaining time: ", 660, 40);
                text(remainingSeconds+"", 900, 40);
                text("seconds", 930, 40);
            }

            // draw snake
            for (int pos: body) {
                int r = pos / NUMCOLS, c = pos % NUMCOLS;
                image(blackSquareImage, c * SPRITESIZE, TOPBAR + r * SPRITESIZE);
            }

            // show if there is a black square on screen
            if (hasBlackSquare) {
                int r = blackPos / NUMCOLS, c = blackPos % NUMCOLS;
                image(blackSquareImage, c * SPRITESIZE, TOPBAR + r * SPRITESIZE);
            }
            else {
                blackTimer ++;
                if (blackTimer == blackCount) {
                    blackTimer = 0;
                    hasBlackSquare = true;
                    blackPos = createNewBlackSquare();
                }
            }

            // show if there is power red square
            if (hasPower) {
                if (showPower) {
                    int r = powerPos / NUMCOLS, c = powerPos % NUMCOLS;
                    image(powerImage, c * SPRITESIZE, TOPBAR + r * SPRITESIZE);
                }
                else {
                    powerTimer ++;
                    if (powerTimer == FPS) {
                        remainingSeconds --;
                        powerTimer = 0;
                        if (remainingSeconds == 0) {
                            moveCount /= 2;     // when power is off, moving faster
                            hasPower = false;
                            powerCount = 10 * FPS + random.nextInt(5 * FPS); // 10s ~ 15s
                            moveTimer = 0;
                        }
                    }
                }
            }
            else {
                powerTimer ++;
                if (powerTimer == powerCount) {
                    powerPos = createNewPowerSquare();
                    powerTimer = 0;
                    showPower = true;
                    hasPower = true;
                    remainingSeconds = POWERDURATION;
                }
            }

            tick();
        }

    }

    public int createNewBlackSquare() {
        boolean collide = false;
        int newPos;
        do {
            newPos = random.nextInt(NUMROWS * NUMCOLS);
            for (int pos: body) {
                if (pos == newPos) {
                    collide = true;
                    break;
                }
            }
            if (!collide && showPower && newPos == powerPos) {
                collide = true;
            }
        } while (collide);
        return newPos;
    }

    public int createNewPowerSquare() {
        boolean collide = false;
        int newPos;
        do {
            newPos = random.nextInt(NUMROWS * NUMCOLS);
            for (int pos: body) {
                if (pos == newPos) {
                    collide = true;
                    break;
                }
            }
            if (!collide && hasBlackSquare && blackPos == newPos) {
                collide = true;
            }
        } while (collide);
        return newPos;
    }

    /**
     * update the position of the snake body
     */
    public void tick() {
        moveTimer ++;
        if (moveTimer == moveCount) {
            int oldPos = body.get(0);
            int row = oldPos / NUMCOLS, col = oldPos % NUMCOLS;
            // 0 -- right; 1 -- down; 2 -- left; 3 -- up
            if (direction == 0) {
                col ++;
                if (col == NUMCOLS) {
                    gameOver = true;
                }
            }
            else if (direction == 1) {
                row ++;
                if (row == NUMROWS) {
                    gameOver = true;
                }
            }
            else if (direction == 2) {
                col --;
                if (col < 0) {
                    gameOver = true;
                }
            }
            else {
                row --;
                if (row < 0) {
                    gameOver = true;
                }
            }
            moveTimer = 0;
            int newPos = row * NUMCOLS + col;

            // case 1: catch a new black square
            if (hasBlackSquare && newPos == blackPos) {
                hasBlackSquare = false;
                body.add(0, newPos);
                score += UNITSCORE;
                if (!harder && score >= SCORETHRESHOLD) {
                    harder = true;
                    moveCount /= 2;
                }
                if (score >= goal) {
                    win = true;
                }
            }
            // case 2: catch a new power square
            else if (hasPower && showPower && newPos == powerPos) {
                showPower = false;
                moveCount *= 2;
                body.remove(body.size() - 1);
                body.add(0, newPos);
            }
            else {
                boolean collide = false;
                for (int i=0; i<body.size()-1; i++) {
                    if (body.get(i) == newPos) {
                        collide = true;
                        break;
                    }
                }

                // case 3: hit itself
                if (collide) {
                    gameOver = true;
                }
                // case 4: normal move
                else {
                    body.remove(body.size() - 1);
                    body.add(0, newPos);
                }
            }
        }
    }

    /**
     * Called every frame if a key is down.
     *
     * You can access the key with the keyCode variable.
     */
    public void keyPressed() {
        // Left: 37
        // Up: 38
        // Right: 39
        // Down: 40
        // 0 -- right; 1 -- down; 2 -- left; 3 -- up
        if (keyCode == 37) {      // press left
            if(direction == 1 || direction == 3) {
                moveTimer = 0;
                direction = 2;
            }

        } else if (keyCode == 39) {   // press right
            if(direction == 1 || direction == 3) {
                moveTimer = 0;
                direction = 0;
            }

        } else if (keyCode == 38) {  // press up
            if(direction == 0 || direction == 2) {
                moveTimer = 0;
                direction = 3;
            }
        }
        else if (keyCode == 40) { // press down
            if(direction == 0 || direction == 2) {
                moveTimer = 0;
                direction = 1;
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main("snake.App");
    }
}
