package com.ivi.widgetptp.domain.policy

enum class HeartRateClassification {
    TOO_LOW,
    LOW,
    NORMAL,
    HIGH,
    VERY_HIGH,
}

enum class RespirationClassification {
    SLOW,
    NORMAL,
    FAST,
}

object HealthClassificationPolicy {
    fun classifyHeartRate(bpm: Int): HeartRateClassification = when {
        bpm < 40 -> HeartRateClassification.TOO_LOW
        bpm < 60 -> HeartRateClassification.LOW
        bpm <= 100 -> HeartRateClassification.NORMAL
        bpm <= 150 -> HeartRateClassification.HIGH
        else -> HeartRateClassification.VERY_HIGH
    }

    fun classifyRespiration(brpm: Int): RespirationClassification = when {
        brpm < 12 -> RespirationClassification.SLOW
        brpm <= 20 -> RespirationClassification.NORMAL
        else -> RespirationClassification.FAST
    }
}
