package ealvalog.impl;

import ealvalog.LogLevel;
import ealvalog.Marker;
import ealvalog.NullMarker;
import ealvalog.util.FormattableThrowable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.logging.LogRecord;

/**
 * Subclass of LogRecord adding the extra info we need. Not thread safe.
 * <p>
 * Don't use the {@link #getParameters()} array length as the actual number of parameters. Use {@link #getParameterCount()} instead.
 * There might be nulls at the end of the array due to reuse
 * <p>
 * Created by Eric A. Snell on 3/4/17.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ExtLogRecord extends LogRecord implements Closeable { // not AutoCloseable to be compatible with Android version < KitKat
  private static final long serialVersionUID = 936230097973648802L;

  private LogLevel logLevel;
  private @NotNull String threadName;
  private @NotNull Marker marker;
  private int lineNumber;
  private int parameterCount;   // actual number of parameters, array might be oversized
  private transient boolean reserved;

  private static ThreadLocal<ExtLogRecord> threadLocalRecord = new ThreadLocal<>();

  private static ExtLogRecord getRecord(final @NotNull LogLevel level, final @NotNull String msg) {
    ExtLogRecord result = threadLocalRecord.get();
    if (result == null) {
      result = new ExtLogRecord(level, msg);
      threadLocalRecord.set(result);
    }
    return result.isReserved() ? new ExtLogRecord(level, msg).reserve() : result.reserve(level, msg);
  }

  /**
   * Get a record and initialize it. Thread name and thread id will be set based on {@link Thread#currentThread()}
   * <p>
   * Canonical use is:
   * <p>
   * <pre>
   * {@code
   * try (ExtLogRecord record = ExtLogRecord.get(...)) {
   *   // use record here.
   * }
   * }
   * </pre>
   * Return the record via {@link #release(ExtLogRecord)} so new records don't need to be created for every log. Not releasing a record
   * defeats the pool. Preference is to use via try with resources as in example above.
   *
   * @return an ExtLogRecord initialized based on parameters and the current thread
   */
  public static ExtLogRecord get(final @NotNull LogLevel level,
                                 final @NotNull String msg,
                                 final @NotNull String loggerName,
                                 final @Nullable StackTraceElement callerLocation,
                                 final @Nullable Throwable throwable,
                                 final @NotNull Object... formatArgs) {
    final ExtLogRecord logRecord = getRecord(level, msg);
    final Thread currentThread = Thread.currentThread();
    logRecord.setThreadName(currentThread.getName());
    logRecord.setThreadID((int)currentThread.getId());
    logRecord.setLoggerName(loggerName);
    if (callerLocation != null) {
      logRecord.setSourceClassName(callerLocation.getClassName());
      logRecord.setSourceMethodName(callerLocation.getMethodName());
      logRecord.setLineNumber(callerLocation.getLineNumber());
    }
    logRecord.setThrown(throwable);
    logRecord.setParameters(formatArgs);
    return logRecord;
  }

  public static void release(final @NotNull ExtLogRecord record) {
    record.release();
  }

  /** Thread name is set in this constructor */
  public ExtLogRecord(@NotNull final LogLevel level, @NotNull final String msg) {
    super(level.getJdkLevel(), msg);
    logLevel = level;
    threadName = Thread.currentThread().getName();
    marker = NullMarker.INSTANCE;
    lineNumber = 0;
    parameterCount = 0;
    reserved = false;
  }

  private boolean isReserved() {
    return reserved;
  }

  private ExtLogRecord reserve() {
    reserved = true;
    return this;
  }

  private ExtLogRecord reserve(@NotNull final LogLevel level, @NotNull final String msg) {
    reserved = true;
    setLevel(level.getJdkLevel());
    setMessage(msg);
    return this;
  }

  private ExtLogRecord release() {
    reserved = false;
    return this;
  }

  public LogLevel getLogLevel() {
    return logLevel;
  }

  /**
   * Name of the thread on which this instance was constructed
   *
   * @return thread name
   */
  @NotNull public String getThreadName() {
    return threadName;
  }

  /**
   * Returns the associated marker, which may be {@link NullMarker#INSTANCE} if no marker was set.
   *
   * @return the {@link Marker} set into this record, or {@link NullMarker#INSTANCE} if no contained marker
   */
  @NotNull public Marker getMarker() {
    return marker;
  }

  /** Set the marker, or clear it to {@link NullMarker#INSTANCE} if null is passed */
  public void setMarker(@Nullable final Marker marker) {
    this.marker = NullMarker.nullToNullInstance(marker);
  }

  /** @return the line number of the log call site, 0 if not available */
  public int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(final int lineNumber) {
    this.lineNumber = lineNumber;
  }

  /** @return the number of parameters passed to {@link #setParameters(Object[])} */
  public int getParameterCount() {
    return parameterCount;
  }

  /** If we start pooling this I want to already reuse the object array and keep track of actual parameter count */
  @Override public void setParameters(final Object[] parameters) {
    parameterCount = parameters.length;
    final Object[] existingParameters = getParameters();
    if (existingParameters != null && existingParameters.length >= parameters.length) {
      System.arraycopy(parameters, 0, existingParameters, 0, parameters.length);
      if (existingParameters.length > parameters.length) {
        Arrays.fill(existingParameters, parameters.length, existingParameters.length, null);
      }
      super.setParameters(existingParameters);  // in current impl this is redundant, but let's not assume LogRecord never changes
    } else {
      super.setParameters(parameters);
    }
  }

  /**
   * If we've already set a {@link FormattableThrowable} reuse it, otherwise wrap the original {@link Throwable} {@code thrown}
   *
   * @param thrown {@link Throwable} passed to log method
   */
  @Override public void setThrown(final Throwable thrown) {
    final Throwable throwable = getThrown();
    if (throwable != null && throwable instanceof FormattableThrowable) {
      super.setThrown(((FormattableThrowable)throwable).setRealThrowable(thrown));
    } else {
      super.setThrown(new FormattableThrowable(thrown));
    }
  }

  public void setThreadName(@NotNull final String threadName) {
    this.threadName = threadName;
  }

  @Override public void close() {
    release(this);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    logLevel = (LogLevel)in.readObject();
    threadName = in.readUTF();
    marker = (Marker)in.readObject();
    lineNumber = in.readInt();
    parameterCount = in.readInt();
    reserved = false;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(logLevel);
    out.writeUTF(threadName);
    out.writeObject(marker);
    out.writeInt(lineNumber);
    out.writeInt(parameterCount);
  }
}