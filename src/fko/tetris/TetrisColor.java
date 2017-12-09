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

import javafx.scene.paint.Color;

/**
 * Represents the 7 different Tetris colors
 */
public enum TetrisColor {
	
	EMPTY,
	YELLOW,		// O
	LBLUE,		// I
	PURPLE,		// T
	ORANGE,		// L
	BLUE,		// J
	GREEN,		// S
	RED;		// Z

    public Color toColor() {
        switch (this) {
            case EMPTY	: return Color.BLACK;
            case YELLOW	: return Color.YELLOW;
            case LBLUE	: return Color.LIGHTBLUE;
            case PURPLE	: return Color.PURPLE;
            case ORANGE	: return Color.ORANGE;
            case BLUE	: return Color.BLUE;
            case GREEN	: return Color.GREEN;
            case RED	: return Color.RED;
        }
		return null;
    }
}
