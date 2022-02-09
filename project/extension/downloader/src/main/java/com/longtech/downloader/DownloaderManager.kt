package com.longtech.downloader

class DownloaderManager : Downloader{

    override fun start(url: String): Long {

        return Long.MAX_VALUE
    }

    override fun pause(id: Long) {

    }

    override fun resume(id: Long) {

    }

    override fun delete(id: Long) {

    }

    override fun restart(url: String) {

    }

}