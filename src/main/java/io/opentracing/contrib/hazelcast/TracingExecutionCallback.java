/*
 * Copyright 2018-2019 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.contrib.hazelcast;

import static io.opentracing.contrib.hazelcast.TracingHelper.decorateAction;
import static io.opentracing.contrib.hazelcast.TracingHelper.extract;
import static io.opentracing.contrib.hazelcast.TracingHelper.nullable;
import static io.opentracing.contrib.hazelcast.TracingHelper.nullableClass;

import com.hazelcast.core.ExecutionCallback;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import java.util.Map;

public class TracingExecutionCallback<V> implements ExecutionCallback<V> {

  private final ExecutionCallback<V> callback;
  private final Map<String, String> spanContextMap;
  private final boolean traceWithActiveSpanOnly;

  public TracingExecutionCallback(ExecutionCallback<V> callback,
      boolean traceWithActiveSpanOnly, Map<String, String> spanContextMap) {
    this.callback = callback;
    this.traceWithActiveSpanOnly = traceWithActiveSpanOnly;
    this.spanContextMap = spanContextMap;
  }

  public void onResponse(V response) {
    SpanContext parent = extract(spanContextMap);
    Span span = TracingHelper.buildSpan("onResponse", parent, traceWithActiveSpanOnly);
    span.setTag("response", nullable(response));
    span.setTag("callback", nullableClass(callback));
    decorateAction(() -> callback.onResponse(response), span);
  }

  @Override
  public void onFailure(Throwable t) {
    SpanContext parent = extract(spanContextMap);
    Span span = TracingHelper.buildSpan("onFailure", parent, traceWithActiveSpanOnly);
    span.setTag("callback", nullableClass(callback));
    TracingHelper.onError(t, span);
    decorateAction(() -> callback.onFailure(t), span);
  }


}
