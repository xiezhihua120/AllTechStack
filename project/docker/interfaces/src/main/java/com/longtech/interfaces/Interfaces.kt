package com.longtech.interfaces

import com.longtech.downloader.Downloader
import com.longtech.downloader.DownloaderManager
import com.longtech.imageload.ImageLoader
import com.longtech.imageload.ImageLoaderManager
import com.longtech.passport.Passport
import com.longtech.passport.PassportManager

object Interfaces {

    var imageLoader: ImageLoader = ImageLoaderManager()

    var downloader: Downloader = DownloaderManager()

    var passport: Passport = PassportManager()

}