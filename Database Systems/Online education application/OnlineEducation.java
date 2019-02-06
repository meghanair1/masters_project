package OnlineEducation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
// ⁨Desktop/138/TermProject/mysql-connector-java-8.0.13/mysql-connector-java-8.0.13.jar 
//export CLASSPATH=Desktop/138/TermProject/mysql-connector-java-8.0.13/mysql-connector-java-8.0.13.jar:$CLASSPATH
//java -cp .:$HOME/Desktop⁩/138⁩/TermProject⁩/mysql-connector-java-8.0.13⁩/mysql-connector-java-8.0.13.jar
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

import com.mysql.cj.jdbc.Driver;

public class OnlineEducation {
	static Statement mystatement;
	static Scanner sc = new Scanner(System.in);
	static boolean options = true;
	boolean choose = true;
	static boolean flag = true;
	static int personID;
	protected static String defaultLogFile = "/Users/megha/Desktop/project/log.txt";

	public static void main(String[] args) throws SQLException, IOException {
		DriverManager.registerDriver(new Driver());
		String url = "jdbc:mysql://localhost:3306/online_edu?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
		String user = "root";
		String password = "root";
		try (Connection myconn = DriverManager.getConnection(url, user, password)) {
			mystatement = myconn.createStatement();
			System.out.println("Welcome to Online Eductation Database");
			int option = 0;
			int selection = 0;
			while (flag) {
				// login as an instructor or a Student
				System.out.println("Do you login as instructor (Option 1) or student (Option 2)?");
				String s = sc.next();

				// login as an instructor
				if (s.equalsIgnoreCase("Instructor") || s.equalsIgnoreCase("1")) {
					System.out.println("Welcome, you logged in as a Instructor! Please type in your ID:");
					OnlineEducation.write("you successfully logged in as a Instructor");

					greeting("Instructor");

					// if logged in as an instructor choose the following option
					while (options) {
						System.out.println(
								"\nChoose from the following options\n\n Option 1: View Category\n Option 2: View students enrolled in different courses\n Option 3: Create courses\n Option 4: Create Sections\n Option 5: View courses created\n "
										+ "Option 6: View section for each course\n Option 7: Create assignments,Quizzes or Exams\n Option 8: Publish grades\n Option 9: Delete course\n Option 10: Delete section");
						System.out.println();
						option = sc.nextInt();
						// if logged in as an instructor and option 1 is choosen

						if (option == 1) {
							option1();
							OnlineEducation.write("Option 1: View Category executed successfully");
						}

						// if logged in as an instructor and option 2 is chosen
						else if (option == 2) {
							option2();
							OnlineEducation.write(
									"Option 2: View students enrolled in different courses executed successfully");

						}
						// if logged in as an instructor and option 3 is chosen
						else if (option == 3) {
							option3();
							OnlineEducation.write("Option 3: Create courses executed successfully");

						}
						// if logged in as an instructor and option 4 is choosen
						else if ((option == 4)) {
							option4();
							OnlineEducation.write("Option 4: Create sections executed successfully");

						}

						// if logged in as an instructor and option 5 is chosen
						else if ((option == 5)) {
							option5();
							OnlineEducation.write("Option 5: View courses created executed successfully");
						} // if logged in as an instructor and option 6 is chosen
						else if (option == 6) {
							option6();
							OnlineEducation.write("Option 6: View section for each course executed successfully");
						}

						// if logged in as an instructor and option 7 is chosen
						else if (option == 7) {
							option7();
							OnlineEducation
									.write("Option 7: Create assignments,Quizzes or Exams executed successfully");

						} else if (option == 8) {
							option8();
							OnlineEducation.write("Option 8: Publish grades executed successfully");
						} else if (option == 9) {
							option9();
							OnlineEducation.write("Option 9: Delete course executed successfully");
						} else if (option == 10) {
							option10();
							OnlineEducation.write("Option 10: Delete section executed successfully");
						}
						if (choose().equalsIgnoreCase("yes")) {
							continue;
						} else {
							System.out.println("Exiting the Online Education Application");
							OnlineEducation.write("Exiting the Online Education Application");
							flag = false;
							break;
						}

					}
				} else if (s.equalsIgnoreCase("Student") || s.equalsIgnoreCase("2")) {
					System.out.println("Welcome, you logged in as a student! Please type in your ID:\n");
					OnlineEducation.write("you successfully logged in as a Student");
					greeting("Student");
					while (options) {
						System.out.println(
								"\nChoose from the following options\n\n Option 1. View Category\n Option 2. View courses of a category\n "
										+ "Option 3. View sections for a course\n Option 4. Enroll in a section \n Option 5: Drop a class(section)\n "
										+ "Option 6. View assignments,Quizzes or Exams\n Option 7: View your grades");
						System.out.println();
						selection = sc.nextInt();
						if (selection == 1) {
							option1();
							OnlineEducation.write("Option 1. View Category executed successfully");
						} else if (selection == 2) {
							selection2();
							OnlineEducation.write("Option 2. View courses of a category executed successfully");

						} else if (selection == 3) {
							selection3();
							OnlineEducation.write("Option 3. View sections for a courses executed successfully");
						} else if ((selection == 4)) {
							selection4();
							OnlineEducation.write("Option 4. Enroll in a section executed successfully");
						} else if ((selection == 5)) {
							selection5(myconn);
							OnlineEducation.write("Option 5: Drop a class(section) executed successfully");
						} else if ((selection == 6)) {
							OnlineEducation.write("Option 6. View assignments,Quizzes or Exams executed successfully");
						} else if ((selection == 7)) {
							selection7();
							OnlineEducation.write("Option 7: View your grades executed successfully");
						}
						if (choose().equalsIgnoreCase("yes")) {
							OnlineEducation.write("continue to choose the option");
							continue;
						} else {
							System.out.println("Exiting the Online Education Application");
							OnlineEducation.write("Exiting the Online Education Application");
							flag = false;
							break;
						}

					}
				} else {
					System.out.println("Invalid input for log in!");
					OnlineEducation.write("Invalid input for log in!");
				}

			}
		}

	}

