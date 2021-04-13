package com.nocteq.petridish

/**
 * A substance that occupies a cell, affecting [Agent]s and [Organism]s within it.
 *
 * Over each generation, a substance naturally diffuses from one cell (the originator) into each adjacent cell (the
 * receivers) as it attempts to seek equilibrium. Unless the receiver resists some or all of the transfer, the
 * [intensity] of energy in the originator is divided evenly between it and each receiver. If the resulting amount is
 * equal to or less than [dissolution], then the substance dissolves into nothing; if the resulting amount exceeds
 * [saturation], then the excess amount evaporates into nothing.
 */
sealed class Substance(open var intensity: Float, open val dissolution: Float, open val saturation: Float) : Actor

/** An energy. */
data class Energy(
    val type: Type,
    override var intensity: Float = 0f,
) : Substance(intensity, type.dissolution, type.saturation) {
    enum class Type(val dissolution: Float = 0f, val saturation: Float = 1f) {
        ELECTRICITY,
        HEAT,
        LIGHT,
        MAGNETISM,
        RADIATION_ALPHA,
        RADIATION_BETA,
        RADIATION_GAMMA,
        ULTRAVIOLET,
    }
}

/** A physical substance. */
data class Agent(
    val type: Type,
    override var intensity: Float = 0f,
) : Substance(intensity, type.dissolution, type.saturation) {
    enum class Type(val dissolution: Float = 0f, val saturation: Float = 1f) {
        ACID,
        CARBOHYDRATE,
        DARKENER, // resists LIGHT and ULTRAVIOLET
        HYDROCARBON,
        INSULATOR, // resists HEAT
        NEUTRAL,
        PRESSURE,
        RESISTOR, // resists ELECTRICITY and MAGNETISM
        SHIELDING, // resists RADIATION_*
        THICKENER, // resists movement
    }
}
