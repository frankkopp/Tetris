/**
MIT License

Copyright (c) 2017 Frank Kopp

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package fko.tetris.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import fko.tetris.Tetris;

/**
 * This is an example program that demonstrates how to play back an audio file
 * using the Clip in Java Sound API.
 * @author www.codejava.net
 */
public class TetrisSounds implements LineListener {

//	public static void main(String[] args) {
//		String audioFilePath = "E:/Test/Audio.wav";
//		TetrisSounds player = new TetrisSounds();
//		player.play(audioFilePath);
//	}
	
	// folder to all sound files
	public static final String SOUND_FOLDER = "./sounds/";
	private final Path _folderPath = FileSystems.getDefault().getPath(SOUND_FOLDER);

	/**
	 * this flag indicates whether the playback completes or not.
	 */
	AtomicBoolean isPlaying = new AtomicBoolean(false);
	
	Object LOCK = new Object();
	
	// available sounds
	private Map<String, File> _sounds;
	private Clip _audioClip;
	
	/**
	 * Create an object with all tetris sounds available 
	 */
	public TetrisSounds() {
		
		_sounds = new HashMap<>();
		
		try {
			Files.newDirectoryStream(_folderPath,
			        path -> path.toString().endsWith(".wav"))
			        .forEach((p) -> _sounds.put(p.getFileName().toString(), p.toFile()));
		} catch (IOException e) {
			Tetris.minorError(String.format(
					"While reading sounds: Path %s could not be read."
					,_folderPath.toString()
					));
		}
		
	}
	
	public void play(String file) {
		play(_sounds.get(file));
	}

	/**
	 * Play a given audio file.
	 * @param audioFilePath Path of the audio file.
	 */
	public void play(File audioFile) {
		
		// wait while another sound is still playing
		while (isPlaying.get()) {
			// wait for the playback completes
			try {
				synchronized (LOCK) {
					LOCK.wait();	
				}
			} catch (InterruptedException ex) {	/* ignore */ }
		}
		
		try {
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

			AudioFormat format = audioStream.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			_audioClip = (Clip) AudioSystem.getLine(info);
			_audioClip.addLineListener(this);
			_audioClip.open(audioStream);
			_audioClip.start();
			isPlaying.set(true);

		} catch (UnsupportedAudioFileException ex) {
			System.out.println("The specified audio file is not supported.");
			ex.printStackTrace();
		} catch (LineUnavailableException ex) {
			System.out.println("Audio line for playing back is unavailable.");
			ex.printStackTrace();
		} catch (IOException ex) {
			System.out.println("Error playing the audio file.");
			ex.printStackTrace();
		}

	}

	/**
	 * @return the _sounds
	 */
	public Map<String, File> getSounds() {
		return Collections.unmodifiableMap(_sounds);
	}
	
	/**
	 * Listens to the START and STOP events of the audio line.
	 */
	@Override
	public void update(LineEvent event) {
		LineEvent.Type type = event.getType();

		if (type == LineEvent.Type.START) {
			// ignore
		} 
		else if (type == LineEvent.Type.STOP) {
			isPlaying.set(false);
			_audioClip.close();
			synchronized (LOCK) {
				LOCK.notifyAll();	
			}
		}

	}

}