	/*************************************
	 * Functionality of Instructor
	 * 
	 **************************************/

	// Option 1. View Category
	private static void option1() throws SQLException {
		System.out.println();
		System.out.println("The following are the categories\n ");
		ResultSet rs = mystatement.executeQuery("Select * from Category");
		System.out.println("Category Name");
		while (rs.next()) {
			System.out.println(rs.getString("c_name"));

		}

	}

	// Option 2. View students enrolled in different courses
	private static void option2() throws SQLException {
		System.out.println("Please type in the Course ID shown below:\n ");
		ResultSet rs = mystatement.executeQuery("Select * from Courses");
		// System.out.println("Course ID" + "\t\t" + "Course name" + "\t\t" + "Category
		// ID");
		System.out.printf("%-20s\t%20s\t%20s \n", "Course ID", "Course Name", "Category ID\n");
		while (rs.next()) {

			System.out.printf("%-20s\t%20s\t%20s \n", rs.getString("cou_id"), rs.getString("cou_name"),
					rs.getString("category_id"));

		}
		System.out.println("Enter the Course ID");
		String courseid = sc.next();

		ResultSet rs2 = mystatement.executeQuery(
				"Select cou_id,s_name,student_id  from Courses join Student on Courses.category_id =Student.category_id where cou_id = "
						+ courseid);
		System.out.printf("%-20s\t%20s\t%20s \n", "Course ID", "Student Name", "Category ID");
		while (rs2.next()) {
			System.out.printf("%-20s\t%20s\t%20s \n", rs2.getString("cou_id"), rs2.getString("s_name"),
					rs2.getString("student_id"));
		}

	}

	// Option 3. Create courses
	private static void option3() throws IOException {
		String coursename = "";
		System.out.println("Select category id  from the below category to create a course \n ");
		try {
			ResultSet rs = mystatement.executeQuery("Select * from Category");
			System.out.printf("%-20s\t%20s \n", "Category ID", "Category name");

			while (rs.next()) {

				System.out.printf("%-20s\t%20s \n", rs.getString("c_id"), rs.getString("c_name"));

			}
			System.out.println("Enter the Category ID");
			String cat_id = sc.next();
			System.out.println("Enter the Course name");
			coursename = sc.next();
			sc.nextLine();

			System.out.println("Enter the Course id");
			String courseid = sc.next();

			String sql1 = "insert into Courses " + " (cou_id,cou_name,category_id)" + "values('" + courseid + "','"
					+ coursename + "','" + cat_id + "')";
			mystatement.executeUpdate(sql1);
			System.out.println("The following new course is added to the course list\nCoursename: " + coursename + "\n"
					+ "Courseid: " + courseid);

		} catch (Exception w) {
			System.out.println("Primary key cannot be duplicate. Choose a different value");
			OnlineEducation.write("Primary key cannot be duplicate. Choose a different value");
		}
	}

