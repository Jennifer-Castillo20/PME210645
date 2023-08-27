package com.example.pme210645;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Adaptador extends RecyclerView.Adapter<Adaptador.EntrevistaViewHolder> {

    private List<Entrevista> personaList;
    private Context mContext;
    private OnItemClickListener mListener;
    private Activity mActivity;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public Adaptador(Context context, List<Entrevista> personaList, Activity activity) {
        this.mContext = context;
        this.personaList = personaList;
        this.mActivity = activity;

    }

    @NonNull
    @Override
    public EntrevistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item, parent, false);
        return new EntrevistaViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull EntrevistaViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Entrevista persona1 =  personaList.get(position);
        holder.periodista.setText(persona1.getPeriodista());
        holder.descripcion.setText(persona1.getDescripcion());
        holder.fecha.setText(persona1.getFecha());
        String imageUrl = persona1.getImg();

        if (imageUrl != null && !imageUrl.isEmpty() && holder.img != null) {
            Picasso.get()
                    .load(persona1.getImg())
                    .placeholder(R.drawable.img)
                    .fit()
                    .centerCrop()
                    .into(holder.img);
        }

        holder.editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogPlus dialogPlus = DialogPlus.newDialog(mActivity)
                        .setContentHolder(new ViewHolder(R.layout.update))
                        .setGravity(Gravity.CENTER)
                        .setExpanded(false)  // Cambiar a false para que el diálogo no ocupe toda la pantalla
                        .create();
                View view = dialogPlus.getHolderView();

                EditText descipcion = view.findViewById(R.id.txtdescripcion);
                EditText img = view.findViewById(R.id.Url);///////(Cambie tenia url) ya no lo arregle
                EditText periodista = view.findViewById(R.id.txtperiodista);
                EditText fecha = view.findViewById(R.id.txtfecha);
                EditText audio = view.findViewById(R.id.audi);
                Button actualizar = view.findViewById(R.id.btnActualizar);

                descipcion.clearFocus();
                img.clearFocus();
                periodista.clearFocus();
                fecha.clearFocus();
                audio.clearFocus();

                // Muestra el cuadro de diálogo
                dialogPlus.show();
                descipcion.setText(persona1.getDescripcion());
                img.setText(persona1.getImg());
                periodista.setText(persona1.getPeriodista());
                fecha.setText(persona1.getFecha());
                audio.setText(persona1.getAudio());
                dialogPlus.show();

                actualizar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Obtén los datos actualizados desde los EditText
                        String nuevoPeriodista = periodista.getText().toString();
                        String nuevaUrl = img.getText().toString();
                        String nuevodescipcion= descipcion.getText().toString();
                        String nuevaFecha = fecha.getText().toString();
                        String naudio = audio.getText().toString();

                        // Crea un mapa con los datos actualizados
                        Map<String, Object> map = new HashMap<>();
                        map.put("periodista", nuevoPeriodista);
                        map.put("descripcion",nuevodescipcion);
                        map.put("fecha", nuevaFecha);
                        map.put("img", nuevaUrl);
                        map.put("audio", naudio);
                        Entrevista persona = personaList.get(position);
                        DatabaseReference personaRef = FirebaseDatabase.getInstance().getReference()
                                .child("Entrevista")
                                .child(persona.getId());
                        personaRef.updateChildren(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(mActivity, "Datos actualizados", Toast.LENGTH_SHORT).show();
                                        dialogPlus.dismiss();

                                        persona.setPeriodista(nuevoPeriodista);
                                        persona.setDescripcion(nuevodescipcion);
                                        persona.setFecha(nuevaFecha);
                                        persona.setImg(nuevaUrl);
                                        persona.setAudio(naudio);
                                        // Aquí se actualiza la lista después de eliminar
                                        notifyDataSetChanged();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(mActivity, "Error al actualizar datos", Toast.LENGTH_SHORT).show();
                                        dialogPlus.dismiss();
                                    }
                                });

                    }
                });
            }
        });
        holder.eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mActivity.isFinishing() && !mActivity.isDestroyed()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setTitle("Estás seguro?");
                    builder.setMessage("Eliminar datos...");

                    builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Entrevista persona = personaList.get(position);
                            String personaKey = persona.getId();
                            DatabaseReference personaRef = FirebaseDatabase.getInstance().getReference()
                                    .child("Entrevista")
                                    .child(personaKey);
                            personaRef.removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(mActivity, "Datos eliminados", Toast.LENGTH_SHORT).show();
                                            personaList.remove(position);
                                            notifyItemRemoved(position);
                                            // Aquí se actualiza la lista después de eliminar
                                            notifyDataSetChanged();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(mActivity, "Error al eliminar datos", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });

                    builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(mActivity, "Cancelado", Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (mActivity.getWindow() != null && mActivity.getWindow().getDecorView().getWindowToken() != null) {
                        builder.show();
                    }
                }
            }
        });
        holder.audi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String audioUrl = persona1.getAudio();
                if (audioUrl != null && !audioUrl.isEmpty()) {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(audioUrl);
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                // Se llama cuando la reproducción ha terminado
                                mediaPlayer.release();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "Error al reproducir el audio", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, "No hay URL de audio disponible", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //////////////LLamar
    /*holder.llamar.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String fecha = persona1.getFecha(); // Cambia esto a la variable que contiene el número de teléfono

            if (fecha != null && !fecha.isEmpty()) {
                Intent intentLlamada = new Intent(Intent.ACTION_DIAL);
                intentLlamada.setData(Uri.parse("tel:" + fechaecha));
                mContext.startActivity(intentLlamada);
            } else {
                Toast.makeText(mContext, "No hay número de teléfono disponible", Toast.LENGTH_SHORT).show();
            }
        }
    });*/


    @Override
    public int getItemCount() {
        return personaList.size();
    }

    public class EntrevistaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        private ImageView img;
        private TextView periodista, descripcion, fecha,audio;
        private Button editar, eliminar,audi;

        public EntrevistaViewHolder(@NonNull View itemView) {
            super(itemView);
            periodista = itemView.findViewById(R.id.txtperiodista);
            descripcion = itemView.findViewById(R.id.txtDescripcion);
            fecha = itemView.findViewById(R.id.txtFecha);
            img =(ImageView)itemView.findViewById(R.id.img);
            audio =itemView.findViewById(R.id.audio);///(tenia audio corregi a udi)ya no lo corregi

            editar = itemView.findViewById(R.id.editar);
            eliminar = itemView.findViewById(R.id.eliminar);
            audi = itemView.findViewById(R.id.audi);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public boolean onMenuItemClick(@NonNull MenuItem item) {
            if (mListener != null) {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    switch (item.getItemId()) {
                        case 1:
                            mListener.onShowItemClick(position);
                            return true;
                        case 2:
                            mListener.onDeleteItemClick(position);
                            return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select Action");
            MenuItem showItem = menu.add(Menu.NONE, 1, Menu.NONE, "Show");
            MenuItem deleteItem = menu.add(Menu.NONE, 2, Menu.NONE, "Delete");

            showItem.setOnMenuItemClickListener(this);
            deleteItem.setOnMenuItemClickListener(this);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onShowItemClick(int position);
        void onDeleteItemClick(int position);
    }



}

