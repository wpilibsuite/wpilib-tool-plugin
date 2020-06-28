#include <Windows.h>

#include <jni.h>

extern "C" {


BOOL WINAPI DllEntryPoint(HINSTANCE hinstDLL, DWORD fdwReason,
        LPVOID lpReserved) {
    return TRUE;
}

/*
 * Class:     edu_wpi_first_wpiutil_WPIUtilJNI
 * Method:    addDllSearchDirectory
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT jboolean JNICALL Java_edu_wpi_first_wpiutil_CombinedRuntimeLoader_addDllSearchDirectory
(JNIEnv* env, jclass cls, jstring directory)
{
    const jchar* dirStr = env->GetStringChars(directory, NULL);
    BOOL retVal = SetDllDirectoryW((PCWCHAR)dirStr);
    env->ReleaseStringChars(directory, dirStr);
    return retVal;
}

}
