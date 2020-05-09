package com.wada811.viewsavedstate.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.savedstate.SavedStateRegistry.AutoRecreated
import androidx.savedstate.SavedStateRegistryOwner
import com.wada811.viewsavedstate.sample.databinding.MainActivityBinding
import com.wada811.viewsavedstate.savedState

class MainActivity : AppCompatActivity() {
    private val state by savedState()
    private var count by state.property({ getInt(it) }, { key, value -> putInt(key, value) })
    private var text by state.property({ getString(it, "Activity: ${hashCode()}") }, { key, value -> putString(key, value) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        state.runOnNextRecreation<OnNextRecreation>()
        binding.title.text = text
        binding.count.text = "$count"
        Log.d("SavedStateProperty", "count: $count")
        binding.plus.setOnClickListener {
            count++
            binding.count.text = "$count"
            Log.d("SavedStateProperty", "plus: $count")
        }
        binding.restart.setOnClickListener {
            startActivity(createIntent(this, count))
        }
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment, MainFragment.newInstance())
                .commit()
        }
    }

    private class OnNextRecreation : AutoRecreated {
        override fun onRecreated(owner: SavedStateRegistryOwner) {
            Log.d("SavedStateProperty", "OnNextRecreation: $owner")
            if (owner is MainActivity) {
                Log.d("SavedStateProperty", "intent: ${owner.intent?.extras}")
                owner.onRecreated()
            }
        }
    }

    private fun onRecreated() {
        Toast.makeText(this, "onRecreated", Toast.LENGTH_LONG).show()
    }

    companion object {
        fun createIntent(context: Context, count: Int) = Intent(context, MainActivity::class.java)
            .also {
                it.putExtra(MainActivity::count.name, count + 10)
                it.putExtra(MainActivity::text.name, "Activity: ${context.hashCode()}")
            }
    }
}