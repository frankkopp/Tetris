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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * 
 */
public class TetrisTimerTest {

	/**
	 * Test method for {@link fko.tetris.TetrisTimer#TetrisTimer(long)}.
	 */
	@Test
	public final void testTetrisTimer() {
		TetrisTimer timer = new TetrisTimer(2000);
		assertTrue(timer.getRemainingTime() == 2000);
		assertFalse(timer.isRunning());
	}

	/**
	 * Test method for {@link fko.tetris.TetrisTimer#start()}.
	 */
	@Test
	public final void testStart() {
		TetrisTimer timer = new TetrisTimer(2000);
		timer.start();
		assertTrue(timer.isRunning());
		timer.start(); // ignored
		assertTrue(timer.isRunning());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertTrue(timer.getRemainingTime() < 2000);
	}

	/**
	 * Test method for {@link fko.tetris.TetrisTimer#stop()}.
	 */
	@Test
	public final void testStop() {
		TetrisTimer timer = new TetrisTimer(2000);
		timer.start();
		assertTrue(timer.isRunning());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {/* nothing */}
		timer.stop();
		long t = timer.getRemainingTime();
		assertTrue(t < 2000);
		assertFalse(timer.isRunning());
		// restart
		timer.start();
		assertTrue(timer.isRunning());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {/* nothing */}
		assertTrue(timer.getRemainingTime() < t);
		timer.stop();
		assertFalse(timer.isRunning());
	}

	/**
	 * Test method for {@link fko.tetris.TetrisTimer#reset()}.
	 */
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public final void testReset() {
		TetrisTimer timer = new TetrisTimer(2000);
		timer.start();

		// resetting a timer while running causes exception
		exception.expect(RuntimeException.class);
		timer.reset();
		ExpectedException.none();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {/* nothing */}

		timer.stop();
		long rt = timer.getRemainingTime();
		timer.reset();
		assertTrue(timer.getRemainingTime() > rt);
		assertTrue(timer.getRemainingTime() == 2000);

	}

	@Test
	public final void testLoop() {

		TetrisTimer timer = new TetrisTimer(1000);
		for(int i=0;i<100;i++ ) {
			timer.start();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {/* nothing */}
			timer.stop();
			long rt = timer.getRemainingTime();
			timer.reset();
			assertTrue(timer.getRemainingTime() > rt);
			assertTrue(timer.getRemainingTime() == 1000);
		}


	}

}
