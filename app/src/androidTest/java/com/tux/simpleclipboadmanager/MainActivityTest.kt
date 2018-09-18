package com.tux.simpleclipboadmanager

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.vanniktech.espresso.core.utils.DrawableMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Rule
    @JvmField
    public var mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun toggleTracking() {
        onView(withId(R.id.action_tracking))
            .check(matches(ViewMatchers.isDisplayed()))
            .check(matches(DrawableMatcher.withDrawable(R.drawable.outline_visibility_24)))
    }
}