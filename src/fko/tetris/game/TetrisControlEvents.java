/**
 * 
 */
package fko.tetris.game;

/**
 * @author fkopp
 *
 */
public enum TetrisControlEvents {
	
	LEFT, 		// move left
	RIGHT, 		// move right
	RTURN,		// clockwise turn
	LTURN, 		// counter clockwise turn
	SOFTDOWN, 	// fall fast
	HARDDOWN,	// fall to surface
	HOLD,		// put current Tetrimino in hold
	NONE;		// null operation
	
}