	// Option 4: Create sections
	private static void option4() throws SQLException {
		System.out.println("Select the Instructor ID from the following list");
		ResultSet rs = mystatement.executeQuery("Select * from instructor");
		System.out.printf("%-20s\t%20s \n", "Instructor ID", "Instructor name");
		while (rs.next()) {

			System.out.printf("%-20s\t%20s \n", rs.getString("i_id"), rs.getString("i_name"));

		}
		System.out.println("Enter the Instructor ID");
		int instructor_id = sc.nextInt();

		System.out.println("Select the course ID from the following list\n");
		System.out.println("Following are the courses");
		rs = mystatement.executeQuery("Select * from Courses");
		System.out.printf("%-20s\t%20s\t%20s \n", "Course ID", "Course name", "Category ID");
		while (rs.next()) {

			System.out.printf("%-20s\t%20s\t%20s \n", rs.getString("cou_id"), rs.getString("cou_name"),
					rs.getString("category_id"));

		}
		System.out.println("Enter the course ID");
		String course_id = sc.next();
		ResultSet rs1 = mystatement
				.executeQuery("Select * from Section where co_id='" + course_id + "'and inst_id=" + instructor_id);
		System.out.println("Enter the section ID");
		int section_id = sc.nextInt();
		String sql1 = "insert into Section " + " (section_number,co_id,inst_id)" + "values('" + section_id + "','"
				+ course_id + "','" + instructor_id + "')";
		mystatement.executeUpdate(sql1);
		System.out.println("Section " + section_id + " for the courseID " + course_id + " created successfully");

	}

	// Option 5. View courses created
	private static void option5() throws SQLException {

		System.out.println("Following are the courses");
		ResultSet rs = mystatement.executeQuery("Select * from Courses");
		System.out.printf("%-20s\t%20s\t%20s\n", "Course ID", "Course name", "Category ID");
		while (rs.next()) {

			System.out.printf("%-20s\t%20s\t%20s\n", rs.getString("cou_id"), rs.getString("cou_name"),
					rs.getString("category_id"));

		}

	}

	// Option 6: View section for each course
	private static void option6() throws SQLException {
		System.out.println("Select course id  from the below courses to view the sections\n ");
		ResultSet rs = mystatement.executeQuery("Select * from Courses");
		System.out.printf("%-20s\t%20s\t%20s\n", "Course ID", "Course name", "Category ID");
		while (rs.next()) {

			System.out.printf("%-20s\t%20s\t%20s\n", rs.getString("cou_id"), rs.getString("cou_name"),
					rs.getString("category_id"));

		}
		System.out.println("enter the course id");
		String courseid = sc.next();
		String sql = "Select * from Section where co_id =" + courseid;
		rs = mystatement.executeQuery(sql);
		System.out.printf("%-20s\t%20s\t%20s\n", "Section Number", "Course Id", "Instructor ID");
		while (rs.next()) {

			System.out.printf("%-20s\t%20s\t%20s\n", rs.getString("section_number"), rs.getString("co_id"),
					rs.getString("inst_id"));

		}
	}

