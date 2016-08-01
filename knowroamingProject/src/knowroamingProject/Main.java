package knowroamingProject;

/*
 * For Know Roaming Job Application
 * 
 * author: Derek Galbraith
 * 
 */

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Random;

public class Main {
	static class State {
		public static final String CREATE_USER = "usr";
		public static final String ENTER_USER_DATA = "data";
		public static final String ERROR = "error";
		
		public static final String ENTER = "ent";
		public static final String RETRIEVE = "ret";
		public static final String EXIT = "exit";
	}

	static class UsageType {
		public static final String DATA = "data";
		public static final String VOICE = "voice";
		public static final String SMS = "sms";
		
		public static final String ALL = "all";
	}
	
	static class Set {
		public boolean consoleToggle;
		public Console c;
		public BufferedReader br;

		public Set() {

		}

		public void setConsoleToggle(boolean consoleToggle) {
			this.consoleToggle = consoleToggle;
		}

		public void setConsole(Console c) {
			this.c = c;
		}

		public void setBufferedReader(BufferedReader br) {
			this.br = br;
		}
	}
	
	public static void main (String args[]) throws Exception {
		Set consoleSet = new Set();
		consoleSet.setBufferedReader(new BufferedReader(new InputStreamReader(System.in)));
		consoleSet.setConsole(System.console());
		consoleSet.setConsoleToggle(true);
		if (consoleSet.c == null) {
			consoleSet.setConsoleToggle(false);
		}

		writeConsoleMessage(consoleSet, "Initialized...");
		String consoleState = "";
		MySQLAccess dao = InitializeDatabase(consoleSet);
		if (dao == null) {
			writeConsoleMessage(consoleSet, "Failed to connect to database");
			consoleState = State.EXIT;
		} else {
			writeConsoleMessage(consoleSet, "Database Found");
			if (true) {
				dao.createUserTable();
				dao.createUserDataTable();
			}/* else {
				if (dao.createUserTable() || dao.createUserDataTable()) {
					String cheating = getConsoleMessage(consoleSet, "Make life alittle easier (print tables)? (y/n)");
					if (checkText(cheating, "y") || checkText(cheating, "yes")) {
						dao.readFromUserTable();
						dao.readFromUserDataTable();
					}
				}
			}//*/
		}
		
		String state = "", exitPrompt = "";
		while (!checkText(consoleState, State.EXIT)) {
			if (!dao.equals(null)) {
				state = getConsoleMessage(consoleSet, "Do you wish to Enter [ent] or Retrieve [ret] information?");
				while (!checkText(state, State.ENTER) && !checkText(state, State.RETRIEVE)
						&& !checkText(state, State.EXIT)) {
					state = getConsoleMessage(consoleSet,
						"Sorry, we cannot recognize that command. Do you wish to \n"
						+"Enter [ent] or Retrieve [ret] information?"
					);
				}

				boolean exitSwitch = true;
				switch (state) {
				case State.ENTER:
					EnterInformation(consoleSet, dao);
					break;
				case State.RETRIEVE:
					RetrieveInformation(consoleSet, dao);
					break;
				case State.EXIT:
					exitSwitch = false;
					break;
				}
				exitPrompt = exitSwitch ? getConsoleMessage(consoleSet, "Do you wish to exit? (y/n)") 
						: getConsoleMessage(consoleSet, "Are you sure? (y/n)");
				if (checkText(exitPrompt, "y") || checkText(exitPrompt, "yes")) {
					dao.close();
					consoleState = State.EXIT;
				}
			} else {
				writeConsoleMessage(consoleSet, "Database connection has been lost");
				consoleState = State.EXIT;
			}
		}
		/*
		if (dao != null) {
			String dropTablesBeforeExit = getConsoleMessage(consoleSet, "Would you like to drop the user tables"
					+ " before you leave? (y/n)");
			if (checkText(dropTablesBeforeExit, "y") || checkText(dropTablesBeforeExit, "yes")) {
				dao.dropUserTable();
				dao.dropUserDataTable();
			}
		}
		//*/
		writeConsoleMessage(consoleSet, "Goodbye.");
	}
	
