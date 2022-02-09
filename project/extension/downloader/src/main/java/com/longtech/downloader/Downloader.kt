package com.longtech.downloader

interface Downloader {

    fun start(url: String): Long

    fun pause(id: Long)

    fun resume(id: Long)

    fun delete(id: Long)

    fun restart(url: String)

}