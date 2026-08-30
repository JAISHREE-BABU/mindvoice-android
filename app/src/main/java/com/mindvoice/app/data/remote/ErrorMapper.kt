package com.mindvoice.app.data.remote

import retrofit2.HttpException
import java.io.IOException

fun friendlyErrorMessage(e: Exception): String {
    return when (e) {
        is HttpException -> when (e.code()) {
            401 -> "Incorrect email or password. Please try again."
            404 -> "We couldn't find that. Please try again."
            409 -> "An account with that email already exists."
            429 -> "The AI tutor is very busy right now. Please try again in a few minutes."
            in 500..599 -> "Something went wrong on our end. Please try again in a moment."
            else -> "Something went wrong. Please try again."
        }
        is IOException -> "Can't reach the server. Check your connection and try again."
        else -> "Something went wrong. Please try again."
    }
}

