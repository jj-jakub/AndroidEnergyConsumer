package com.jj.androidenergyconsumer.utils

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId

fun performClick(viewId: Int) {
    onView(withId(viewId)).perform(click())
}

fun typeText(viewId: Int, text: String) {
    onView(withId(viewId)).perform(clearText())
    onView(withId(viewId)).perform(typeText(text), closeSoftKeyboard())
}