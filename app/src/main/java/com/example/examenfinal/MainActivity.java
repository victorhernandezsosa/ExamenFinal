package com.example.examenfinal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button bntgrabar,btnsave,btnver,btndetener,btnfoto;
    EditText descripcion,periodista,fecha;
    ImageView imagen;

    static final int peticion_captura_imagen = 101;
    static final int peticion_acceso_camara = 102;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;


    String currentPhotoPath;

    private Calendar selectedDate = Calendar.getInstance();

    private MediaRecorder mediaRecorder;
    private String audioFilePath;

    private byte[] imagenBytes;
    private byte[] audioBytes;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btndetener = findViewById(R.id.btndetener);
        btnfoto= findViewById(R.id.btnfoto);
        bntgrabar = findViewById(R.id.btngrabar);
        btnsave = findViewById(R.id.btnsave);
        btnver = findViewById(R.id.btnver);

        descripcion = findViewById(R.id.descripcion);
        periodista = findViewById(R.id.periodista);
        fecha = findViewById(R.id.fehca);
        imagen = findViewById(R.id.imagen);

        fecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarfecha();
            }
        });

        btnfoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permisos();
            }
        });

        btndetener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parar();
            }
        });

        bntgrabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciar();
            }
        });

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        btnver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Mostrar.class);
                startActivity(intent);
            }
        });
    }


    private void save() {
        String descripcionTexto = descripcion.getText().toString();
        String periodistaTexto = periodista.getText().toString();
        String fechaTexto = fecha.getText().toString();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date fechaDate = null;
        try {
            fechaDate = sdf.parse(fechaTexto);
        } catch (ParseException e) {
            e.printStackTrace();
        }



        if (descripcionTexto.isEmpty() || periodistaTexto.isEmpty() || fechaDate == null || imagenBytes == null || audioBytes == null) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            Log.d("Datos", "Descripcion: " + descripcionTexto);
            Log.d("Datos", "Periodista: " + periodistaTexto);
            Log.d("Datos", "Fecha: " + fechaTexto);
            Log.d("Datos", "Imagen Bytes: " + (imagenBytes != null ? "No Vacío" : "Vacío"));
            Log.d("Datos", "Audio Bytes: " + (audioBytes != null ? "No Vacío" : "Vacío"));
            return;
        }

        guardarEntrevista();
    }

    private void guardarEntrevista() {
        String descripcionTexto = descripcion.getText().toString();
        String periodistaTexto = periodista.getText().toString();
        String fechaTexto = fecha.getText().toString();
        String imagenBase64 = Base64.encodeToString(imagenBytes, Base64.DEFAULT);
        String audioBase64 = Base64.encodeToString(audioBytes, Base64.DEFAULT);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date fechaDate = null;
        try {
            fechaDate = sdf.parse(fechaTexto);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Entrevista entrevista = new Entrevista();
        entrevista.setIdEntrevista(UUID.randomUUID().toString());
        entrevista.setDescripcion(descripcionTexto);
        entrevista.setPeriodista(periodistaTexto);
        entrevista.setFecha(fechaDate);
        entrevista.setImagen(imagenBase64);
        entrevista.setAudio(audioBase64);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("entrevistas");
        String entrevistaId = databaseReference.push().getKey();
        databaseReference.child(entrevistaId).setValue(entrevista);

        SharedPreferences sharedPreferences = getSharedPreferences("entrevista_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String formattedDate = sdf.format(entrevista.getFecha());
        editor.putString("fecha_entrevista", formattedDate);

        editor.putString("imagen_entrevista", entrevista.getImagen());

        editor.apply();

        descripcion.getText().clear();
        periodista.getText().clear();
        fecha.getText().clear();
        imagen.setImageBitmap(null); 

        imagenBytes = null;
        audioBytes = null;

        Toast.makeText(this, "Entrevista guardada correctamente", Toast.LENGTH_SHORT).show();
    }


    private void parar() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            audioBytes = convertirAudioAByteArray(audioFilePath);


            Toast.makeText(MainActivity.this, "Grabación finalizada", Toast.LENGTH_SHORT).show();
        }
    }



    private void iniciar() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }

        try {
            mediaRecorder = new MediaRecorder();
            audioFilePath = getExternalCacheDir().getAbsolutePath() + "/audio_record.mp3";

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();

            Toast.makeText(MainActivity.this, "Grabando audio...", Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error al iniciar la grabación de audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void permisos() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},peticion_acceso_camara);
        }
        else
        {
            dispatchTakePictureIntent();
            //TomarFoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == peticion_acceso_camara)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                dispatchTakePictureIntent();
                //TomarFoto();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "se necesita el permiso de la camara",Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciar(); // Si se otorgaron los permisos, iniciar la grabación
            } else {
                Toast.makeText(this, "Se necesita permiso para grabar audio", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == peticion_captura_imagen && resultCode == RESULT_OK) {
            try {
                Bitmap correctedBitmap = corregirOrientacionImagen(currentPhotoPath);
                if (correctedBitmap != null) {
                    imagenBytes = convertirImagenAByteArray(correctedBitmap);
                    imagen.setImageBitmap(correctedBitmap);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.toString();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.examenfinal.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, peticion_captura_imagen);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private byte[] convertirImagenAByteArray(Bitmap imagen) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imagen.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        return stream.toByteArray();
    }

    private byte[] convertirAudioAByteArray(String filePath) {
        File audioFile = new File(filePath);
        byte[] audioBytes = new byte[(int) audioFile.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(audioFile);
            fileInputStream.read(audioBytes);
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return audioBytes;
    }

    private Bitmap corregirOrientacionImagen(String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int rotationAngle = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotationAngle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotationAngle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotationAngle = 270;
                    break;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // Ajusta este valor según sea necesario

            Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath, options);
            Matrix matrix = new Matrix();
            matrix.setRotate(rotationAngle);
            return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void mostrarfecha() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                EditText fecha = findViewById(R.id.fehca);
                fecha.setText(sdf.format(selectedDate.getTime()));
            }
        },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}