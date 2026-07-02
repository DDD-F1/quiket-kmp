package com.f1.quiket.core.network.auth

object AuthenticatedRequest {
    const val HEADER_NAME: String = "X-Quiket-Requires-Auth"
    const val HEADER_VALUE: String = "true"
    const val HEADER: String = "$HEADER_NAME: $HEADER_VALUE"
}
