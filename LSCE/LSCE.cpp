#include <iostream>
#include <windows.h>
#include <string>
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
    char home[MAX_PATH];
    GetModuleFileName(NULL, home, MAX_PATH);
    char home2[MAX_PATH];
    string cheminTemp = string(home);
    size_t size = cheminTemp.find_last_of("\\/");
    size_t o = 0;
    for (size_t i = 0; i < size; i++) {
        o++;
        home2[i] = home[i];
    }
    char cheminRelatif[] = "\\LSCE";
    for (size_t i = 0; i < string(cheminRelatif).size(); i++) {
        home2[o] = cheminRelatif[i];
        o++;
    }
    string command_1 = "javaw -cp \"" + string(home2) + "\" -Djava.library.path=\"" + string(home2) + "\" codeEditor";
    char command[command_1.size()];
    int n = 0;
    for (int i = 0; i < command_1.size(); i++) {
        command[i] = command_1[i];
        n++;
    } 
    command[n + 1] = '\0';
    CreateProcess(NULL, command, NULL, NULL, TRUE, 0, NULL, home2, &startupInfo, &processInfo);
    return 0;
}