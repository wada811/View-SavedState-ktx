package com.wada811.viewsavedstate.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.savedstate.SavedStateRegistry.AutoRecreated
import androidx.savedstate.SavedStateRegistryOwner
import com.wada811.viewsavedstate.sample.databinding.MainFragmentBinding
import com.wada811.viewsavedstate.savedState

class MainFragment : Fragment(R.layout.main_fragment) {
    private val state by savedState()
    private var count by state.property({ getInt(it) }, { key, value -> putInt(key, value) })
    private var text by state.property({ getString(it, "Fragment: ${hashCode()}") }, { key, value -> putString(key, value) })
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        state.runOnNextRecreation<OnNextRecreation>()
        val binding = MainFragmentBinding.bind(view)
        binding.title.text = text
        binding.count.text = "$count"
        Log.d("SavedStateProperty", "count: $count")
        binding.plus.setOnClickListener {
            count++
            binding.count.text = "$count"
            Log.d("SavedStateProperty", "plus: $count")
        }
        binding.replace.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(id, newInstance(count + 10))
                .commit()
        }
    }

    private class OnNextRecreation : AutoRecreated {
        override fun onRecreated(owner: SavedStateRegistryOwner) {
            Log.d("SavedStateProperty", "OnNextRecreation: $owner")
            if (owner is MainFragment) {
                Log.d("SavedStateProperty", "arguments: ${owner.arguments}")
                owner.onRecreated()
            }
        }
    }

    private fun onRecreated() {
        Toast.makeText(requireContext(), "onRecreated", Toast.LENGTH_LONG).show()
    }

    companion object {
        fun newInstance(count: Int = 0) = MainFragment().apply {
            arguments = bundleOf(
                this::count.name to count,
                this::text.name to "Fragment: ${hashCode()}"
            )
        }
    }
}