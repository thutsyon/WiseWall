package org.wisepanda.wisewall;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.InputStream;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class PostActivity extends Activity {
    // UI references.
    private EditText postEditText;
    private TextView characterCountTextView;
    private Button postButton;
    private Button picButton;

    private int maxCharacterCount = Application.getConfigHelper().getPostMaxCharacterCount();
    private ParseGeoPoint geoPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post);

        Intent intent = getIntent();
        Location location = intent.getParcelableExtra(Application.INTENT_EXTRA_LOCATION);
        geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

        postEditText = (EditText) findViewById(R.id.post_edittext);
        postEditText.addTextChangedListener(new TextWatcher() {
        @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePostButtonState();
                updateCharacterCountTextViewText();
            }
        });

        characterCountTextView = (TextView) findViewById(R.id.character_count_textview);

        postButton = (Button) findViewById(R.id.post_button);
        picButton = (Button) findViewById(R.id.pic_button);
        postButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                post();
            }
        });

        updatePostButtonState();
        updateCharacterCountTextViewText();

        // Add pic button
        final int SELECT_PICTURE = 1;

        String selectedImagePath;
        //ADDED
        String filemanagerstring;
        picButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

                // in onCreate or any event where your want the user to
                // select a file
                Intent choosePicIntent = new Intent();
                choosePicIntent.setType("image/*");
                choosePicIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(choosePicIntent, "Select Picture"), SELECT_PICTURE);
            }
        });
    }

    private void post () {
        String text = postEditText.getText().toString().trim();

        // Set up a progress dialog
        final ProgressDialog dialog = new ProgressDialog(PostActivity.this);
        dialog.setMessage(getString(R.string.progress_post));
        dialog.show();

        // Create a post.
        AnywallPost post = new AnywallPost();

        // Set the location to the current user's location
        post.setLocation(geoPoint);
        post.setText(text);
        post.setUser(ParseUser.getCurrentUser());
        ParseACL acl = new ParseACL();

        // Give public read access
        acl.setPublicReadAccess(true);
        post.setACL(acl);

        // Save the post
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent choosePicIntent) {
        // Make sure the request was successful
        if (resultCode == RESULT_OK) {
            Uri selectedImageUri = choosePicIntent.getData();
            final int takeFlags = choosePicIntent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION & Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);
            Log.d(Application.APPTAG, selectedImageUri.toString());
        }
    }

    private String getPostEditTextText () {
    return postEditText.getText().toString().trim();
  }

    private void updatePostButtonState () {
        int length = getPostEditTextText().length();
        boolean enabled = length > 0 && length < maxCharacterCount;
        postButton.setEnabled(enabled);
        picButton.setEnabled(enabled);
    }

    private void updateCharacterCountTextViewText () {
        String characterCountString = String.format("%d/%d", postEditText.length(), maxCharacterCount);
        characterCountTextView.setText(characterCountString);
    }
}
