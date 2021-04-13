import com.nocteq.petridish.conway
import com.nocteq.petridish.petriDish
import com.soywiz.korge.Korge
import com.soywiz.korge.input.keys
import com.soywiz.korim.color.Colors

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
            'i' -> petriDish.showStatistics = !petriDish.showStatistics
            'r' -> petriDish.conway()
        }
    }
}
