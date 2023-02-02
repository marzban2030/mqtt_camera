package info.staticfree.mqtt_camera.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.Date;

import info.staticfree.mqtt_camera.mqtt.MqttRemote;
import info.staticfree.mqtt_camera.ftp.FTPWrapper;

/**
 * Publishes the image to an MQTT subTopic.
 */
public class ImagePublisher implements Runnable {
    private final Context context;
    private final Image image;
    private final String subTopic;
    private final MqttRemote mqttRemote;
    
    private FTPWrapper ftpClient = new FTPWrapper();

    public ImagePublisher(@NonNull Context context, @NonNull Image image, @NonNull MqttRemote mqttRemote,
            @NonNull String subTopic) {
        this.context = context;
        this.image = image;
        this.mqttRemote = mqttRemote;
        this.subTopic = subTopic;
    }

    @Override
    public void run() {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        boolean ftpStatus = ftp(context, imageBitmap);
        try {
            if (ftpStatus == true) {
                mqttRemote.publish(subTopic, "True".getBytes());
            } else {
                mqttRemote.publish(subTopic, "False".getBytes());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            image.close();
        }
    }

    private int getQuality(Context context) {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        if (!preferences.getBoolean("mqtt_remote_enable", false)) {
            return 1;
        }

        int quality = Integer.valueOf(preferences.getString("mqtt_quality", "0"));
        return quality;
    }
    
    public boolean ftp(Context context, Bitmap bitmap) {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        if (!preferences.getBoolean("mqtt_remote_enable", false)) {
            return false;
        }

        String host = preferences.getString("ftp_hostname", null);
        String username = preferences.getString("ftp_username", null);
        String password = preferences.getString("ftp_password", null);
        int port = Integer.valueOf(preferences.getString("ftp_port", "0"));
        String dir = preferences.getString("ftp_dir", null);
        
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "/JPEG_" + timeStamp + ".jpg";
        boolean status = ftpClient.ftpConnect(host, username, password, port);
        if (status == true) {
            ftpClient.ftpUploadBitmap(bitmap, dir + imageFileName);
            ftpClient.ftpDisconnect();
        } else {
            Log.e("ImagePublisher", "FTP connection faild");
        }
        return status;
    }
}
