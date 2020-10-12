package com.wada811.viewsavedstate

import android.os.Binder
import android.os.Bundle
import android.os.Parcelable
import android.util.Size
import android.util.SizeF
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
import java.io.Serializable
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
        internal const val Key = "com.wada811.viewsavedstate.SavedState.Key"
    }

    val bundle: Bundle by lazy {
        owner().savedStateRegistry.consumeRestoredStateForKey(key) ?: defaultBundle() ?: Bundle()
    }

    internal fun registerSavedStateProvider() {
        owner().savedStateRegistry.registerSavedStateProvider(key) { bundle }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> property(): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
        override operator fun getValue(thisRef: Any, property: KProperty<*>): T = bundle.get(property.name) as T
        override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) = setValue(property, value)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> property(defaultValue: T): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
        override operator fun getValue(thisRef: Any, property: KProperty<*>): T = bundle.get(property.name)?.let { it as T } ?: defaultValue
        override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) = setValue(property, value)
    }

    private fun <T> setValue(property: KProperty<*>, value: T) {
        when (value) {
            is Boolean -> bundle.putBoolean(property.name, value)
            is BooleanArray -> bundle.putBooleanArray(property.name, value)
            is Double -> bundle.putDouble(property.name, value)
            is DoubleArray -> bundle.putDoubleArray(property.name, value)
            is Int -> bundle.putInt(property.name, value)
            is IntArray -> bundle.putIntArray(property.name, value)
            is Long -> bundle.putLong(property.name, value)
            is LongArray -> bundle.putLongArray(property.name, value)
            is String -> bundle.putString(property.name, value)
            is Binder -> bundle.putBinder(property.name, value)
            is Bundle -> bundle.putBundle(property.name, value)
            is Byte -> bundle.putByte(property.name, value)
            is ByteArray -> bundle.putByteArray(property.name, value)
            is Char -> bundle.putChar(property.name, value)
            is CharArray -> bundle.putCharArray(property.name, value)
            is CharSequence -> bundle.putCharSequence(property.name, value)
            is Float -> bundle.putFloat(property.name, value)
            is FloatArray -> bundle.putFloatArray(property.name, value)
            is Parcelable -> bundle.putParcelable(property.name, value)
            is Serializable -> bundle.putSerializable(property.name, value)
            is Short -> bundle.putShort(property.name, value)
            is ShortArray -> bundle.putShortArray(property.name, value)
            is Size -> bundle.putSize(property.name, value)
            is SizeF -> bundle.putSizeF(property.name, value)
            else -> throw IllegalArgumentException(
                "Can't set the property(${property.name})'s value($value). Use property(getValue: Bundle.(key: String) -> T, setValue: Bundle.(key: String, value: T) -> Unit) method."
            )
        }
    }

    fun <T> property(
        getValue: Bundle.(String) -> T
    ): ReadOnlyProperty<Any, T> = object : ReadOnlyProperty<Any, T> {
        override operator fun getValue(thisRef: Any, property: KProperty<*>): T = bundle.getValue(property.name)
    }

    fun <T> property(
        getValue: Bundle.(String) -> T,
        setValue: Bundle.(String, T) -> Unit
    ): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
        override operator fun getValue(thisRef: Any, property: KProperty<*>): T = bundle.getValue(property.name)
        override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) = bundle.setValue(property.name, value)
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
