#include <iostream>
#include <windows.h>

using namespace std;

int main() {
    char JavaHome_1[MAX_PATH];
    DWORD size = GetEnvironmentVariable("JAVA_HOME", JavaHome_1, MAX_PATH);
    SECURITY_ATTRIBUTES securityAttributes;
    string JavaHome = JavaHome_1;
    if (JavaHome != "") {
        JavaHome += "\\bin\\java.exe";        
        securityAttributes.nLength = sizeof(SECURITY_ATTRIBUTES);
        securityAttributes.bInheritHandle = TRUE;
        securityAttributes.lpSecurityDescriptor = NULL;
        PROCESS_INFORMATION processInfo;
        STARTUPINFO startupInfo;
        ZeroMemory(&processInfo, sizeof(PROCESS_INFORMATION));
        ZeroMemory(&startupInfo, sizeof(STARTUPINFO));
        startupInfo.cb = sizeof(STARTUPINFO);
        string command_1 = "\"" + JavaHome + "\" -cp \"LSCE\" -Djava.library.path=LSCE codeEditor";
        char command[command_1.size()];
        for (int i = 0; i < command_1.size(); i++) {
            command[i] = command_1[i];
        } 
        CreateProcess(NULL, command, NULL, NULL, TRUE, DETACHED_PROCESS, NULL, NULL, &startupInfo, &processInfo);
    }
    return 0;
}