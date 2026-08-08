package org.tesira.civic

import android.widget.Button
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testSpecificBuying() {
        // 1. Reset
        testNewGameSelection()

        // 2. Navigation
        onView(allOf(withId(R.id.buyingFragment), isDisplayed())).perform(click())
        Thread.sleep(1000)

        // 3. 500 Schatz eingeben und Bestätigen
        onView(withId(R.id.treasure)).perform(replaceText("500"), pressImeActionButton())
        // Neutraler Klick um Fokus zu verlieren
        onView(withId(R.id.top_controls_layout)).perform(click())
        Thread.sleep(1000)

        // 4. Sortieren nach Preis
        var sorted = false
        repeat(7) {
            if (!sorted) {
                onView(withId(R.id.btnSort)).check { view, _ ->
                    val button = view as Button
                    if (button.text == "Price (c)") {
                        sorted = true
                    } else {
                        button.performClick()
                    }
                }
                if (!sorted) Thread.sleep(500)
            }
        }
        Thread.sleep(1000)

        // 5. Mysticism auswählen
        clickCardWithName("Mysticism")
        Thread.sleep(1000)

        // 6. Sculpture auswählen
        clickCardWithName("Sculpture")
        Thread.sleep(1000)

        // 7. Kauf abschließen
        onView(withId(R.id.btnBuy)).perform(click())

        // 8. 5s auf Übersicht verweilen
        onView(withId(R.id.bought_cards_title)).check(matches(isDisplayed()))
        Thread.sleep(5000)

        // 9. Schließen und Dashboard prüfen
        onView(withId(R.id.close_button)).perform(click())
        onView(withId(R.id.tvVp)).check(matches(isDisplayed()))
    }

    @Test
    fun testNewGameSelection() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.menu_new_game)).perform(click())
        onView(withId(R.id.spinner_civilization)).perform(click())
        onData(`is`("Hati"))
            .inRoot(isPlatformPopup())
            .perform(click())
        onView(withText(R.string.start_button_text)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.tvCivilization)).check(matches(withText("A.S.T. Ranking: 9")))
    }

    private fun clickCardWithName(name: String) {
        // Erst hinscrollen
        onView(withId(R.id.purchasable_cards)).perform(
            RecyclerViewActions.scrollTo<AllCardsAdapter.CardViewHolder>(
                hasDescendant(withText(name))
            )
        )
        // Dann die Kachel an der Stelle klicken, wo der Text gefunden wurde
        onView(withId(R.id.purchasable_cards)).perform(
            RecyclerViewActions.actionOnItem<AllCardsAdapter.CardViewHolder>(
                hasDescendant(withText(name)),
                click()
            )
        )
    }
}