package com.valentincolombat.upnews.utils

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.isNetworkError(): Boolean {
    var cause: Throwable? = this
    while (cause != null) {
        if (cause is UnknownHostException || cause is ConnectException || cause is SocketTimeoutException)
            return true
        cause = cause.cause
    }
    return false
}
