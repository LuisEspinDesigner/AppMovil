package com.example.appbean;

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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appbean.ml.ModelfermentedcocoabeanVersion10;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    ImageView imagen;
    Button btnCamara, btnExplorador, btnDetectar;
    TextView twResultado;
    private Bitmap img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imagen = (ImageView) findViewById(R.id.imageView);
        btnCamara = findViewById(R.id.abrirCamara);
        btnExplorador = findViewById(R.id.abrirExplorador);
        btnDetectar = findViewById(R.id.btndetectar);
        twResultado = findViewById(R.id.twIResultado);
        btnCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCamara();
            }
        });
        btnExplorador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFolder();
            }
        });
        btnDetectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Detecta();

            }
        });
    }

    private void Detecta() {
        img = Bitmap.createScaledBitmap(img, 156, 256, true);
        try {
            ModelfermentedcocoabeanVersion10 Modelo = ModelfermentedcocoabeanVersion10.newInstance(getApplicationContext());
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 156, 256, 3}, DataType.FLOAT32);
            TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
            tensorImage.load(img);
            ByteBuffer byteBuffer = tensorImage.getBuffer();
            inputFeature0.loadBuffer(byteBuffer);
            ModelfermentedcocoabeanVersion10.Outputs outputs = Modelo.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            Modelo.close();
            float[] A = outputFeature0.getFloatArray();
            if (outputFeature0.getFloatArray()[0] == 1) {
                twResultado.setText("Buena Fermentacion");
            } else if (outputFeature0.getFloatArray()[1] == 1) {
                twResultado.setText("Mediana Fermentacion");
            } else if (outputFeature0.getFloatArray()[2] == 1) {
                twResultado.setText("Moho");
            } else if (outputFeature0.getFloatArray()[3] == 1) {
                twResultado.setText("Violeta");
            }
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Error" + e, Toast.LENGTH_SHORT).show();
        }
    }


    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    Bundle extras = data.getExtras();
                    img = (Bitmap) extras.get("data");
                    imagen.setImageBitmap(img);
                    break;
                case 10:
                    Uri MIpath = data.getData();
                    imagen.setImageURI(MIpath);
                    try {
                        img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), MIpath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public void openFolder() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        startActivityForResult(intent.createChooser(intent, "Seleccione la app"), 10);
    }

}