#include <jni.h>
#include <string>

#include "TVMRunner.h"

extern "C"
JNIEXPORT void JNICALL
Java_com_deelvin_mnist_1tvm_1recognizer_TVM_1MNIST_1Helper_setModelPathAndInit(JNIEnv *env, jobject thiz,
                                                                               jstring path) {
    auto& runner = TVMRunner::getInstance();
    const char *nativeString = env->GetStringUTFChars(path, 0);
    runner.setModelPathAndInit(nativeString);
    env->ReleaseStringUTFChars(path, nativeString);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_deelvin_mnist_1tvm_1recognizer_TVM_1MNIST_1Helper_run(JNIEnv *env, jobject thiz,
                                                               jintArray input) {
    jsize size = env->GetArrayLength(input);
    std::vector<int> vec(size);
    env->GetIntArrayRegion(input, jsize{0}, size, &vec[0]);
    auto& runner = TVMRunner::getInstance();

    std::string res = runner.run(vec);
    return env->NewStringUTF(res.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_deelvin_mnist_1tvm_1recognizer_TVM_1MNIST_1Helper_getInferenceTime(JNIEnv *env,
                                                                            jobject thiz) {
    auto& runner = TVMRunner::getInstance();
    std::string res = runner.getInferenceTime();
    return env->NewStringUTF(res.c_str());
}