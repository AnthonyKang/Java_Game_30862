import java.awt.*;
import javax.swing.JFrame;

public class Game extends JFrame {
// Contains:
// 1) Player character
// 2) List of monsters
// 3) List of in game items
// 4) List of player bullets
// 5) List of monster bullets
// 6) Game score (health)
// 7) Sound queues
// 8) Game status (Running or paused)

	private final static int UNIT = 20;
        private Char mainChar;



// Main Function
        public static void main(String[] args) {
                DisplayMode displayMode = new DisplayMode(800, 600, 16, DisplayMode.REFRESH_RATE_UNKNOWN);
                Game gameCore = new Game();
                gameCore.run(displayMode);
        }

        private ScreenManager screen;

        public void run(DisplayMode displayMode) {
                setBackground(Color.blue);
                screen = new ScreenManager();
                try {
                        screen.setFullScreen(displayMode, this);
                }
                finally {
                        screen.restoreScreen();
                }
        }



// Game loop
// 1) Update location of map (Based on haracter displacement)
// 2) Update locations of objects (Character displacement)
// 3) Check collisions
//     a) Player with monster bullets
//     b) Player with in-game objects
//     c) Monsters with player bullets
//     d) Player with objects (y-direction collisions when jumping)
// 4) Create sounds
// 5) Update statuses
// 6) Redraw


/////////////////////////////////////////
// Screen Manager Methods
/////////////////////////////////////////
        


}
