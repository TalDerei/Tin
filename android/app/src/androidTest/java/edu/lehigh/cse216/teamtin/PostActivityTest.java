package edu.lehigh.cse216.teamtin;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PostActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void postActivityTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction overflowMenuButton = Espresso.onView(
                Matchers.allOf(ViewMatchers.withContentDescription("More options"),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withId(R.id.toolbar),
                                        1),
                                0),
                        ViewMatchers.isDisplayed()));
        overflowMenuButton.perform(ViewActions.click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction overflowMenuButton2 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withContentDescription("More options"),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withId(R.id.toolbar),
                                        1),
                                0),
                        ViewMatchers.isDisplayed()));
        overflowMenuButton2.perform(ViewActions.click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatTextView = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.title), ViewMatchers.withText("Message Board (Home)"),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withId(R.id.content),
                                        0),
                                0),
                        ViewMatchers.isDisplayed()));
        appCompatTextView.perform(ViewActions.click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction overflowMenuButton3 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withContentDescription("More options"),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withId(R.id.toolbar),
                                        1),
                                0),
                        ViewMatchers.isDisplayed()));
        overflowMenuButton3.perform(ViewActions.click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatTextView2 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.title), ViewMatchers.withText("Post a New Message"),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withId(R.id.content),
                                        0),
                                0),
                        ViewMatchers.isDisplayed()));
        appCompatTextView2.perform(ViewActions.click());

        ViewInteraction appCompatEditText = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.editText),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withClassName(Matchers.is("androidx.constraintlayout.widget.ConstraintLayout")),
                                        0),
                                1),
                        ViewMatchers.isDisplayed()));
        appCompatEditText.perform(ViewActions.replaceText("the"), ViewActions.closeSoftKeyboard());

        ViewInteraction appCompatButton = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.buttonOk), ViewMatchers.withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withClassName(Matchers.is("androidx.constraintlayout.widget.ConstraintLayout")),
                                        0),
                                3),
                        ViewMatchers.isDisplayed()));
        appCompatButton.perform(ViewActions.click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction overflowMenuButton4 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withContentDescription("More options"),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withId(R.id.toolbar),
                                        1),
                                0),
                        ViewMatchers.isDisplayed()));
        overflowMenuButton4.perform(ViewActions.click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatTextView3 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.title), ViewMatchers.withText("Post a New Message"),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withId(R.id.content),
                                        0),
                                0),
                        ViewMatchers.isDisplayed()));
        appCompatTextView3.perform(ViewActions.click());

        ViewInteraction appCompatButton2 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.buttonCancel), ViewMatchers.withText("Cancel"),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withClassName(Matchers.is("androidx.constraintlayout.widget.ConstraintLayout")),
                                        0),
                                2),
                        ViewMatchers.isDisplayed()));
        appCompatButton2.perform(ViewActions.click());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
