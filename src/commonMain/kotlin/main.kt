import com.nocteq.petridish.conway
import com.nocteq.petridish.petriDish
import com.soywiz.korge.Korge
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
    petriDish(WIDTH, HEIGHT).conway()
}
