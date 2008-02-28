#ifndef MERKAARTOR_PAINTSTYLE_EDITOR_H_
#define MERKAARTOR_PAINTSTYLE_EDITOR_H_

#include "GeneratedFiles/ui_PaintStyleEditor.h"
#include "PaintStyle/PaintStyle.h"

#include <QtGui/QDialog>

#include <vector>

class PaintStyleEditor : public QDialog, public Ui::PaintStyleEditor
{
	Q_OBJECT

	public:
		PaintStyleEditor(QWidget* aParent, const std::vector<FeaturePainter>& aPainters);

	public slots:
		void on_PaintList_itemClicked(QListWidgetItem*);
		void on_Key_editingFinished();
		void on_Value1_editingFinished();
		void on_Value2_editingFinished();
		void on_LowerZoomBoundary_valueChanged();
		void on_UpperZoomBoundary_valueChanged();
		void on_DrawBackground_clicked(bool b);
		void on_BackgroundColor_clicked();
		void on_ProportionalBackground_valueChanged();
		void on_FixedBackground_valueChanged();
		void on_DrawForeground_clicked(bool b);
		void on_ForegroundColor_clicked();
		void on_ProportionalForeground_valueChanged();
		void on_FixedForeground_valueChanged();
		void on_ForegroundDashed_clicked();
		void on_ForegroundDashOn_valueChanged();
		void on_ForegroundDashOff_valueChanged();
		void on_DrawTouchup_clicked(bool b);
		void on_TouchupColor_clicked();
		void on_ProportionalTouchup_valueChanged();
		void on_FixedTouchup_valueChanged();
		void on_TouchupDashed_clicked();
		void on_TouchupDashOn_valueChanged();
		void on_TouchupDashOff_valueChanged();
		void on_DrawFill_clicked(bool b);
		void on_FillColor_clicked();
		void on_DrawIcon_clicked(bool b);
		void on_IconName_textEdited();
		void on_AddButton_clicked();
		void on_RemoveButton_clicked();

	public:
		std::vector<FeaturePainter> thePainters;
	private:
		void updatePaintList();
	private:
		bool FreezeUpdate;
};

#endif