	static void EnterInformation(Set consoleSet, MySQLAccess dao) throws Exception {
		String state = getConsoleMessage(consoleSet, "Would you like create "
				+ "a user [usr] or enter user data [data]?");
		while (!checkText(state, State.EXIT) && !checkText(state, State.CREATE_USER) 
				&& !checkText(state, State.ENTER_USER_DATA)) {
			state = getConsoleMessage(consoleSet, "Could not recognize that command. "
					+ "\nWould you like create a user [usr] or enter user data [data]?");
		}
		switch (state) {
		case State.CREATE_USER:
			CreateUser(consoleSet, dao);
			break;
		case State.ENTER_USER_DATA:
			EnterUserDataUsage(consoleSet, dao);
			break;
		}
	}
	
	static void RetrieveInformation(Set consoleSet, MySQLAccess dao) throws Exception {		
		String state = "", retry = "", regexNumber = "[0-9]+"
			, regexFullDate = "[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])";
		String userId = "";
		boolean validUserId = false;
		while (!checkText(state, State.EXIT) && !validUserId) {
			userId = getConsoleMessage(consoleSet, "Please enter your user id: ");
			validUserId = dao.validateUserId(userId);
			if (!validUserId) {
				retry = getConsoleMessage(consoleSet, "Invalid user id, would you like to retry? (y/n)");
				if (!checkText(retry, "y") && !checkText(retry, "yes")) {
					state = State.EXIT;
				}
			}
		}
		
		String useSimpleDate = "";
		boolean useSimpleDateFlag = false;
		if (!checkText(state, State.EXIT)) {
			useSimpleDate = getConsoleMessage(consoleSet, "Would you like to use simple date format"
				+ " (yyyy-mm-dd)? (y/n)");
		}
		if (checkText(useSimpleDate, "y") || checkText(useSimpleDate, "yes")) useSimpleDateFlag = true;
		if (useSimpleDateFlag) {
			String startDate = "", endDate = "";
			boolean validStartDate = false, validEndDate = false;
			while (!checkText(state, State.EXIT) && !validStartDate && !validEndDate) {
				startDate = getConsoleMessage(consoleSet, "Please enter your start date(yyyy-mm-dd): ");			
				if (startDate.matches(regexFullDate)) {
					validStartDate = true;
					endDate = getConsoleMessage(consoleSet, "Please enter your end date(yyyy-mm-dd): ");			
					if (endDate.matches(regexFullDate)) {
						validEndDate = true;
					} else {
						retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
						if (!checkText(retry, "y") && !checkText(retry, "yes")) {
							state = State.EXIT;
						}
					}
					String usageType = "";
					boolean validUsage = false;
					while (!checkText(state, State.EXIT) && !validUsage) {
						usageType = getConsoleMessage(consoleSet, "Please enter your usage"
								+ " type('all', 'data', 'voice, 'sms'): ");
						usageType = usageType.toLowerCase();
						if (checkText(usageType, UsageType.ALL)
							|| checkText(usageType, UsageType.DATA)
							|| checkText(usageType, UsageType.VOICE)
							|| checkText(usageType, UsageType.SMS)) {
							validUsage = true;
						} else {
							retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
							if (!checkText(retry, "y") && !checkText(retry, "yes")) {
								state = State.EXIT;
							}
						}
					}
					if (!checkText(state, State.EXIT) && validUserId && validStartDate && validEndDate && validUsage) {
						dao.readFromUserDataTableBetweenDates(userId, startDate, endDate, usageType);
						state = State.EXIT;
					}
				} else {
					retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
					if (!checkText(retry, "y") && !checkText(retry, "yes")) {
						state = State.EXIT;
					}
				}
			}
		} else {
			String startDay = "";
			boolean validStartDay = false;
			while (!checkText(state, State.EXIT) && !validStartDay) {
				startDay = getConsoleMessage(consoleSet, "Please enter your usage start day of the month(ie. "
						+ "1, 2, ..., 31): ");
				if (startDay.matches(regexNumber) && Float.valueOf(startDay) >= 1 && Float.valueOf(startDay) <= 31) {
					validStartDay = true;
					if (startDay.length() == 1){
						startDay = "0"+startDay;
					}
				} else {
					retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
					if (!checkText(retry, "y") && !checkText(retry, "yes")) {
						state = State.EXIT;
					}
				}
			}
			String startMonth, startMonthNum = "";
			boolean validStartMonth = false;
			while (!checkText(state, State.EXIT) && !validStartMonth) {
				startMonth = getConsoleMessage(consoleSet, "Please enter your usage start month(ie. Jan, January): ");
				startMonthNum = getMonthNumber(startMonth);
				if (startMonthNum.matches(regexNumber) && !checkText(startMonthNum, State.ERROR) 
						&& !checkText(startMonthNum, "")) {
					validStartMonth = true;
				}
				if (!validStartMonth) {
					startMonthNum = "";
					retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
					if (!checkText(retry, "y") && !checkText(retry, "yes")) {
						state = State.EXIT;
					}
				}
			}
			String startYear = "";
			boolean validStartYear = false;
			while (!checkText(state, State.EXIT) && !validStartYear) {
				startYear = getConsoleMessage(consoleSet, "Please enter your usage start year(ie. 2015, 2016): ");			
				if (startYear.length() == 4 && startYear.matches(regexNumber)) {
					validStartYear = true;
				} else {
					retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
					if (!checkText(retry, "y") && !checkText(retry, "yes")) {
						state = State.EXIT;
					}
				}
			}
			String endDay = "";
			boolean validEndDay = false;
			while (!checkText(state, State.EXIT) && !validEndDay) {
				endDay = getConsoleMessage(consoleSet, "Please enter your usage end day of the month(ie. "
						+ "1, 2, ..., 31): ");
				if (endDay.matches(regexNumber) && Float.valueOf(endDay) >= 1 && Float.valueOf(endDay) <= 31) {
					validEndDay = true;
					if (endDay.length() == 1){
						endDay = "0"+endDay;
					}
				} else {
					retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
					if (!checkText(retry, "y") && !checkText(retry, "yes")) {
						state = State.EXIT;
					}
				}
			}
			String endMonth, endMonthNum = "";
			boolean validEndMonth = false;
			while (!checkText(state, State.EXIT) && !validEndMonth) {
				endMonth = getConsoleMessage(consoleSet, "Please enter your usage end month(ie. Jan, January): ");
				endMonthNum = getMonthNumber(endMonth);
				if (endMonthNum.matches(regexNumber) && !checkText(endMonthNum, State.ERROR) 
						&& !checkText(endMonthNum, "")) {
					validEndMonth = true;
				}
				if (!validEndMonth) {
					endMonthNum = "";
					retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
					if (!checkText(retry, "y") && !checkText(retry, "yes")) {
						state = State.EXIT;
					}
				}
			}
			String endYear = "";
			boolean validEndYear = false;
			while (!checkText(state, State.EXIT) && !validEndYear) {
				endYear = getConsoleMessage(consoleSet, "Please enter your usage end year(ie. 2015, 2016): ");			
				if (endYear.length() == 4 && endYear.matches(regexNumber)) {
					validEndYear = true;
				} else {
					retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
					if (!checkText(retry, "y") && !checkText(retry, "yes")) {
						state = State.EXIT;
					}
				}
			}
			String usageType = "";
			boolean validUsage = false;
			while (!checkText(state, State.EXIT) && !validUsage) {
				usageType = getConsoleMessage(consoleSet, "Please enter your usage"
						+ " type('all', 'data', 'voice, 'sms'): ");
				usageType = usageType.toLowerCase();
				if (checkText(usageType, UsageType.ALL)
					|| checkText(usageType, UsageType.DATA)
					|| checkText(usageType, UsageType.VOICE)
					|| checkText(usageType, UsageType.SMS)) {
					validUsage = true;
				} else {
					retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
					if (!checkText(retry, "y") && !checkText(retry, "yes")) {
						state = State.EXIT;
					}
				}
			}
			if (!checkText(state, State.EXIT) && validUserId && validStartDay && validStartMonth && validStartYear 
					&& validEndDay && validEndMonth && validEndYear && validUsage) {
				dao.readFromUserDataTableBetweenDates(userId, startYear+"-"+startMonthNum+"-"+startDay
						, endYear+"-"+endMonthNum+"-"+endDay, usageType);
				state = State.EXIT;
			}
		}
	}
	
