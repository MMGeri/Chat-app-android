package com.geri.chat.data;

        import android.content.Context;
        import android.util.Log;

        import com.geri.chat.data.model.Message;
        import com.geri.chat.data.model.User;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.firestore.CollectionReference;
        import com.google.firebase.firestore.FirebaseFirestore;
        import com.google.firebase.firestore.Query;
        import com.google.firebase.firestore.QuerySnapshot;

        import java.time.LocalDateTime;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.Map;

public class DAO {
    private static DAO instance;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser currentUser;

    private DAO() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public static DAO getInstance() {
        if (instance == null) {
            instance = new DAO();
        }
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public Task<Void> addUserToFirestore(User user, Context context) {
        Map<String, Object> userMap = new HashMap<String, Object>() {
            {
                put("id", user.getId());
                put("name", user.getName());
            }
        };
        Task<Void> task = firebaseFirestore.collection("users").document(currentUser.getUid()).set(userMap);
        return task;
    }

    public FirebaseFirestore getFirebaseDatabase() {
        return firebaseFirestore;
    }




}
