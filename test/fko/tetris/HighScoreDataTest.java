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
package fko.tetris;

import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;

import fko.tetris.HighScoreData.HighScoreEntry;

/**
 * 
 */
public class HighScoreDataTest {

	@Test
	public final void testRead() {
		HighScoreData hd = HighScoreData.getInstance();
		assertTrue(hd!=null);
		assertTrue(hd.getList() != null);
	}

	@Test
	public final void testWrite() {
		HighScoreData hd = HighScoreData.getInstance();
		boolean result = hd.saveToFile();
		assertTrue(result);
	}

	@Test
	public final void testAdd() {
		HighScoreData hd = HighScoreData.getInstance();
		assertTrue(hd!=null);
		final List<HighScoreEntry> list = hd.getList();
		assertTrue(list != null);
		final HighScoreEntry newEntry = new HighScoreEntry("Test Name", 0, LocalDateTime.now());
		hd.addEntry(newEntry);
		assertTrue(list.contains(newEntry));
		list.stream().forEach((e) -> System.out.println(e.toString()));
		boolean result = hd.saveToFile();
		assertTrue(result);
	}
	
	@Test
	public final void testAddAndSave() {
		HighScoreData hd = HighScoreData.getInstance();
		assertTrue(hd!=null);
		final List<HighScoreEntry> list = hd.getList();
		assertTrue(list != null);
		final HighScoreEntry newEntry = new HighScoreEntry("Test Name", 0, LocalDateTime.now());
		boolean result = hd.addEntryAndSave(newEntry);
		assertTrue(list.contains(newEntry));
		assertTrue(result);
		list.stream().forEach((e) -> System.out.println(e.toString()));
	}

	
}
