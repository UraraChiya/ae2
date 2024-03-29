/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.storage.cells;

/**
 * @author yueh
 */
public enum CellState {
    /**
     * No cell at all
     */
    ABSENT(0),

    /**
     * A cell without anything stored
     */
    EMPTY(0x00FF00),

    /**
     * Stored something, but neither types nor totally full
     */
    NOT_EMPTY(0x00AAFF),

    /**
     * Available types exhausted
     */
    TYPES_FULL(0xFFAA00),

    /**
     * Full cell, technically could have free types
     */
    FULL(0xFF0000);

    /**
     * A color indicating this state.
     */
    private final int stateColor;

    CellState(int stateColor) {
        this.stateColor = stateColor;
    }

    /**
     * @return A color representative of this state. Used for the drive LEDs for example.
     */
    public int getStateColor() {
        return stateColor;
    }
}