	// Option 7. Create assignments,Quizzes or Exams
	private static void option7() {
		System.out.println("Select an option to create\n Assignment(Option 1)\n Exam(Option 2)\n Quizzes(Option 3)");
		int option6 = sc.nextInt();
		// create an assignment
		if (option6 == 1) {
			while (true) {
				try {
					System.out.println("Select the following details to create an assignment");

					System.out.println("Select section id from the below section to create an assignment\n ");
					ResultSet rs1 = mystatement.executeQuery("Select * from Section");
					System.out.printf("%-20s\t%20s\t%20s\n", "Section Number", "Course ID", "Instructor ID");
					while (rs1.next()) {

						System.out.printf("%-20s\t%20s\t%20s\n", rs1.getString("section_number"),
								rs1.getString("co_id"), rs1.getString("inst_id"));

					}
					System.out.println("Enter the section number");
					String sec_num = sc.next();
					System.out.println("Enter the course number");
					String courseid = sc.next();
					System.out.println("Enter the assignment number");
					String ass_no = sc.next();

					String sql = "insert into Sec_Assignments " + " (s_numb,co_id,ass_no)" + "values('" + sec_num
							+ "','" + courseid + "','" + ass_no + "')";
					mystatement.executeUpdate(sql);
					System.out.println("The following new assignment is added to the assignment list\nAssignment No: "
							+ ass_no + "\n" + "Course Id: " + courseid);
					break;

				} catch (Exception w) {
					System.out.println("Primary key cannot be duplicate. Choose a different value");

				}
			}

		}
		// create an exam
		if (option6 == 2) {
			while (true) {
				try {
					System.out.println("Select the following details to create an exam");
					System.out.println("Select section id  from the below section to create an exam\n ");
					ResultSet rs1 = mystatement.executeQuery("Select * from Section");
					System.out.printf("%-20s\t%20s\t%20s\n", "Section Number", "Course_ID", "Instructor ID");
					while (rs1.next()) {

						System.out.printf("%-20s\t%20s\t%20s\n", rs1.getString("section_number"),
								rs1.getString("co_id"), rs1.getString("inst_id"));

					}
					System.out.println("Enter the section number");
					String sec_num = sc.next();
					System.out.println("Enter the course number");
					String courseid = sc.next();
					System.out.println("Enter the exam number");
					String exam_no = sc.next();

					String sql = "insert into Sec_Exams " + " (s_numb,co_id,exam_no)" + "values('" + sec_num + "','"
							+ courseid + "','" + exam_no + "')";
					mystatement.executeUpdate(sql);
					System.out.println("The following new exam is added to the exam list\n Exam No: " + exam_no + "\n"
							+ "Course ID: " + courseid);
					break;

				} catch (Exception w) {
					System.out.println("Primary key cannot be duplicate. Choose a different value");

				}
			}

		}
		// create a Quiz
		else if (option6 == 3) {
			System.out.println("select the following details to create a Quiz");
			int option6Quiz = sc.nextInt();
			if (option6 == 3) {
				while (true) {
					try {
						System.out.println("select the following details to create an Quiz");

						System.out.println("Select section id  from the below section to create an Quiz\n ");
						ResultSet rs1 = mystatement.executeQuery("Select * from Section");
						System.out.printf("%-20s\t%20s\t%20s\n", "section_number", "co_id", "inst_id");
						while (rs1.next()) {

							System.out.printf("%-20s\t%20s\t%20s\n", rs1.getString("section_number"),
									rs1.getString("co_id"), rs1.getString("inst_id"));

						}
						System.out.println("enter the section number");
						String sec_num = sc.next();
						System.out.println("enter the course number");
						String courseid = sc.next();
						System.out.println("enter the Quiz number");
						String Quiz_no = sc.next();

						String sql = "insert into Sec_Quizzes " + " (s_numb,co_id,quiz_no)" + "values('" + sec_num
								+ "','" + courseid + "','" + Quiz_no + "')";
						mystatement.executeUpdate(sql);
						System.out.println("The following new assignment is added to the assignment list\naquiz no: "
								+ Quiz_no + "\n" + "courseid: " + courseid);
						break;

					} catch (Exception w) {
						System.out.println("Primary key cannot be duplicate. Choose a different value");

					}
				}
			}
		}
	}

	// Option 8: Publish grades
	private static void option8() throws SQLException {
		System.out.println();

		ResultSet rs = mystatement.executeQuery("Select * from Student");
		System.out.println("Select Student ID from student list to view his/her grades");
		System.out.printf("%-20s\t%20s\n", "Student ID", "Student Name");
		while (rs.next()) {
			System.out.printf("%-20s\t%20s\n", rs.getString("Student_id"), rs.getString("s_name"));

		}
		System.out.println();
		System.out.println("Choose Student ID");
		int student_id = sc.nextInt();
		String sql = "Select * from enrolled_in where Sst_id =" + student_id;
		rs = mystatement.executeQuery(sql);
		System.out.printf("%-20s\t%20s\t%20s\t%20s\n", "Student ID", "Section Number", "Course ID", "grade");

		while (rs.next()) {

			System.out.printf("%-20s\t%20s\t%20s\t%20s\n", rs.getString("Sst_id"), rs.getString("Ssec_num"),
					rs.getString("Sco_id"), rs.getString("grade"));
		}
	}

