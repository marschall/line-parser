package com.github.marschall.lineparser;

import java.io.IOException;

/**
 * Thrown when an unmap() on a mapped byte buffer failed.
 */
public final class UnmapFailedException extends IOException {

  private static final long serialVersionUID = -3797546804890352354L;

  UnmapFailedException(String message, Throwable cause) {
    super(message, cause);
  }

}
