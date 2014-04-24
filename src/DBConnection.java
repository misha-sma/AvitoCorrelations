
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	private final static String JDBC_DRIVER = "org.postgresql.Driver";

	private final static String HOST = "localhost";
	private final static String PORT = "5432";
	private final static String USER = "postgres";
	private final static String PASSWORD = "postgres";
	private final static String DATABASE = "cars_sbor_net";

	private final static String URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE;

	public static Connection getDbConnection() throws ClassNotFoundException, SQLException {
		Class.forName(JDBC_DRIVER);
		Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
		return con;
	}

}
