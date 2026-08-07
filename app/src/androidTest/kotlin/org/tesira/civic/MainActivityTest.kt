package org.tesira.civic

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
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
    fun testAppLaunch() {
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.tvVp)).check(matches(isDisplayed()))
    }

    @Test
    fun testMenuNavigation() {
        // 1. Klicke auf Buying (mit allOf + isDisplayed, um Eindeutigkeit zu erzwingen)
        onView(allOf(withId(R.id.buyingFragment), isDisplayed())).perform(click())
        Thread.sleep(2000)

        // 2. Klicke auf Inventory
        onView(allOf(withId(R.id.inventoryFragment), isDisplayed())).perform(click())
        Thread.sleep(2000)

        // 3. Klicke auf Library
        onView(allOf(withId(R.id.allCardsFragment), isDisplayed())).perform(click())
        Thread.sleep(2000)
    }

    @Test
    fun testNewGameSelection() {
        // 1. Das Overflow-Menü ("...") öffnen
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)

        // 2. "New Game" klicken
        onView(withText(R.string.menu_new_game)).perform(click())

        // 3. Im Dialog: Spinner für Zivilisation klicken
        onView(withId(R.id.spinner_civilization)).perform(click())

        // 4. "Hati" auswählen. Wir sagen Espresso explizit, dass er im Popup suchen soll.
        onData(`is`("Hati"))
            .inRoot(androidx.test.espresso.matcher.RootMatchers.isPlatformPopup())
            .perform(click())

        // 5. Start-Knopf drücken
        onView(withText(R.string.start_button_text)).perform(click())

        // 6. Prüfen, ob Dashboard aktualisiert wurde (Wert "9" für Hati)
        onView(withId(R.id.tvCivilization)).check(matches(withText("A.S.T. Ranking: 9")))
    }
}
