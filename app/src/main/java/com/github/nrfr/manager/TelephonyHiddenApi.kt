package com.github.nrfr.manager

import android.content.Context
import android.os.IBinder
import android.os.PersistableBundle
import rikka.shizuku.ShizukuBinderWrapper
import java.lang.reflect.InvocationTargetException

internal object TelephonyHiddenApi {
    private const val LOADER_STUB = "com.android.internal.telephony.ICarrierConfigLoader\$Stub"

    fun getConfigForSubId(callingPackage: String, subId: Int): PersistableBundle? {
        val loader = getCarrierConfigLoaderOrNull() ?: return null
        val method = loader.javaClass.getMethod(
            "getConfigForSubId",
            Int::class.javaPrimitiveType,
            String::class.java
        )
        return method.invoke(loader, subId, callingPackage) as? PersistableBundle
    }

    fun overrideConfig(subId: Int, bundle: PersistableBundle?, persistent: Boolean) {
        val loader = getCarrierConfigLoader()
        CarrierConfigOverrideCompat.invoke(loader, subId, bundle, persistent)
    }

    private fun getCarrierConfigLoader(): Any {
        val binder = CarrierConfigServiceLookup.resolve(
            modern = { getModernCarrierConfigBinder() },
            legacy = {
                Class.forName("android.os.ServiceManager")
                    .getMethod("getService", String::class.java)
                    .invoke(null, Context.CARRIER_CONFIG_SERVICE) as? IBinder
            }
        )
        // Both lookup paths must use Shizuku for the actual Binder calls.
        return Class.forName(LOADER_STUB)
            .getMethod("asInterface", IBinder::class.java)
            .invoke(null, ShizukuBinderWrapper(binder))
            ?: throw IllegalStateException("CarrierConfigLoader.asInterface 返回空接口")
    }

    internal fun getCarrierConfigLoaderOrNull(): Any? {
        return runCatching { getCarrierConfigLoader() }.getOrNull()
    }

    private fun getModernCarrierConfigBinder(): IBinder? {
        val initializer = Class.forName("android.telephony.TelephonyFrameworkInitializer")
        val serviceManager = initializer.getMethod("getTelephonyServiceManager").invoke(null)
        val registerer = serviceManager.javaClass
            .getMethod("getCarrierConfigServiceRegisterer")
            .invoke(serviceManager)
        return registerer.javaClass.getMethod("get").invoke(registerer) as? IBinder
    }
}

internal fun Throwable.unwrapCarrierConfigCause(): Throwable {
    var current: Throwable = this
    while (current is InvocationTargetException && current.cause != null) {
        current = current.cause!!
    }
    return current
}
