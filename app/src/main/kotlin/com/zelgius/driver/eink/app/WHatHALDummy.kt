package com.zelgius.driver.eink.app

import com.pi4j.io.gpio.Pin
import com.pi4j.io.spi.SpiChannel
import com.pi4j.io.spi.SpiDevice
import com.pi4j.io.spi.SpiFactory
import com.pi4j.io.spi.SpiMode
import com.zelgius.driver.eink.InkyColor
import com.zelgius.driver.eink.WHatHAL
import com.zelgius.driver.eink.output
import mhashim6.pi4k.digitalInput
import mhashim6.pi4k.digitalOutput
import java.io.PrintWriter
import java.nio.ByteBuffer
import kotlin.math.min

class WHatHALDummy(
    color: InkyColor
) : WHatHAL(color = color) {

    init {
        borderColor = InkyColor.RED
    }


    override fun readBusy(): Boolean = false

    override fun writeReset(high: Boolean) {

    }

    override fun writeDC(high: Boolean) {
    }

    override fun writeChipSelect(high: Boolean) {
    }

    override fun writeSpi(value: Int) {
    }

    override fun writeSpi(value: IntArray) {

    }

    suspend fun setImage(image: Array<IntArray>) {
        image.copyInto(buffer)
        show(true)
    }

}
