package com.rituraj.shivamkiranastore.helper;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadHelper {
    public interface UploadCallback {
        void onSuccess(String imageUrl);

        void onFailure(String error);
    }

    private static String BASEURL = "https://shivamkiranastore.onrender.com";

    public static void uploadImage(File file, UploadCallback callback) {

        OkHttpClient client = new OkHttpClient();

        RequestBody fileBody = RequestBody.create(
                file,
                MediaType.parse("image/*")
        );

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "image",
                        file.getName(),
                        fileBody
                )
                .build();

        Request request = new Request.Builder()
                .url(BASEURL + "/api/upload")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {

                if (response.body() != null) {

                    String result = response.body().string();

                    try {

                        JSONObject json = new JSONObject(result);

                        boolean success =
                                json.getBoolean("success");

                        if (success) {

                            String fileId =
                                    json.getString("fileId");

                            String imageUrl =
                                    "https://lh3.googleusercontent.com/d/"
                                            + fileId;

                            callback.onSuccess(imageUrl);

                        } else {

                            callback.onFailure(
                                    json.getString("message")
                            );
                        }

                    } catch (Exception e) {

                        callback.onFailure(e.getMessage());
                    }
                }
            }
        });
    }

    public static File uriToFile(Context context, Uri uri) throws IOException {
        String fileName = "temp_image";
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile(
                fileName,
                ".jpg",
                context.getCacheDir()
        );
        OutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[4096];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();

        return tempFile;
    }
}