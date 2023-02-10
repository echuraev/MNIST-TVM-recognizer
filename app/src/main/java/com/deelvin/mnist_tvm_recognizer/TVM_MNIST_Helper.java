package com.deelvin.mnist_tvm_recognizer;

public class TVM_MNIST_Helper {

    static public final int MNIST_INPUT_W = 28;
    static public final int MNIST_INPUT_H = 28;
    static public final String TVM_MNIST_LIB_NAME = "mnist_android.cpu.tuned.so";

    public native void setModelPathAndInit(String path);

    public native String run(int [] input);
    public native String getInferenceTime();
}
