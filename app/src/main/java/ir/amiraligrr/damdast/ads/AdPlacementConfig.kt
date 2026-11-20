package ir.amiraligrr.damdast.ads

/**
 * Central map of Tapsell placements.
 *
 * The two "instant banner" IDs have different labels but similar roles. Only A is
 * used for interstitials until the dashboard format is verified. B stays reserved
 * and disabled so a request can never be sent to the wrong format.
 */
object AdPlacementConfig {
    const val STANDARD_BANNER = "6aff4d7eaeabeb750feb8f25"
    const val NATIVE_BANNER = "6aff4de46cbc4169d6c3855e"
    const val REWARDED_VIDEO = "6aff4e03728e2256843b0c91"

    const val INTERSTITIAL_A = "6aff4dae6cbc4169d6c3855d"
    const val INTERSTITIAL_B = "6aff4e1e728e2256843b0c92"
    const val PRE_ROLL = "6aff4e3c4f21b435f886825d"
    const val NATIVE_VIDEO = "6aff4e5b728e2256843b0c93"

    /** Candidate A is the only interstitial placement enabled for now. */
    const val ACTIVE_INTERSTITIAL = INTERSTITIAL_A

    const val INTERSTITIAL_ENABLED = true
    const val NATIVE_ENABLED = true
    const val NATIVE_VIDEO_ENABLED = false
    const val PRE_ROLL_ENABLED = false
}
