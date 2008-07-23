#include <windows.h>
#include "ceglue.h"

BOOL FAR (*SHFullScreenPtr)(HWND hwnd, DWORD state) = NULL;

void InitCeGlue (void)
{
  HINSTANCE ayg = LoadLibraryW (TEXT ("aygshell.dll"));
  if (ayg != NULL) {
    SHFullScreenPtr = (BOOL (*)(HWND, DWORD))
      GetProcAddressW (ayg, TEXT ("SHFullScreen"));
  }
}
