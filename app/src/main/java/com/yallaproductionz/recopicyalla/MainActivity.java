package com.yallaproductionz.recopicyalla;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yallaproductionz.recopicyalla.Adapter.Prediction;
import com.yallaproductionz.recopicyalla.Adapter.PredictionsAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_PERMISSION = 1;
    private static final String TAG = "CameraActivity";

    LinearLayout firstLayout,resultLayout;
    RecyclerView rvHome;

    // Storage And Camera Permissions
    private static String[] PERMISSIONS_REQ = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        boolean availbe_permission = true;
        // For API 23+ you need to request the read/write permissions even if they are already in your manifest.
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M ) {
            availbe_permission = verifyPermissions(this);
        }

        initViews();

    }//end of onCreate

    private void initViews(){
        //The first Layout with the button and title
        firstLayout=(LinearLayout)findViewById(R.id.firstviewLay);
        resultLayout=(LinearLayout)findViewById(R.id.resultviewLay);

        //Recycler view setup
        rvHome=(RecyclerView)findViewById(R.id.recyclerpd);
        rvHome.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplication(), 1);
        rvHome.setLayoutManager(mLayoutManager);
    }

    private static boolean verifyPermissions(Activity activity) {
        // Check if we have write permission
        int write_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read_persmission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int camera_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (write_permission != PackageManager.PERMISSION_GRANTED ||
                read_persmission != PackageManager.PERMISSION_GRANTED ||
                camera_permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_REQ,
                    REQUEST_CODE_PERMISSION
            );
            return false;
        } else {
            return true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        try {
            // Restart it after granting permission
            if (requestCode == REQUEST_CODE_PERMISSION) {
                finish();
                startActivity(getIntent());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong, try again with permissions", Toast.LENGTH_LONG).show();
        }
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    public void takePicClick(View v){


        //camera stuff
        Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        //folder stuff
        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "MyImages");
        imagesFolder.mkdirs();

        File image = new File(imagesFolder, "sura1.JPEG");
        Uri uriSavedImage = Uri.fromFile(image);

        imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
        startActivityForResult(imageIntent, REQUEST_IMAGE_CAPTURE);

        /*
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        */


    }

    File finalPic;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode ==  Activity.RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(),"You've canceled taking picture..",Toast.LENGTH_SHORT).show();
        }
        else {

            File imagesFolder = new File(Environment.getExternalStorageDirectory(), "MyImages");


            File image = new File(imagesFolder, "sura1.JPEG");

            finalPic=image;
            Bitmap bm = BitmapFactory.decodeFile(image.getPath());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 60, stream);
            byte[] byteFormat = stream.toByteArray();
            // get the base 64 string
            String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);



            new UploadPicAndRecognize().execute("batata");
            //  uploadPic(imgString);

        }



    }

    class UploadPicAndRecognize extends AsyncTask<String, Void, List<ClarifaiOutput<Concept>>> {

        ProgressDialog dialog;

        protected List<ClarifaiOutput<Concept>> doInBackground(String... urls) {

            try {
                final ClarifaiClient client = new ClarifaiBuilder("USER_NAME", "USER_KEY")
                        .client(new OkHttpClient()) // OPTIONAL. Allows customization of OkHttp by the user
                        .buildSync();
                final List<ClarifaiOutput<Concept>> predictionResults =
                        client.getDefaultModels().generalModel() // You can also do Clarifai.getModelByID("id") to get custom models
                                .predict()
                                .withInputs(
                                        //  ClarifaiInput.forImage(ClarifaiImage.of("http://api.yallaproductionz.com/uploads/batata.jpeg"))
                                        ClarifaiInput.forImage(ClarifaiImage.of(finalPic))
                                )
                                .executeSync() // optionally, pass a ClarifaiClient parameter to override the default client instance with another one
                                .get();
                return predictionResults;
            }catch (Exception e) {
                Toast.makeText(MainActivity.this, "Connection error, try again",Toast.LENGTH_SHORT).show();
                System.out.println("Error in http connection" + e.toString());
                return null;
            }

        }


        protected void onPostExecute(List<ClarifaiOutput<Concept>> feed) {

            if(feed!=null) {
                List<Concept> concepts = feed.get(0).data();
                ArrayList<Prediction> predictions = new ArrayList<>();

                for (int i = 0; i < concepts.size(); i++) {
                    Prediction pd = new Prediction(concepts.get(i).name(), concepts.get(i).value());
                    predictions.add(pd);
                }

                try {
                    PredictionsAdapter adapter = new PredictionsAdapter(getApplication(), predictions);
                    rvHome.setAdapter(adapter);
                    resultLayout.setVisibility(View.VISIBLE);
                    firstLayout.setVisibility(View.GONE);
                } catch (Exception ei) {
                    Log.e("ErrorMore:", "Error adapter More");
                }
            }//end if the list is null
            else{
                Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
            }
            //  Log.v("reje3",feed.get(0).data);
            dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "Recognizing the pic", "Please wait....");
            dialog.show();
            super.onPreExecute();

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.info) {

            Intent infoIntent= new Intent(MainActivity.this, InfoActivity.class);
            startActivity(infoIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


