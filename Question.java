package onlineTest;

import java.io.Serializable;

public class Question implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int questionNumber;
	private String text;
	private double points;

	public Question(int questionNumber, String text, double points) {
		this.questionNumber = questionNumber;
		this.text = text;
		this.points = points;
	}

	public int getQuestionNumber() {
		return questionNumber;
	}
	
	public String getText() {
		return text;
	}
	
	public double getPoints() {
		return points;
	}

}
