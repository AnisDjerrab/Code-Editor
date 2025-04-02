#include <windows.h>
#include <jni.h>
#include <string>
#include <iostream>
#include <locale>
#include <codecvt>
HANDLE CmdIn = NULL;
HANDLE CmdInRd = NULL;
HANDLE CmdOutRd = NULL;
HANDLE CmdOutWr = NULL;
HANDLE CmdErrRd = NULL;
HANDLE CmdErrWr = NULL;
HANDLE hProcess = NULL;
HANDLE hThread = NULL;
extern "C" {
    JNIEXPORT void JNICALL Java_codeEditor_CreateTerminal(JNIEnv*, jobject) {
        SECURITY_ATTRIBUTES securityAttributes;
        securityAttributes.nLength = sizeof(SECURITY_ATTRIBUTES);
        securityAttributes.bInheritHandle = TRUE;
        securityAttributes.lpSecurityDescriptor = NULL;
        CreatePipe(&CmdOutRd, &CmdOutWr, &securityAttributes, 4096);
        SetHandleInformation(CmdOutRd, HANDLE_FLAG_INHERIT, 0);
        CreatePipe(&CmdErrRd, &CmdErrWr, &securityAttributes, 4096);
        SetHandleInformation(CmdErrRd, HANDLE_FLAG_INHERIT, 0);
        CreatePipe(&CmdInRd, &CmdIn, &securityAttributes, 4096);
        SetHandleInformation(CmdIn, HANDLE_FLAG_INHERIT, 0);
        PROCESS_INFORMATION processInfo;
        STARTUPINFO startupInfo;
        ZeroMemory(&processInfo, sizeof(PROCESS_INFORMATION));
        ZeroMemory(&startupInfo, sizeof(STARTUPINFO));
        startupInfo.cb = sizeof(STARTUPINFO);
        startupInfo.hStdError = CmdErrWr;
        startupInfo.hStdOutput = CmdOutWr;
        startupInfo.hStdInput = CmdInRd;
        startupInfo.dwFlags |= STARTF_USESTDHANDLES;
        char cmd[] = "cmd.exe /K chcp 850 >nul"; 
        CreateProcess(NULL, cmd, NULL, NULL, TRUE, CREATE_NO_WINDOW, NULL, NULL, &startupInfo, &processInfo);
        hProcess = processInfo.hProcess;
        hThread = processInfo.hThread;
    }
    JNIEXPORT void JNICALL Java_codeEditor_SendCommand(JNIEnv *env, jobject, jstring command) {
        const jchar* originalJString = env->GetStringChars(command, NULL);
        jsize len = env->GetStringLength(command);
        std::wstring wideStr(len, L'\0');
        for (jsize i = 0; i < len; ++i) {
            wideStr[i] = static_cast<wchar_t>(originalJString[i]);
        }
        int bufferSize = WideCharToMultiByte(850, 0, wideStr.c_str(), -1, NULL, 0, NULL, NULL);
        std::string cmd(bufferSize - 1, L'\0'); 
        WideCharToMultiByte(850, 0, wideStr.c_str(), -1, &cmd[0], bufferSize, NULL, NULL);
        DWORD written;
        WriteFile(CmdIn, cmd.c_str(), cmd.size(), &written, NULL);
        WriteFile(CmdIn, "\n", 1, &written, NULL);
        FlushFileBuffers(CmdIn);
        env->ReleaseStringChars(command, originalJString);
    }
    JNIEXPORT jstring JNICALL Java_codeEditor_ReadOutput(JNIEnv* env, jobject) {
        char buffer[4096];
        DWORD read, available;
        std::string output = "";
        while (true) {
            if (!PeekNamedPipe(CmdOutRd, NULL, 0, NULL, &available, NULL) || available == 0) {
                break;
            }
            if (ReadFile(CmdOutRd, buffer, sizeof(buffer) - 1, &read, NULL) && read > 0) {
                buffer[read] = '\0';  
                output.append(buffer);
            }
        }
        int wideSize = MultiByteToWideChar(850, 0, output.c_str(), -1, NULL, 0);
        std::wstring wideStr(wideSize - 1, L'\0');
        MultiByteToWideChar(850, 0, output.c_str(), -1, &wideStr[0], wideSize); 
        return env->NewString((const jchar*)wideStr.c_str(), wideStr.length());
    }
    JNIEXPORT jstring JNICALL Java_codeEditor_ReadError(JNIEnv* env, jobject) {
        fflush(stdout);
        char buffer[4096];
        DWORD read, available;
        std::string output = "";
        while (true) {
            if (!PeekNamedPipe(CmdErrRd, NULL, 0, NULL, &available, NULL) || available == 0) {
                break;
            }
            if (ReadFile(CmdErrRd, buffer, sizeof(buffer) - 1, &read, NULL) && read > 0) {
                buffer[read] = '\0';  
                output.append(buffer);
            }
        }
        int wideSize = MultiByteToWideChar(850, 0, output.c_str(), -1, NULL, 0);
        std::wstring wideStr(wideSize - 1, L'\0');
        MultiByteToWideChar(850, 0, output.c_str(), -1, &wideStr[0], wideSize); 
        return env->NewString((const jchar*)wideStr.c_str(), wideStr.length());
    }
    JNIEXPORT void JNICALL Java_codeEditor_CloseTerminal(JNIEnv* env, jobject) {
        fflush(stdout);
        TerminateProcess(hProcess, 0);
        CloseHandle(hProcess);
        CloseHandle(hThread);
        hProcess = NULL;
        hThread = NULL;
        CloseHandle(CmdIn);
        CloseHandle(CmdOutRd);
        CloseHandle(CmdErrRd);
        CloseHandle(CmdErrWr);
        CloseHandle(CmdOutWr);
        CloseHandle(CmdInRd);
    }
}