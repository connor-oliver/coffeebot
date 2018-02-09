package com.screenscape.core.database;

import com.screenscape.models.ResultTotals;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBOperations {
    private static final String SQL_INSERT_WIN_QUERY = "INSERT INTO results (user, type, logged_on) VALUES (?, 1, CURRENT_TIMESTAMP)";
    private static final String SQL_INSERT_LOSS_QUERY = "INSERT INTO results (user, type, logged_on) VALUES (?, 0, CURRENT_TIMESTAMP)";
    private static final String SQL_GET_TOTALS_QUERY = "SELECT (SELECT COUNT(type) FROM results WHERE type = 1) as wins, " +
            "(SELECT COUNT(type) FROM results WHERE type = 0) as losses FROM results";
    private static final String SQL_IS_ABLE_TO_ADD_QUERY = "SELECT CAST ((JulianDay('now') - JulianDay(logged_on)) * 24 * 60 AS Integer) AS lastAdd "
            + "FROM results WHERE user = ? ORDER BY logged_on desc LIMIT 1";

    private SQLiteConnection db;

    public DBOperations() throws SQLException {
        this.db = new SQLiteConnection();
        this.db.connect();
    }

    public void insertWin(String user){
        try {
            PreparedStatement psqls = this.db.getConnection().prepareStatement(SQL_INSERT_WIN_QUERY);
            psqls.setString(1, user);
            psqls.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void insertLoss(String user){
        try {
            PreparedStatement psqls = this.db.getConnection().prepareStatement(SQL_INSERT_LOSS_QUERY);
            psqls.setString(1, user);
            psqls.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public ResultTotals getCounts(){
        ResultTotals totals = null;

        try {
            Statement statement = this.db.getConnection().createStatement();
            ResultSet results   = statement.executeQuery(SQL_GET_TOTALS_QUERY);

            totals = new ResultTotals();
            totals.setWins(results.getInt("wins"));
            totals.setLosses(results.getInt("losses"));
        } catch (SQLException e){
            e.printStackTrace();
        }

        return totals;
    }

    public Boolean getIsAbleToAdd(String user){
        Boolean result = false;

        try {
            PreparedStatement psqls = this.db.getConnection().prepareStatement(SQL_IS_ABLE_TO_ADD_QUERY);
            psqls.setString(1, user);

            ResultSet results = psqls.executeQuery();

            if(results.next()) {
                result = results.getInt("lastAdd") > 14;
            } else {
                result = true;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        return result;
    }
}
