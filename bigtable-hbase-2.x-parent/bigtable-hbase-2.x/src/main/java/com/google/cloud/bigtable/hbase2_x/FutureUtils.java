/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.bigtable.hbase2_x;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.bigtable.config.Logger;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Utility methods for converting guava {@link ListenableFuture} Future to
 * {@link CompletableFuture}. Useful to convert the ListenableFuture types used by
 * bigtable-client-core component to Java 8 CompletableFuture types used in Hbase 2
 * 
 * @author spollapally
 */
public class FutureUtils {

  public static final ExecutorService DIRECT_EXECUTOR = MoreExecutors.newDirectExecutorService();
  static Logger logger = new Logger(FutureUtils.class);

  public static <T> CompletableFuture<T> toCompletableFuture(ApiFuture<T> apiFuture) {
    CompletableFuture<T> completableFuture = new CompletableFuture<T>() {
      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
        boolean result = apiFuture.cancel(mayInterruptIfRunning);
        super.cancel(mayInterruptIfRunning);
        return result;
      }
    };

    ApiFutureCallback<T> callback = new ApiFutureCallback<T>() {
      public void onFailure(Throwable throwable) {
        completableFuture.completeExceptionally(throwable);
      }

      public void onSuccess(T t) {
        completableFuture.complete(t);
      }
    };
    ApiFutures.addCallback(apiFuture, callback, MoreExecutors.directExecutor());

    return completableFuture;
  }

  public static <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenableFuture) {
    CompletableFuture<T> completableFuture = new CompletableFuture<T>() {
      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
        boolean result = listenableFuture.cancel(mayInterruptIfRunning);
        super.cancel(mayInterruptIfRunning);
        return result;
      }
    };

    FutureCallback<T> callback = new FutureCallback<T>() {
      public void onFailure(Throwable throwable) {
        completableFuture.completeExceptionally(throwable);
      }

      public void onSuccess(T t) {
        completableFuture.complete(t);
      }
    };
    Futures.addCallback(listenableFuture, callback, MoreExecutors.directExecutor());

    return completableFuture;
  }

  public static <T> CompletableFuture<T> failedFuture(Throwable error) {
    CompletableFuture<T> future = new CompletableFuture<>();
    future.completeExceptionally(error);
    return future;
  }

}
