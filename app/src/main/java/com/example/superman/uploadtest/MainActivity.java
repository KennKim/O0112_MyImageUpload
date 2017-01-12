package com.example.superman.uploadtest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_FILE_REQUEST = 1;
    private static final int TAKE_IMAGE_REQUEST = 2;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String selectedFilePath;
    private Uri cameraTempUri;
    private static final String SERVER_URL = "http://jip.dothome.co.kr/myhome/test/";
    private static final String UPLOAD_PHP_URL = SERVER_URL + "/upload.php";
    private static final String UPLOAD_DIR_URL = SERVER_URL + "/uploads";
    ImageView ivAttachment;
    Button bUpload;
    Button bCamera;
    TextView tvFileName;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivAttachment = (ImageView) findViewById(R.id.ivAttachment);
        bUpload = (Button) findViewById(R.id.b_upload);
        bCamera = (Button) findViewById(R.id.camera);
        tvFileName = (TextView) findViewById(R.id.tv_file_name);
        ivAttachment.setOnClickListener(this);
        bUpload.setOnClickListener(this);
        bCamera.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == ivAttachment) {

            //on attachment icon click
            openGallery();
        }
        if (v == bUpload) {

            //on upload button Click
            if (selectedFilePath != null) {
                Log.i(TAG, "bUpload 직전 Selected File Path:" + selectedFilePath);

                dialog = ProgressDialog.show(MainActivity.this, "", "Uploading File...", true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //creating new thread to handle Http Operations
                        uploadFile(selectedFilePath);
                    }
                }).start();
            } else {
                Toast.makeText(MainActivity.this, "Please choose a File First", Toast.LENGTH_SHORT).show();
            }

        }
        if (v == bCamera) {
            openCamera();
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        //sets the select file to all types of files
        intent.setType("*/*");
        //allows to select data and return it
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //starts new activity to select file and return data
        startActivityForResult(Intent.createChooser(intent, "Choose File to Upload.."), PICK_FILE_REQUEST);
    }

    private void openGallery() {
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    private void openCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_IMAGE_REQUEST);
//
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//        // 임시로 사용할 파일의 경로를 생성
//        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
//        cameraTempUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
//
//        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, cameraTempUri);
//
//        selectedFilePath = FilePath.getPath(this, cameraTempUri);
//        Log.i(TAG, "Selected File Path:" + selectedFilePath);
//
//        if (selectedFilePath != null && !selectedFilePath.equals("")) {
//            tvFileName.setText(selectedFilePath);
//        } else {
//            Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
//        }
//        startActivityForResult(intent,TAKE_IMAGE_REQUEST);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Activity.RESULT_OK" + selectedFilePath);

            if (requestCode == PICK_FILE_REQUEST) {
                if (data == null) {
                    Log.d(TAG, "--NULL--PICK_FILE_REQUEST" + selectedFilePath);
                }
                Log.d(TAG, "--NOT NULL--PICK_FILE_REQUEST" + selectedFilePath);


                Uri selectedFileUri = data.getData();
                selectedFilePath = FilePath.getPath(this, selectedFileUri);
                Log.i(TAG, "Selected File Path:" + selectedFilePath);

                if (selectedFilePath != null && !selectedFilePath.equals("")) {
                    tvFileName.setText(selectedFilePath);
                } else {
                    Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == TAKE_IMAGE_REQUEST) {
                if (data == null) {
                    Log.d(TAG, "--NULL--TAKE_IMAGE_REQUEST" + selectedFilePath);
                }
                Log.d(TAG, "--NOT NULL--TAKE_IMAGE_REQUEST" + selectedFilePath);

//
//                Uri selectedFileUri = data.getData();
//                selectedFilePath = FilePath.getPath(this, selectedFileUri);
//                Log.i(TAG, "Selected File Path:" + selectedFilePath);

                if (selectedFilePath != null && !selectedFilePath.equals("")) {
                    tvFileName.setText(selectedFilePath);
                } else {
                    Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //android upload file to server
    public int uploadFile(final String selectedFilePath) {

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length - 1];

        if (!selectedFile.isFile()) {
            dialog.dismiss();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvFileName.setText("Source File Doesn't Exist: " + selectedFilePath);
                }
            });
            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(UPLOAD_PHP_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file", selectedFilePath);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0) {
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvFileName.setText("File Upload completed.\n\n" + UPLOAD_DIR_URL + fileName);
                        }
                    });
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "File Not Found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "URL error!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
            return serverResponseCode;
        }

    }
}