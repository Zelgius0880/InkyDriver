package com.zelgius.driver.eink

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.ByteOrder

internal class ArrayUtilsTest {

    private fun setupArray() = with(Array(5) { IntArray(4) { 0 } }) {
        // 1111
        // 2222
        // 3333
        // 4444
        // 5555
        for (i in this.indices) {
            for (j in this[i].indices)
                this[i][j] = i + 1
        }

        printTable(this)
        this
    }

    @Test
    @DisplayName("Test an array rotation clock wise")
    fun `Array rotateClockwise`() {
        val array = setupArray()

        val clockWise = array.rotateClockwise()
        printTable(clockWise)
        assertArrayEquals(clockWise[0], intArrayOf(5, 4, 3, 2, 1))
    }

    @Test
    @DisplayName("Test an array rotation counter clock wise")
    fun `Array rotateCounterClockwise`() {

        val array = setupArray()

        val counterClockWise = array.rotateCounterClockwise()
        printTable(counterClockWise)
        assertArrayEquals(counterClockWise[0], intArrayOf(1, 2, 3, 4, 5))

    }

    @Test
    @DisplayName("Test an array rotation with degree")
    fun `Array rotate`() {

        val array = setupArray()

        with(array.rotate(90)) {
            printTable(this)
            assertArrayEquals(this[0], intArrayOf(5, 4, 3, 2, 1))
        }

        with(array.rotate(-90)) {
            printTable(this)
            assertArrayEquals(this[0], intArrayOf(1, 2, 3, 4, 5))
        }

        with(array.rotate(270)) {
            printTable(this)
            assertArrayEquals(this[0], intArrayOf(1, 2, 3, 4, 5))
        }

        with(array.rotate(180)) {
            printTable(this)
            assertArrayEquals(this[0], intArrayOf(5, 5, 5, 5))
        }
    }

    @Test
    @DisplayName("test packing boolean array to bytes")
    fun packToBits() {
        val a1 = arrayOf(
            booleanArrayOf(true, false, false, true),   //1001 0000
            booleanArrayOf(false, false, false, true),  //0001 0000
            booleanArrayOf(false, true, false, true)    //0101 0000
        )
        val r1 = intArrayOf(0b10010000, 0b00010000, 0b01010000)
        assertArrayEquals(ArrayUtils.packToBits(a1), r1)

        val a2 = arrayOf(
            booleanArrayOf(true, false, false, true, true, false, false, true),   //1001 1001
            booleanArrayOf(false, false, false, true, true, false, false, true),  //0001 1001
            booleanArrayOf(false, true, false, true, false, true, true, false),   //0101 0110
            booleanArrayOf(false, true, false, true, true, true, true, true)      //0101 1111
        )
        val r2 = intArrayOf(0b10011001, 0b00011001, 0b01010110, 0b01011111)
        assertArrayEquals(ArrayUtils.packToBits(a2), r2)

        val a3 = arrayOf(
            booleanArrayOf(true, false, false, true, true, false, false, true,   true, true, false),   //1001 1001  1100 0000
            booleanArrayOf(false, false, false, true, true, false, false, true,  false, true, false),  //0001 1001  0100 0000
            booleanArrayOf(false, true, false, true, false, true, true, false,   false, false, false), //0101 0110  0000 0000
            booleanArrayOf(false, true, false, true, true, true, true, true,     true, true, true)     //0101 1111  1110 0000
        )
        val r3 = intArrayOf(0b10011001,  0b11000000, 0b00011001,  0b01000000, 0b01010110,  0b00000000, 0b01011111,  0b11100000)
        assertArrayEquals(ArrayUtils.packToBits(a3), r3)

        val a4 = arrayOf(
            booleanArrayOf(
                true, false, false, true, true, false, false, true,
                true, true, false, true, true, false, true, true
            ),   //1001 1001    1101 1011
            booleanArrayOf(
                false, false, false, true, true, false, false, true,
                false, false, false, true, true, true, false, true
            )  //0001 1001    0001 1101
        )
        val r4 = intArrayOf(0b10011001, 0b11011011, 0b00011001, 0b00011101)
        val r4b = intArrayOf(0b10011001, 0b11011011, 0b10011000, 0b10111000)
        assertArrayEquals(ArrayUtils.packToBits(a4), r4)
        assertArrayEquals(ArrayUtils.packToBits(a4, order = ByteOrder.LITTLE_ENDIAN), r4b)

    }

    private fun printTable(array: Array<IntArray>) =
        printTable(array.map { it.toTypedArray() }.toTypedArray())

    private fun <T> printTable(array: Array<Array<T>>) {
        array.forEach {
            println("${it.joinToString()} ")
        }
        println()
    }
}
