import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CarDao {
	public static int addCar(String fulltext, String url) {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DBConnection.getDbConnection();
			con.setAutoCommit(false);
			String sql = "INSERT INTO cars (fulltext, url) VALUES (?, ?) RETURNING id_car";
			ps = con.prepareStatement(sql);
			ps.setString(1, fulltext);
			ps.setString(2, url);
			ResultSet rs = ps.executeQuery();
			int idCar = rs.next() ? rs.getInt(1) : 0;
			rs.close();
			con.commit();
			return idCar;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

}
