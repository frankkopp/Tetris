/**
 * MIT License
 *
 * <p>Copyright (c) 2017 Frank Kopp
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package fko.tetris.game;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fko.tetris.Tetris;
import javafx.scene.media.AudioClip;

/**
 * This is an example program that demonstrates how to play back an audio file using the Clip in
 * Java Sound API.
 *
 * @author www.codejava.net
 */
public class SoundManager {

  /** All available audio clips of this class */
  public enum Clips {
    // ENUM		Filename w/o .wav
    FALLING("SFX_PieceFall"),
    TOUCHDOWN("SFX_PieceTouchDown"),
    LOCK("SFX_PieceLockdown"),
    SOFTDROP("SFX_PieceSoftDrop"),
    HARDDROP("SFX_PieceHardDrop"),
    HOLD("SFX_PieceHold"),
    MOVE_LR("SFX_PieceMoveLR"),
    ROTATE_FAIL("SFX_PieceRotateFail"),
    ROTATE_LR("SFX_PieceRotateLR"),
    TOUCH_LR("SFX_PieceTouchLR"),
    LINECLEAR("lines"),
    TETRIS("SFX_SpecialTetris"),
    LEVELUP("levelup"),
    GAME_START("SFX_GameStart"),
    GAME_OVER("SFX_GameOver");

    private final String _name;

    private Clips(String name) {
      _name = name;
    }
  }
  // Singleton instance
  private static SoundManager instance = null;

  // folder to all sound files
  private static final String SOUND_FOLDER = "/sounds/";

  // to play sounds parallel
  private final ExecutorService _executor = Executors.newScheduledThreadPool(2);

  // available sounds mapped by the enum
  private final Map<Clips, AudioClip> _sounds;

  // sound on/off
  private boolean soundOn = true;

  /** Get theSounds instance with all sounds available */
  public static synchronized SoundManager getInstance() {
    if (instance == null) {
      instance = new SoundManager();
    }
    return instance;
  }

  /** Create an object with all Tetris sounds available */
  private SoundManager() {
    _sounds = new HashMap<>();
    // for all defined values in ENUM Clips
    // read in the Clip and store them in the Map
    Arrays.stream(Clips.values())
        .forEach(
            c -> {
              final String filename = SOUND_FOLDER + c._name + ".wav";
              final URL url = Tetris.class.getResource(filename);
              // create AudioInputStream object
              if (url != null) {
                AudioClip clip = null;
                clip = new AudioClip(url.toExternalForm());
                _sounds.put(c, clip);
              } else {
                Tetris.criticalError("Sound file: " + filename + " cannot be loaded!");
              }
            });
  }

  /**
   * Plays the give clip once.
   *
   * @param c enum from Clips
   */
  public void playClip(Clips c) {
    // sound was not available
    if (_sounds.get(c) == null || !soundOn) return;
    // execute in a new thread to play sound
    _executor.execute(
        () -> {
          AudioClip clip = _sounds.get(c);
          clip.play(); // Start playing
        });
  }

  /** Turn sound playing off. Causes the call to playClip to be ignored. */
  public void soundOff() {
    soundOn = false;
  }

  /** Turns sound playing on. */
  public void soundOn() {
    soundOn = true;
  }

  /** @return true if sound playing is turned on */
  public boolean isSoundOn() {
    return soundOn;
  }

  /** Stop all threads and media players. */
  public void shutdown() {
    _executor.shutdown();
  }
}
