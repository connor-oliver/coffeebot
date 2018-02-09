package com.screenscape.core.slack;

import com.screenscape.core.database.DBOperations;
import com.screenscape.models.ResultTotals;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import java.io.IOException;
import java.sql.SQLException;

public class SlackBot {
    private static final String TOKEN = "";
    private static final String CHANNEL = "general";
    private static final String CMD_STRING = "!coffeebot %s";
    private static final String WIN_MESSAGE = "Hi %s, I've added a new win. \uD83D\uDE04";
    private static final String LOSS_MESSAGE = "Hi %s, I've added a new loss. \uD83D\uDE1E";
    private static final String STAT_MESSAGE = "Hi %s, there are currently %d win(s) and %d losses. \uD83D\uDCCA";
    private static final String HELP_MESSAGE = "Hi %s, my current available commands are \"!coffeebot [win|loss|stats|help]\".";
    private static final String TOO_RECENTLY_MESSAGE = "Hi %s, it looks like you added a win or loss less than 15 minutes ago so you're going to have to wait a bit. \uD83D\uDED1";

    private SlackSession session;
    private DBOperations dbOperations;

    public SlackBot() throws SQLException, IOException {
        this.dbOperations = new DBOperations();
        this.session = SlackSessionFactory.createWebSocketSlackSession(TOKEN);
        this.session.connect();
        this.registerMessageListener();
    }

    private static String commandBuild(String command) {
        return String.format(CMD_STRING, command);
    }

    private void registerMessageListener() {
        SlackMessagePostedListener messagePostedListener = new SlackMessagePostedListener() {
            public void onEvent(SlackMessagePosted event, SlackSession session) {
                String messageContent = event.getMessageContent();
                SlackUser messageSender = event.getSender();

                if (event.getChannel().getName().equals(CHANNEL)) {
                    if (messageContent.equals(commandBuild("win")) || messageContent.equals(commandBuild("loss"))) {
                        this.logWinOrLoss(event, messageSender, messageContent);
                    } else {
                        this.informationCommands(event, messageSender, messageContent);
                    }
                }
            }

            private void logWinOrLoss(SlackMessagePosted event, SlackUser messageSender, String messageContent){
                if (dbOperations.getIsAbleToAdd(messageSender.getUserName())) {
                    if (messageContent.equals(commandBuild("win"))) {
                        dbOperations.insertWin(messageSender.getUserName());
                        session.sendMessage(event.getChannel(), String.format(WIN_MESSAGE, messageSender.getUserName()));
                    } else if (messageContent.equals(commandBuild("loss"))) {
                        dbOperations.insertLoss(messageSender.getUserName());
                        session.sendMessage(event.getChannel(), String.format(LOSS_MESSAGE, messageSender.getUserName()));
                    }
                } else {
                    session.sendMessage(event.getChannel(), String.format(TOO_RECENTLY_MESSAGE, messageSender.getUserName()));
                }
            }

            private void informationCommands(SlackMessagePosted event, SlackUser messageSender, String messageContent){
                if (messageContent.equals(commandBuild("stats"))) {
                    ResultTotals results = dbOperations.getCounts();
                    String message = String.format(STAT_MESSAGE, messageSender.getUserName(), results.getWins(), results.getLosses());
                    session.sendMessage(event.getChannel(), message);
                } else if (messageContent.equals(commandBuild("help"))) {
                    session.sendMessage(event.getChannel(), String.format(HELP_MESSAGE, messageSender.getUserName()));
                }
            }
        };

        this.session.addMessagePostedListener(messagePostedListener);
    }
}
