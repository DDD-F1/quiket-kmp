package com.f1.quiket.core.testing

fun fakeThrowable(message: String = "forced failure"): Throwable = IllegalStateException(message)
