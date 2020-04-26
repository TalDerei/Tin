package edu.lehigh.cse216.teamtin;

import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;

/**
 * This class encodes a file into Base64 and forms it into a multipart/form-data request
 */
public class MultiPartStringFileRequest extends StringRequest {

    File file;
    String encodedString;

    public MultiPartStringFileRequest(int method, String url, File file, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.file = file;
        byte[] content = new byte[0];
        try {
            content = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MPSFR", "Could not encode the file");
        }
        encodedString = Base64.encodeToString(content, Base64.DEFAULT);
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data";
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return createFileRequestBody(file.getName(), encodedString);
    }

    /**
     * Uses okhttp to create a multipart request body for the server
     */
    byte[] createFileRequestBody(String fileName, String encodedFile) {
        RequestBody rb = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"" + fileName +"\""),
                        RequestBody.create(MediaType.parse("image/jpg"), encodedFile))
                .build();
        Buffer buf = new Buffer();
        try {
            rb.writeTo(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf.readByteArray();
    }
}
