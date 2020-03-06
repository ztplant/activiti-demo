package com.xianzhi.activiti.demo.vo;

public class RequestVO {
  int amount;
  String username;

  public RequestVO(int amount, String username) {
    this.amount = amount;
    this.username = username;
  }

  public RequestVO() {
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