	/*
	 * Enter Information Functions
	 */
	
	static void CreateUser(Set consoleSet, MySQLAccess dao) throws Exception {
		String state = "", retry = "", regexCharacterSet = "[A-Za-z]+"
				, regexEmail = "\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b"
					, regexPhoneNumber = "[0-9\\-]+";
		String name = "";
		boolean validName = false;
		while (!checkText(state, State.EXIT) && !validName) {
			name = getConsoleMessage(consoleSet, "Please enter user name: ");			
			if (name.matches(regexCharacterSet)) {
				validName = true;
			} else {
				retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
				if (!checkText(retry, "y") && !checkText(retry, "yes")) {
					state = State.EXIT;
				}
			}			
		}
		String email = "";
		boolean validEmail = false;
		while (!checkText(state, State.EXIT) && !validEmail) {
			email = getConsoleMessage(consoleSet, "Please enter user email: ");
			boolean originalEmailCheck = dao.validateEmail(email);
			if (email.matches(regexEmail) && originalEmailCheck) {
				validEmail = true;
			} else {
				if (!originalEmailCheck) {
					retry = getConsoleMessage(consoleSet, "That email is already in use, would you like to retry? (y/n)");
				} else {
					retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
				}
				if (!checkText(retry, "y") && !checkText(retry, "yes")) {
					state = State.EXIT;
				}
			}			
		}
		String country = "";
		boolean validCountry = false;
		while (!checkText(state, State.EXIT) && !validCountry) {
			country = getConsoleMessage(consoleSet, "Please enter user country: ");
			country = country.toLowerCase();
			if (country.matches(regexCharacterSet)) {
				validCountry = true;
			} else {
				retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
				if (!checkText(retry, "y") && !checkText(retry, "yes")) {
					state = State.EXIT;
				}
			}			
		}
		String phoneNumber = "";
		boolean validPhoneNumber = false;
		while (!checkText(state, State.EXIT) && !validPhoneNumber) {
			phoneNumber = getConsoleMessage(consoleSet, "Please enter user phone number: ");			
			if (phoneNumber.matches(regexPhoneNumber)) {
				validPhoneNumber = true;
			} else {
				retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
				if (!checkText(retry, "y") && !checkText(retry, "yes")) {
					state = State.EXIT;
				}
			}			
		}
		if (!checkText(state, State.EXIT) && validName && validEmail && validCountry && validPhoneNumber) {
			String guid = dao.writeToUserTable(name, email, country, phoneNumber);
			writeConsoleMessage(consoleSet, "User has been created user id is: "+guid);
		}
	}
	
