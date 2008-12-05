#ifndef CEGLUE_H
#define CEGLUE_H
#ifdef __cplusplus
extern "C" {
#endif

#include <aygshell.h>

extern BOOL (*SHFullScreenPtr)(HWND hwnd, DWORD state);
extern BOOL (*SHInitDialogPtr)(PSHINITDLGINFO pshidi);

void InitCeGlue (void);

int CeEnableBacklight(int enable);

#ifdef __cplusplus
}
#endif

#endif
