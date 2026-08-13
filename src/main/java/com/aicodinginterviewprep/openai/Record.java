package com.aicodinginterviewprep.openai;

/**
 * 
 * Record: Represents the evaluation result of a candidate's answer, including a rating and feedback.
 */
public class Record {
  
  private int rating;
  private String evaluation;

  public Record(String evaluation, int rating) {
    this.evaluation = evaluation;
    this.rating = rating;
  }

  public int getRating() {
    return rating;
  }

  public String getEvaluation() {
    return evaluation;
  }
}
