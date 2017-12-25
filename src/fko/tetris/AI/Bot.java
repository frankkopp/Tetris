/**
 * 
 */
package fko.tetris.AI;

/**
 * A Bot is a AI player running in its own thread. It can be started and stopped.<br/> 
 * A real implementation will watch the matrix and the current Tetrimino (and maybe the next queue and hold queue) to calculate its best move.<br/>
 * The best move will be send to the game via the <code>_game.controlQueueAdd()</code> method as a the ui would do when a human player is pressing a key.<br/> 
 */
public interface Bot {
	void startBot();
	void stopBot();
}
