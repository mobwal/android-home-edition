package com.mobwal.home.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.mobwal.home.DataManager;
import com.mobwal.home.Names;
import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.adapter.AttachmentItemAdapter;
import com.mobwal.home.models.LocationInfo;
import com.mobwal.home.models.PointBundle;
import com.mobwal.home.models.SettingRoute;
import com.mobwal.home.models.db.Attachment;
import com.mobwal.home.utilits.ActivityUtil;
import com.mobwal.home.utilits.FileManager;
import com.mobwal.home.utilits.ImageUtil;

/**
 * Модуль для отображения вложений
 */
public class AttachmentLayout extends LinearLayout
        implements View.OnClickListener, View.OnLongClickListener, RecycleViewItemListeners {

    private final String[] REQUIRED_PERMISSIONS = new String[] { Manifest.permission.CAMERA };

    private final RecyclerView mAttachList;

    private AttachmentItemAdapter mItemAdapter;
    private List<Attachment> mAttachmentList;
    private PointBundle mPointBundle;
    public String FileName;
    private LocationInfo mLocationInfo;
    private final FileManager mFileManager;

    private SettingRoute mSettingRoute;
    private final DataManager mDataManager;

    @Nullable
    private ActivityResultLauncher<String[]> mPermissionActivityResultLauncher;

    @Nullable
    private ActivityResultLauncher<Intent> mChoiceActivityResultLauncher;

    public AttachmentLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        mDataManager = new DataManager(getContext());
        mFileManager = new FileManager(getContext().getFilesDir());


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // нужно переименовать fragment_attachment на attachments_layout
        inflater.inflate(R.layout.attachment_layout, this, true);

        ImageButton attachAdd = findViewById(R.id.attach_add);
        attachAdd.setOnClickListener(this);
        attachAdd.setOnLongClickListener(this);
        mAttachList = findViewById(R.id.attach_list);
        mAttachList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    public void onPermission(Map<String, Boolean> result) {
        boolean areAllGranted = true;
        for(Boolean b : result.values()) {
            areAllGranted = areAllGranted && b;
        }

        if(areAllGranted) {
            onCamera();
        } else {
            String message = ActivityUtil.getMessageNotGranted(getContext(), new String[] { getContext().getString(R.string.camera) });
            Snackbar.make(getRootView(), message, Snackbar.LENGTH_LONG).setAction(getContext().getString(R.string.more), view -> getContext().startActivity(ActivityUtil.getIntentApplicationSetting(getContext()))).show();
        }
    }

    public void setActivityResultLauncherChoice(ActivityResultLauncher<Intent> activityResultLauncher) {
        mChoiceActivityResultLauncher = activityResultLauncher;
    }

    public void setActivityResultLauncherPermission(ActivityResultLauncher<String[]> activityResultLauncher) {
        mPermissionActivityResultLauncher = activityResultLauncher;
    }

    /**
     * Получение информации о текущей привязке
     * @return список вложений
     */
    public Attachment[] getData() {
        return mAttachmentList.toArray(new Attachment[0]);
    }

    /**
     * Установка координат
     * @param locationInfo информация о координатах
     */
    public void setLocationInfo(@Nullable LocationInfo locationInfo) {
        mLocationInfo = locationInfo;
    }

    public void setPointBundle(PointBundle bundle) {
        mSettingRoute = new SettingRoute(mDataManager.getRouteSettings(bundle.f_route));
        mPointBundle = bundle;

        if (mAttachmentList == null) {
            Collection<Attachment> attachments = mDataManager.getAttachments(bundle.f_route);
            if(attachments != null) {
                mAttachmentList = new ArrayList<>(attachments);
            } else {
                mAttachmentList = new ArrayList<>();
            }
        }

        mItemAdapter = new AttachmentItemAdapter(getContext(), mAttachmentList, this);
        mAttachList.setAdapter(mItemAdapter);
    }

    /**
     * установка данных
     * @param items список вложений
     */
    public void setData(@Nullable Collection<Attachment> items) {
        if(items != null) {
            mAttachmentList = new ArrayList<>();
            mAttachmentList.addAll(items);
        }
    }

    @Override
    public void onClick(View v) {
        if (allPermissionsGranted()) {
            onCamera();
        } else {
            WalkerApplication.Debug("Доступ к камере не предоставлен.");
            if(mPermissionActivityResultLauncher != null) {
                mPermissionActivityResultLauncher.launch(REQUIRED_PERMISSIONS);
            }
        }
    }

    /**
     * Обработчик выбра способа получения файла
     * @param v представления, которое вызвало событие
     * @return результат долгого нажатия
     */
    @Override
    public boolean onLongClick(@Nullable View v) {
        CharSequence[] items;

        items = new CharSequence[] { getContext().getString(R.string.camera), getContext().getString(R.string.album)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.choice_mode);

        builder.setItems(items, (dialog, item) -> {
            switch (item) {
                case 0:
                    onClick(v);
                    break;

                case 1:
                    onAlbum();
                    break;

                default:
                    Log.d(Names.LOG_ERROR, "Обработчик для выбора файла не найден.");
                    break;
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

        return true;
    }

    /**
     * Получение данных из камеры
     */
    private void onCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        FileName = "from-camera.jpg";
        File imageFileOutput = new File(getContext().getCacheDir(), FileName);
        Uri imageUriOutput = FileProvider.getUriForFile(
                getContext(),
                getContext().getPackageName() + ".provider",
                imageFileOutput);

        Log.d(Names.LOG, imageFileOutput.getPath());

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriOutput);

        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        if(mChoiceActivityResultLauncher != null) {
            mChoiceActivityResultLauncher.launch(takePictureIntent);
        }
    }

    /**
     * обработчик получения данных из альбома
     */
    private void onAlbum() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Intent chooserIntent = Intent.createChooser(intent, getContext().getString(R.string.choice_image));
        if(mChoiceActivityResultLauncher != null) {
            mChoiceActivityResultLauncher.launch(chooserIntent);
        }
    }

    public void onActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            // There are no request codes
            Intent data = result.getData();
            Uri uri = null;

            if(data != null) {
                uri = data.getData();
            }

            if(uri != null) {
                // это альбом
                Log.d(Names.LOG, uri.toString());
            } else {
                File img = new File(getContext().getCacheDir(), FileName);

                Log.d(Names.LOG, img.getPath());

                if(img.exists()) {
                    uri = Uri.fromFile(img);
                }
            }

            if(uri == null) {
                Toast.makeText(getContext(), R.string.camera_error, Toast.LENGTH_SHORT).show();
                return;
            }

            //https://nuancesprog.ru/p/9627/
            Executor executor = Executors.newSingleThreadExecutor();
            Uri finalUri = uri;
            executor.execute(() -> {
                try {
                    String id = UUID.randomUUID().toString();

                    InputStream iStream = getContext().getContentResolver().openInputStream(finalUri);
                    byte[] bytes = ImageUtil.compress(iStream, (int) (mSettingRoute.image_quality * 100), mSettingRoute.image_height);
                    if(bytes != null) {
                        Bitmap bitmap = ImageUtil.rotateImage(finalUri.getPath(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length));

                        byte[] byteArray = ImageUtil.bitmapToBytes(bitmap);

                        mFileManager.writeBytes(mPointBundle.f_route, id + ".jpg", byteArray);

                        ((AppCompatActivity) getContext()).runOnUiThread(() -> addAttachment(id, finalUri));
                    }
                } catch (Exception e) {
                    WalkerApplication.Debug("Ошибка сохранения вложенного изображения.", e);
                }
            });
        }
    }

    /**
     * Добавление вложения
     * @param id идентификатор
     * @param uri путь к файлу, где храниться файл
     */
    private void addAttachment(String id, @Nullable Uri uri) {
        if(uri == null) {
            return;
        }
        Attachment attachment = new Attachment();
        attachment.id = id;
        attachment.f_point = mPointBundle.f_point;
        attachment.f_route = mPointBundle.f_route;
        attachment.f_result = mPointBundle.f_result;

        attachment.c_name = id + ".jpg";
        attachment.d_date = new Date();

        if(mLocationInfo != null) {
            attachment.n_distance = mLocationInfo.getDistance();
            attachment.n_longitude = mLocationInfo.myLongitude;
            attachment.n_latitude = mLocationInfo.myLatitude;
        }

        attachment.n_date = new Date().getTime();

        mItemAdapter.add(attachment);

        int itemCount = mItemAdapter.getItemCount() - 1;
        mAttachList.smoothScrollToPosition(itemCount);
    }

    /**
     * Открыть изображение в галереи
     * @param id идентификатор вложения
     */
    @Override
    public void onViewItemClick(String id) {
        for (Attachment attach: mItemAdapter.getData()) {
            if(attach.id.equals(id)) {
                ActivityUtil.openGallery(getContext(), new File(mFileManager.getRootCatalog(mPointBundle.f_route), attach.c_name));
                return;
            }
        }

        Toast.makeText(getContext(), R.string.attach_not_found, Toast.LENGTH_SHORT).show();
    }

    /**
     * Удаление изображения
     * @param id идентификатор вложения
     */
    @Override
    public void onViewItemInfo(String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.attention);
        builder.setMessage(R.string.attach_remove);

        builder.setCancelable(false);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            mItemAdapter.removeAttachment(id);
        });
        builder.setNegativeButton(R.string.no, null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean allPermissionsGranted() {
        for(String permission : REQUIRED_PERMISSIONS) {
            if(ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * Сохранение данных в БД
     * @param f_result иден. результат
     * @return результат сохранения
     */
    public boolean saveData(@Nullable String f_result) {
        if(f_result != null) {
            Attachment[] attachments = getData();
            if (attachments.length > 0) {
                return mDataManager.updateAttachments(f_result, attachments);
            } else {
                return true;
            }
        }
        return false;
    }
}