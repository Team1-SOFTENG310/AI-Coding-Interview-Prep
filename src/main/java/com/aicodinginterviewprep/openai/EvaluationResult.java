package com.aicodinginterviewprep.openai;

/**
 * Represents the evaluation result of a candidate's answer.
 *
 * <p>The API returns each category as a nested object containing {@code rating} and {@code
 * feedback}; this class exposes those values through category-specific getters. Ratings use
 * {@link Integer} rather than {@code int} because the API can return {@code null} when a category
 * is not applicable.
 */
public class EvaluationResult {

  private Integer correctness_rating;
  private Integer efficiency_rating;
  private Integer communication_rating;
  private Integer code_quality_rating;

  private String correctness_evaluation;
  private String efficiency_evaluation;
  private String communication_evaluation;
  private String code_quality_evaluation;

  public EvaluationResult(
      Integer correctness_rating,
      Integer efficiency_rating,
      Integer communication_rating,
      Integer code_quality_rating,
      String correctness_evaluation,
      String efficiency_evaluation,
      String communication_evaluation,
      String code_quality_evaluation) {
    this.correctness_rating = correctness_rating;
    this.efficiency_rating = efficiency_rating;
    this.communication_rating = communication_rating;
    this.code_quality_rating = code_quality_rating;
    this.correctness_evaluation = correctness_evaluation;
    this.efficiency_evaluation = efficiency_evaluation;
    this.communication_evaluation = communication_evaluation;
    this.code_quality_evaluation = code_quality_evaluation;
  }

  public Integer getCorrectnessRating() {
    return correctness_rating;
  }

  public Integer getEfficiencyRating() {
    return efficiency_rating;
  }

  public Integer getCommunicationRating() {
    return communication_rating;
  }

  public Integer getCodeQualityRating() {
    return code_quality_rating;
  }

  public String getCorrectnessEvaluation() {
    return correctness_evaluation;
  }

  public String getEfficiencyEvaluation() {
    return efficiency_evaluation;
  }

  public String getCommunicationEvaluation() {
    return communication_evaluation;
  }

  public String getCodeQualityEvaluation() {
    return code_quality_evaluation;
  }
}