	// option 9:delete course
	private static void option9() throws SQLException {
		System.out.println("Select the course id of courses you want to delete");

		ResultSet rs = mystatement.executeQuery("Select * from courses");
		System.out.printf("%-20s\t%20s\t%20s\n", "Course ID", "Course Name", "Category ID");
		while (rs.next()) {
			System.out.printf("%-20s\t%20s\t%20s\n", rs.getString("cou_id"), rs.getString("cou_name"),
					rs.getString("category_id"));

		}
		System.out.println("choose Course ID");
		String course_id = sc.next();
		String sql = "Delete from courses where cou_id =" + course_id;
		mystatement.executeUpdate(sql);
		System.out.println("Course " + course_id + " deleted successfully");

	}

	// option 10:delete section
	private static void option10() throws SQLException {
		System.out.println("Select the Course ID for which you want to delete the section");

		ResultSet rs = mystatement.executeQuery("Select * from courses");
		System.out.printf("%-20s\t%20s\t%20s\n", "Course ID", "Course Name", "Category ID");
		while (rs.next()) {
			System.out.printf("%-20s\t%20s\t%20s\n", rs.getString("cou_id"), rs.getString("cou_name"),
					rs.getString("category_id"));

		}

		System.out.println("Select the section id from courses which you want to delete the section");
		System.out.println("choose Course ID");
		String course_id = sc.next();
		ResultSet rs1 = mystatement.executeQuery("Select * from Section where co_id=" + course_id);
		System.out.printf("%-20s\t%20s\t%20s \n", "section_number", "co_id", "inst_id");
		while (rs1.next()) {

			System.out.printf("%-20s\t%20s\t%20s \n", rs1.getString("section_number"), rs1.getString("co_id"),
					rs1.getString("inst_id"));

		}
		System.out.println("choose section ID you want to delete");
		int sec_id = sc.nextInt();
		String sql = "Delete from section where section_number =" + sec_id;
		mystatement.executeUpdate(sql);
		System.out.println("Section " + sec_id + " of course " + course_id + " deleted successfully");

	}

	/*************************************
	 * Functionality of Student
	 * 
	 **************************************/

	// *****************************************************selection 2. View
	// courses of a category*********************************

	private static void selection2() throws SQLException {
		System.out.println("Please type in the Category ID shown below:\n ");
		ResultSet rs = mystatement.executeQuery("Select * from Category");
		System.out.printf("%-20s\t%20s \n", "Category ID", "Category name");
		System.out.println();
		try {
			while (rs.next()) {

				System.out.printf("%-20s\t%20s \n", rs.getString("c_id"), rs.getString("c_name"));

			}
			System.out.println("enter the Category ID");
			String c_id = sc.next();
			ResultSet rs1 = mystatement.executeQuery("select * from Courses where category_id = " + c_id);
			System.out.printf("%-20s\t%20s\t%20s\n", "Course ID", "Course name", "Category ID");
			while (rs1.next()) {
				System.out.printf("%-20s\t%20s\t%20s\n", rs1.getString("cou_id"), rs1.getString("cou_name"),
						rs1.getString("category_id"));
			}

		} catch (Exception w) {
			System.out.println(w);
		}

	}

	// *******************************************************selection 3. View
	// sections of a course**************************************

	private static void selection3() throws SQLException {
		String continu = "yes";
		while (continu.equalsIgnoreCase("yes")) {
			selection2();
			System.out.format("Type the course id that you want view its section details: \n");
			String courseid = sc.next();
			try {

				String sql_s3 = "select * from Section where co_id = " + courseid;
				ResultSet rs_s3 = mystatement.executeQuery(sql_s3);
				System.out.printf("%-20s\t%20s\t%20s\n", "Section no", "Course ID", "Instructor ID");
				while (rs_s3.next()) {
					System.out.printf("%-20s\t%20s\t%20s\n", rs_s3.getString("section_number"),
							rs_s3.getString("co_id"), rs_s3.getString("inst_id"));
				}
				System.out.println("Do you want to continue viewing sections? yes or no?");
				continu = sc.next();
			} catch (Exception w) {
				System.out.println(w);
			}
		}

	}

