package com.example.mirrorme;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;

    private View homeLayout;
    private View cameraLayout;
    private PreviewView previewView;
    private TextView modeLabel;
    private Button buttonMirror;
    private Button buttonOthers;

    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private boolean mirrored = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View rootLayout = findViewById(R.id.rootLayout);
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return windowInsets;
            });
        }

        homeLayout = findViewById(R.id.homeLayout);
        cameraLayout = findViewById(R.id.cameraLayout);
        previewView = findViewById(R.id.previewView);
        modeLabel = findViewById(R.id.modeLabel);

        MaterialCardView cardMirrorView = findViewById(R.id.cardMirrorView);
        MaterialCardView cardOthersView = findViewById(R.id.cardOthersView);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonMirror = findViewById(R.id.buttonMirror);
        buttonOthers = findViewById(R.id.buttonOthers);
        View buttonCapture = findViewById(R.id.buttonCapture);

        if (previewView != null) {
            // COMPATIBLE mode supports transformations such as horizontal mirroring.
            previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        }

        if (cardMirrorView != null) {
            cardMirrorView.setOnClickListener(v -> selectModeAndOpen(true));
        }
        if (cardOthersView != null) {
            cardOthersView.setOnClickListener(v -> selectModeAndOpen(false));
        }

        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> showHomeScreen());
        }

        if (buttonMirror != null) {
            buttonMirror.setOnClickListener(v -> {
                mirrored = true;
                applyMirrorMode();
            });
        }

        if (buttonOthers != null) {
            buttonOthers.setOnClickListener(v -> {
                mirrored = false;
                applyMirrorMode();
            });
        }

        if (buttonCapture != null) {
            buttonCapture.setOnClickListener(v -> takePhoto());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (cameraLayout != null && cameraLayout.getVisibility() == View.VISIBLE) {
                    showHomeScreen();
                } else {
                    finish();
                }
            }
        });
    }

    private void selectModeAndOpen(boolean mirrorMode) {
        this.mirrored = mirrorMode;
        if (hasCameraPermission()) {
            openCameraScreen();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );
        }
    }

    private void openCameraScreen() {
        if (homeLayout != null) homeLayout.setVisibility(View.GONE);
        if (cameraLayout != null) cameraLayout.setVisibility(View.VISIBLE);
        applyMirrorMode();
        startCamera();
    }

    private void showHomeScreen() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (cameraLayout != null) cameraLayout.setVisibility(View.GONE);
        if (homeLayout != null) homeLayout.setVisibility(View.VISIBLE);
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void applyMirrorMode() {
        if (previewView != null) {
            // Front camera preview is mirrored by default at scaleX(1f).
            // Therefore: scaleX(1f) = Mirror View (Selfie Mirror)
            // scaleX(-1f) = Others View (Unmirrored, how others see you)
            previewView.setScaleX(mirrored ? 1f : -1f);
        }
        if (modeLabel != null) {
            modeLabel.setText(mirrored ? "Mirror View" : "Others View");
        }

        if (buttonMirror != null && buttonOthers != null) {
            if (mirrored) {
                buttonMirror.setAlpha(1.0f);
                buttonOthers.setAlpha(0.5f);
            } else {
                buttonMirror.setAlpha(0.5f);
                buttonOthers.setAlpha(1.0f);
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraFuture =
                ProcessCameraProvider.getInstance(this);

        cameraFuture.addListener(() -> {
            try {
                cameraProvider = cameraFuture.get();

                Preview preview = new Preview.Builder().build();
                if (previewView != null) {
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                }

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Unable to start the camera", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera is not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }

        java.io.File photoFile = new java.io.File(
                getCacheDir(), "mirrorme_preview_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        showSaveDialog(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(MainActivity.this, "Photo capture failed", Toast.LENGTH_LONG).show();
                        exception.printStackTrace();
                    }
                });
    }

    private void showSaveDialog(java.io.File photoFile) {
        new AlertDialog.Builder(this)
                .setTitle("Photo captured")
                .setMessage("Would you like to save this photo to your Gallery?")
                .setPositiveButton("Save", (dialog, which) -> saveToGallery(photoFile))
                .setNegativeButton("Discard", (dialog, which) -> {
                    //noinspection ResultOfMethodCallIgnored
                    photoFile.delete();
                    Toast.makeText(this, "Photo discarded", Toast.LENGTH_SHORT).show();
                })
                .setOnCancelListener(dialog -> {
                    //noinspection ResultOfMethodCallIgnored
                    photoFile.delete();
                })
                .show();
    }

    private void saveToGallery(java.io.File photoFile) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "MirrorMe_" + timestamp + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MirrorMe");

        Uri imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (imageUri == null) {
            Toast.makeText(this, "Could not save photo", Toast.LENGTH_LONG).show();
            //noinspection ResultOfMethodCallIgnored
            photoFile.delete();
            return;
        }

        try (java.io.InputStream input = new java.io.FileInputStream(photoFile);
             java.io.OutputStream output = getContentResolver().openOutputStream(imageUri)) {
            if (output == null) throw new java.io.IOException("Unable to open Gallery destination");
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
            output.flush();
            Toast.makeText(this, "Saved to Pictures/MirrorMe", Toast.LENGTH_LONG).show();
        } catch (java.io.IOException e) {
            getContentResolver().delete(imageUri, null, null);
            Toast.makeText(this, "Could not save photo", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            //noinspection ResultOfMethodCallIgnored
            photoFile.delete();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraScreen();
            } else {
                Toast.makeText(this, "Camera permission is required to use MirrorMe", Toast.LENGTH_LONG).show();
            }
        }
    }
}