	static void EnterUserDataUsage(Set consoleSet, MySQLAccess dao) throws Exception {
		String state = "", retry = "", regexNumber = "[0-9]+"
			, regexFullDate = "[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])";
		String userId = "";
		boolean validUserId = false;
		while (!checkText(state, State.EXIT) && !validUserId) {
			userId = getConsoleMessage(consoleSet, "Please enter your user id: ");
			validUserId = dao.validateUserId(userId);
			if (!validUserId) {
				retry = getConsoleMessage(consoleSet, "Invalid user id, would you like to retry? (y/n)");
				if (!checkText(retry, "y") && !checkText(retry, "yes")) {
					state = State.EXIT;
				}
			}
		}
		String usageType = "";
		boolean validUsage = false;
		while (!checkText(state, State.EXIT) && !validUsage) {
			usageType = getConsoleMessage(consoleSet, "Please enter your usage type('data', 'voice, 'sms'): ");
			usageType = usageType.toLowerCase();
			if (checkText(usageType, UsageType.DATA)
				|| checkText(usageType, UsageType.VOICE)
				|| checkText(usageType, UsageType.SMS)) {
				validUsage = true;
			} else {
				retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
				if (!checkText(retry, "y") && !checkText(retry, "yes")) {
					state = State.EXIT;
				}
			}
		}
		String useSimpleDate = "";
		boolean useSimpleDateFlag = false;
		if (!checkText(state, State.EXIT)) {
			useSimpleDate = getConsoleMessage(consoleSet, "Would you like to use simple date format"
				+ " (yyyy-mm-dd)? (y/n)");
		}
		if (checkText(useSimpleDate, "y") || checkText(useSimpleDate, "yes")) useSimpleDateFlag = true;
		if (useSimpleDateFlag) {
			String date = "";
			boolean validDate = false;
			while (!checkText(state, State.EXIT) && !validDate) {
				date = getConsoleMessage(consoleSet, "Please enter your usage date(yyyy-mm-dd): ");			
				if (date.matches(regexFullDate)) {
					validDate = true;
					if (!checkText(state, State.EXIT) && validUserId && validUsage && validDate) {
						dao.writeToUserDataTable(userId, usageType, date);
						state = State.EXIT;
					}
				} else {
					retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
					if (!checkText(retry, "y") && !checkText(retry, "yes")) {
						state = State.EXIT;
					}
				}
			}
		} else {
			String day = "";
			boolean validDay = false;
			while (!checkText(state, State.EXIT) && !validDay) {
				day = getConsoleMessage(consoleSet, "Please enter your usage day of the month(ie. 1, 2, ..., 31): ");
				if (day.matches(regexNumber) && Float.valueOf(day) >= 1 && Float.valueOf(day) <= 31) {
					validDay = true;
					if (day.length() == 1){
						day = "0"+day;
					}
				} else {
					retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
					if (!checkText(retry, "y") && !checkText(retry, "yes")) {
						state = State.EXIT;
					}
				}
			}
			String month, monthNum = "";
			boolean validMonth = false;
			while (!checkText(state, State.EXIT) && !validMonth) {
				month = getConsoleMessage(consoleSet, "Please enter your usage month(ie. Jan, January, 1): ");
				monthNum = getMonthNumber(month);
				if (monthNum.matches(regexNumber) && !checkText(monthNum, State.ERROR) && !checkText(monthNum, "")) {
					validMonth = true;
				}
				if (!validMonth) {
					monthNum = "";
					retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
					if (!checkText(retry, "y") && !checkText(retry, "yes")) {
						state = State.EXIT;
					}
				}
			}
			String year = "";
			boolean validYear = false;
			while (!checkText(state, State.EXIT) && !validYear) {
				year = getConsoleMessage(consoleSet, "Please enter your usage year(ie. 2015, 2016): ");			
				if (year.length() == 4 && year.matches(regexNumber)) {
					validYear = true;
				} else {
					retry = getConsoleMessage(consoleSet, "Invalid input, would you like to retry? (y/n)");
					if (!checkText(retry, "y") && !checkText(retry, "yes")) {
						state = State.EXIT;
					}
				}			
			}
			if (!checkText(state, State.EXIT) && validUserId && validUsage && validDay && validMonth && validYear) {
				dao.writeToUserDataTable(userId, usageType, year+"-"+monthNum+"-"+day);
			}
		}
	}	

