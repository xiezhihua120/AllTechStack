package com.longtech.app.initialize

class Base<T>(var name: String = "", var age: Int = -1, var input: T) {
    var fullName = this.name + this.age

    inner class Advance() {
        fun foo() {
            fullName = "1"
        }
    }
}