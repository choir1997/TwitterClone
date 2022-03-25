package edu.byu.cs.tweeter.server.Calculations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;

public class UserCalculations {

    public static String getSecurePassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "FAILED TO HASH PASSWORD";
    }

    public static String getSalt() {
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
            byte[] salt = new byte[16];
            sr.nextBytes(salt);
            return Base64.getEncoder().encodeToString(salt);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return "FAILED TO GET SALT";
    }

    public static String removeNonAlphanumeric(String salt)
    {
        salt = salt.replaceAll(
                "[^a-zA-Z0-9]", "");
        return salt;
    }

    public static String getAuthToken() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static String getTimeStamp() {
        return new SimpleDateFormat("yy/MM/dd HH:mm:ss").format(new Date());
    }

    public static String uploadImageToS3(String image, String imageKey) {
        AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion("us-west-1")
                .build();
        try {
            byte[] decodedString =  Base64.getDecoder().decode(image);

            InputStream stream = new ByteArrayInputStream(decodedString);

            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(decodedString.length);
            meta.setContentType("image/png");

            String bucketName = "choir1997";
            s3.putObject(bucketName, imageKey, stream, meta);

            System.out.println("successfully uploaded file : " +  s3.getUrl(bucketName, imageKey).toString());


            stream.close();

            return s3.getUrl(bucketName, imageKey).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
