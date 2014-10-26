// Screen Manager used for putting the game in full screen.
// Uses code from the book

import java.awt.*;
import javax.swing.JFrame;

public class ScreenManager {
  private GraphicsDevice device;

  public ScreenManager() {
    GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    device = environment.getDefaultScreenDevice();
  }

  public void setFullScreen(DisplayMode displayMode, JFrame window) {
    window.setUndecorated(true);
    window.setResizable(false);
    device.setFullScreenWindow(window);
    if(displayMode != null && device.isDisplayChangeSupported()) {
      try {
        device.setDisplayMode(displayMode);
      }
      catch (IllegalArgumentException ex) {
      }
    }
  }

  public Window getFullScreenWindow() {
    return device.getFullScreenWindow();
  }

  public void restoreScreen() {
    Window window.device.getFullScreenWindow();
    if(window != null) {
      window.dispose();
    }
    device.setFullScreenWindow(null);
  }
}