	static class MySQLAccess {
		private Connection connect = null;
		private Statement statement = null;
		private Set consoleSet = null;
		
		public void addConsoleSet(Set consoleSet) {
			this.consoleSet = consoleSet;
		}

		public boolean ConnectToDatabase() throws Exception {
			boolean databaseConnected = true;
			try {
				String localDatabaseCheck = getConsoleMessage(consoleSet, "First we need to connect to a MySql"
						+ " database.\nDo you have a local database to connect to? (y/n)");
				boolean hasLocalDatabase = false;
				if (checkText(localDatabaseCheck, "y") || checkText(localDatabaseCheck, "yes")) {
					hasLocalDatabase = true;
				}
				String portNumber = "", remoteUrl = "", databaseName = "", ssl = "", userName = "", password = "";
				if (hasLocalDatabase) {
					portNumber = getConsoleMessage(consoleSet, "Which port can we connect to the"
							+ " database with?");
				} else {
					remoteUrl = getConsoleMessage(consoleSet, "Please enter the remote url of a MySql database"
							+ " we can connect to.");
				}
				databaseName = getConsoleMessage(consoleSet, "What is the name of your database?");
				ssl = getConsoleMessage(consoleSet, "What is the name of your ssl?");
				if (checkText(ssl.trim(), "")) {
					ssl = "false";
				}
				userName = getConsoleMessage(consoleSet, "What is your username?");
				password = getConsoleMessage(consoleSet, "What is your password?");
				if (connect == null) {
					Class.forName("com.mysql.jdbc.Driver");
					String connectUrl = "";
					if (hasLocalDatabase) {
						connectUrl = "jdbc:mysql://localhost:"+portNumber+"/"+databaseName+"?useSSL="+ssl;
						connect = DriverManager.getConnection(connectUrl, userName, password);
					} else {
						connectUrl = "jdbc:mysql://"+remoteUrl+"/"+databaseName+"?useSSL=false";
						connect = DriverManager.getConnection(connectUrl, userName, password);
					}
					statement = connect.createStatement();
				}
			} catch (Exception e) {
				databaseConnected = false;
			}
			return databaseConnected;
		}
		
