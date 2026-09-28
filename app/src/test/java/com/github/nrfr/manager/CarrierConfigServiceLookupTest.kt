package com.github.nrfr.manager

import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CarrierConfigServiceLookupTest {
    @Test fun modernServiceDoesNotCallLegacyLookup() {
        val service = Any()
        assertSame(service, CarrierConfigServiceLookup.resolve(
            modern = { service },
            legacy = { error("Unexpected legacy lookup") }
        ))
    }

    @Test fun missingInitializerFallsBackToLegacyService() {
        val service = Any()
        assertSame(service, CarrierConfigServiceLookup.resolve(
            modern = { throw ClassNotFoundException("TelephonyFrameworkInitializer") },
            legacy = { service }
        ))
    }

    @Test fun nullModernServiceFallsBackToLegacyService() {
        val service = Any()
        assertSame(service, CarrierConfigServiceLookup.resolve(
            modern = { null },
            legacy = { service }
        ))
    }

    @Test fun reportsBothLookupFailures() {
        val modernFailure = ClassNotFoundException("TelephonyFrameworkInitializer")
        val legacyFailure = SecurityException("access denied")
        val error = assertThrows(IllegalStateException::class.java) {
            CarrierConfigServiceLookup.resolve<Any>(
                modern = { throw modernFailure },
                legacy = { throw legacyFailure }
            )
        }
        assertSame(legacyFailure, error.cause)
        assertTrue(error.suppressed.contains(modernFailure))
        assertTrue(error.message.orEmpty().contains("access denied"))
    }

    @Test fun nullServicesReportUnavailable() {
        val error = assertThrows(IllegalStateException::class.java) {
            CarrierConfigServiceLookup.resolve<Any>({ null }, { null })
        }
        assertTrue(error.message.orEmpty().contains("CarrierConfigLoader"))
    }
}
