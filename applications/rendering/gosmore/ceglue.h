#ifndef CEGLUE_H
#define CEGLUE_H
#ifdef __cplusplus
extern "C" {
#endif

extern BOOL (*SHFullScreenPtr)(HWND hwnd, DWORD state);
void InitCeGlue (void);

#ifdef __cplusplus
}
#endif

#endif
