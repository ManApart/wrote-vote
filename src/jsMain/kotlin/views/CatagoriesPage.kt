package views

import categories
import components.saveAndToast
import createCategory
import el
import getCategories
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import replaceElement
import updateUrl

fun categoriesPage() {
    updateUrl(Route.CATEGORIES)
    replaceElement {
        nav()
        categories = getCategories().associateBy { it.id }
        div {
            h3 { +"Existing Categories:" }
            ul {
                categories.values.forEach { category ->
                    li { +category.name }
                }
            }

            span {
                input {
                    id = "add-category-input"
                    placeholder = "New Category"
                }
                button {
                    +"Add Category"
                    onClickFunction = {
                        val cat = el<HTMLInputElement>("add-category-input").value
                        saveAndToast(
                            "Saved Category",
                            "Failed to save category",
                            { categoriesPage() }) {
                            createCategory(cat)
                        }
                    }
                }
            }
            //Display categories
            //Allow create new category
        }
    }
}