/**
 * The MIT License (MIT)
 *
 * "Chessly by Frank Kopp"
 *
 * mail-to:frank@familie-kopp.de
 *
 * Copyright (c) 2016 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package fko.tetris;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reads and stores the highscore. 
 */
public class HighScoreData {

	// Singleton
	private static HighScoreData _instance = null;
	
	// max number of entry to be written in db
	private static final int MAX_ENTRIES = 100; 
	
	/** default value for folder */
	public String _folderPath = "./var/";
	public String _fileNamePlain = "highscore.csv";
	private Path _path;

	private List<HighScoreEntry> _list;

	/**
	 * Return singleton instance of HIghScoreData 
	 * @return
	 */
	static public HighScoreData getInstance() {
		if (HighScoreData._instance == null) {
			HighScoreData._instance = new HighScoreData();
		}
		return _instance;
	}
	
	/*
	 * Private constructor because of Singleton pattern.
	 * Reads the file and adds the entries to _list
	 */
	private HighScoreData() {
		_path = FileSystems.getDefault().getPath(_folderPath, _fileNamePlain);
		List<String> lines = readFile();
		_list = Collections.synchronizedList(new ArrayList<HighScoreEntry>(lines.size()));
		lines.parallelStream().forEach(line -> {
			String[] parts = line.split(";");
			_list.add(new HighScoreEntry(
					parts[0].trim(), 
					Integer.parseInt(parts[1]), 
					LocalDateTime.parse(parts[2].trim())
							));
		});
		sortList();
	}

	/**
	 * Return the highscore list as unmodifiable list
	 * @return unmodifiable list of high score entries
	 */
	public List<HighScoreEntry> getList() {
		return Collections.unmodifiableList(_list);
	}
	
	/**
	 * Put a new entry into the highscore table
	 * @param newEntry
	 */
	public void addEntry(HighScoreEntry newEntry) {
		_list.add(newEntry);
		sortList();
	}
	
	/**
	 * Put a new entry into the highscore table and save to file
	 * @param newEntry
	 * @return true if save was successful, false otherwise
	 */
	public boolean addEntryAndSave(HighScoreEntry newEntry) {
		_list.add(newEntry);
		sortList();
		return saveFile();
	}
	
	/**
	 * Save the highscore file
	 * @return true if success, false if error
	 */
	public boolean saveToFile() {
		return saveFile();
	}
	
	/*
	 * Save _list to file. Max MAX_ENTRIES are written.
	 */
	private boolean saveFile() {
		Charset charset = Charset.forName("ISO-8859-1");
		//Use try-with-resource to get auto-closeable writer instance
		try (BufferedWriter writer = Files.newBufferedWriter(_path,charset))
		{
			_list.stream().limit(MAX_ENTRIES).forEach((e) -> {
					try {
						writer.write(e.toString()+String.format("%n"));
					} catch (IOException e1) {
						Tetris.criticalError("Highscore file '" + _path + "' could not be saved!");
						e1.printStackTrace();
					}
			});
		} catch (IOException e) {
			Tetris.criticalError("Highscore file '" + _path + "' could not be saved!");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/*
	 * Read the file and return all lines
	 */
	private List<String> readFile() {
		Charset charset = Charset.forName("ISO-8859-1");
		List<String> lines = null;
		try {
			lines = Files.readAllLines(_path, charset);
		} catch (CharacterCodingException e) {
			Tetris.criticalError("Highscore file '" + _path + "' has wrong charset (needs to be ISO-8859-1) - not loaded!");
		} catch (IOException e) {
			Tetris.criticalError("Highscore file '" + _path + "' could not be loaded!");
		}
		return lines;
	}
	
	/*
	 * sort the list with the highest score first 
	 */
	private void sortList() {
		//Sorting using Anonymous Inner class.
		Collections.sort(_list, (HighScoreEntry e1, HighScoreEntry e2) -> e2.score - e1.score);
	}

	/**
	 * A entry in the Highscore list. 
	 */
	public static class HighScoreEntry {
		
		final public String name;
		final public int score;
		final public LocalDateTime date;
		
		/**
		 * @param name
		 * @param score
		 * @param date
		 */
		public HighScoreEntry(String name, int score, LocalDateTime date) {
			this.name = name;
			this.score = score;
			this.date = date;
		}
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return name+";"+score+";"+date.toString();
		}
	}
}
