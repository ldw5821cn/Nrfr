package com.github.nrfr.manager

internal object CarrierConfigServiceLookup {
    fun <T : Any> resolve(modern: () -> T?, legacy: () -> T?): T {
        val modernFailure = try {
            modern()?.let { return it }
            IllegalStateException("TelephonyFrameworkInitializer 返回空服务")
        } catch (error: Exception) {
            error
        }
        try {
            return legacy() ?: throw IllegalStateException("carrier_config 返回空服务")
        } catch (error: Exception) {
            val cause = error.unwrapCarrierConfigCause()
            throw IllegalStateException(
                "无法获取 CarrierConfigLoader 服务：${cause.javaClass.simpleName}: ${cause.message.orEmpty()}",
                cause
            ).apply { addSuppressed(modernFailure) }
        }
    }
}
