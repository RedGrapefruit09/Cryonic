package com.redgrapefruit.cryonic.util

import com.redgrapefruit.cryonic.RANDOM

/**
 * A range of integers to randomly pick from
 */
class IntRange(
    /**
     * Minimum value of the resulting int
     */
    private val min: Int,
    /**
     * Maximum value of the resulting int
     */
    private val max: Int
) {
    /**
     * Picks a random number from this range
     *
     * @return Random number
     */
    fun pick(): Int {
        val initial = max - min
        return RANDOM.nextInt(initial) + min
    }
}