package com.longtech.services

object ARouterConst {

    /**
     * Bundle配置登陆校验
     */
    var NEED_LOGIN_EXTRA: String = "NEED_LOGIN"         // 放置到Bundle中，bundle.putString(ARouterConst.NEED_LOGIN_EXTRA, true)

    /**
     * @RouteNote配置登陆校验
     */
    var NEED_LOGIN_FLAG: Int = Int.MAX_VALUE            // 放置到@RouteNode中 @RouteNode(name="{path}", extra=ARouterConst.NEED_LOGIN_FLAG)
}