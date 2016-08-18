package com.bitflake.allcount;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.bitflake.counter.Constances;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class ShareDialog extends DialogFragment implements AdapterView.OnItemSelectedListener {
    private Spinner spinner;
    private EditText customExercise;
    private EditText count;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View root = inflater.inflate(R.layout.send_dialog, null);
        spinner = (Spinner) root.findViewById(R.id.exercise);
        spinner.setOnItemSelectedListener(this);
        customExercise = (EditText) root.findViewById(R.id.exerciseCustom);
        count = (EditText) root.findViewById(R.id.count);
        builder.setView(root)
                // Add action buttons
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String exercise = (String) spinner.getSelectedItem();
                        if (spinner.getSelectedItemPosition() == spinner.getCount() - 1) {
                            exercise = customExercise.getText().toString();
                            exercise = exercise.trim();
                        }

                        if (exercise.length() <= 0) {
                            Snackbar
                                    .make(spinner, "Select a exercise", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        int c = count.getText().length() <= 0 ? 0 : Integer.parseInt(count.getText().toString());
                        if (c <= 0) {
                            Snackbar
                                    .make(spinner, "Enter a valid number", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        shareDataFile(exercise, c);
                        ShareDialog.this.getDialog().cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ShareDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    private void shareDataFile(String exercise, int count) {

        File cacheDir = getActivity().getCacheDir();
        File recordFile = new File(cacheDir, Constances.DATA_FILE_RECORD);
        File countFile = new File(cacheDir, Constances.DATA_FILE_COUNT);

        long time = System.currentTimeMillis();
        File tmpFile = new File(cacheDir, String.format(Constances.DATA_FILE_FORMAT,
                exercise,
                count, time));

        try {
            copy(countFile, tmpFile);

            countFile = tmpFile;

            if (recordFile != null && recordFile.exists() && getActivity().getIntent().getBooleanExtra("start", false)) {
                tmpFile = new File(cacheDir, String.format(Constances.DATA_FILE_FORMAT_REC,
                        exercise.toString(),
                        count, time));
                copy(recordFile, tmpFile);
                recordFile = tmpFile;
            } else {
                recordFile = null;
            }

            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);

            intent.setType("text/plain");
            intent.putExtra(android.content.Intent.EXTRA_EMAIL,
                    new String[]{"f.ahorner@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, countFile.getName());

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            ArrayList<Uri> uris = new ArrayList<>();
            uris.add(FileProvider.getUriForFile(getActivity(), "com.bitflake.allcount.fileprovider", countFile));
            if (recordFile != null)
                uris.add(FileProvider.getUriForFile(getActivity(), "com.bitflake.allcount.fileprovider", recordFile));
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == spinner.getCount() - 1) {
            customExercise.setVisibility(View.VISIBLE);
        } else {
            customExercise.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}