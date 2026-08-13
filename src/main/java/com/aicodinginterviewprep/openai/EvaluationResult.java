package com.aicodinginterviewprep.openai;

/**
 * 
 * Record: Represents the evaluation result of a candidate's answer, including a rating and feedback.
 */
public class EvaluationResult {
  
  private int rating;
  private String evaluation;

  public EvaluationResult(String evaluation, int rating) {
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
