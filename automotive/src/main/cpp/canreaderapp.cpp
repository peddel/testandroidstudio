#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/can.h>
#include <linux/can/raw.h>
#include <net/if.h>
#include <cerrno>

#define LOG_TAG "CAN_NATIVE"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {
int socket_fd = -1;

JNIEXPORT jint JNICALL
Java_com_example_canreaderapp_MainActivity_openCAN(JNIEnv *env, jobject /* this */, jstring interfaceName) {
    const char *ifname = env->GetStringUTFChars(interfaceName, nullptr);
    struct sockaddr_can addr{};
    struct ifreq ifr{};

    // Open a CAN socket
    socket_fd = socket(PF_CAN, SOCK_RAW, CAN_RAW);
    if (socket_fd < 0) {
        LOGE("Error opening CAN socket: %s", strerror(errno));
        return -1;
    }

    strcpy(ifr.ifr_name, ifname);
    if (ioctl(socket_fd, SIOCGIFINDEX, &ifr) < 0) {
        LOGE("Error getting interface index: %s", strerror(errno));
        return -2;
    }

    addr.can_family = AF_CAN;
    addr.can_ifindex = ifr.ifr_ifindex;

    if (bind(socket_fd, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        LOGE("Error binding CAN socket: %s", strerror(errno));
        return -3;
    }

    LOGI("CAN interface %s opened successfully", ifname);
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_example_canreaderapp_MainActivity_sendCANMessage(JNIEnv *env, jobject /* this */, jint id, jbyteArray data) {
    if (socket_fd < 0) {
        LOGE("CAN socket not open!");
        return -1;
    }

    struct can_frame frame{};
    jsize len = env->GetArrayLength(data);
    jbyte *dataBytes = env->GetByteArrayElements(data, 0);

    frame.can_id = id;
    frame.can_dlc = len > 8 ? 8 : len;  // CAN frame max length is 8 bytes
    memcpy(frame.data, dataBytes, frame.can_dlc);

    if (write(socket_fd, &frame, sizeof(struct can_frame)) < 0) {
        LOGE("Error sending CAN message: %s", strerror(errno));
        return -2;
    }

    env->ReleaseByteArrayElements(data, dataBytes, 0);
    return 0;
}

JNIEXPORT jbyteArray JNICALL
Java_com_example_canreaderapp_MainActivity_receiveCANMessage(JNIEnv *env, jobject /* this */) {
    if (socket_fd < 0) {
        LOGE("CAN socket not open!");
        return nullptr;
    }

    struct can_frame frame{};
    int bytesRead = read(socket_fd, &frame, sizeof(struct can_frame));
    if (bytesRead < 0) {
        LOGE("Error reading CAN message: %s", strerror(errno));
        return nullptr;
    }

    jbyteArray result = env->NewByteArray(frame.can_dlc);
    env->SetByteArrayRegion(result, 0, frame.can_dlc, (jbyte *)frame.data);
    return result;
}

JNIEXPORT void JNICALL
Java_com_example_canreaderapp_MainActivity_closeCAN(JNIEnv *env, jobject /* this */) {
    if (socket_fd >= 0) {
        close(socket_fd);
        socket_fd = -1;
        LOGI("CAN socket closed");
    }
}
}