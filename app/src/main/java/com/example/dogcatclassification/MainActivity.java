package com.example.dogcatclassification;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.dogcatclassification.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    Button chooseImage, takeImage, predict;
    ImageView imageView;
    TextView decisionText;

    int SELECT_PHOTO = 1;
    Uri uri;
    Bitmap captureImage, chooseImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chooseImage =findViewById(R.id.chooseImage);
        //takeImage = findViewById(R.id.takeImage);
        predict = findViewById(R.id.predict);
        imageView = findViewById(R.id.image);
        decisionText = findViewById(R.id.decisionText);

        /*if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, 100);
        }*/

        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_PHOTO);
            }
        });

        /*takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 100);
            }
        });*/

        predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                chooseImageBitmap = Bitmap.createScaledBitmap(chooseImageBitmap, 224,224, true);

                try {
                    Model model = Model.newInstance(getApplicationContext());

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

                    TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                    tensorImage.load(chooseImageBitmap);

                    ByteBuffer byteBuffer = tensorImage.getBuffer();

                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    Model.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    // Releases model resources if no longer used.
                    model.close();

                    decisionText.setText(outputFeature0.getFloatArray()[0] + "\n" + outputFeature0.getFloatArray()[1]);


                    /*if (outputFeature0.getFloatArray()[0] == 6.467258){
                        decisionText.setText("Prediction output is Dog");

                    }else if (outputFeature0.getFloatArray()[1] == 0.9999994) {
                        decisionText.setText("Prediction output is Cat");
                    }*/


                } catch (IOException e) {

                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null){
            uri = data.getData();
            try {
                chooseImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                imageView.setImageBitmap(chooseImageBitmap);
            }catch (Exception e){
                Log.d("TAG", e.getLocalizedMessage());
            }
        }
        /*else if (requestCode == 100){
            captureImage = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(captureImage);

        }*/

    }
}