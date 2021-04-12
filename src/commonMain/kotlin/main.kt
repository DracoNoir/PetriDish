import com.nocteq.petridish.petriDish
import com.soywiz.korge.Korge
import com.soywiz.korim.color.Colors

suspend fun main() = Korge(width = 1920, height = 1080, bgcolor = Colors.BLACK) {
    petriDish(160, 90, 12.0)
}
