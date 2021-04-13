package com.nocteq.petridish

import com.soywiz.kds.Queue
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korma.math.roundDecimalPlaces

/** Container of all life. */
class PetriDish(private val columns: Int, private val rows: Int) : Container() {
    private var last = gridOf(columns, rows, ::Cell)
    private var next = gridOf(columns, rows, ::Cell)

    private val agents: MutableMap<Agent.Type, Float> = mutableMapOf()
    private val energies: MutableMap<Energy.Type, Float> = mutableMapOf()

    private var generation = 0
    private val generationCodes = Queue<Int>()
    private var repeats = 0

    var showStatistics = true

    private var message: String? = null

    private val fps = Queue<Double>()

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

    fun populate(block: (x: Int, y: Int) -> Occupant) {
        resetStats()

        for (x in 0 until columns) {
            for (y in 0 until rows) {
                this[x, y] = block(x, y)
            }
        }

        detect()
        advance()
        render(TimeSpan(0.0))
    }

    private fun generate(timeSpan: TimeSpan) {
        last.forEach { row -> row.forEach { it.act(this) } }

        detect()
        advance()
        render(timeSpan)
    }

    private fun detect() {
        generation++
        val generationCode = next.map { row -> row.joinToString { it.code } }.joinToString { it }
        val generationHash = generationCode.hashCode()
        if (generationCodes.contains(generationHash)) {
            if (repeats == 0) {
                while (generationCodes.peek() != generationHash) generationCodes.dequeue()
                repeats = generationCodes.size
                message = "Cycles every $repeats @ ${generation - repeats}"
                // TODO repopulate if enabled
            }
        } else {
            generationCodes.enqueue(generationHash)
            if (generationCodes.size > MAX_GENERATIONS_CHECKED) generationCodes.dequeue()
        }

    }

    private fun advance() {
        val temp = last
        last = next
        next = temp
    }

    private fun render(timeSpan: TimeSpan) {
        removeChildren()

        last.forEach { row -> row.forEach { it.render(this) } }

        if (showStatistics) {
            text("Generation $generation", textSize = 3.0).xy(1, 1).alpha = .5
            timeSpan.milliseconds.let {
                if (it > 0.0) fps.enqueue(1000.0 / timeSpan.milliseconds)
                if (fps.size > 60) fps.dequeue()
            }
            text("FPS ${fps.average().roundDecimalPlaces(2)}", textSize = 3.0).xy(1, 5).alpha = .5
        }

        message?.let { text(it, textSize = 5.0).centerOn(this).alpha = .9 }
    }

    private fun resetStats() {
        generation = 0
        generationCodes.clear()
        repeats = 0
        message = null
        fps.clear()
    }

    private fun outside(x: Int, y: Int) = x < 0 || x >= columns || y < 0 || y >= rows

    init {
        addUpdater { generate(it) }
    }

    /** Contents of a cell within the [PetriDish]. */
    private data class Cell(
        val x: Int,
        val y: Int,
        var occupant: Occupant = Empty(x, y),
        val agents: MutableMap<Agent.Type, Agent> = mutableMapOf(),
        val energies: MutableMap<Energy.Type, Energy> = mutableMapOf(),
    ) {
        val code
            get() = occupant.code + agents.values.joinToString { it.code } + energies.values.joinToString { it.code }


        fun act(petriDish: PetriDish) {
            occupant.act(petriDish)
            agents.values.forEach { it.act(petriDish) }
            energies.values.forEach { it.act(petriDish) }
        }

        fun render(petriDish: PetriDish) {
            agents.values.mapNotNull { it.view }.forEach { it.addTo(petriDish) }
            occupant.view?.addTo(petriDish)
            energies.values.mapNotNull { it.view }.forEach { it.addTo(petriDish) }
        }
    }

    companion object {
        private const val MAX_GENERATIONS_CHECKED = 4096

        private inline fun <reified T> gridOf(width: Int, height: Int, block: (x: Int, y: Int) -> T): Array<Array<T>> =
            (0 until width).map { x -> (0 until height).map { y -> block(x, y) }.toTypedArray() }.toTypedArray()
    }
}

fun Stage.petriDish(columns: Int, rows: Int) = PetriDish(columns, rows).addTo(this)

/** An actor within the dish. */
interface Actor {
    /** Identifies the state of the actor. */
    val code: String

    val view: View? get() = null

    fun act(petriDish: PetriDish) {}
}
