package com.nocteq.petridish

import com.soywiz.korge.view.View

/** An occupant of the cell at [x]:[y]. */
abstract class Occupant(open var x: Int, open var y: Int) : Actor {
    abstract val code: String
}

/** Neither an [Organism] nor a [Device]. */
data class Empty(override var x: Int, override var y: Int) : Occupant(x, y) {
    override val code = " "
}

/** A living (or pseudo-living) organism. */
abstract class Organism(override var x: Int, override var y: Int) : Occupant(x, y)

/** The body of an [Organism] that extends beyond its center (at [centerX]:[centerY]). */
data class Extent(override var x: Int, override var y: Int, val centerX: Int, val centerY: Int) : Occupant(x, y) {
    override val code = " "
}

/**
 * A device that produces something at a [frequency] per generation into adjacent cells over a [spread] with a certain
 * [probability] (with 0 meaning never and 1 meaning always) until exhausting its [supply] (with -1 meaning an infinite
 * supply), at which time the device disintegrates and produce a single something into its own cell at the same
 * [probability].
 *
 * The [spread] identifies cells starting with `1` indicating the cell in the row above the device (`D`) and continuing
 * clockwise around it:
 * ```
 * 8 1 2
 * 7 D 3
 * 6 5 4
 * ```
 */
abstract class Device(
    override var x: Int,
    override var y: Int,
    open val spread: IntRange,
    open val frequency: Float,
    open val probability: Float,
    open var supply: Long,
) : Occupant(x, y)

/** A device that replicates its host cell's [Substance]s instead of diffusing them into adjacent cells. */
data class Replicator(
    override var x: Int,
    override var y: Int,
    override val spread: IntRange = 1..8,
    override val frequency: Float = 1f,
    override val probability: Float = 1f,
    override var supply: Long = -1,
) : Device(x, y, spread, frequency, probability, supply) {
    override val view: View? = null

    override val code = "R@$supply"

    override fun act(petriDish: PetriDish) {
        // TODO replicate local substances
    }
}

/** A device that spawns [organism]s. */
data class Spawner(
    override var x: Int,
    override var y: Int,
    val organism: Organism,
    override val spread: IntRange = 1..8,
    override val frequency: Float = 1f,
    override val probability: Float = 1f,
    override var supply: Long = -1,
) : Device(x, y, spread, frequency, probability, supply) {
    override val view: View? = null

    override val code = "S.${organism.code}@$supply"

    override fun act(petriDish: PetriDish) {
        // TODO spawn organism
    }
}
