package com.example.pme210645;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

//import android.graphics.Bitmap; //ya esta
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Button guardar,lista, nuevo;
    private String audioUrl;
    ImageButton reproducir,grabar;
    private EditText periodista,descripcion,fecha, Latitud;

    private MaterialCardView cardView;
    private Uri ImageUri;
    private Bitmap bitmap;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private String PhotUrl;
    private  String currentUserId;
    ImageView img;
    String name="",tel="";///.-------------------
    String respuesta="";
    private int currentId = 0;
    static final int PETICION_ACCESO_PERMISOS = 100;
    ProgressDialog progressDialog;

    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private StorageReference audioStorageRef;
    private String audioFilePath; // Ruta de almacenamiento del archivo de audio
    private MediaPlayer mediaPlayer;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = findViewById(R.id.imageView);
        periodista = findViewById(R.id.txtperiodista);
        Latitud = findViewById(R.id.txtlatitud);
        descripcion = findViewById(R.id.txtdescripcion);
        fecha = findViewById(R.id.txtfecha);

        guardar = (Button) findViewById(R.id.btnActualizar);
        //nuevo = (Button) findViewById(R.id.btnnuevo);
        lista = (Button) findViewById(R.id.btnlista);

        reproducir = (ImageButton) findViewById(R.id.btnreproducir);
        grabar = (ImageButton) findViewById(R.id.btngrabar);

        firestore=FirebaseFirestore.getInstance();
        storage=FirebaseStorage.getInstance();
        mStorage=storage.getReference();
        mAuth=FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        lista = (Button) findViewById(R.id.btnlista);
        lista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), lista.class);
                startActivity(intent);
            }
        });

        /*nuevo = (Button) findViewById(R.id.btnnuevo);
        nuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });*/
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
                inicializarFirebase();

            }
        });
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permisos();
            }
        });

        audioStorageRef = FirebaseStorage.getInstance().getReference().child("audio");
        grabar = findViewById(R.id.btngrabar);

        grabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        });

        reproducir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reproducirAudio();
            }
        });

        //GPS
        if (checkGPS()){
            blockfields(true);
        }else{
            blockfields(false);
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        tel = preferences.getString("tel", "");
        name = preferences.getString("nombre", "");

        if(!tel.isEmpty() || !name.isEmpty()){
            descripcion.setText(name);
            fecha.setText(tel);
        }
        getIP();





    }

    private void permisosAudio(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, PETICION_ACCESO_PERMISOS);
        else {
            Obtener();
        }


    }
    private void inicializarFirebase(){
        FirebaseApp.initializeApp(this);
        firebaseDatabase= FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference();
    }
    private void permisos() {
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PETICION_ACCESO_PERMISOS);
            } else {
                Obtener();
            }
        }else {
            Obtener();
        }
    }
    private void Obtener() {

        Intent intent =new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        launcher.launch(intent);
    }
    ActivityResultLauncher<Intent> launcher
            =registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    ImageUri=data.getData();
                    try {

                        bitmap = MediaStore.Images.Media.getBitmap(
                                getContentResolver(),
                                ImageUri
                        );
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
                if ( ImageUri!=null){
                    img.setImageBitmap(bitmap);

                }
            }
    );

    private void uploadImage() {
        if (ImageUri != null) {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Cargando imagen...");
            progressDialog.show();
            String imageName = UUID.randomUUID().toString();
            StorageReference filePath = mStorage.child("images").child(imageName);

            UploadTask uploadTask = filePath.putFile(ImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        currentId++;
                        PhotUrl = task.getResult().toString();
                        Toast.makeText(getApplicationContext(), "URL de imagen: " + PhotUrl, Toast.LENGTH_LONG).show();
                        uploadInfo();

                    } else {
                        Toast.makeText(getApplicationContext(), "Error al obtener la URL de la imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Selecciona una imagen primero", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadInfo() {
        String perio = periodista.getText().toString().trim();
        String descrip = descripcion.getText().toString().trim();
        String fech = fecha.getText().toString().trim();

        if (TextUtils.isEmpty(perio) || TextUtils.isEmpty(descrip)  || TextUtils.isEmpty(fech)) {
            Toast.makeText(getApplicationContext(), "Por Favor rellene todos los datos", Toast.LENGTH_LONG).show();
        } else {
            Entrevista p = new Entrevista();
            p.setId(String.valueOf(currentId));
            p.setPeriodista(perio);
            p.setDescripcion(descrip);
            p.setFecha(fech);
            p.setImg(PhotUrl);
            p.setAudio(audioUrl);
            databaseReference.child("Entrevista").child(p.getId()).setValue(p);
            Toast.makeText(getApplicationContext(), "Agregado", Toast.LENGTH_LONG).show();
        }

        limpiarCampos();

    }

    private void startRecording() {
        // Verificar y solicitar permisos si es necesario
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, PETICION_ACCESO_PERMISOS);
            return;
        }


        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // Cambio de formato
        audioFilePath = getExternalCacheDir().getAbsolutePath() + "/audio_record.mp3"; // Cambio de extensión
        mediaRecorder.setOutputFile(audioFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            grabar.setImageResource(R.drawable.grabar); // Cambiar la imagen del botón
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;

            Uri audioUri = Uri.fromFile(new File(audioFilePath));

            StorageReference audioFileRef = audioStorageRef.child("audio_" + UUID.randomUUID().toString() + ".3gp");

            // Configurar el tipo MIME del archivo a audio/3gpp
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("audio/3gpp")
                    .build();

            audioFileRef.putFile(audioUri, metadata)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Obtener la URL de descarga del archivo de audio subido
                            audioFileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {


                                @Override
                                public void onSuccess(Uri downloadUri) {
                                    audioUrl = downloadUri.toString();

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Manejar el fallo de la subida
                            // ...
                        }
                    });
        }
    }


    private void reproducirAudio() {
        if (!TextUtils.isEmpty(audioFilePath)) {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }

            if (!mediaPlayer.isPlaying()) {
                try {
                    mediaPlayer.setDataSource(audioFilePath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    reproducir.setImageResource(R.drawable.reproducir); // Cambiar la imagen del botón a pausa
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                mediaPlayer.pause();
                reproducir.setImageResource(R.drawable.reproducir); // Cambiar la imagen del botón a reproducir
            }
        } else {
            // El archivo de audio no existe o la ruta está vacía
            Toast.makeText(this, "No se encontró el archivo de audio", Toast.LENGTH_SHORT).show();
        }
    }

    //----------LIMPIAR Campus----------------------------------
    private void limpiarCampos() {
        periodista.setText("");
        Latitud.setText("");
        descripcion.setText("");
        fecha.setText("");
        img.setImageDrawable(null);
        if (isRecording) {
            stopRecording();
        }
    }




    ////GPS--------------------------
    public void getIP() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://checkip.amazonaws.com";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override public void onResponse(String response) {respuesta=response.toString().replaceAll("\n","");}
        },
                new Response.ErrorListener() {@Override public void onErrorResponse(VolleyError error) {error.printStackTrace();}
                });
        queue.add(stringRequest);
    }

    //checkeo de gps
    private boolean checkGPS(){
        boolean check=false;
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            dialog_gps();
        }else{
            check=true;
        }
        return check;
    }

    //bloquear o habilitar campos al estar habilitado el gps
    public void blockfields(boolean stado){
        if(stado){
            //img.setClickable(true);
            //NombreContacto.setClickable(true);
            //TelefonoContacto.setClickable(true);
            guardar.setClickable(true);

        }
        else{
            //img.setClickable(false);
            //NombreContacto.setClickable(false);
            //TelefonoContacto.setClickable(false);
            guardar.setClickable(false);

        }
    }

    ///////////////////////////////////////
    //METODOS DE UBICACION
    ///////////////////////////////////////
    //obtener la localizacion exacta del cel
    private void obtenerLocalizacion() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        encontrarUbicacion(getApplicationContext(), lm);
    }
    //metodo completo de encontrar ubicacion
    public void encontrarUbicacion(Context contexto, LocationManager locationManager) {
        String location_context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) contexto.getSystemService(location_context);
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(provider, 1000, 0,
                    new LocationListener() {

                        public void onLocationChanged(Location location) {
                            String txtperiodista = String.valueOf(location.getLongitude());
                            String txtlatitud = String.valueOf(location.getLatitude());
                            //contacto.setLongitud(txtperiodista);///periodista
                            //contacto.setLatitud(txtlatitud);
                            periodista.setText(txtperiodista);///periodista
                            Latitud.setText(txtlatitud);
                        }

                        public void onProviderDisabled(String provider) {
                        }

                        public void onProviderEnabled(String provider) {
                        }

                        public void onStatusChanged(String provider, int status,
                                                    Bundle extras) {
                        }
                    });
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                String txtlongitud = String.valueOf(location.getLongitude());
                String txtlatitud = String.valueOf(location.getLatitude());
                //contacto.setLongitud(txtlongitud);
                //contacto.setLatitud(txtlatitud);
                periodista.setText(txtlongitud);
                Latitud.setText(txtlatitud);
            }
        }
    }

    private void dialog_coordinate_nofund() {
        new AlertDialog.Builder(this)
                .setTitle("COORDENADAS NO ENCONTRADAS")
                .setMessage("NO SE ENCONTRO SU UBICACION")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }
    private void dialog_gps() {
        new AlertDialog.Builder(this)
                .setTitle("GPS NO ACTIVO")
                .setMessage("Active el GPS Y REINICIE LA APP")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }

}