package com.example.productivitygame.ui.utils

sealed class Result(val message: String) {
    class Success : Result(message = "Success!")
    class Fail(message: String) : Result(message)
}