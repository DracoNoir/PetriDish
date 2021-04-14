import com.nocteq.petridish.Conway
import com.nocteq.petridish.conway
import com.nocteq.petridish.petriDish
import com.soywiz.korev.MouseButton
import com.soywiz.korge.Korge
import com.soywiz.korge.input.keys
import com.soywiz.korge.input.mouse
import com.soywiz.korge.input.onMove
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Point
import kotlin.math.roundToInt

const val CELL_SIZE = 20
const val WIDTH = CELL_SIZE * 16
const val HEIGHT = CELL_SIZE * 9

suspend fun main() = Korge(
    width = 1920,
    height = 1080,
    virtualWidth = WIDTH,
    virtualHeight = HEIGHT,
    bgcolor = Colors.BLACK) {
    val petriDish = petriDish(WIDTH, HEIGHT).apply { conway() }

    keys.typed {
        when (it.character.toLowerCase()) {
            ' ' -> petriDish.running = !petriDish.running
            '\t' -> petriDish.step = !petriDish.step
            'i' -> petriDish.showStatistics = !petriDish.showStatistics
            'r' -> petriDish.conway()
        }
    }

    var downAt: Point? = null
    var button: MouseButton? = null
    var dragging = false
    petriDish.mouse {
        down {
            downAt = mouseXY.rounded()
            button = it.button
        }
        up {
            val upAt = mouseXY.rounded()
            if (!dragging && downAt == upAt) {
                with(petriDish[upAt.x.toInt(), upAt.y.toInt()] as Conway) {
                    alive = !alive
                    petriDish.cycle = 0
                }
            }
            downAt = null
            button = null
            dragging = false
        }
    }
    onMove {
        val moveAt = mouseXY.rounded()
        if (dragging || (downAt != null && downAt != moveAt)) {
            dragging = true

            if (button!!.isLeft) {
                (petriDish[moveAt.x.toInt(), moveAt.y.toInt()] as Conway).alive = true
                petriDish.cycle = 0
            } else if (button!!.isRight) {
                (petriDish[moveAt.x.toInt(), moveAt.y.toInt()] as Conway).alive = false
                petriDish.cycle = 0
            }
        }
    }
}

fun Point.rounded() = Point(x.roundToInt(), y.roundToInt())
