package com.github.nrfr.manager

import android.os.PersistableBundle
import java.lang.reflect.InvocationTargetException

/** Shared signature lookup for the framework manager and its Binder proxy. */
internal object CarrierConfigOverrideCompat {
    fun invoke(target: Any, subId: Int, bundle: PersistableBundle?, persistent: Boolean) {
        val method = try {
            target.javaClass.getMethod(
                "overrideConfig",
                Int::class.javaPrimitiveType,
                PersistableBundle::class.java,
                Boolean::class.javaPrimitiveType
            )
        } catch (_: NoSuchMethodException) {
            // Older frameworks only support overrides for the phone process lifetime.
            target.javaClass.getMethod(
                "overrideConfig",
                Int::class.javaPrimitiveType,
                PersistableBundle::class.java
            )
        }
        try {
            if (method.parameterTypes.size == 3) {
                method.invoke(target, subId, bundle, persistent)
            } else {
                method.invoke(target, subId, bundle)
            }
        } catch (error: InvocationTargetException) {
            throw error.targetException ?: error
        }
    }
}
