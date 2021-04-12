package com.nocteq.petridish

import com.soywiz.korge.view.RoundRect
import com.soywiz.korge.view.View

/** An occupant of one or multiple cells. */
interface Occupant : Actor

/** Neither an [Organism] nor a [Device]. */
object Empty : Occupant

/** A cell that contains a living (or pseudo-living) organism. */
interface Organism : Occupant
// TODO storage for any type-specific data
//    var size: Int = 1,
//    var orientation: Float = 0f,
//    var speed: Float = 0f,

/** The body of an [Organism] that extends beyond its center (at [column]:[row]). */
data class Extent(val column: Int, val row: Int) : Occupant

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
    open val spread: IntRange,
    open val frequency: Float,
    open val probability: Float,
    open var supply: Long,
) : Occupant

/** A device that replicates its host cell's [Substance]s instead of diffusing them into adjacent cells. */
data class Replicator(
    override val spread: IntRange = 1..8,
    override val frequency: Float = 1f,
    override val probability: Float = 1f,
    override var supply: Long = -1,
) : Device(spread, frequency, probability, supply) {
    override val view: View? = null

    override fun act(column: Int, row: Int, petriDish: PetriDish) {
        // TODO replicate local substances
    }
}

/** A device that spawns [organism]s. */
data class Spawner(
    val organism: Organism,
    override val spread: IntRange = 1..8,
    override val frequency: Float = 1f,
    override val probability: Float = 1f,
    override var supply: Long = -1,
) : Device(spread, frequency, probability, supply) {
    override val view: View? = null

    override fun act(column: Int, row: Int, petriDish: PetriDish) {
        // TODO spawn organism
    }
}

class Conway(private val alive: Boolean = true) : Organism {
    override val view: View? get() = if (alive) RoundRect(12.0, 12.0, 1.0) else null

    override fun act(column: Int, row: Int, petriDish: PetriDish) {
        val neighbors = isAlive(column - 1, row - 1, petriDish) +
                isAlive(column, row - 1, petriDish) +
                isAlive(column + 1, row - 1, petriDish) +
                isAlive(column + 1, row, petriDish) +
                isAlive(column + 1, row + 1, petriDish) +
                isAlive(column, row + 1, petriDish) +
                isAlive(column - 1, row + 1, petriDish) +
                isAlive(column - 1, row, petriDish)
        petriDish[column, row] = when {
            alive -> if (neighbors == 2 || neighbors == 3) live else dead
            else -> if (neighbors == 3) live else dead
        }
    }

    private fun isAlive(column: Int, row: Int, petriDish: PetriDish): Int {
        val neighbor = petriDish[column, row]
        return if (neighbor is Conway && neighbor.alive) 1 else 0
    }

    companion object {
        val live = Conway(true)
        val dead = Conway(false)
    }
}
