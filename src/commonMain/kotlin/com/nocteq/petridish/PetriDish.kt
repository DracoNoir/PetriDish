package com.nocteq.petridish

import com.soywiz.korge.view.*
import kotlin.random.Random

/** Container of all life. */
class PetriDish(private val columns: Int, private val rows: Int, private val cellSize: Double) : Container() {
    private val energies: MutableMap<Energy.Type, Float> = mutableMapOf()
    private val agents: MutableMap<Agent.Type, Float> = mutableMapOf()

    private val even = (1..(columns * rows)).map { Cell() }.toTypedArray()
    private val odd = (1..(columns * rows)).map { Cell() }.toTypedArray()
    private var generation = 0

    init {
        even.forEach { it.occupant = if (Random.nextFloat() > .75f) Conway.live else Conway.dead }
    }

    private val current get() = if (generation % 2 == 0) even else odd
    private val next get() = if (generation % 2 == 1) even else odd

    private val generations = mutableListOf<Long>()

    /** Gets the [Occupant] at [column]:[row] in the current generation. */
    operator fun get(column: Int, row: Int) = if (outside(column, row)) Empty else current[at(column, row)].occupant

    /** Sets the [occupant] at [column]:[row] in the next generation. */
    operator fun set(column: Int, row: Int, occupant: Occupant) {
        if (outside(column, row)) return
        next[at(column, row)].occupant = occupant
    }

    /** Gets the local intensity of [type] energy at [column]:[row] in the current generation. */
    fun local(column: Int, row: Int, type: Energy.Type) =
        if (outside(column, row)) 0f else current[at(column, row)].energies[type]?.intensity ?: 0f

    /** Gets the intensity (ambient + local) of [type] energy at [column]:[row] in the current generation. */
    operator fun get(column: Int, row: Int, type: Energy.Type) =
        if (outside(column, row)) 0f
        else (energies[type] ?: 0f) + (current[at(column, row)].energies[type]?.intensity ?: 0f)

    /** Sets the local [intensity] of [type] energy at [column]:[row] in the next generation. */
    operator fun set(column: Int, row: Int, type: Energy.Type, intensity: Float) {
        if (outside(column, row)) return
        next[at(column, row)].energies.getOrPut(type) { Energy(type) }.intensity = intensity
    }

    /** Gets the local intensity of [type] agent at [column]:[row] in the current generation. */
    fun local(column: Int, row: Int, type: Agent.Type) =
        if (outside(column, row)) 0f else current[at(column, row)].agents[type]?.intensity ?: 0f

    /** Gets the intensity (ambient + local) of [type] agent at [column]:[row] in the current generation. */
    operator fun get(column: Int, row: Int, type: Agent.Type) =
        if (outside(column, row)) 0f
        else (agents[type] ?: 0f) + (current[at(column, row)].agents[type]?.intensity ?: 0f)

    /** Sets the local [intensity] of [type] agent at [column]:[row] in the next generation. */
    operator fun set(column: Int, row: Int, type: Agent.Type, intensity: Float) {
        if (outside(column, row)) return
        next[at(column, row)].agents.getOrPut(type) { Agent(type) }.intensity = intensity
    }

    /** Contents of a cell within the [PetriDish]. */
    private data class Cell(
        var occupant: Occupant = Empty,
        val energies: MutableMap<Energy.Type, Energy> = mutableMapOf(),
        val agents: MutableMap<Agent.Type, Agent> = mutableMapOf(),
    )

    private fun advance() {
        removeChildren()

        // TODO diffuse energies and agents

        for (column in 0 until columns) {
            for (row in 0 until rows) {
                this[column, row].act(column, row, this)
            }
        }
        generation++

        for (column in 0 until columns) {
            for (row in 0 until rows) {
                this[column, row].view?.xy(column * cellSize, row * cellSize)?.addTo(this)
            }
        }

        // TODO Hash cells to new generation
        // TODO finish if generation completes cycle
    }

    private fun at(column: Int, row: Int) = row * columns + column
    private fun outside(column: Int, row: Int) = column < 0 || column >= columns || row < 0 || row >= rows

    init {
        addUpdater { advance() }
    }
}

fun Stage.petriDish(columns: Int, rows: Int, cellSize: Double) = PetriDish(columns, rows, cellSize).addTo(this)

interface Actor {
    val view: View? get() = null

    fun act(column: Int, row: Int, petriDish: PetriDish) {}
}
