----------------------------------------------------------------------------------------------------
1、如何进行配置的
retrofit = Retrofit.Builder()
            .client(OkHttpClient())
            .baseUrl("https://www.baidu.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()

----------------------------------------------------------------------------------------------------
2、CallAdapter原理回顾
   public interface CallAdapter<R, T> {
     Type responseType();
     T adapt(Call<R> call);

     abstract class Factory {
       public abstract @Nullable CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit);

       protected static Type getParameterUpperBound(int index, ParameterizedType type) {
         return Utils.getParameterUpperBound(index, type);
       }

       protected static Class<?> getRawType(Type type) {
         return Utils.getRawType(type);
       }
     }
   }
   分析：retrofit利用注解来标示请求，然后将请求逻辑封装到了HttpServiceMethod
        retrofit针对每个方法调用，生成了动态代理类，每个方法的调用对应到了一个HttpServiceMethod逻辑
        在调用Proxy的invoke方法时，实际上

   @Override
   final @Nullable ReturnT invoke(Object[] args) {
     Call<ResponseT> call = new OkHttpCall<>(requestFactory, args, callFactory, responseConverter);
     return adapt(call, args);
   }
   分析：retrofit内部实际上生成了一个OkHttpCall对象，再此利用CallAdapter可以对OKHttpCall对象进行转化
        这里rxjava将一个call对象，转化成了一个observable对象

----------------------------------------------------------------------------------------------------
3、retrofit2.adapter.rxjava3初步分析
   由于call的基本调用方式是call.enqueue(callback)或者call.execute()
   且observable的基本调用方式是obserable.subscribeOn(Scheduler.xx).observeOn(Scheduler.xx).subscribe(observer)
   可以分析得知，主要就是把call的一个调用过程，写成rxjava3的异步调用过程

   Observable.create(new ObservableSubscribeOn() {
        void subscribe(ObservableEmiiter emitter) {
            try {
                Response response = call.execute()
                emitter.onNext(response)
            } catch(Throwable r) {
                emitter.onError(r)
            }
            emitter.onComplete()
        }
   })

----------------------------------------------------------------------------------------------------
4、查看源码验证分析

    final class RxJava3CallAdapter<R> implements CallAdapter<R, Object> {
      private final Type responseType;
      private final @Nullable Scheduler scheduler;
      private final boolean isAsync;
      private final boolean isResult;
      private final boolean isBody;
      private final boolean isFlowable;
      private final boolean isSingle;
      private final boolean isMaybe;
      private final boolean isCompletable;

      RxJava3CallAdapter(Type responseType,@Nullable Scheduler scheduler,boolean isAsync,boolean isResult,boolean isBody,boolean isFlowable,boolean isSingle,boolean isMaybe,boolean isCompletable) {
        this.responseType = responseType;
        this.scheduler = scheduler;
        this.isAsync = isAsync;
        this.isResult = isResult;
        this.isBody = isBody;
        this.isFlowable = isFlowable;
        this.isSingle = isSingle;
        this.isMaybe = isMaybe;
        this.isCompletable = isCompletable;
      }

      @Override
      public Type responseType() {
        return responseType;
      }

      @Override
      public Object adapt(Call<R> call) {
        Observable<Response<R>> responseObservable = isAsync ? new CallEnqueueObservable<>(call) : new CallExecuteObservable<>(call);

        Observable<?> observable;
        if (isResult) {
          observable = new ResultObservable<>(responseObservable);
        } else if (isBody) {
          observable = new BodyObservable<>(responseObservable);
        } else {
          observable = responseObservable;
        }

        if (scheduler != null) {
          observable = observable.subscribeOn(scheduler);
        }

        if (isFlowable) {
          return observable.toFlowable(BackpressureStrategy.LATEST);
        }
        if (isSingle) {
          return observable.singleOrError();
        }
        if (isMaybe) {
          return observable.singleElement();
        }
        if (isCompletable) {
          return observable.ignoreElements();
        }
        return RxJavaPlugins.onAssembly(observable);
      }
    }
    分析：1、根据同步或者异步，生成两类Observable：CallEnqueueObservabl、CallExecuteObservable，上面自己的预测分析漏掉了
         2、根据请求的为isResult、isBody，在原始的Observable上再代理一层
         3、调度器设置
         4、返回observable，注意调用RxJavaPlugins.onAssembly，保证每个地方都可以被客户Hook控制

