package com.example.edubridge;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.edubridge.databinding.ActivityChangePasswordBinding;

public class ChangePasswordActivity extends AppCompatActivity {
    private ActivityChangePasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnSubmitChange.setOnClickListener(v -> {
            String oldP = binding.etOldPassword.getText().toString().trim();
            String newP = binding.etNewPassword.getText().toString().trim();
            String confP = binding.etConfirmPassword.getText().toString().trim();

            if (oldP.isEmpty() || newP.isEmpty() || confP.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newP.equals(confP)) {
                Toast.makeText(this, "Confirmation does not match", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Password Changed Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}