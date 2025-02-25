package io.github.wykopmobilny.api.errorhandler

import io.github.wykopmobilny.api.responses.ApiResponse
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.functions.Function

class ErrorHandler<T : ApiResponse<*>> : Function<T, SingleSource<T>> {
    override fun apply(apiResponse: T): SingleSource<T> {
        WykopExceptionParser.getException(apiResponse)?.apply {
            return Single.error(this)
        }
        return Single.just(apiResponse)
    }
}
