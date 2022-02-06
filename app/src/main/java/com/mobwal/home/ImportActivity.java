package com.mobwal.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import pw.appcode.mimic.SimpleFormLayout;
import com.mobwal.home.databinding.ActivityImportBinding;
import com.mobwal.home.utilits.StreamUtil;

public class ImportActivity extends AppCompatActivity {

    public static Intent getIntent(Context context, Uri uri) {
        Intent intent = new Intent(context, ImportActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        ContentResolver cr = context.getContentResolver();
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType(cr.getType(uri));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        WalkerApplication.Debug(MessageFormat.format("Экран импорта {0}", type));

        com.mobwal.home.databinding.ActivityImportBinding binding = ActivityImportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarImport.toolbar);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_route).build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_import);
        if(navHostFragment != null) {

            NavInflater inflater = navHostFragment.getNavController().getNavInflater();
            NavGraph graph = inflater.inflate(R.navigation.import_navigation);
            navHostFragment.setArguments(getIntent().getExtras());

            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if ("text/plain".equals(type)) {
                    Uri textUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    try {
                        InputStream iStream = ImportActivity.this.getContentResolver().openInputStream(textUri);
                        byte[] bytes = StreamUtil.readBytes(iStream);
                        String data = new String(bytes, StandardCharsets.UTF_8);
                        if(SimpleFormLayout.isSimpleLayout(data)) {
                            WalkerApplication.Debug("Файлом импорта является шаблон.");
                            graph.setStartDestination(R.id.nav_layout_import);
                        }
                    } catch (IOException e) {
                        WalkerApplication.Log("Ошибка чтения файла импорта. Формат " + type, e);
                    }
                } else if("application/zip".equals(type)) {
                    graph.setStartDestination(R.id.nav_zip_import);
                }
            }

            navHostFragment.getNavController().setGraph(graph);

            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(MainActivity.getIntent(this));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}