	// *******************************************************selection 4. Enrol in
	// a section of choice**************************************
	private static void selection4() throws SQLException {
		selection3();
		System.out.println("Select from below to get enrolled\n ");
		ResultSet rs = mystatement.executeQuery("Select * from Courses join Section where co_id = cou_id");
		System.out.printf("%-20s\t%20s\t%20s\t%20s\n", "Course number", "Course name", "Section number",
				"Instructor ID");
		try {
			while (rs.next()) {

				System.out.printf("%-20s\t%20s\t%20s\t%20s\n", rs.getString("co_id"), rs.getString("cou_name"),
						rs.getString("section_number"), rs.getString("inst_id"));

			}
			System.out.println("enter the Course ID you are interested in: \n");
			String Sco_id = sc.next();
			System.out.println("enter the Section ID \n");
			String Ssec_num = sc.next();

			String Sst_id = Integer.toString(personID);
			String sql1 = "insert into Enrolled_in " + " (Sst_id,Ssec_num,Sco_id)" + "values('" + Sst_id + "','"
					+ Ssec_num + "','" + Sco_id + "')";

			mystatement.executeUpdate(sql1);
			System.out.println("You successfully enrolled in section: " + Sst_id + " " + Sco_id + "\n");
			// flag = false;

		} catch (Exception w) {
			System.out.println(w);
		}
	}

	// *******************************************************selection 5. Drop a
	// class**********************************************************
	private static void selection5(Connection myconn) throws SQLException {

		System.out.println("Select what you don't want to participate in any more. \n");
		String std_id = Integer.toString(personID);
		ResultSet rs = mystatement.executeQuery(
				"Select Sco_id, cou_name, Ssec_num from Enrolled_in left join Courses on cou_id = Sco_id where Sst_id = "
						+ std_id);
		System.out.printf("%-20s\t%20s\t%20s\n", "Course ID", "Course name", "Section number");

		while (rs.next()) {

			System.out.printf("%-20s\t%20s\t%20s\n", rs.getString("Sco_id"), rs.getString("cou_name"),
					rs.getString("Ssec_num"));

		}
		System.out.println("enter the course ID you are not interested in any more: \n");
		String cou_drop = sc.next();
		System.out.println("enter the section ID you are not interested in any more: \n");
		String sec_drop = sc.next();

		try {
			myconn.setAutoCommit(false);
			String sql_s5 = "insert into Drops values(" + std_id + "," + sec_drop + "," + "'" + cou_drop + "')";
			String sql_s51 = "delete from Enrolled_in where Sst_id = " + std_id + " and Sco_id = '" + cou_drop
					+ "' and Ssec_num = " + sec_drop;
			mystatement.executeUpdate(sql_s5);
			mystatement.executeUpdate(sql_s51);

		} catch (Exception e) {
			myconn.rollback();
		}

		System.out.println("We are sorry you don't like our class. Hope you won't regret though:)");
	}

