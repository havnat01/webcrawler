package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object delegate;
  private final ProfilingState state;

  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState state) {
    this.clock = Objects.requireNonNull(clock);
    this.delegate = Objects.requireNonNull(delegate);
    this.state = Objects.requireNonNull(state);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    boolean isProfiled = method.isAnnotationPresent(Profiled.class);
    Instant startTime = isProfiled ? clock.instant() : null;
    Object result;
    try {
      result = isProfiled ? profiledInvoke(method, args) : method.invoke(delegate, args);
    } finally {
      if (isProfiled) {
        recordProfiledInvocation(method, startTime);
      }
    }
    return result;
  }

  private Object profiledInvoke(Method method, Object[] args) throws Throwable {
    Object result;
    try {
      result = method.invoke(delegate, args);
    } catch (IllegalAccessException | IllegalArgumentException ex) {
      throw new RuntimeException(ex);
    } catch (InvocationTargetException ex) {
      throw ex.getTargetException();
    }
    return result;
  }

  private void recordProfiledInvocation(Method method, Instant startTime) {
    Duration duration = Duration.between(startTime, clock.instant());
    state.record(delegate.getClass(), method, duration);
  }

}
