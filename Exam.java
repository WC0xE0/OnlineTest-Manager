package onlineTest;

import java.io.Serializable;
import java.util.ArrayList;

public class Exam implements Serializable {
	private static final long serialVersionUID = 1L;
	private int examId;
	private String title;
	private ArrayList<Question> questions;
	
	public Exam(int examId, String title) {
		this.examId = examId;
		this.title = title;
		questions = new ArrayList<>();
	}
	
	public int getExamId() {
		return examId;
	}
	
	public String getTitle() {
		return title;
	}
	
	public ArrayList<Question> getQuestions() {
		return questions;
	}

	public double getExamTotalScores() {
		double examTotalScores = 0;
		for(Question question: questions) {
			examTotalScores += question.getPoints();
		}
		return examTotalScores;
	}
	
	// get a specific Question object based on question number
	public Question getQuestion(int questionNumber) {
		return questions.get(questionNumber - 1);
	}
	
	// add a Question obj to the ArrayList of Questions for this exam
	public void addQuestion(Question question) {
		questions.add(question.getQuestionNumber() - 1, question);
	}