		public boolean createUserTable() throws Exception {
			boolean result = false;
			try {
				String query = "CREATE TABLE IF NOT EXISTS `users` ("
							+"`id` VARCHAR( 10 ),"
							+"`name` VARCHAR( 50 ),"
							+"`email` VARCHAR( 50 ),"
							+"`country` VARCHAR( 50 ),"
							+"`phone_number` VARCHAR( 12 ),"
							+"PRIMARY KEY(`id`)"
							+") engine=InnoDB ;";
				result = statement.execute(query);
				if (result) {
					writeConsoleMessage(consoleSet, 
						"database table `users` not found... database table `users` created"
					);
				}
			} catch (Exception e) {
				throw e;
			}
			return result == false;
		}
		
		public boolean createUserDataTable() throws Exception {
			boolean result = false;
			try {
				String query = "CREATE TABLE IF NOT EXISTS `user_data` ("
							+"`id` VARCHAR( 10 ),"
							+"`type` VARCHAR( 5 ),"
							+"`date` DATE"
							+") engine=InnoDB ;";
				result = statement.execute(query);
				if (result) {
					writeConsoleMessage(consoleSet, 
						"database table `user_data` not found... database table `user_data` created"
					);
				}
			} catch (Exception e) {
				throw e;
			}
			return result == false;
		}
		
		public void dropUserTable() throws Exception {
			try {
				boolean result = false;
				String query = "DROP TABLE `users`";
				result = statement.execute(query);
				if (result) {
					writeConsoleMessage(consoleSet, 
						"database table `users` not found... database table `users` could not be dropped"
					);
				}
			} catch (Exception e) {
				throw e;
			}
		}
		
		public void dropUserDataTable() throws Exception {
			try {
				boolean result = false;
				String query = "DROP TABLE `user_data`";
				result = statement.execute(query);
				if (result) {
					writeConsoleMessage(consoleSet, 
						"database table `users` not found... database table `users` could not be dropped"
					);
				}
			} catch (Exception e) {
				throw e;
			}
		}
		
		public String writeToUserTable(String name, String email, String country, String phoneNumber) throws Exception {
			//create guid for id
			String guid = "", regexAlphaNumeric = "[A-Z0-9]+";
			while (!guid.matches(regexAlphaNumeric)) {
				guid = createGUID();
			}
			String query = "INSERT INTO `users`"
						+" (`id`, `name`, `email`, `country`, `phone_number`)"
						+" VALUES ('"+guid+"', '"+name+"', '"+email+"', '"+country+"', '"+phoneNumber+"') ;";
			try {
				statement.execute(query);
			} catch (Exception e) {
				writeConsoleMessage(consoleSet, "Insert data into `users` failed");
			}
			return guid;
		}
		
		public void writeToUserDataTable(String id, String data, String date) throws Exception {
			//check id exists
			String query = "INSERT INTO `user_data`"
						+" (`id`, `type`, `date`)"
						+" VALUES ('"+id+"', '"+data+"', '"+date+"') ;";
			try {
				statement.execute(query);
				writeConsoleMessage(consoleSet, "Data has been entered into `user_data` table");
			} catch (Exception e) {
				writeConsoleMessage(consoleSet, "Insert data into `user_data` failed");
			}
		}

		public void readFromUserTable() throws Exception {
			try {
				String query = "SELECT * from `users`";
				statement.executeQuery(query);
				
				writeConsoleMessage(consoleSet, "Print table `users`:");
				printUserResultSet(statement.getResultSet());
			} catch (Exception e) {
				throw e;
			}
		}
		
		public void readFromUserDataTable() throws Exception {
			try {
				String query = "SELECT * from `user_data`";
				statement.executeQuery(query);
				
				writeConsoleMessage(consoleSet, "Print table `user_data`:");
				printUserDataResultSet(statement.getResultSet());
			} catch (Exception e) {
				throw e;
			}
		}
		
		public void readFromUserDataTableBetweenDates(String userId, String startDate, String endDate
				, String usageType) throws Exception {
			try {
				String query = "SELECT * from `user_data`"
							+ " WHERE `date` >= '"+startDate+"'"
							+ "AND `date` <= '"+endDate+"'"
							+ "AND id = '"+userId+"'";
				
				if (!usageType.isEmpty() && !checkText(usageType, UsageType.ALL)) {
					query += " AND usage_type = '"+usageType+"'";
				}
				
				statement.executeQuery(query);
				
				writeConsoleMessage(consoleSet, "Print from table `user_data`:");
				printUserDataResultSet(statement.getResultSet());
			} catch (Exception e) {
				throw e;
			}
		}
		
