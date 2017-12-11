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

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class implements a timer which can be set for an arbitrary time.<br/>
 * The timer can be queried for the remaining time.<br/>
 * The timer allows to be observed so Observer will be called via update() when time runs out.<br/>
 * Timer operations:<br/>
 * 	start, stop, reset, remaining time  
 */
public class TetrisTimer extends Observable {
	
	// timer to set alarm in the after remaining time
	private Timer _remainingTimeTimer; 
	
	// when timer runs out all observers are notified
	private TimerTask task;
	
	private long _timerTime;	// the initial time for this timer
	private long _remaingTime;	// remaining time (e.g. after stop)
	
	private long _lastSystemtime; // save the system time
	
	private boolean _isStarted = false; // flag to see if timer is running or stopped
	
	/**
	 * Creates a timer with the specified countdown time.<br/>
	 * timer.start() to start or resume the timer.<br/>
	 * timer.stop() to stop/pause the timer.<br/>
	 * timer.reset() to reset timer to initial time.<br/>
	 * timer.addObserver() to be notified if timer runs out of time<br/>
	 * @param time in ms
	 */
	public TetrisTimer(long t) {
		_timerTime = t;
		_remaingTime = t;
	}
	
	/**
	 * Starts or re-starts a timer with the remaining time.
	 * Is ignored if already started  
	 */
	public synchronized void start() {
		//System.out.println("start() "+_remaingTime);
		if (_isStarted) return; // ignore
		if (_remaingTime <= 0) return;
		_lastSystemtime = System.currentTimeMillis();
		task =  new TimerTask() {
		    @Override
		    public void run() {
		    	_remainingTimeTimer.cancel();
				_isStarted = false;
		    	_remaingTime = 0;
		        setChanged();
		        notifyObservers();
		    }
		};
		_remainingTimeTimer = new Timer(true);
		_remainingTimeTimer.schedule(task, _remaingTime);
		_isStarted = true;
	}
	
	/**
	 * Stops a running timer. Can be restarted with the remaining time with start()<br/>
	 * Is ignored of timer is already stopped. 
	 */
	public synchronized void stop() {
		if (!_isStarted) return; // ignore
		_remainingTimeTimer.cancel();
		_remaingTime -= System.currentTimeMillis() - _lastSystemtime;
		//System.out.println("stop() "+_remaingTime);
		_isStarted = false;
	}
	
	/**
	 * Reset a stopped timer to its initial remaining time 
	 * @throws RuntimeException if Timer is running
	 */
	public synchronized void reset() {
		if (_isStarted) 
			throw new RuntimeException("Resetting a running timer is not allowed!");

		_remaingTime = _timerTime;
	}
	
	/**
	 * Stops the timer, resets the time to initial time and restarts the timer
	 */
	public void stopResetRestart() {
		stop();
		reset();
		start();
	}
	
	/**
	 * Returns the remaining time of this timer.
	 * @return remaining time
	 */
	public synchronized long getRemainingTime() {
		if (_isStarted)
			return _remaingTime - (System.currentTimeMillis()-_lastSystemtime);
		else
			return _remaingTime;
	}
	
	/**
	 * Returns if the timer is currently running.
	 * @return true if timer is currently running, false otherwise
	 */
	public synchronized boolean isRunning() {
		return _isStarted; 
	}

}