----------------------------------------------------------------------------------------------------
5、RxJava3CallAdapterFactory
    public final class RxJava3CallAdapterFactory extends CallAdapter.Factory {

      public static RxJava3CallAdapterFactory create() {
        return new RxJava3CallAdapterFactory(null, true);
      }

      public static RxJava3CallAdapterFactory createSynchronous() {
        return new RxJava3CallAdapterFactory(null, false);
      }

      public static RxJava3CallAdapterFactory createWithScheduler(Scheduler scheduler) {
        if (scheduler == null) throw new NullPointerException("scheduler == null");
        return new RxJava3CallAdapterFactory(scheduler, false);
      }

      private final @Nullable Scheduler scheduler;
      private final boolean isAsync;

      private RxJava3CallAdapterFactory(@Nullable Scheduler scheduler, boolean isAsync) {
        this.scheduler = scheduler;
        this.isAsync = isAsync;
      }

      @Override
      public @Nullable CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        Class<?> rawType = getRawType(returnType);
        // 分析：returnType为Call<R>

        boolean isFlowable = rawType == Flowable.class;
        boolean isSingle = rawType == Single.class;
        boolean isMaybe = rawType == Maybe.class;
        if (rawType != Observable.class && !isFlowable && !isSingle && !isMaybe) {
          return null;
        }

        boolean isResult = false;
        boolean isBody = false;
        Type responseType;

        Type observableType = getParameterUpperBound(0, (ParameterizedType) returnType);
        Class<?> rawObservableType = getRawType(observableType);
        // 分析：observableType为得到的是R

        if (rawObservableType == Response.class) {
          responseType = getParameterUpperBound(0, (ParameterizedType) observableType);
          // 分析：responseType得到的Response<XX>中的用户定义类型
        } else if (rawObservableType == Result.class) {
          responseType = getParameterUpperBound(0, (ParameterizedType) observableType);
          // 分析：responseType得到的Result<XX>中的用户定义类型
          isResult = true;
        } else {
          // 分析：responseType得到的XX中的用户定义类型
          responseType = observableType;
          isBody = true;
        }

        return new RxJava3CallAdapter(responseType, scheduler, isAsync, isResult, isBody, isFlowable, isSingle, isMaybe, false);
      }
    }
    分析：RxJava3CallAdapter主体的构造函数就一个：RxJava3CallAdapterFactory(@Nullable Scheduler scheduler, boolean isAsync)，可以指定调度器、同步异步
         根据CallAdapter的T adapt(Call<R> call)可知，其中R是返回值的类型，即ResponseType；而T是适配后的类型，即ReturnType，这里主要是Observable<Response<R>>

----------------------------------------------------------------------------------------------------
6、CallEnqueueObservable
   final class CallEnqueueObservable<T> extends Observable<Response<T>> {
     private final Call<T> originalCall;

     CallEnqueueObservable(Call<T> originalCall) {
       this.originalCall = originalCall;
     }

     @Override
     protected void subscribeActual(Observer<? super Response<T>> observer) {
       Call<T> call = originalCall.clone();
       CallCallback<T> callback = new CallCallback<>(call, observer);
       observer.onSubscribe(callback);
       if (!callback.isDisposed()) {
         call.enqueue(callback);
       }
     }

     private static final class CallCallback<T> implements Disposable, Callback<T> {
       private final Call<?> call;
       private final Observer<? super Response<T>> observer;
       private volatile boolean disposed;
       boolean terminated = false;

       CallCallback(Call<?> call, Observer<? super Response<T>> observer) {
         this.call = call;
         this.observer = observer;
       }

       @Override
       public void onResponse(Call<T> call, Response<T> response) {
         if (disposed) return;

         try {
           observer.onNext(response);

           if (!disposed) {
             terminated = true;
             observer.onComplete();
           }
         } catch (Throwable t) {
           Exceptions.throwIfFatal(t);
           if (terminated) {
             RxJavaPlugins.onError(t);
           } else if (!disposed) {
             try {
               observer.onError(t);
             } catch (Throwable inner) {
               Exceptions.throwIfFatal(inner);
               RxJavaPlugins.onError(new CompositeException(t, inner));
             }
           }
         }
       }

       @Override
       public void onFailure(Call<T> call, Throwable t) {
         if (call.isCanceled()) return;

         try {
           observer.onError(t);
         } catch (Throwable inner) {
           Exceptions.throwIfFatal(inner);
           RxJavaPlugins.onError(new CompositeException(t, inner));
         }
       }

       @Override
       public void dispose() {
         disposed = true;
         call.cancel();
       }

       @Override
       public boolean isDisposed() {
         return disposed;
       }
     }
   }
   分析：虽然之前的预测分析漏掉了异步方法的实现，但分析发现其实挺简单，就是将工作线程的逻辑调用call.enqueue(callback)
        其中callback来通知observer
        其中observer来取消的话，利用observer.onSubscribe(call)来构建双向关联即可

