package com.zelgius.driver.eink.app

import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.GpioPinDigitalOutput
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

class WHatHALPi4J(
    private val dc: GpioPinDigitalOutput,
    private val busy: GpioPinDigitalInput,
    private val reset: GpioPinDigitalOutput,
    color: InkyColor
) : WHatHAL(color = color) {

    constructor(
                 dc: Pin,
                 busy: Pin,
                 reset: Pin,
                 color: InkyColor) : this(digitalOutput(dc), digitalInput(busy), digitalOutput(reset), color)
    private val spi = SpiFactory.getInstance(
        SpiChannel.CS1,
        488000, // default spi speed 1 MHz
        SpiMode.MODE_0   // default spi mode 0
    )

    init {
        borderColor = InkyColor.RED
    }

    override fun readBusy(): Boolean = busy.isHigh

    override fun writeReset(high: Boolean) {
        reset.setState(high)
    }

    override fun writeDC(high: Boolean) {
        dc.setState(high)
    }


    override fun writeSpi(value: Int) {
        spi.write(value.toByte())
    }

    override fun writeSpi(value: IntArray) {
        val buffer = ByteBuffer.wrap(value.map { it.toByte() }.toByteArray())

        while (buffer.remaining() > 0) {
            with(ByteArray(1024)) {
                val get = min(buffer.remaining(), 1024)
                buffer.get(this, 0, get)
                spi.write(this, 0, get)

            }
        }
    }

    suspend fun setImage(image: Array<IntArray>) {
        image.copyInto(buffer)
        show(true)
    }

}
