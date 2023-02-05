package com.deelvin.mnist_tvm_recognizer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.deelvin.mnist_tvm_recognizer.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'mnist_tvm_recognizer' library on application startup.
    static {
        System.loadLibrary("mnist_tvm_recognizer");
    }

    private ActivityMainBinding binding;
    private AssetManager mgr;
    private static ImageView imageView;
    private static TextView predictedNumber;
    private static TextView inferenceTime;
    private static TVM_MNIST_Helper tvm_helper = new TVM_MNIST_Helper();

    private static int [] getPixels(Bitmap b) {
        int size = b.getRowBytes() * b.getHeight();
        ByteBuffer buffer = ByteBuffer.allocate(size);
        b.copyPixelsToBuffer(buffer);
        byte [] bytes = buffer.array();
        int [] pixels = new int[TVM_MNIST_Helper.MNIST_INPUT_H * TVM_MNIST_Helper.MNIST_INPUT_W];
        for (int i = 0; i < TVM_MNIST_Helper.MNIST_INPUT_H * TVM_MNIST_Helper.MNIST_INPUT_W; ++i) {
            pixels[i] = bytes[i] & 0xff;
        }
        return pixels;
    }


    static public void setBitmap(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        int [] number = getPixels(bitmap);
        Log.w("myApp", "Print bitmap:");
        for (int i = 0; i < TVM_MNIST_Helper.MNIST_INPUT_H; ++i) {
            String str = "";
            for (int j = 0; j < TVM_MNIST_Helper.MNIST_INPUT_W; ++j) {
                str += Integer.toString(number[i * TVM_MNIST_Helper.MNIST_INPUT_W + j]) + ", ";
            }
            Log.w("myApp", str);
        }
        Log.w("myApp", "Print bitmap!");
        predictedNumber.setText(tvm_helper.run(number));
        inferenceTime.setText(tvm_helper.getInferenceTime());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        imageView = binding.imageView;
        predictedNumber = binding.predictedNumber;
        inferenceTime = binding.inferenceTime;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        PaintView paintView = binding.PaintView;
        ViewGroup.LayoutParams params = paintView.getLayoutParams();
        params.height = displayMetrics.widthPixels;
        params.width = displayMetrics.widthPixels;
        paintView.setLayoutParams(params);

        if (mgr == null) {
            mgr = getAssets();
        }
        String libCacheFilePath = "";
        try {
            libCacheFilePath = getTempLibFilePath();
            copyToTempLib(libCacheFilePath, TVM_MNIST_Helper.TVM_MNIST_LIB_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        tvm_helper.setModelPathAndInit(libCacheFilePath + "/" + TVM_MNIST_Helper.TVM_MNIST_LIB_NAME);
    }

    private byte[] getBytesFromFile(AssetManager assets, String fileName) throws IOException {
        InputStream is = assets.open(fileName);
        int length = is.available();
        byte[] bytes = new byte[length];
        // Read in the bytes
        int offset = 0;
        int numRead;
        try {
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            is.close();
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + fileName);
        }
        return bytes;
    }

    private String getTempLibFilePath() throws IOException {
        File tempDir = File.createTempFile("tvm_demo_", "");
        if (!tempDir.delete() || !tempDir.mkdir()) {
            throw new IOException("Couldn't create directory " + tempDir.getAbsolutePath());
        }
        return (tempDir + File.separator );
    }

    private void copyToTempLib(String pth, String fileName) throws IOException {
        byte[] modelLibByte = getBytesFromFile(mgr,  fileName);
        FileOutputStream fos = new FileOutputStream(pth + fileName);
        fos.write(modelLibByte);
        fos.close();
    }
}