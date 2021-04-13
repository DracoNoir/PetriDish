package com.nocteq.petridish

import com.soywiz.korge.view.SolidRect
import com.soywiz.korge.view.View
import com.soywiz.korge.view.xy
import kotlin.random.Random

/** An occupant of the cell at [x]:[y]. */
abstract class Occupant(open var x: Int, open var y: Int) : Actor

/** Neither an [Organism] nor a [Device]. */
data class Empty(override var x: Int, override var y: Int) : Occupant(x, y)

/** A living (or pseudo-living) organism. */
abstract class Organism(override var x: Int, override var y: Int) : Occupant(x, y)
// TODO storage for any type-specific data
//    var size: Int = 1,
//    var orientation: Float = 0f,
//    var speed: Float = 0f,

/** The body of an [Organism] that extends beyond its center (at [centerX]:[centerY]). */
data class Extent(override var x: Int, override var y: Int, val centerX: Int, val centerY: Int) : Occupant(x, y)

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

    override fun act(petriDish: PetriDish) {
        // TODO spawn organism
    }
}

data class Conway(
    override var x: Int,
    override var y: Int,
    private val alive: Boolean = true,
    private val _view: View = SolidRect(.95, .95).xy(x, y),
) : Organism(x, y) {
    private val leftOf = x - 1
    private val rightOf = x + 1
    private val above = y - 1
    private val below = y + 1

    override val view: View? get() = if (alive) _view else null

    override fun act(petriDish: PetriDish) {
        val neighbors = isAlive(leftOf, above, petriDish) +
                isAlive(x, above, petriDish) +
                isAlive(rightOf, above, petriDish) +
                isAlive(rightOf, y, petriDish) +
                isAlive(rightOf, below, petriDish) +
                isAlive(x, below, petriDish) +
                isAlive(leftOf, below, petriDish) +
                isAlive(leftOf, y, petriDish)
        petriDish[x, y] = copy(alive = if (alive) neighbors == 2 || neighbors == 3 else neighbors == 3)
    }

    private fun isAlive(neighborX: Int, neighborY: Int, petriDish: PetriDish) =
        petriDish[neighborX, neighborY].let { if (it is Conway && it.alive) 1 else 0 }
}

/** Populates the dish with [Conway] cells. */
fun PetriDish.conway() = populate { x, y -> Conway(x, y, Random.nextFloat() > .5f) }
