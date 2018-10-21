package com.ugcs.positiontracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SendMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
    }

    // "Send Message" button click handler
    public void onSendTextMessage(View view) {
        TextView textView = findViewById(R.id.msgTextEdit);
        if (textView != null) {
            sendMessageToUgcsUcs( textView.getText());
        }
    }

    // "I'm OK" button click handler
    public void onSendOkMessage(View view) {
        sendMessageToUgcsUcs( "I'm OK");
    }

    // "Help Me" button click handler
    public void onSendHelpMeMessage(View view) {
        sendMessageToUgcsUcs( "Help me!");
    }

    // "Found!" button click handler
    public void onSendFoundMessage(View view) {
        sendMessageToUgcsUcs( "Found!");
    }

    // Sends text message to UgCS UCS server
    private void sendMessageToUgcsUcs( @NonNull CharSequence msgText) {
        showToast( "Message sent.");
    }

    // Displays message on the screen
    private void showToast(@NonNull CharSequence text) {
        Toast.makeText(SendMessageActivity.this, text, Toast.LENGTH_LONG).show();
    }
}
