package com.wada811.viewsavedstate.sample

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView
import androidx.savedstate.SavedStateRegistry.AutoRecreated
import androidx.savedstate.SavedStateRegistryOwner
import com.wada811.viewsavedstate.savedState

class SampleView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private val state by savedState()
    private var count: Int by state.property(0)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        text = "$count"
        state.runOnNextRecreation<OnNextRecreation>()

    }

    private class OnNextRecreation : AutoRecreated {
        override fun onRecreated(owner: SavedStateRegistryOwner) {
            Log.d("View-SavedState-ktx", "SampleView: OnNextRecreation: $owner")
            if (owner is SampleActivity) {
                Log.d("View-SavedState-ktx", "SampleView: intent: ${owner.intent?.extras}")
            } else if (owner is SampleFragment) {
                Log.d("View-SavedState-ktx", "SampleView: arguments: ${owner.arguments}")
            }
        }
    }

    fun countUp() {
        count++
        text = "$count"
    }

    fun countDown() {
        count--
        text = "$count"
    }
}
