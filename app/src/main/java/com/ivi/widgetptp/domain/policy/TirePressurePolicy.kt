package com.ivi.widgetptp.domain.policy

data class TirePressureThresholds(
    val normalMinimumPsi: Int = 35,
    val normalMaximumPsi: Int = 40,
    val warningMinimumPsi: Int = 30,
    val warningMaximumPsi: Int = 45,
)

enum class TirePressureClassification {
    NORMAL,
    WARNING,
    CRITICAL,
}

class TirePressurePolicy(
    private val thresholds: TirePressureThresholds = TirePressureThresholds(),
) {
    fun classify(psi: Int): TirePressureClassification = when {
        psi in thresholds.normalMinimumPsi..thresholds.normalMaximumPsi ->
            TirePressureClassification.NORMAL

        psi in thresholds.warningMinimumPsi..thresholds.warningMaximumPsi ->
            TirePressureClassification.WARNING

        else -> TirePressureClassification.CRITICAL
    }
}
