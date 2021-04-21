package com.jj.androidenergyconsumer.utils

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId

fun performClick(viewId: Int) {
    onView(withId(viewId)).perform(click())
}