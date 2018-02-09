package com.screenscape;

import com.screenscape.core.slack.SlackBot;

import java.io.IOException;
import java.sql.SQLException;

public class Application {
    public static void main(String[] args) {
        try {
            new SlackBot();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
