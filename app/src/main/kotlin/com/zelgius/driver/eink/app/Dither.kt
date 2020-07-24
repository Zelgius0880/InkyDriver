package com.zelgius.driver.eink.app

import java.awt.Color
import java.awt.Graphics
import java.awt.GridLayout
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.JComponent
import javax.swing.JFrame
import kotlin.math.max
import kotlin.math.min


// from :
// http://stackoverflow.com/questions/5940188/how-to-convert-a-24-bit-png-to-3-bit-png-using-floyd-steinberg-dithering


internal object Dither {
    private fun findClosestPaletteColor(c: C3?, palette: Array<C3>): C3 {
        var closest = palette[0]
        for (n in palette) {
            if (n.diff(c) < closest.diff(c)) {
                closest = n
            }
        }
        return closest
    }

    private fun floydSteinbergDithering(img: BufferedImage): BufferedImage {
        val palette = arrayOf(
            C3(0, 0, 0),  // black
            C3(255, 255, 255),  // black
            C3(255, 0, 0)  // black
          /*  C3(0, 0, 255),  // green
            C3(0, 255, 0),  // blue
            C3(0, 255, 255),  // cyan
            C3(255, 0, 0),  // red
            C3(255, 0, 255),  // purple
            C3(255, 255, 0),  // yellow
            C3(255, 255, 255) // white*/
        )
        val w = img.width
        val h = img.height
        val d = Array(h) { arrayOfNulls<C3>(w) }
        for (y in 0 until h) {
            for (x in 0 until w) {
                d[y][x] = C3(img.getRGB(x, y))
            }
        }
        for (y in 0 until img.height) {
            for (x in 0 until img.width) {
                val oldColor = d[y][x]
                val newColor = findClosestPaletteColor(oldColor, palette)
                img.setRGB(x, y, newColor.toColor().rgb)
                val err = oldColor!!.sub(newColor)
                if (x + 1 < w) {
                    d[y][x + 1] = d[y][x + 1]!!.add(err.mul(7.0 / 16))
                }
                if (x - 1 >= 0 && y + 1 < h) {
                    d[y + 1][x - 1] = d[y + 1][x - 1]!!.add(err.mul(3.0 / 16))
                }
                if (y + 1 < h) {
                    d[y + 1][x] = d[y + 1][x]!!.add(err.mul(5.0 / 16))
                }
                if (x + 1 < w && y + 1 < h) {
                    d[y + 1][x + 1] = d[y + 1][x + 1]!!.add(err.mul(1.0 / 16))
                }
            }
        }
        return img
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val fis = FileInputStream(File("wallpaper.png"))
        val normal = ImageIO.read(
            //URL("https://upload.wikimedia.org/wikipedia/en/7/7d/Lenna_%28test_image%29.png")
            fis
        )//.getSubimage(100, 100, 300, 300)
        val dithered = floydSteinbergDithering(
            ImageIO.read(
                //URL("https://upload.wikimedia.org/wikipedia/en/7/7d/Lenna_%28test_image%29.png")
                FileInputStream(File("wallpaper.png"))
            )
        )//.getSubimage(100, 100, 300, 300)
        val frame = JFrame("Test")
        frame.layout = GridLayout(1, 2)
        frame.add(object : JComponent() {
            val serialVersionUID = 2963702769416707676L
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                g.drawImage(normal, 0, 0, this)
            }
        })
        frame.add(object : JComponent() {
            val serialVersionUID = -6919658458441878769L
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                g.drawImage(dithered, 0, 0, this)
            }
        })
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(650, 350)
        frame.isVisible = true
    }

    internal class C3 {
        var r: Int
        var g: Int
        var b: Int

        constructor(c: Int) {
            val color = Color(c)
            r = color.red
            g = color.green
            b = color.blue
        }

        constructor(r: Int, g: Int, b: Int) {
            this.r = r
            this.g = g
            this.b = b
        }

        fun add(o: C3): C3 {
            return C3(r + o.r, g + o.g, b + o.b)
        }

        fun clamp(c: Int): Int {
            return max(0, min(255, c))
        }

        fun diff(o: C3?): Int {
            val Rdiff = o!!.r - r
            val Gdiff = o.g - g
            val Bdiff = o.b - b
            return Rdiff * Rdiff + Gdiff * Gdiff + Bdiff * Bdiff
        }

        fun mul(d: Double): C3 {
            return C3((d * r).toInt(), (d * g).toInt(), (d * b).toInt())
        }

        fun sub(o: C3): C3 {
            return C3(r - o.r, g - o.g, b - o.b)
        }

        fun toColor(): Color {
            return Color(clamp(r), clamp(g), clamp(b))
        }

        fun toRGB(): Int {
            return toColor().rgb
        }
    }
}
