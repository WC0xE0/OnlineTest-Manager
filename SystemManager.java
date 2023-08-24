package onlineTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SystemManager implements Manager, Serializable {

	private static final long serialVersionUID = 1L;
	
	private Map<Integer, Exam> exams;
	private Map<String, Student> students;
	private String[] letterGrades; // e.g. = {"A", "B", "C", "D", "F"};
	private double[] cutoffs; // e.g. = {90, 80, 70, 60, 0};

	public SystemManager() {
		exams = new HashMap<>();
		students = new HashMap<>();
	}

	/**
	 * Adds the specified exam to the database.
	 * @return false if exam already exists.
	 */
	public boolean addExam(int examId, String title) {
		if (exams.containsKey(examId)) {
			return false;
		} else {
			exams.put(examId, new Exam(examId, title));
			return true;
		}
	}

	/**
	 * Adds the specified student to the database. 
	 * Names are specified in the format LastName,FirstName
	 * @return false if student already exists.
	 */
	public boolean addStudent(String studentName) {
		if (students.containsKey(studentName)) {
			return false;
		} else {
			students.put(studentName, new Student(studentName));
			return true;
		}
	}

	/**
	 * Adds a true and false question to the specified exam. 
	 * If the question already exists it is overwritten.
	 * 
	 * @param examId
	 * @param questionNumber
	 * @param text           Question text
	 * @param points         total points
	 * @param answer         expected answer
	 */
	public void addTrueFalseQuestion(int examId, int questionNumber, String text, 
											double points, boolean answer) {
		Exam exam = exams.get(examId);
		Question question = new TrueFalseQuestion(questionNumber, text, points, answer);
		exam.addQuestion(question);
	};

	/**
	 * Adds a multiple choice question to the specified exam. 
	 * If the question already exists it is overwritten.
	 * 
	 * @param examId
	 * @param questionNumber
	 * @param text           Question text
	 * @param points         total points
	 * @param answer         expected answer
	 */
	public void addMultipleChoiceQuestion(int examId, int questionNumber, 
								String text, double points, String[] answer) {
		Exam exam = exams.get(examId);
		Question question = new MultipleChoiceQuestion(questionNumber, text, points, answer);
		exam.addQuestion(question);
	}

	/**
	 * Adds a fill-in-the-blanks question to the specified exam. 
	 * If the question already exits it is overwritten. 
	 * Each correct response is worth points/entries in the answer.
	 * 
	 * @param examId
	 * @param questionNumber
	 * @param text           Question text
	 * @param points         total points
	 * @param answer         expected answer
	 */
	public void addFillInTheBlanksQuestion(int examId, int questionNumber, 
								String text, double points, String[] answer) {
		Question question = new FillInTheBlanksQuestion(questionNumber, text, points, answer);
		exams.get(examId).addQuestion(question);
	}

	/**
	 * Returns a string with the following information per question: 
	 * "Question Text: " followed by the question's text. 
	 * "Points: " followed by the points for the question. 
	 * "Correct Answer: " followed by the correct answer.
	 * 
	 * The format for the correct answer will be: 
	 * 		a. True or false question: "True" or "False" 
	 * 		b. Multiple choice question: [ ] enclosing the answer 
	 * 			(each entry separated by commas) and in sorted order. 
	 * 		c. Fill in the blanks question: [ ] enclosing the answer 
	 * 			(each entry separated by commas) and in sorted order.
	 * 
	 * @param examId
	 * @return "Exam not found" if exam not found, otherwise the key
	 */
	public String getKey(int examId) {
		Exam exam = exams.get(examId);
		if (exam == null) {
			return "Exam not found";
		}

		ArrayList<Question> questions = exam.getQuestions();
		StringBuffer key = new StringBuffer();

		for (Question question : questions) {
			key.append("Question Text: ").append(question.getText());
			key.append("\nPoints: ").append(question.getPoints());
			key.append("\nCorrect Answer: ");

			if (question instanceof TrueFalseQuestion) {
				boolean ans = ((TrueFalseQuestion) question).getCorrectAnswer();
				key.append(ans ? "True" : "False").append("\n");

			} else {
				String[] ans = null;

				if (question instanceof MultipleChoiceQuestion) {
					ans = ((MultipleChoiceQuestion) question).getCorrectAnswer();
				} else if (question instanceof FillInTheBlanksQuestion) {
					ans = ((FillInTheBlanksQuestion) question).getCorrectAnswer();
				}

				Arrays.sort(ans);

				key.append("[");
				for (int i = 0; i < ans.length - 1; i++) {
					key.append(ans[i]).append(", ");
				}
				key.append(ans[ans.length - 1]).append("]\n");
			}
		}
		return key.toString();
	}

	/**
	 * Enter a question's response(i.e. student's answer) to the database.
	 * 
	 * @param studentName
	 * @param examId
	 * @param questionNumber
	 * @param answer
	 */
	public void answerTrueFalseQuestion(String studentName, int examId, 
										int questionNumber, boolean answer) {

		Student student = students.get(studentName);
		Collection<Exam> examsTaken = student.getExamsTaken().values();
		Exam currExam = exams.get(examId);

		if (!examsTaken.contains(currExam)) {
			student.addToExamsTaken(examId, currExam);
		}

		Question question = currExam.getQuestion(questionNumber);
		TrueFalseQuestion TFQuestion = (TrueFalseQuestion) question;

		if (TFQuestion.isCorrectAnswer(answer)) {
			student.updateExamScore(currExam, question.getPoints());
		}

		student.addResponse(examId, questionNumber, answer);
	}

	/**
	 * Enter a question's response(i.e. student's answer) to the database.
	 *
	 * @param studentName
	 * @param examId
	 * @param questionNumber
	 * @param answer
	 */
	public void answerMultipleChoiceQuestion(String studentName, int examId, 
										int questionNumber, String[] answer) {
		Student student = students.get(studentName);
		Collection<Exam> examsTaken = student.getExamsTaken().values();
		Exam currExam = exams.get(examId);

		if (!examsTaken.contains(currExam)) {
			student.addToExamsTaken(examId, currExam);
		}

		Question question = currExam.getQuestion(questionNumber);
		MultipleChoiceQuestion MCQuestion = (MultipleChoiceQuestion) question;

		if (MCQuestion.isCorrectAnswer(answer)) {
			student.updateExamScore(currExam, question.getPoints());
		} else {
			student.updateExamScore(currExam, 0.0);
		}

		student.addResponse(examId, questionNumber, answer);
	}

	/**
	 * Enter a question's response(i.e. student's answer) to the database.
	 *
	 * @param studentName
	 * @param examId
	 * @param questionNumber
	 * @param answer
	 */
	public void answerFillInTheBlanksQuestion(String studentName, int examId, 
										int questionNumber, String[] answer) {
		Student student = students.get(studentName);
		Exam currExam = exams.get(examId);
		Collection<Exam> examsTaken = student.getExamsTaken().values();

		if (!examsTaken.contains(currExam)) {
			student.addToExamsTaken(examId, currExam);
			student.getGradingReports().put(examId, new ArrayList<>());
		}

		Question question = currExam.getQuestion(questionNumber);
		FillInTheBlanksQuestion FBQuestion = (FillInTheBlanksQuestion) question;

		if (FBQuestion.isCorrectAnswer(answer)) {
			student.updateExamScore(currExam, question.getPoints());
		} else {
			double score = FBQuestion.computeQuestionScore(answer);
			student.updateExamScore(currExam, score);
		}

		student.addResponse(examId, questionNumber, answer);
	}

	/**
	 * Returns the score the student got for the specified exam.
	 * 
	 * @param studentName
	 * @param examId
	 * @return score
	 */
	public double getExamScore(String studentName, int examId) {
		return students.get(studentName).getExamScore(examId);
	}

	/**
	 * Generates a grading report for the specified exam.
	 * The report will include the following info for each exam question:
	 * "Question #" {questionNumber} {questionScore} " points out of "
	 * {totalQuestionPoints}
	 * 
	 * The report will end with the following info: "Final Score: " {score} "
	 * out of " {totalExamPoints};
	 * 
	 * @param studentName
	 * @param examId
	 * @return report
	 */
	public String getGradingReport(String studentName, int examId) {
		Student student = students.get(studentName);
		return student.getGradingReport(examId);
	}

	/**
	 * Sets the cutoffs for letter grades. e.g., a typical curve has new
	 * String[]{"A","B","C","D","F"}, new double[] {90,80,70,60,0}. 
	 * Anyone with 90 or above gets an A, anyone with 80 or above gets a B, etc. 
	 * Notice we can have different letter grades and cutoffs.
	 * 
	 * @param letterGrades
	 * @param cutoffs
	 */
	public void setLetterGradesCutoffs(String[] letterGrades, double[] cutoffs) {
		this.letterGrades = letterGrades;
		this.cutoffs = cutoffs;
	}

// ------------- compute Student grades: ------------------

	/**
	 * Computes a numeric grade (between 0 and a 100) for the student, taking 
	 * into consideration all the exams. All exams have the same weight.
	 * 
	 * Formula: (exam1score/exam1totalscore + ...) / totalNumberOfExams
	 */
	public double getCourseNumericGrade(String studentName) {
		Student student = students.get(studentName);
		Map<Integer, Double> studentScores = student.getStudentExamScores();
		Set<Integer> examIds = studentScores.keySet();
		double totalAdjustedScore = 0.0;
		double numericGrade = 0.0;

		for (int examId : examIds) {
			double currExamStudentScore = studentScores.get(examId);
			double currExamTotalScore = exams.get(examId).getExamTotalScores();
			double currExamAdjustedScore = 0.0;

			if (currExamTotalScore != 0.0) {
				currExamAdjustedScore = 100 * currExamStudentScore / currExamTotalScore;
				totalAdjustedScore += currExamAdjustedScore;
			}
		}

		if (studentScores.size() != 0) {
			numericGrade = totalAdjustedScore / studentScores.size();
		}

		return numericGrade;
	}

	/**
	 * Computes a letter grade based on cutoffs provided. It is assumed the 
	 * cutoffs have been set before the method is called.
	 */
	public String getCourseLetterGrade(String studentName) {
		String letterGrade = "F";
		double numericGrade = getCourseNumericGrade(studentName);

		for (int i = 0; i < cutoffs.length; i++) {
			if (numericGrade >= cutoffs[i]) {
				letterGrade = letterGrades[i];
				break;
			}
		}

		return letterGrade;
	}

	/**
	 * Returns a listing with grades for each student.
	 * For each student, the report will include the following information:
	 * {studentName} {courseNumericGrade} {courseLetterGrade}
	 * The names will appear in sorted order.
	 * 
	 * @return grades
	 */
	public String getCourseGrades() {
		StringBuffer sb = new StringBuffer();
		Collection<Student> allStudents = students.values();
		Student[] studentsArray = allStudents.toArray(new Student[] {});
		Arrays.sort(studentsArray); 

		for (Student student : studentsArray) {
			String name = student.getName();
			sb.append(name).append(" ");
			sb.append(getCourseNumericGrade(name)).append(" ");
			sb.append(getCourseLetterGrade(name)).append("\n");
		}

		return sb.toString();
	}

	
// ---------------------- Compute Exam Statistics: -----------------------
	
	/**
	 * Returns the maximum score (among all students) for the specified exam.
	 *
	 * @param examId
	 * @return maxScore
	 */
	public double getMaxScore(int examId) {
		double maxScore = 0.0, currScore = 0.0;
		
		for(Student student : students.values()) {
			currScore = student.getExamScore(examId);
			if (currScore > maxScore) {
				maxScore = currScore;
			}
		}

		return maxScore;		
	}

	/**
	 * Returns the minimum score (among all students) for the specified exam.
	 *
	 * @param examId
	 * @return minScore
	 */
	public double getMinScore(int examId) {
		double minScore = 1000.0, currScore = 0.0;
		
		for(Student student : students.values()) {
			currScore = student.getExamScore(examId);
			if (currScore < minScore) {
				minScore = currScore;
			}
		}

		return minScore;	
	}

	/**
	 * Returns the average score (among all students) for the specified exam.
	 *
	 * @param examId
	 * @return average
	 */
	public double getAverageScore(int examId) {
		double average = 0.0, currScore = 0.0;
		double sumScores = 0.0;

		for(Student student : students.values()) {
			currScore = student.getExamScore(examId);
			sumScores += currScore;
		}
		average = sumScores / students.size();

		return average;
	}
	

//---------------------------- Serialization: ------------------------------

	/**
	 * It will serialize the Manager object and store it in the specified file.
	 */
	public void saveManager(Manager manager, String fileName) {
		try {
			FileOutputStream fileOut = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(manager);

			out.close();
			fileOut.close();
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * It will return a Manager object based on the serialized data 
	 * found in the specified file.
	 */
	public Manager restoreManager(String fileName) {
		Manager manager = null;
		
		try {
			FileInputStream fileIn = new FileInputStream(fileName);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			manager = (Manager) in.readObject();	
	
			in.close();
			fileIn.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return (Manager) manager;
	}
}
