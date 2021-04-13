package com.nocteq.petridish

import com.soywiz.korge.view.*

/** Container of all life. */
class PetriDish(private val columns: Int, private val rows: Int) : Container() {
    private var last = gridOf(columns, rows, ::Cell)
    private var next = gridOf(columns, rows, ::Cell)

    private val agents: MutableMap<Agent.Type, Float> = mutableMapOf()
    private val energies: MutableMap<Energy.Type, Float> = mutableMapOf()

    private val generations = mutableListOf<Long>()

    /** Gets the [Occupant] at [x]:[y] in the last generation. */
    operator fun get(x: Int, y: Int) = if (outside(x, y)) Empty(x, y) else last[x][y].occupant

    /** Gets the intensity (ambient + local) of [type] agent at [x]:[y] in the last generation. */
    operator fun get(x: Int, y: Int, type: Agent.Type) =
        if (outside(x, y)) 0f else (agents[type] ?: 0f) + (last[x][y].agents[type]?.intensity ?: 0f)

    /** Gets the intensity (ambient + local) of [type] energy at [x]:[y] in the last generation. */
    operator fun get(x: Int, y: Int, type: Energy.Type) =
        if (outside(x, y)) 0f else (energies[type] ?: 0f) + (last[x][y].energies[type]?.intensity ?: 0f)

    /** Gets the local intensity of [type] agent at [x]:[y] in the last generation. */
    fun local(x: Int, y: Int, type: Agent.Type) =
        if (outside(x, y)) 0f else last[x][y].agents[type]?.intensity ?: 0f

    /** Gets the local intensity of [type] energy at [x]:[y] in the last generation. */
    fun local(x: Int, y: Int, type: Energy.Type) =
        if (outside(x, y)) 0f else last[x][y].energies[type]?.intensity ?: 0f

    /** Sets the [occupant] at [x]:[y] in the next generation. */
    operator fun set(x: Int, y: Int, occupant: Occupant) {
        next[x][y].occupant = occupant
    }

    /** Sets the local [intensity] of [type] agent at [x]:[y] in the next generation. */
    operator fun set(x: Int, y: Int, type: Agent.Type, intensity: Float) {
        next[x][y].agents.getOrPut(type) { Agent(type) }.intensity = intensity
    }

    /** Sets the local [intensity] of [type] energy at [x]:[y] in the next generation. */
    operator fun set(x: Int, y: Int, type: Energy.Type, intensity: Float) {
        next[x][y].energies.getOrPut(type) { Energy(type) }.intensity = intensity
    }

    /** Contents of a cell within the [PetriDish]. */
    data class Cell(
        val x: Int,
        val y: Int,
        var occupant: Occupant = Empty(x, y),
        val agents: MutableMap<Agent.Type, Agent> = mutableMapOf(),
        val energies: MutableMap<Energy.Type, Energy> = mutableMapOf(),
    )

    fun populate(block: (x: Int, y: Int) -> Occupant) {
        for (x in 0 until columns) {
            for (y in 0 until rows) {
                this[x, y] = block(x, y)
            }
        }
        advance()
        render()
    }

    private fun generate() {
        // TODO diffuse energies and agents

        last.forEach { row -> row.forEach { it.occupant.act(this) } }

        // TODO Hash cells to new generation
        // TODO finish if generation completes cycle

        advance()
    }

    private fun advance() {
        val temp = last
        last = next
        next = temp
    }

    private fun render() {
        removeChildren()
        last.forEach { row ->
            row.forEach {
                it.occupant.view?.addTo(this)
            }
        }
    }

    private fun outside(x: Int, y: Int) = x < 0 || x >= columns || y < 0 || y >= rows

    init {
        addUpdater {
            generate()
            render()
        }
    }

    companion object {
        private inline fun <reified T> gridOf(width: Int, height: Int, block: (x: Int, y: Int) -> T): Array<Array<T>> =
            (0 until width).map { x -> (0 until height).map { y -> block(x, y) }.toTypedArray() }.toTypedArray()
    }
}

fun Stage.petriDish(columns: Int, rows: Int) = PetriDish(columns, rows).addTo(this)

interface Actor {
    val view: View? get() = null

    fun act(petriDish: PetriDish) {}
}
