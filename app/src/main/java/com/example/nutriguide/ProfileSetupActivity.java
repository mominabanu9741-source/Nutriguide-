package com.example.nutriguide;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {

    private EditText etAge, etHeight, etWeight;
    private CheckBox cbDiabetes, cbPcos, cbHypertension, cbObesity;
    private CheckBox cbPeanuts, cbDairy, cbGluten;
    private RadioGroup rgGender;
    private Button btnSaveProfile;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);

        cbDiabetes = findViewById(R.id.cbDiabetes);
        cbPcos = findViewById(R.id.cbPcos);
        cbHypertension = findViewById(R.id.cbHypertension);
        cbObesity = findViewById(R.id.cbObesity);

        cbPeanuts = findViewById(R.id.cbPeanuts);
        cbDairy = findViewById(R.id.cbDairy);
        cbGluten = findViewById(R.id.cbGluten);

        rgGender = findViewById(R.id.rgGender);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String ageStr = etAge.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        if (ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please fill in age, height, and weight", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedGenderBtn = findViewById(selectedGenderId);
        String gender = selectedGenderBtn.getText().toString(); // "Male" or "Female"

        List<String> conditions = new ArrayList<>();
        if (cbDiabetes.isChecked()) conditions.add("diabetes");
        if (cbPcos.isChecked()) conditions.add("pcos");
        if (cbHypertension.isChecked()) conditions.add("hypertension");
        if (cbObesity.isChecked()) conditions.add("obesity");

        List<String> allergies = new ArrayList<>();
        if (cbPeanuts.isChecked()) allergies.add("peanuts");
        if (cbDairy.isChecked()) allergies.add("dairy");
        if (cbGluten.isChecked()) allergies.add("gluten");

        Map<String, Object> profile = new HashMap<>();
        profile.put("age", Integer.parseInt(ageStr));
        profile.put("height", Double.parseDouble(heightStr));
        profile.put("weight", Double.parseDouble(weightStr));
        profile.put("gender", gender);
        profile.put("conditions", conditions);
        profile.put("allergies", allergies);

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .set(profile)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}