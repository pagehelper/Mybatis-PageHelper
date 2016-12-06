package com.github.pagehelper;

import org.apache.ibatis.session.RowBounds;

/**
 * @author liuzenghui
 */
public class PageRowBounds extends RowBounds {
  private Long total;

  public PageRowBounds() {
  }

  public PageRowBounds(int offset, int limit) {
    super(offset, limit);
  }

  public Long getTotal() {
    return total;
  }

  public void setTotal(Long total) {
    this.total = total;
  }
}
