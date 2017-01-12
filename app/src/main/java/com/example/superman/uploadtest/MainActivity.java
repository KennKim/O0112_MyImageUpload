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

import com.bumptech.glide.Glide;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_FILE_REQUEST = 1;
    private static final int TAKE_IMAGE_REQUEST = 2;
    private static final String TAG = MainActivity.class.getSimpleName();
    private ArrayList<String> selectedFilePath = new ArrayList<>();
    private int imgNo = 0;
    private ArrayList<String> uploadedFileURL = new ArrayList<>();
    private Uri mImageCaptureUri;
    private static final String SERVER_URL = "http://jip.dothome.co.kr/myhome/test/";
    private static final String UPLOAD_PHP_URL = SERVER_URL + "/upload.php";
    private static final String UPLOAD_DIR_URL = SERVER_URL + "/uploads";
    ImageView iv1;
    ImageView iv2;
    ImageView iv3;
    Button bUpload;
    Button bCamera;
    TextView tvFileName;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv1 = (ImageView) findViewById(R.id.iv1);
        iv1.setOnClickListener(this);
        iv2 = (ImageView) findViewById(R.id.iv2);
        iv2.setOnClickListener(this);
        iv3 = (ImageView) findViewById(R.id.iv3);
        iv3.setOnClickListener(this);
        bUpload = (Button) findViewById(R.id.b_upload);
        bCamera = (Button) findViewById(R.id.camera);
        tvFileName = (TextView) findViewById(R.id.tv_file_name);
        bUpload.setOnClickListener(this);
        bCamera.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == iv1 || v == iv2 || v == iv3) {
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
                        if (selectedFilePath.size() == 0) {
                            Toast.makeText(MainActivity.this, "selectedFilePath == 0", Toast.LENGTH_SHORT).show();
                        } else {
                            for (String s : selectedFilePath) {
                                uploadFile(s);
                            }
//                selectedFilePath.clear();
//                Glide.with(this).load(R.drawable.ic_android_black_24dp).into(iv1);
//                Glide.with(this).load(R.drawable.ic_android_black_24dp).into(iv2);
//                Glide.with(this).load(R.drawable.ic_android_black_24dp).into(iv3);
                        }
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

        /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File file = null;
            try {
                file = createImageFile(); // 사진찍은 후 저장할 임시 파일
            } catch (IOException ex) {
                Toast.makeText(getApplicationContext(), "createImageFile Failed", Toast.LENGTH_LONG).show();
            }

            if (file != null) {
                mImageCaptureUri = Uri.fromFile(file); // 임시 파일의 위치,경로 가져옴
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri); // 임시 파일 위치에 저장
                startActivityForResult(intent, TAKE_IMAGE_REQUEST);
            }
        }*/
    }

    /*private File createImageFile() throws IOException {

        File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/aaPhoto");
        if (!fileDir.exists()) // SmartWheel 디렉터리에 폴더가 없다면 (새로 이미지를 저장할 경우에 속한다.)
            fileDir.mkdir();

        String imgFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aaPhoto/" + System.currentTimeMillis() + ".jpg";
        File file = new File(imgFilePath);
        return file;
    }*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Activity.RESULT_OK" + selectedFilePath);

            if (requestCode == PICK_FILE_REQUEST) {
                if (data == null) {
                    Log.d(TAG, "--NULL--PICK_FILE_REQUEST" + selectedFilePath);
                } else {
                    imgNo = selectedFilePath.size() != 0 ? +1 : 0;
                    Uri selectedFileUri = data.getData();
                    selectedFilePath.add(imgNo, FilePath.getPath(this, selectedFileUri));
                    Log.i(TAG, "Selected File Path:" + selectedFilePath.get(imgNo));


                    tvFileName.setText(selectedFilePath.get(imgNo));
                    switch (imgNo) {
                        case 0:
                            Glide.with(this)
                                    .load(new File(selectedFileUri.getPath())) // Uri of the picture
                                    .into(iv1);
                            break;
                        case 1:
                            Glide.with(this)
                                    .load(new File(selectedFileUri.getPath())) // Uri of the picture
                                    .into(iv2);
                            break;
                        case 2:
                            Glide.with(this)
                                    .load(new File(selectedFileUri.getPath())) // Uri of the picture
                                    .into(iv3);
                            break;
                    }


                }
            } else if (requestCode == TAKE_IMAGE_REQUEST) {
                if (data == null) {
                    Log.d(TAG, "--NULL--TAKE_IMAGE_REQUEST" + selectedFilePath);
                }
                Log.d(TAG, "--NOT NULL--TAKE_IMAGE_REQUEST" + selectedFilePath);


            }
        }
    }

    /*private Uri getLastCaptureImageUri(){
        Uri uri =null;
        String[] IMAGE_PROJECTION = {
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns._ID,
        };

        try {
            Cursor cursorImages = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    IMAGE_PROJECTION, null, null,null);
            if (cursorImages != null && cursorImages.moveToLast()) {
                uri = Uri.parse(cursorImages.getString(0)); //경로
                int id = cursorImages.getInt(1); //아이디
                cursorImages.close(); // 커서 사용이 끝나면 꼭 닫아준다.
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return uri;
    }*/

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
        int maxBufferSize = 1024 * 1024;
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