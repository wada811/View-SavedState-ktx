package com.wada811.viewsavedstatektx.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.savedstate.SavedStateRegistry.AutoRecreated
import androidx.savedstate.SavedStateRegistryOwner
import com.wada811.viewsavedstatektx.sample.databinding.SampleActivityBinding
import com.wada811.viewsavedstatektx.savedState

class SampleActivity : AppCompatActivity() {
    private val state by savedState()
    private var count: Int by state.property(0)
    private var text: String by state.property("Activity: ${hashCode()}")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SampleActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        state.runOnNextRecreation<OnNextRecreation>()
        binding.title.text = text
        binding.count.text = "$count"
        Log.d("View-SavedState-ktx", "count: $count")
        binding.plus.setOnClickListener {
            count++
            binding.count.text = "$count"
            binding.countViewInActivity.countUp()
            Log.d("View-SavedState-ktx", "plus: $count")
        }
        binding.restart.setOnClickListener {
            startActivity(createIntent(this, count))
        }
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment, SampleFragment.newInstance())
                .commit()
        }
    }

    private class OnNextRecreation : AutoRecreated {
        override fun onRecreated(owner: SavedStateRegistryOwner) {
            Log.d("View-SavedState-ktx", "SampleActivity: OnNextRecreation: $owner")
            if (owner is SampleActivity) {
                Log.d("View-SavedState-ktx", "SampleActivity: intent: ${owner.intent?.extras}")
                owner.onRecreated()
            }
        }
    }

    private fun onRecreated() {
        Toast.makeText(this, "onRecreated", Toast.LENGTH_LONG).show()
    }

    companion object {
        fun createIntent(context: Context, count: Int) = Intent(context, SampleActivity::class.java)
            .also {
                it.putExtra(SampleActivity::count.name, count + 10)
                it.putExtra(SampleActivity::text.name, "Activity: ${context.hashCode()}")
            }
    }
}
