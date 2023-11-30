package com.fificard.api

import com.fificard.api.domain.FetcherResult
import com.fificard.api.domain.Failure
import com.fificard.api.domain.Success

actual suspend fun multipartUpload(
    body: Any,
    authorization: String?,
    url: String
): FetcherResult<Unit> {
    TODO()
}
