package com.zix;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DBConnection {

    private static DBConnection instance;
    private final FirebaseFirestore firestore;
    private final Gson gson;

    private DBConnection() {
        this.firestore = FirebaseFirestore.getInstance();
        this.gson = new Gson();
    }

    public static DBConnection getInstance() {
        if (instance == null) instance = new DBConnection();
        return instance;
    }

    public <T> void put(String collection, String id, T object) {
        final String json = this.gson.toJson(object, Object.class);
        System.out.println(json);
        firestore.collection(collection).document(id).set(this.gson.fromJson(json, Map.class));
    }

    public <T> void get(String collection, String id, Class<T> C, DBLoader<T> loader) {
        firestore.collection(collection).document(id).get().addOnCompleteListener(response -> {
            if (response.isSuccessful()) {
                JsonElement json = gson.toJsonTree(response.getResult().getData());
                loader.load(gson.fromJson(json, C));
            }
            else loader.load(null);
        });
    }

    public <T> void getCollection(String collection, Class<T> C, DBLoader<List<T>> loader) {
        firestore.collection(collection).get().addOnCompleteListener(response -> {
           if (response.isSuccessful()) {
               List<DocumentSnapshot> l = response.getResult().getDocuments();
               List<T> result = new ArrayList<>();
               l.forEach(doc -> {
                   JsonElement json = gson.toJsonTree(doc.getData());
                   result.add(gson.fromJson(json, C));
               });
               loader.load(result);
           } else loader.load(null);
        });
    }

    public interface DBLoader<T> {
        void load(T object);
    }

}
