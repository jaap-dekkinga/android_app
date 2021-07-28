#include <jni.h>
#include <string>
#include <iostream>
#include "Fingerprint.h"
#include "FingerprintManager.h"

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_dekidea_tuneurl_service_SoundMatchingService_extractFingerprint(JNIEnv* env, jobject /* this */, jobject byteBuffer, jint waveLength) {

    int16_t* wave = (int16_t*) env->GetDirectBufferAddress(byteBuffer);

    Fingerprint* fingerprint = ExtractFingerprint(wave, waveLength);

    jbyteArray result = (env)->NewByteArray(fingerprint->dataSize);

    (env)->SetByteArrayRegion(result, 0, fingerprint->dataSize, reinterpret_cast<const jbyte *>(fingerprint->data));

    FingerprintFree(fingerprint);

    return result;
}


extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_dekidea_tuneurl_service_SoundMatchingService_extractFingerprintFromRawFile(JNIEnv* env, jobject /* this */, jstring filePath) {

    const char * _nativeString = env->GetStringUTFChars(filePath, 0);

    Fingerprint* fingerprint = ExtractFingerprintFromRawFile(_nativeString);

    jbyteArray result = (env)->NewByteArray(fingerprint->dataSize);

    (env)->SetByteArrayRegion(result, 0, fingerprint->dataSize, reinterpret_cast<const jbyte*>(fingerprint->data));

    FingerprintFree(fingerprint);

    return result;
}

