package onlineTest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Student class will represent the student who takes the exam(s) and the
 * questions that they have entered responses to.
 */
public class Student implements Comparable<Student>, Serializable {
	private static final long serialVersionUID = 1L;
	private String name;

	// maps an examId to a Map that maps questionNum to student response
	private Map<Integer, Map<Integer, Object>> responses;  // 2 types of Object

	// maps an examId to the student's score on that exam
	private Map<Integer, Double> studentExamScores;

	// maps an examId to the corresponding Exam object
	private Map<Integer, Exam> examsTaken;
	
	// maps an examId to the corresponding exam's gradingReport for the student
	private Map<Integer, Collection<String[]>> gradingReports;

	public Student(String name) {
		this.name = name;
		
		// instantiate the containers in the constructor!!
		responses = new HashMap<>();  
		studentExamScores = new HashMap<>();
		examsTaken = new HashMap<>();
		gradingReports = new HashMap<>();
	}

	public String getName() {
		return name;
	}
	
	public Map<Integer, Double> getStudentExamScores() {
		return studentExamScores;
	}
	
	public Map<Integer, Exam> getExamsTaken() {
		return examsTaken;
	}
	
	public void addToExamsTaken(Integer examId, Exam exam) {
		examsTaken.put(examId, exam);
	}
	
	public Map<Integer, Collection<String[]>> getGradingReports() {
		return gradingReports;
	}

	// This method is called in SystemManager
	public double getExamScore(int examId) {
		if (studentExamScores.containsKey(examId)) {
			return studentExamScores.get(examId);
			
		} else {
			return 0.0;
		}
	}
	
	// This method is called in SystemManager by the "answerXXXXX" methods
	public void updateExamScore(Exam exam, double increaseScore) {
		int examId = exam.getExamId();

		if (!studentExamScores.containsKey(examId)) {
			studentExamScores.put(examId, increaseScore);
		} else {
			double newScore = studentExamScores.get(examId) + increaseScore;
			studentExamScores.put(examId, newScore);
		}
		examsTaken.put(examId, exam);
	}
	
	// add a boolean response
	public void addResponse(int examId, int questionNumber, boolean response) {
		if (!responses.containsKey(examId)) {
			responses.put(examId, new HashMap<>());
		}
		Map<Integer, Object> entry = responses.get(examId);

		if (entry == null) {
			entry = new HashMap<>();
		}
		entry.put(questionNumber, response);
		responses.put(examId, entry);
	}
	
	// add a String[] response
	public void addResponse(int examId, int questionNumber, String[] response) {
		if (!responses.containsKey(examId)) {
			responses.put(examId, new HashMap<Integer, Object>());
		}
		Map<Integer, Object> entry = responses.get(examId);
		
		if (entry == null) {
			entry = new HashMap<>();
		}
		entry.put(questionNumber, response);
		responses.put(examId, entry);
	}
	
	public Object getResponse(int questionNumber) {
		return responses.get(questionNumber);
	}
	
	// returns the student's grading report for a specified exam
	public String getGradingReport(int examId) {
		StringBuffer sb = new StringBuffer();
		Exam exam = examsTaken.get(examId);
		ArrayList<Question> questions = exam.getQuestions();
		Double studenTotalScore = 0.0;
		Double totalPoints = 0.0;
		
		for (Question question : questions) {
			int questionNum = question.getQuestionNumber();
			sb.append("Question #").append(questionNum);
			sb.append(" ");
			
			if (question instanceof TrueFalseQuestion) {
				TrueFalseQuestion TFQuestion = (TrueFalseQuestion) question;
				boolean response = (boolean) responses.get(examId).get(questionNum);
				double score = TFQuestion.computeQuestionScore(response);
				sb.append(score);
				studenTotalScore += score ;

			} else if (question instanceof MultipleChoiceQuestion) {				
				MultipleChoiceQuestion MCQuestion = (MultipleChoiceQuestion) question;
				String[] response = (String[]) responses.get(examId).get(questionNum);
				double score = MCQuestion.computeQuestionScore(response);
				studenTotalScore += score;
				sb.append(score);

			} else {
				FillInTheBlanksQuestion FBQuestion = (FillInTheBlanksQuestion) question;
				String[] response = (String[]) responses.get(examId).get(questionNum);
				double score = FBQuestion.computeQuestionScore(response);
				studenTotalScore += score;
				sb.append(score);						 
			}

			sb.append(" points out of ");
			sb.append(question.getPoints()).append("\n");
			totalPoints += question.getPoints();
		}
		sb.append("Final Score: ").append(getExamScore(examId));
		sb.append(" out of ").append(totalPoints);

		return sb.toString();
	}

	@Override
	public int compareTo(Student other) {
		return getName().compareTo(other.getName());
	}
}
	
