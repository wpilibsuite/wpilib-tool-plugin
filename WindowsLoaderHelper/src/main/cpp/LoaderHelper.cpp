#include <Windows.h>

#include <jni.h>

extern "C" {


BOOL WINAPI DllEntryPoint(HINSTANCE hinstDLL, DWORD fdwReason,
        LPVOID lpReserved) {
    return TRUE;
}

/*
 * Class:     edu_wpi_first_util_WPIUtilJNI
 * Method:    setDllDirectory
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_edu_wpi_first_util_CombinedRuntimeLoader_setDllDirectory
(JNIEnv* env, jclass cls, jstring directory)
{
    WCHAR data[512];
    DWORD dirLength = GetDllDirectoryW(512, data);

    if (directory == NULL) {
        goto end;
    }
    const jchar* dirStr = env->GetStringChars(directory, NULL);
    SetDllDirectoryW((PCWCHAR)dirStr);
    env->ReleaseStringChars(directory, dirStr);
    end:
    if (dirLength > 0) {
        return env->NewString((const jchar*)data, dirLength);
    } else {
        return NULL;
    }
}

/*
 * Class:     edu_wpi_first_wpiutil_WPIUtilJNI
 * Method:    setDllDirectory
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_edu_wpi_first_wpiutil_CombinedRuntimeLoader_setDllDirectory
(JNIEnv* env, jclass cls, jstring directory)
{
    // for backwards compatibility
    return Java_edu_wpi_first_util_CombinedRuntimeLoader_setDllDirectory(env, cls, directory);
}

}
