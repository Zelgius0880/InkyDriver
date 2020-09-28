package com.zelgius.driver.eink

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class InkyHAL(
    private val resolution: Pair<Int, Int>,
    private val color: InkyColor = InkyColor.BLACK,
    private val vFlip: Boolean = false,
    private val hFlip: Boolean = false,
    private val rotation: Int = 0
) {
    abstract fun readBusy(): Boolean
    abstract fun writeReset(high: Boolean)
    abstract fun writeDC(high: Boolean)
    abstract fun writeSpi(value: Int)
    abstract fun writeSpi(value: IntArray)

    protected val buffer = Array(resolution.second) { IntArray(resolution.first) { 0 } }
    var borderColor = InkyColor.WHITE

    /**
    Inky Lookup Tables.
    These lookup tables comprise of two sets of values.
    The first set of values, formatted as binary, describe the voltages applied during the six update phases:
    Phase 0     Phase 1     Phase 2     Phase 3     Phase 4     Phase 5     Phase 6
    A B C D
    0b01001000, 0b10100000, 0b00010000, 0b00010000, 0b00010011, 0b00000000, 0b00000000,  LUT0 - Black
    0b01001000, 0b10100000, 0b10000000, 0b00000000, 0b00000011, 0b00000000, 0b00000000,  LUT1 - White
    0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000,  NOT USED BY HARDWARE
    0b01001000, 0b10100101, 0b00000000, 0b10111011, 0b00000000, 0b00000000, 0b00000000,  LUT3 - Yellow or Red
    0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000,  LUT4 - VCOM
    There are seven possible phases, arranged horizontally, and only the phases with duration/repeat information
    (see below) are used during the update cycle.
    Each phase has four steps: A, B, C and D. Each step is represented by two binary bits and these bits can
    have one of four possible values representing the voltages to be applied. The default values follow:
    0b00: VSS or Ground
    0b01: VSH1 or 15V
    0b10: VSL or -15V
    0b11: VSH2 or 5.4V
    During each phase the Black, White and Yellow (or Red) stages are applied in turn, creating a voltage
    differential across each display pixel. This is what moves the physical ink particles in their suspension.
    The second set of values, formatted as hex, describe the duration of each step in a phase, and the number
    of times that phase should be repeated:
    Duration                Repeat
    A     B     C     D
    0x10, 0x04, 0x04, 0x04, 0x04,  <-- Timings for Phase 0
    0x10, 0x04, 0x04, 0x04, 0x04,  <-- Timings for Phase 1
    0x04, 0x08, 0x08, 0x10, 0x10,      etc
    0x00, 0x00, 0x00, 0x00, 0x00,
    0x00, 0x00, 0x00, 0x00, 0x00,
    0x00, 0x00, 0x00, 0x00, 0x00,
    0x00, 0x00, 0x00, 0x00, 0x00,
    The duration and repeat parameters allow you to take a single sequence of A, B, C and D voltage values and
    transform them into a waveform that - effectively - wiggles the ink particles into the desired position.
    In all of our LUT definitions we use the first and second phases to flash/pulse and clear the display to
    mitigate image retention. The flashing effect is actually the ink particles being moved from the bottom to
    the top of the display repeatedly in an attempt to reset them back into a sensible resting position.

     */

    private val luts = mapOf(
        InkyColor.BLACK to intArrayOf(
            0b01001000, 0b10100000, 0b00010000, 0b00010000, 0b00010011, 0b00000000, 0b00000000,
            0b01001000, 0b10100000, 0b10000000, 0b00000000, 0b00000011, 0b00000000, 0b00000000,
            0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000,
            0b01001000, 0b10100101, 0b00000000, 0b10111011, 0b00000000, 0b00000000, 0b00000000,
            0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000,
            0x10, 0x04, 0x04, 0x04, 0x04,
            0x10, 0x04, 0x04, 0x04, 0x04,
            0x04, 0x08, 0x08, 0x10, 0x10,
            0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00
        ),
        InkyColor.RED to intArrayOf(
            0b01001000, 0b10100000, 0b00010000, 0b00010000, 0b00010011, 0b00000000, 0b00000000,
            0b01001000, 0b10100000, 0b10000000, 0b00000000, 0b00000011, 0b00000000, 0b00000000,
            0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000,
            0b01001000, 0b10100101, 0b00000000, 0b10111011, 0b00000000, 0b00000000, 0b00000000,
            0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000,
            0x40, 0x0C, 0x20, 0x0C, 0x06,
            0x10, 0x08, 0x04, 0x04, 0x06,
            0x04, 0x08, 0x08, 0x10, 0x10,
            0x02, 0x02, 0x02, 0x40, 0x20,
            0x02, 0x02, 0x02, 0x02, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00
        ),
        InkyColor.RED_HT to intArrayOf(
            0b01001000, 0b10100000, 0b00010000, 0b00010000, 0b00010011, 0b00010000, 0b00010000,
            0b01001000, 0b10100000, 0b10000000, 0b00000000, 0b00000011, 0b10000000, 0b10000000,
            0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000,
            0b01001000, 0b10100101, 0b00000000, 0b10111011, 0b00000000, 0b01001000, 0b00000000,
            0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000,
            0x43, 0x0A, 0x1F, 0x0A, 0x04,
            0x10, 0x08, 0x04, 0x04, 0x06,
            0x04, 0x08, 0x08, 0x10, 0x0B,
            0x01, 0x02, 0x01, 0x10, 0x30,
            0x06, 0x06, 0x06, 0x02, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00
        ),
        InkyColor.YELLOW to intArrayOf(
            0b11111010, 0b10010100, 0b10001100, 0b11000000, 0b11010000, 0b00000000, 0b00000000,
            0b11111010, 0b10010100, 0b00101100, 0b10000000, 0b11100000, 0b00000000, 0b00000000,
            0b11111010, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000, 0b00000000,
            0b11111010, 0b10010100, 0b11111000, 0b10000000, 0b01010000, 0b00000000, 0b11001100,
            0b10111111, 0b01011000, 0b11111100, 0b10000000, 0b11010000, 0b00000000, 0b00010001,
            0x40, 0x10, 0x40, 0x10, 0x08,
            0x08, 0x10, 0x04, 0x04, 0x10,
            0x08, 0x08, 0x03, 0x08, 0x20,
            0x08, 0x04, 0x00, 0x00, 0x10,
            0x10, 0x08, 0x08, 0x00, 0x20,
            0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00
        )
    )

    suspend fun setup() {
        writeReset(false)
        delay(100)
        writeReset(true)
        delay(100)

        sendCommand(0x12) // soft reset
        waitBusy()
    }

    private suspend fun waitBusy() = withContext(Dispatchers.IO) {
        while (readBusy()){
            delay(100)
            println("waiting")
        }
    }

    private suspend fun update(bufferA: IntArray, bufferB: IntArray, wait: Boolean = true) =
        withContext(Dispatchers.IO) {
            setup()

            val (cols, rows) = resolution
            val packedHeight = rows
                .toShort()
                .toByteArray(ByteOrder.LITTLE_ENDIAN)
                .map { it.toInt() }
                .toIntArray()

            sendCommand(0x74, 0x54) // Set Analog Block Control
            sendCommand(0x7e, 0x3b)  // Set Digital Block Control

            sendCommand(0x01, *packedHeight, 0x00)  // Gate setting

            sendCommand(0x03, 0x17)  // Gate Driving Voltage
            sendCommand(0x04, 0x41, 0xAC, 0x32)  // Source Driving Voltage

            sendCommand(0x3a, 0x07)  // Dummy line period
            sendCommand(0x3b, 0x04)  // Gate line width
            sendCommand(0x11, 0x03)  // Data entry mode setting 0x03 = X/Y increment

            sendCommand(0x2c, 0x3c)  // VCOM Register, 0x3c = -1.5v?

            sendCommand(0x3c, 0b00000000)

            when {
                borderColor == InkyColor.BLACK ->
                    sendCommand(0x3c, 0b00000000)  // GS Transition Define A + VSS + LUT0

                borderColor == InkyColor.RED && (color == InkyColor.RED || color == InkyColor.RED_HT) ->
                    sendCommand(0x3c, 0b01110011)  // Fix Level Define A + VSH2 + LUT3

                borderColor == InkyColor.YELLOW && color == InkyColor.YELLOW ->
                    sendCommand(0x3c, 0b00110011)  // GS Transition Define A + VSH2 + LUT3

                else ->
                    sendCommand(0x3c, 0b00110001)  // GS Transition Define A + VSH2 + LUT1

            }

            if (color == InkyColor.YELLOW)
                sendCommand(0x04, 0x07, 0xAC, 0x32)  // Set voltage of VSH and VSL
            if ((color == InkyColor.RED || color == InkyColor.RED_HT) && resolution == 400 to 300)
                sendCommand(0x04, 0x30, 0xAC, 0x22)

            sendCommand(0x32, *luts[color] ?: error("LUT for $color is NULL"))  // Set LUTs

            sendCommand(0x44, 0x00, (cols / 8) - 1)  // Set RAM X Start/End
            sendCommand(0x45, 0x00, 0x00, *packedHeight)  // Set RAM Y Start/End

            // 0x24 == RAM B/W, 0x26 == RAM Red/Yellow/etc
            arrayOf(0x24 to bufferA, 0x26 to bufferB).forEach {
                val (cmd, buf) = it
                sendCommand(0x4e, 0x00)  // Set RAM X Pointer Start
                sendCommand(0x4f, 0x00, 0x00)  // Set RAM Y Pointer Start
                sendCommand(cmd, *buf)
            }

            sendCommand(0x22, 0xC7)  // Display Update Sequence
            sendCommand(0x20)  // Trigger Display Update
            delay(50)

            if (wait) {
                waitBusy()
                sendCommand(0x10, 0x01)  // Enter Deep Sleep
            }
        }

    fun setPixel(x: Int, y: Int, v: InkyColor) {
        buffer[x][y] = v.code
    }

    suspend fun show(wait: Boolean = true) {
        val region = buffer
            .let { b ->
                if (vFlip) b.map { it.reversedArray() }.toTypedArray()
                else b
            }
            .let { b ->
                if (hFlip) b.reversedArray()
                else b
            }
            .rotate(rotation)

        region.output()
        update(
            bufferA = ArrayUtils.packToBits(
                array = region
                    .map {
                        it.map { code -> code != InkyColor.BLACK.code /*It's ok: it really is != BLACK*/ }
                            .toBooleanArray()
                    }
                    .toTypedArray()
            ),
            bufferB = ArrayUtils.packToBits(
                array = region
                    .map { it.map { code -> code == 2 /* Color code of RED or YELLOW */ }.toBooleanArray() }
                    .toTypedArray()
            ),
            wait = wait
        )
    }


    private suspend fun sendCommand(command: Int, vararg data: Int) {
        writeDC(false)
        writeSpi(command)

        if (data.isNotEmpty()) {
            sendData(data)
        }
    }

    private suspend fun sendData(data: IntArray) {
        writeDC(true)
        delay(50)
        writeSpi(data)
    }


}

enum class InkyColor(val code: Int) {
    WHITE(0),
    BLACK(1),
    RED(2),
    RED_HT(2),
    YELLOW(2)
}

fun Int.toByteArray(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
    val buffer = ByteBuffer.allocate(4)
    buffer.order(order)
    buffer.putInt(this)

    return buffer.array()
}

fun Short.toByteArray(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
    val buffer = ByteBuffer.allocate(2)
    buffer.order(order)
    buffer.putShort(this)

    return buffer.array()
}

