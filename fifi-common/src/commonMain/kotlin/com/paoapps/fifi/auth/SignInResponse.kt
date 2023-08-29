package com.paoapps.fifi.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignInResponse(
    val tokens: Tokens? = null,
    val challenge: AuthenticationChallenge? = null,
    val session: String? = null,
    val message: String? = null,
)

@Serializable
sealed class AuthenticationChallenge {
    @Serializable
    @SerialName("NEW_PASSWORD_REQUIRED")
    object NewPasswordRequired: AuthenticationChallenge()

    @Serializable
    @SerialName("EMAIL_VERIFICATION_REQUIRED")
    object EmailVerificationRequired: AuthenticationChallenge()

    @Serializable
    @SerialName("ASK_REFERRAL_CODE")
    object AskReferralCode: AuthenticationChallenge()
}

@Serializable
data class RespondToAuthChallenge(
    val challenge: AuthenticationChallenge,
    val challengeResponse: AuthenticationChallengeResponse,
    val session: String
)

@Serializable
sealed class AuthenticationChallengeResponse {

    @Serializable
    @SerialName("NEW_PASSWORD_REQUIRED")
    data class NewPasswordRequired(val username: String, val password: String): AuthenticationChallengeResponse()

    @Serializable
    @SerialName("EMAIL_VERIFICATION_REQUIRED")
    data class EmailVerificationRequired(val username: String, val verificationCode: String): AuthenticationChallengeResponse()

    @Serializable
    @SerialName("ASK_REFERRAL_CODE")
    data class AskReferralCode(val username: String, val code: String?): AuthenticationChallengeResponse()
}
