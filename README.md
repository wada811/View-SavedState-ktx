View-SavedState-ktx
=====

`View-SavedState-ktx` make easy handling saved state by delegated property.

## Overview

`View-SavedState-ktx` is kotlin extensions of [AndroidX SavedState](https://developer.android.com/jetpack/androidx/releases/savedstate).

## Usage

- You can automatically save and restore properties by delegated properties.
- The properties default value is a value of **Activity's Intent** or **Fragment's arguments**.
- You can use in Activity, Fragment and View. Activity's sample is below.

```kotlin
class SampleActivity : AppCompatActivity(R.layout.sample_activity) {
    private val state by savedState()
    private var count: Int by state.property(defaultValue = 0)
    private var text: String by state.property({ getString(it, "default value") }, { key, value -> putString(key, value) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // You can use properties after onCreate
        count++
        // You can register callback of onRecreated
        state.runOnNextRecreation<OnNextRecreation>()
    }

    private class OnNextRecreation : AutoRecreated {
        override fun onRecreated(owner: SavedStateRegistryOwner) {
            // You can do something when Activity is re-created.
            if (owner is SampleActivity) {
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
                it.putExtra(SampleActivity::count.name, count)
            }
    }
}
```

## Gradle

[![](https://jitpack.io/v/wada811/View-SavedState-ktx.svg)](https://jitpack.io/#wada811/View-SavedState-ktx)

```groovy
repositories {
    maven { url "https://www.jitpack.io" }
}

dependencies {
    implementation 'com.github.wada811:View-SavedState-ktx:x.y.z'
}
```

## License

Copyright (C) 2020 wada811

Licensed under the Apache License, Version 2.0
