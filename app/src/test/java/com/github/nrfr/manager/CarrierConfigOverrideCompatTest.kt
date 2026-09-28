package com.github.nrfr.manager

import android.os.PersistableBundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class CarrierConfigOverrideCompatTest {
    @Test fun usesLegacySignatureWhenPersistentOverloadIsMissing() {
        val target = LegacyManager()
        CarrierConfigOverrideCompat.invoke(target, 7, null, true)
        assertEquals(7, target.subId)
    }

    @Test fun prefersModernSignatureAndPreservesPersistentFlag() {
        for (persistent in listOf(true, false)) {
            val target = ModernManager()
            CarrierConfigOverrideCompat.invoke(target, 8, null, persistent)
            assertEquals(8, target.subId)
            assertEquals(persistent, target.persistent)
            assertFalse(target.legacyCalled)
        }
    }

    @Test fun doesNotRetryLegacySignatureAfterInvocationFailure() {
        val target = ModernManager().apply { failure = SecurityException("denied") }
        val error = assertThrows(SecurityException::class.java) {
            CarrierConfigOverrideCompat.invoke(target, 7, null, true)
        }
        assertSame(target.failure, error)
        assertFalse(target.legacyCalled)
    }

    @Test fun doesNotTreatServiceNoSuchMethodExceptionAsLookupFailure() {
        val target = ModernManager().apply { failure = NoSuchMethodException("service failure") }
        val error = assertThrows(NoSuchMethodException::class.java) {
            CarrierConfigOverrideCompat.invoke(target, 7, null, true)
        }
        assertSame(target.failure, error)
        assertFalse(target.legacyCalled)
    }

    @Test fun reportsUnsupportedSignatures() {
        assertThrows(NoSuchMethodException::class.java) {
            CarrierConfigOverrideCompat.invoke(Any(), 7, null, true)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    class LegacyManager {
        var subId: Int? = null
        fun overrideConfig(subId: Int, bundle: PersistableBundle?) {
            this.subId = subId
        }
    }

    @Suppress("UNUSED_PARAMETER")
    class ModernManager {
        var subId: Int? = null
        var persistent: Boolean? = null
        var legacyCalled = false
        var failure: Throwable? = null
        fun overrideConfig(subId: Int, bundle: PersistableBundle?, persistent: Boolean) {
            failure?.let { throw it }
            this.subId = subId
            this.persistent = persistent
        }
        fun overrideConfig(subId: Int, bundle: PersistableBundle?) {
            legacyCalled = true
        }
    }
}
