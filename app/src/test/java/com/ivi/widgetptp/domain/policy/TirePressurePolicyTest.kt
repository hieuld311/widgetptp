package com.ivi.widgetptp.domain.policy

import org.junit.Assert.assertEquals
import org.junit.Test

class TirePressurePolicyTest {
    private val policy = TirePressurePolicy()

    @Test
    fun `pressure boundaries match default requirements`() {
        assertEquals(TirePressureClassification.CRITICAL, policy.classify(29))
        assertEquals(TirePressureClassification.WARNING, policy.classify(30))
        assertEquals(TirePressureClassification.WARNING, policy.classify(34))
        assertEquals(TirePressureClassification.NORMAL, policy.classify(35))
        assertEquals(TirePressureClassification.NORMAL, policy.classify(40))
        assertEquals(TirePressureClassification.WARNING, policy.classify(41))
        assertEquals(TirePressureClassification.WARNING, policy.classify(45))
        assertEquals(TirePressureClassification.CRITICAL, policy.classify(46))
    }
}
