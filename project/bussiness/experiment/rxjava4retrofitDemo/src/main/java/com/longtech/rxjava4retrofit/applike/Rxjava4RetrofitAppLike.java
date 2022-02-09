package com.longtech.rxjava4retrofit.applike;

import com.luojilab.component.componentlib.applicationlike.IApplicationLike;
import com.luojilab.component.componentlib.router.Router;
import com.luojilab.component.componentlib.router.ui.UIRouter;

public class Rxjava4RetrofitAppLike implements IApplicationLike {

    Router router = Router.getInstance();
    UIRouter uiRouter = UIRouter.getInstance();

    @Override
    public void onCreate() {
        //uiRouter.registerUI("reader");
        //router.addService(ReadBookService.class.getSimpleName(), new ReadBookServiceImpl());
        //router.addService(ReadBookService.class.getSimpleName(), new ReadBookServiceImplKotlin());
    }

    @Override
    public void onStop() {
        //uiRouter.unregisterUI("reader");
        //router.removeService(ReadBookService.class.getSimpleName());
    }
}
