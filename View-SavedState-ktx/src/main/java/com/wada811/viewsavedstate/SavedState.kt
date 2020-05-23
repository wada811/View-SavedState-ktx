package com.wada811.viewsavedstate

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnAttach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.State.CREATED
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistry.AutoRecreated
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun FragmentActivity.savedState(
    key: String = SavedState.Key,
    defaultBundle: () -> Bundle? = { intent?.extras }
): Lazy<SavedState> = lazy {
    SavedState({ this }, key, defaultBundle).also { savedState ->
        savedState.registerSavedStateProvider()
    }
}

fun Fragment.savedState(
    key: String = SavedState.Key,
    defaultBundle: () -> Bundle? = { arguments }
): Lazy<SavedState> = lazy {
    SavedState({ this }, key, defaultBundle).also { savedState ->
        savedState.registerSavedStateProvider()
    }
}

fun View.savedState(
    key: String = SavedState.Key,
    defaultBundle: () -> Bundle? = { Bundle() }
): Lazy<SavedState> = lazy {
    SavedState({ findViewTreeSavedStateRegistryOwner()!! }, "$key@$id", defaultBundle).also { savedState ->
        doOnAttach {
            savedState.registerSavedStateProvider()
        }
    }
}

class SavedState
internal constructor(
    private val owner: () -> SavedStateRegistryOwner,
    private val key: String,
    defaultBundle: () -> Bundle?
) {
    companion object {
        internal const val Key = "com.wada811.savedstateproperty.SavedState.Key"
    }

    private val savedState by lazy {
        owner().savedStateRegistry.consumeRestoredStateForKey(key) ?: defaultBundle() ?: Bundle()
    }

    internal fun registerSavedStateProvider() {
        owner().savedStateRegistry.registerSavedStateProvider(key) { savedState }
    }

    fun <T> property(
        getValue: Bundle.(String) -> T
    ): ReadOnlyProperty<Any, T> = object : ReadOnlyProperty<Any, T> {
        override operator fun getValue(thisRef: Any, property: KProperty<*>): T = savedState.getValue(property.name)
    }

    fun <T> property(
        getValue: Bundle.(String) -> T,
        setValue: Bundle.(String, T) -> Unit
    ): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
        override operator fun getValue(thisRef: Any, property: KProperty<*>): T = savedState.getValue(property.name)
        override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) = savedState.setValue(property.name, value)
    }

    @Suppress("DEPRECATION")
    inline fun <reified T : AutoRecreated> runOnNextRecreation() = runOnNextRecreation(T::class.java)

    @Suppress("DEPRECATION")
    @Deprecated("Use runOnNextRecreation<T>()", ReplaceWith("this.runOnNextRecreation<T>()"), DeprecationLevel.WARNING)
    fun <T : AutoRecreated> runOnNextRecreation(clazz: Class<T>) = owner().runOnNextRecreation(clazz)
}

@Suppress("DEPRECATION")
inline fun <reified T : AutoRecreated> SavedStateRegistryOwner.runOnNextRecreation() = runOnNextRecreation(T::class.java)

@Deprecated("Use runOnNextRecreation<T>()", ReplaceWith("this.runOnNextRecreation<T>()"), DeprecationLevel.WARNING)
fun <T : AutoRecreated> SavedStateRegistryOwner.runOnNextRecreation(clazz: Class<T>) {
    when (lifecycle.currentState) {
        DESTROYED -> return
        CREATED -> lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Event) {
                if (event == ON_START) {
                    lifecycle.removeObserver(this)
                    savedStateRegistry.runOnNextRecreation(clazz)
                }
            }
        })
        else -> savedStateRegistry.runOnNextRecreation(clazz)
    }
}
