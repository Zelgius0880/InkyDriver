package com.zelgius.driver.eink

import java.io.PrintWriter
import java.nio.ByteOrder
import kotlin.math.abs

object ArrayUtils {
    fun packToBits(array: Array<BooleanArray>, order: ByteOrder = ByteOrder.BIG_ENDIAN): IntArray {
        val list = mutableListOf<Int>()

        array.forEachIndexed { _, v ->
            var byte = 0x00
            var shift = if(order == ByteOrder.BIG_ENDIAN) 7 else 0

            v.forEachIndexed { _, w ->
                if (shift < 0 && order == ByteOrder.BIG_ENDIAN
                    || shift > 7 && order == ByteOrder.LITTLE_ENDIAN) {
                    shift = if(order == ByteOrder.BIG_ENDIAN) 7 else 0
                    list.add(byte)
                    byte = 0
                }

                if(w) byte = byte or ( 1 shl shift)
                if(order == ByteOrder.BIG_ENDIAN) --shift
                else ++shift
            }
            list.add(byte)
        }

        return list.toIntArray()
    }
}


fun Array<IntArray>.output(){
    val out = PrintWriter("filename.txt")
    for (i in this.indices) {
        for (j in this[i].indices) {
            out.print(if (this[i][j] == 0) " " else this[i][j])
        }

        out.println()
    }

    out.close()
}
fun Array<IntArray>.rotate(rotation: Int): Array<IntArray> {
    var array = this
    return with(rotation % 360) {
        for (r in abs(this) downTo 1 step 90) {
            array = if (this > 0) array.rotateClockwise()
            else array.rotateCounterClockwise()
        }

        array
    }
}

fun Array<IntArray>.rotateClockwise() =
    Array(this.first().size) { IntArray(this.size) { 0 } }.also { rotated ->
        val lengthX = this.first().size
        val lengthY = this.size

        for ((newRow, i) in (0 until lengthX).withIndex()) {
            for ((newColumn, j) in (lengthY - 1 downTo 0).withIndex()) {
                rotated[newRow][newColumn] = this[j][i]
            }
        }
    }

fun Array<IntArray>.rotateCounterClockwise() =
    Array(this.first().size) { IntArray(this.size) { 0 } }.also { rotated ->
        val lengthX = this.first().size
        val lengthY = this.size

        for ((newRow, i) in (lengthX - 1 downTo 0).withIndex()) {
            for ((newColumn, j) in (0 until lengthY).withIndex()) {
                rotated[newRow][newColumn] = this[j][i]
            }
        }
    }
