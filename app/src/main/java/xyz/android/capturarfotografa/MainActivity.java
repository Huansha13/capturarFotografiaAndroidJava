package xyz.android.capturarfotografa;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Agregar la imagen a la galería
            galleryAddPic();

            // Decodificar el archivo de imagen en un objeto Bitmap escalado
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inPurgeable = true;
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

            // Mostrar la imagen capturada en el ImageView
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(bitmap);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso otorgado, iniciar la actividad de la cámara
                dispatchTakePictureIntent();
            } else {
                // Permiso denegado, mostrar un mensaje al usuario
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void capturePhoto(View view) {
        // Revisar si el permiso para acceder a la cámara está otorgado
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // Si no se ha otorgado el permiso, solicitarlo al usuario
            ActivityCompat.requestPermissions(this,
                    new String[] { android.Manifest.permission.CAMERA },
                    REQUEST_CAMERA_PERMISSION);
        } else {
            // Si se otorgó el permiso, iniciar la actividad de la cámara
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Crear un archivo para almacenar la imagen
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Manejar la excepción
        }
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {
        // Crear un nombre de archivo único para la imagen
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefijo */
                ".jpg",         /* sufijo */
                storageDir      /* directorio */
        );
        // Guardar la ruta del archivo para usarlo en la galería
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        File f = new File(currentPhotoPath);

        // Insertar la imagen en la galería de fotos
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Foto de mi app");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Foto capturada por mi app");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, currentPhotoPath.hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, f.getName());
        values.put("_data", currentPhotoPath);
        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Escribir los datos de la imagen en el archivo
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            // Manejo de excepciones
        }

        // Mostrar un mensaje de éxito
        Toast.makeText(MainActivity.this, "Foto guardada en la galería de fotos", Toast.LENGTH_SHORT).show();
    }


}