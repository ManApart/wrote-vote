import kotlinx.html.TagConsumer
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement

fun mainPage() {
     val root = el("root")
     root.innerHTML = ""
     root.append {
        categorySection()
     }
 }

fun TagConsumer<HTMLElement>.categorySection() {
    TODO("Not yet implemented")
}
