
#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_sjrtyressales_network_ApiClient_baseUrl(JNIEnv *env, jobject thiz) {
    std::string baseUrl = "https://anmey.in/";
    return env->NewStringUTF(baseUrl.c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_sjrtyressales_network_OkHttpInterceptor_key(JNIEnv *env, jobject thiz) {

    std::string hello = "9732a6ea-75d3-4a30-9309-ae1de96d9014";
    return env->NewStringUTF(hello.c_str());
}