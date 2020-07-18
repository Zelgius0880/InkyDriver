package com.zelgius.driver.eink

class Image : AbstractImage(){
    fun quantize(colors: Int = 256, method: Quantization, palette: Palette? = null, dither: Int = 1) {
        load()
        palette ?.let{
            it.load()

        }
    }

    enum class Quantization {
        MEDIAN_CUT, FAST_OCTREE, LIB_IMAGE_QUANT, MAX_COVERAGE
    }
}


class Palette : AbstractImage(){

}

abstract class AbstractImage {

    fun load(){

    }
}
