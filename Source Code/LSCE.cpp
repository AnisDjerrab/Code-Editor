#include <iostream>
#include <windows.h>

using namespace std;

int main() {
    SECURITY_ATTRIBUTES securityAttributes;
    securityAttributes.nLength = sizeof(SECURITY_ATTRIBUTES);
    securityAttributes.bInheritHandle = TRUE;
    securityAttributes.lpSecurityDescriptor = NULL;
    PROCESS_INFORMATION processInfo;
    STARTUPINFO startupInfo;
    ZeroMemory(&processInfo, sizeof(PROCESS_INFORMATION));
    ZeroMemory(&startupInfo, sizeof(STARTUPINFO));
    startupInfo.cb = sizeof(STARTUPINFO);
    char cmd[] = "cmd.exe /C cd \"LSCE/\" && java -cp . -Djava.library.path=. codeEditor"; 
    CreateProcess(NULL, cmd, NULL, NULL, TRUE, CREATE_NO_WINDOW, NULL, NULL, &startupInfo, &processInfo);
    return 0;
}