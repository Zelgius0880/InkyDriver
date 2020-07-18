package com.zelgius.driver.eink

abstract class WHatHAL (
    resolution: Pair<Int, Int> = 400 to 300,
    color: InkyColor = InkyColor.BLACK,
    vFlip: Boolean = false,
    hFlip: Boolean = false
) : InkyHAL(resolution, color, vFlip, hFlip)
