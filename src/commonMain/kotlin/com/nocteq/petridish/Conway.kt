package com.nocteq.petridish

import com.soywiz.korge.view.SolidRect
import com.soywiz.korge.view.View
import com.soywiz.korge.view.xy
import com.soywiz.korim.color.Colors
import kotlin.random.Random

/**
 * An organism that behaves according to the rules of
 * [Conway's Game of Life](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life)
 */
data class Conway(
    override var x: Int,
    override var y: Int,
    private val alive: Boolean = true,
    private val _view: SolidRect = SolidRect(.95, .95).xy(x, y),
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
        when {
            alive && neighbors == 2 -> _view.color = Colors.RED
            alive && neighbors == 3 -> _view.color = Colors.GREEN
            !alive && neighbors == 3 -> _view.color = Colors.BLUE
        }
        petriDish[x, y] = copy(alive = if (alive) neighbors == 2 || neighbors == 3 else neighbors == 3)
    }

    private fun isAlive(neighborX: Int, neighborY: Int, petriDish: PetriDish) =
        petriDish[neighborX, neighborY].let { if (it is Conway && it.alive) 1 else 0 }
}

/** Populates the dish with [Conway] cells. */
fun PetriDish.conway() = populate { x, y -> Conway(x, y, Random.nextFloat() > .5f) }
