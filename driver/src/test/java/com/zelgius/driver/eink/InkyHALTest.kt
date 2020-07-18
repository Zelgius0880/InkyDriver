package com.zelgius.driver.eink

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.ByteOrder

@Suppress("FunctionName")
internal class InkyHALTest {

    @Test
    @DisplayName("Test the byte serialization of a Int")
    fun `Int toByteArray`() {
        val littleEndian = arrayOf( 0x2c, 0x01,0x00, 0x00)
        val bigEndian = arrayOf(0x00, 0x00, 0x01, 0x2c)

        assertArrayEquals(300.toByteArray().map { it.toInt() }.toTypedArray(), bigEndian)
        assertArrayEquals(300.toByteArray(ByteOrder.LITTLE_ENDIAN).map { it.toInt() }.toTypedArray(), littleEndian)
    }


    @Test
    @DisplayName("Test the byte serialization of a Short")
    fun `Short toByteArray`() {
        val littleEndian = arrayOf(0x2c, 0x01)
        val bigEndian = arrayOf(0x01, 0x2c)

        assertArrayEquals(300.toShort().toByteArray().map { it.toInt() }.toTypedArray(), bigEndian)
        assertArrayEquals(300.toShort().toByteArray(ByteOrder.LITTLE_ENDIAN).map { it.toInt() }.toTypedArray(), littleEndian)
    }

}