	// *******************************************************selection 6. View
	// Assign/exams/quizzes**********************************************************
	private static void selection6() throws SQLException {
		try {
			System.out.println("Select the section of a course that you want to view assignments,Quizzes or Exams. \n");
			String std_id = Integer.toString(personID);
			// Return the student-specific enrolled section list
			ResultSet rs = mystatement.executeQuery(
					"Select Sco_id, cou_name, Ssec_num from Enrolled_in left join Courses on cou_id = Sco_id where Sst_id = "
							+ std_id);
			System.out.printf("%-20s\t%20s\t%20s\n", "Course ID", "Course name", "Section number");
			// output result set
			while (rs.next()) {
				System.out.printf("%-20s\t%20s\t%20s\n", rs.getString("Sco_id"), rs.getString("cou_name"),
						rs.getString("Ssec_num"));
			}
			System.out.println(
					"enter the course ID you want to view more information about what you will be working on: \n");
			String cou_drop = sc.next();
			System.out.println("enter the section ID: \n");
			String sec_drop = sc.next();

			String sql_s61 = "select * from Sec_Assignments where co_id = '" + cou_drop + "' and s_numb =" + sec_drop;
			String sql_s62 = "select * from Sec_Quizzes where co_id = '" + cou_drop + "'and s_numb =" + sec_drop;
			String sql_s63 = "select * from Sec_Exams where co_id = '" + cou_drop + "'and s_numb =" + sec_drop;

			ResultSet rs61 = mystatement.executeQuery(sql_s61);
			System.out.printf("%-20s\t%20s\t%20s\n", "Section number", "Course number", "Assignment number");
			while (rs61.next()) {
				System.out.printf("%-20s\t%20s\t%20s\n", rs61.getString("co_id"), rs61.getString("s_numb"),
						rs61.getString("ass_no"));
			}
			System.out.printf("%-20s\t%20s\t%20s\n", "Section number", "Course number", "Quiz number");
			ResultSet rs62 = mystatement.executeQuery(sql_s62);
			while (rs62.next()) {
				System.out.printf("%-20s\t%20s\t%20s\n", rs62.getString("co_id"), rs62.getString("s_numb"),
						rs62.getString("quiz_no"));
			}
			System.out.printf("%-20s\t%20s\t%20s\n", "Section number", "Course number", "Exam number");
			ResultSet rs63 = mystatement.executeQuery(sql_s63);
			while (rs63.next()) {
				System.out.printf("%-20s\t%20s\t%20s\n", rs63.getString("co_id"), rs63.getString("s_numb"),
						rs63.getString("exam_no"));
			}

		} catch (Exception w) {
			System.out.println(w);
		}
	}

	// *******************************************************selection 7. View your
	// enclosed grades **********************************************************
	private static void selection7() throws SQLException {
		try {
			String std_id = Integer.toString(personID);
			// Return the student-specific enrolled section list
			ResultSet rs = mystatement.executeQuery(
					"Select Sco_id, cou_name, Ssec_num, Grade from Enrolled_in left join Courses on cou_id = Sco_id where Grade is not NULL and  Sst_id = "
							+ std_id);
			System.out.printf("%-20s\t%30s\t%20s\t%20s\n", "Course ID", "Course name", "Section number", "Grade");
			// output result set
			while (rs.next()) {
				System.out.printf("%-20s\t%30s\t%20s\t%20s\n", rs.getString("Sco_id"), rs.getString("cou_name"),
						rs.getString("Ssec_num"), rs.getString(4));
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	// choose if the user wants to continue or exit the application
	private static String choose() {
		System.out.println("Do you want to choose another option(yes) or exit the application(No)?");
		String optionchoice = sc.next();
		return optionchoice;
	}

	private static void greeting(String person) throws SQLException {
		while (true) {
			System.out.println("Enter your id: ");
			if (person.equalsIgnoreCase("Instructor")) {
				try {
					personID = sc.nextInt();
					String sql = "Select i_name from Instructor where i_id =" + personID;
					ResultSet rs = mystatement.executeQuery(sql);
					if (rs.next()) {
						System.out.println("Welcome Prof. " + rs.getString("i_name"));
						break;
					} else {
						System.out.println("This ID doesn't exists in database");
					}
				} catch (Exception e) {
					System.out.println("Invalid id type!");
					sc.next();
				}
			} else if (person.equalsIgnoreCase("student")) {
				try {
					personID = sc.nextInt();
					String sql = "Select s_name from Student where student_id =" + personID;
					ResultSet rs = mystatement.executeQuery(sql);
					if (rs.next()) {
						System.out.println("Welcome " + rs.getString("s_name") + "!");
						break;
					} else {
						System.out.println("Sorry, please type in correct user ID.");
					}

				} catch (Exception e) {
					System.out.println("Invalid id type!");
					sc.next();
				}

			}
		}
	}

	// code to log the output and the errors in log file
	public static void write(String s) throws IOException {
		write(defaultLogFile, s);
	}

	public static void write(String f, String s) throws IOException {
		TimeZone tz = TimeZone.getTimeZone("PST"); // or PST, MID, etc ...
		Date now = new Date();
		DateFormat df = new SimpleDateFormat("yyyy.mm.dd hh:mm:ss ");
		df.setTimeZone(tz);
		String currentTime = df.format(now);

		FileWriter aWriter = new FileWriter(f, true);
		aWriter.write(currentTime + " " + s + "\n");
		aWriter.flush();
		aWriter.close();
	}
}