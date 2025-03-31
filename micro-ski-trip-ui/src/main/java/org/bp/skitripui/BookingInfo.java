
package org.bp.skitripui;

import java.math.BigDecimal;

public class BookingInfo {
  private String id;
  private BigDecimal cost;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public BigDecimal getCost() {
    return cost;
  }

  public void setCost(BigDecimal cost) {
    this.cost = cost;
  }
}

