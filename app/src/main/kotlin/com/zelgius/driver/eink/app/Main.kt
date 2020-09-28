package com.zelgius.driver.eink.app

import com.zelgius.driver.eink.InkyColor
import com.zelgius.driver.eink.WHatHAL
import com.zelgius.driver.eink.output
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mhashim6.pi4k.digitalInput
import mhashim6.pi4k.digitalOutput
import org.test.si4432.RPi3GPIO
import java.awt.BasicStroke
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt
import java.io.PrintWriter
import kotlin.random.Random


fun main() {
    //img.output

    runBlocking {
        println(
            """
            **************************
            *   Starting to display  *
            ************************** 
        """.trimIndent()
        )


        val dc = digitalOutput(RPi3GPIO.GPIO_22)
        drawToScreen(
            WHatHALPi4J(
                busy = digitalInput(RPi3GPIO.GPIO_5),
                reset = digitalOutput(RPi3GPIO.GPIO_16),
                dc = dc,
                color = InkyColor.RED_HT
            )
        )

        println("Done screen 1")
/*        delay(17000)

        println("Begin screen 2")
        drawToScreen(
            WHatHALPi4J(
                busy = digitalInput(RPi3GPIO.GPIO_5),
                reset = digitalOutput(RPi3GPIO.GPIO_16),
                dc = dc,
                cs = digitalOutput(RPi3GPIO.GPIO_20),
                color = InkyColor.RED_HT
            )
        )*/

        /*WHatHALDummy(
            color = InkyColor.RED
        ).setImage(img)*/
    }


}

private suspend fun drawToScreen(driver: WHatHALPi4J) {
    val bufferedImage = BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB)
    with(bufferedImage.createGraphics()) {
        paint = Color.getColor("InkyBlack", 1)
        fillRect(0, 0, 400, 300)


        paint = Color.getColor("InkyWhite", 0)
        stroke = BasicStroke(10f)
        drawRect(
            Random.nextInt(10, 290),
            Random.nextInt(10, 190),
            100, 100
        )

        paint = Color.getColor("InkyRed", 2)
        stroke = BasicStroke(10f)
        drawOval(
            Random.nextInt(10, 290),
            Random.nextInt(10, 190),
            100, 100
        )
    }

    val array = when (val b = bufferedImage.data.dataBuffer) {
        is DataBufferInt -> b.data
        is DataBufferByte -> b.data.map { it.toInt() }.toIntArray()
        else -> error("type not managed ${b::class.java}")
    }

    val img = Array(bufferedImage.height)
    { IntArray(bufferedImage.width) { 0 } }

    for (i in 0 until bufferedImage.height) {
        for (j in 0 until bufferedImage.width) {
            img[i][j] = array[i * img[i].size + j] and 0x00FFFFFF

        }
    }
    driver.setImage(img)
}


/*

   Bitmap sourceBitmap = BitmapFactory.decodeFile(imgPath);
    float[] colorTransform = {
            0, 1f, 0, 0, 0,
            0, 0, 0f, 0, 0,
            0, 0, 0, 0f, 0,
            0, 0, 0, 1f, 0};

    ColorMatrix colorMatrix = new ColorMatrix();
    colorMatrix.setSaturation(0f); //Remove Colour
    colorMatrix.set(colorTransform); //Apply the Red

    ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
    Paint paint = new Paint();
    paint.setColorFilter(colorFilter);

    Display display = getWindowManager().getDefaultDisplay();

    Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap, 0, (int)(display.getHeight() * 0.15), display.getWidth(), (int)(display.getHeight() * 0.75));

    image.setImageBitmap(resultBitmap);

    Canvas canvas = new Canvas(resultBitmap);
    canvas.drawBitmap(resultBitmap, 0, 0, paint);

 */
