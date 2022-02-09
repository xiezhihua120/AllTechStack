package com.longtech.passport

interface Passport {

    fun login(name: String, password: String)

    fun logout()

}