package com.ivi.widgetptp.domain.policy

import org.junit.Assert.assertEquals
import org.junit.Test

class HealthClassificationPolicyTest {
    @Test
    fun `heart-rate boundaries match requirements`() {
        assertEquals(HeartRateClassification.TOO_LOW, HealthClassificationPolicy.classifyHeartRate(39))
        assertEquals(HeartRateClassification.LOW, HealthClassificationPolicy.classifyHeartRate(40))
        assertEquals(HeartRateClassification.LOW, HealthClassificationPolicy.classifyHeartRate(59))
        assertEquals(HeartRateClassification.NORMAL, HealthClassificationPolicy.classifyHeartRate(60))
        assertEquals(HeartRateClassification.NORMAL, HealthClassificationPolicy.classifyHeartRate(100))
        assertEquals(HeartRateClassification.HIGH, HealthClassificationPolicy.classifyHeartRate(101))
        assertEquals(HeartRateClassification.HIGH, HealthClassificationPolicy.classifyHeartRate(150))
        assertEquals(HeartRateClassification.VERY_HIGH, HealthClassificationPolicy.classifyHeartRate(151))
    }

    @Test
    fun `respiration boundaries match requirements`() {
        assertEquals(RespirationClassification.SLOW, HealthClassificationPolicy.classifyRespiration(11))
        assertEquals(RespirationClassification.NORMAL, HealthClassificationPolicy.classifyRespiration(12))
        assertEquals(RespirationClassification.NORMAL, HealthClassificationPolicy.classifyRespiration(20))
        assertEquals(RespirationClassification.FAST, HealthClassificationPolicy.classifyRespiration(21))
    }
}