		public boolean validateUserId(String userId) throws Exception {
			try {
				String query = "SELECT id from `users` WHERE id = '"+userId+"'";
				statement.executeQuery(query);
			} catch (Exception e) {
				throw e;
			}
			return statement.getResultSet().next();
		}
		
		public boolean validateEmail(String email) throws Exception {
			try {
				String query = "SELECT email from `users` WHERE email = '"+email+"'";
				statement.executeQuery(query);
			} catch (Exception e) {
				throw e;
			}
			return !statement.getResultSet().next();
		}

		private void printUserResultSet(ResultSet resultSet) throws Exception {
			while (resultSet.next()) {
				String id = resultSet.getString("id");
				String name = resultSet.getString("name");
				String email = resultSet.getString("email");
				String country = resultSet.getString("country");
				String phoneNumber = resultSet.getString("phone_number");
				writeConsoleMessage(consoleSet, "id: " + id + ", name: " + name + ", email: " + email
						 + ", country: " + country + ", phoneNumber: " + phoneNumber);
			}
		}
		
		private void printUserDataResultSet(ResultSet resultSet) throws Exception {
			while (resultSet.next()) {
				String id = resultSet.getString("id");
				String type = resultSet.getString("type");
				String date = resultSet.getString("date");
				writeConsoleMessage(consoleSet, "id: " + id + ", type: " + type + ", date: " + date);
			}
		}
		
		private String createGUID() {
			String guid = "";
			Random rand = new Random();
			for (int i = 0; i < 10; i++) {
				guid += getCharFromInt(rand.nextInt(35));
			}
			return guid;
		}
		
		private String getCharFromInt(int index) {
			String c = "";
			if (index < 9) {
				char temp = (char) (index + 48);
				c = String.valueOf(temp);
			} else {
				char temp = (char) (index + 55);
				c = String.valueOf(temp);
			}
			return c;
		}

		private void close() {
			try {
				if (statement != null) {
					statement.close();
				}

				if (connect != null) {
					connect.close();
				}
			} catch (Exception e) {

			}
		}
	}
	
	static MySQLAccess InitializeDatabase(Set consoleSet) throws Exception {
		MySQLAccess dao = new MySQLAccess();
		boolean databaseConnected = false;
		try {
			dao.addConsoleSet(consoleSet);
			databaseConnected = dao.ConnectToDatabase();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return databaseConnected ? dao : null;
	}
	
	static String getMonthNumber(String month) {
		String monthNum = "";
		switch (month.toLowerCase()) {
		case "jan":
		case "january":
		case "1":
		case "01":
			monthNum = "01";
			break;
		case "feb":
		case "february":
		case "2":
		case "02":
			monthNum = "02";
			break;
		case "mar":
		case "march":
		case "3":
		case "03":
			monthNum = "03";
			break;
		case "apr":
		case "april":
		case "4":
		case "04":
			monthNum = "04";
			break;
		case "may":
		case "5":
		case "05":
			monthNum = "05";
			break;
		case "jun":
		case "june":
		case "6":
		case "06":
			monthNum = "06";
			break;
		case "jul":
		case "july":
		case "7":
		case "07":
			monthNum = "07";
			break;
		case "aug":
		case "august":
		case "8":
		case "08":
			monthNum = "08";
			break;
		case "sep":
		case "september":
		case "9":
		case "09":
			monthNum = "09";
			break;
		case "oct":
		case "october":
		case "10":
			monthNum = "10";
			break;
		case "nov":
		case "november":
		case "11":
			monthNum = "11";
			break;
		case "dec":
		case "december":
		case "12":
			monthNum = "12";
			break;
		default:
			monthNum = State.ERROR;
			break;
		}
		return monthNum;
	}

	static String readInputLine(BufferedReader br, String text) {
		System.out.println(text);
		String input = "";
		try {
			input = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return input;
	}

	static String getConsoleMessage(Set consoleSet, String text) {
		return consoleSet.consoleToggle ? consoleSet.c.readLine(text) : readInputLine(consoleSet.br, text);
	}

	static void writeConsoleMessage(Set consoleSet, String text) {
		if (consoleSet.consoleToggle) consoleSet.c.printf(text);
		else System.out.println(text);
	}

	static boolean checkText(String text, String test) {
		return text.compareTo(test) == 0;
	}
}
