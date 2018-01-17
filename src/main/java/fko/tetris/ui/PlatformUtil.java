/**
 * The MIT License (MIT)
 *
 * "Tetris by Frank Kopp"
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

package fko.tetris.ui;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javafx.application.Platform;

/**
 * Provides a few helper methods for the JavaFX Platform
 *
 * @author fkopp
 */
public class PlatformUtil {

    /**
     * Calls Platform.runLater to run the runnable but waits until it has
     * finished execution.
     * Waiting is implemented by means of a CountDownLatch.
     * @param runnable
     */
    public static void platformRunAndWait(final Runnable runnable) {
        if (runnable == null)
            throw new NullPointerException("runnable");

        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        // create latch
        CountDownLatch latch = new CountDownLatch(1);

        // extend runnable with latch count down
        Runnable r = () -> {
            try {
                runnable.run();
            } finally {
                latch.countDown();
            }
        };

        // run the extended runnable through Platform.runLater()
        Platform.runLater(r);

        // wait for the latch
        try {
            latch.await();
        } catch (InterruptedException e) {
            // ignore
        }

    }

    /**
     * Invokes a Runnable in JFX Thread and waits until it's finished.
     * Similar to SwingUtilities.invokeAndWait.
     * This method is not intended to be called from the FAT, but when this happens the runnable is executed synchronously.
     *
     * @param runnable The Runnable that has to be executed on JFX application thread.
     * @throws RuntimeException which wraps a possible InterruptedException or ExecutionException
     */
    static public void runFutureTask(final Runnable runnable) {
        if (runnable == null)
            throw new NullPointerException("runnable");

        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        // run the FutureTask with Platform.runLater() and query the
        // result with get() which blocks until the task is done.
        try {
            FutureTask<Void> future = new FutureTask<>(runnable, null);
            Platform.runLater(future);
            future.get();
        }
        catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
