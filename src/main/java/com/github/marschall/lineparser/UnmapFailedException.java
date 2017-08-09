package com.github.marschall.lineparser;

import java.nio.file.FileSystemException;

/**
 * Thrown when an unmap() on a mapped byte buffer failed.
 */
public final class UnmapFailedException extends FileSystemException {

  private static final long serialVersionUID = -8755203639973805560L;

  UnmapFailedException(String file, String reason, Throwable cause) {
    super(file, null, reason);
    this.initCause(cause);
  }

}
