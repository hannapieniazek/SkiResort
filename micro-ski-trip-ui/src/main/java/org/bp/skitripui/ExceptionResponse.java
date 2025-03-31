
package org.bp.skitripui;

import java.time.OffsetDateTime;

public class ExceptionResponse {
  private OffsetDateTime timestamp = null;

  private String message = null;

  public OffsetDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