----------------------------------------------------------------------------------------------------
7、CallEnqueueObservable
    final class CallExecuteObservable<T> extends Observable<Response<T>> {
      private final Call<T> originalCall;

      CallExecuteObservable(Call<T> originalCall) {
        this.originalCall = originalCall;
      }

      @Override
      protected void subscribeActual(Observer<? super Response<T>> observer) {
        // Since Call is a one-shot type, clone it for each new observer.
        Call<T> call = originalCall.clone();
        CallDisposable disposable = new CallDisposable(call);
        observer.onSubscribe(disposable);
        if (disposable.isDisposed()) {
          return;
        }

        boolean terminated = false;
        try {
          Response<T> response = call.execute();
          if (!disposable.isDisposed()) {
            observer.onNext(response);
          }
          if (!disposable.isDisposed()) {
            terminated = true;
            observer.onComplete();
          }
        } catch (Throwable t) {
          Exceptions.throwIfFatal(t);
          if (terminated) {
            RxJavaPlugins.onError(t);
          } else if (!disposable.isDisposed()) {
            try {
              observer.onError(t);
            } catch (Throwable inner) {
              Exceptions.throwIfFatal(inner);
              RxJavaPlugins.onError(new CompositeException(t, inner));
            }
          }
        }
      }

      private static final class CallDisposable implements Disposable {
        private final Call<?> call;
        private volatile boolean disposed;

        CallDisposable(Call<?> call) {
          this.call = call;
        }

        @Override
        public void dispose() {
          disposed = true;
          call.cancel();
        }

        @Override
        public boolean isDisposed() {
          return disposed;
        }
      }
    }
    分析：与之前的分析基本一致，只不过这里写了一个对call的取消操作，同样是由observer来发起的

----------------------------------------------------------------------------------------------------
8、BodyObservable
    final class BodyObservable<T> extends Observable<T> {
      private final Observable<Response<T>> upstream;

      BodyObservable(Observable<Response<T>> upstream) {
        this.upstream = upstream;
      }

      @Override
      protected void subscribeActual(Observer<? super T> observer) {
        upstream.subscribe(new BodyObserver<>(observer));
      }

      private static class BodyObserver<R> implements Observer<Response<R>> {
        private final Observer<? super R> observer;
        private boolean terminated;

        BodyObserver(Observer<? super R> observer) {
          this.observer = observer;
        }

        @Override
        public void onSubscribe(Disposable disposable) {
          observer.onSubscribe(disposable);
        }

        @Override
        public void onNext(Response<R> response) {
          if (response.isSuccessful()) {
            observer.onNext(response.body());
          } else {
            terminated = true;
            Throwable t = new HttpException(response);
            try {
              observer.onError(t);
            } catch (Throwable inner) {
              Exceptions.throwIfFatal(inner);
              RxJavaPlugins.onError(new CompositeException(t, inner));
            }
          }
        }

        @Override
        public void onComplete() {
          if (!terminated) {
            observer.onComplete();
          }
        }

        @Override
        public void onError(Throwable throwable) {
          if (!terminated) {
            observer.onError(throwable);
          } else {
            broken.initCause(throwable);
            RxJavaPlugins.onError(broken);
          }
        }
      }
    }
    // 分析，这里只不过是代理了一个Observable，对结果的response做了一个转化，变成response.body()

----------------------------------------------------------------------------------------------------
9、总结
  1、rxjava分为生产线程、消费线程，其中call.enqueue和call.execute都应该放在生产线程中，即放到Observable的subscribeActual中
  2、对于同步模型：call.execute执行得到的结果，直接在生产线程中通知观察者即可
     对于异步模型：call.enqueue(callback)，让observer放到callback中，由callback来通知observer
  3、关于取消，构建call与observer之间的关联即可
     同步：在call调用execute之前，构建一个DisposableCall，绑定给observer
     异步：在call执行enqueue之前，构造一个可取消的Callback，绑定给observer
