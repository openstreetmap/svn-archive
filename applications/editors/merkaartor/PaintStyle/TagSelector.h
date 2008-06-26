#ifndef MERKAARTOR_STYLE_TAGSELECTOR_H_
#define MERKAARTOR_STYLE_TAGSELECTOR_H_

class MapFeature;

#include <QtCore/QString>
#include <QRegExp>
#include <QVector>

#include <vector>

class TagSelector
{
	public:
		virtual ~TagSelector() = 0;

		virtual TagSelector* copy() const = 0;
		virtual bool matches(const MapFeature* F) const = 0;
		virtual QString asExpression(bool Precedence) const = 0;

		static TagSelector* parse(const QString& Expression);
};

class TagSelectorIs : public TagSelector
{
	public:
		TagSelectorIs(const QString& key, const QString& value);

		virtual TagSelector* copy() const;
		virtual bool matches(const MapFeature* F) const;
		virtual QString asExpression(bool Precedence) const;

	private:
		QRegExp rx;
		QString Key, Value;
};

class TagSelectorTypeIs : public TagSelector
{
	public:
		TagSelectorTypeIs(const QString& type);

		virtual TagSelector* copy() const;
		virtual bool matches(const MapFeature* F) const;
		virtual QString asExpression(bool Precedence) const;

	private:
		QString Type;
};

class TagSelectorHasTags : public TagSelector
{
	public:
		TagSelectorHasTags();

		virtual TagSelector* copy() const;
		virtual bool matches(const MapFeature* F) const;
		virtual QString asExpression(bool Precedence) const;
};

class TagSelectorIsOneOf : public TagSelector
{
	public:
		TagSelectorIsOneOf(const QString& key, const std::vector<QString>& values);

		virtual TagSelector* copy() const;
		virtual bool matches(const MapFeature* F) const;
		virtual QString asExpression(bool Precedence) const;

	private:
		QVector<QRegExp> rxv;
		QString Key;
		std::vector<QString> Values;
};

class TagSelectorOr : public TagSelector
{
	public:
		TagSelectorOr(const std::vector<TagSelector*> Terms);
		~TagSelectorOr();

		virtual TagSelector* copy() const;
		virtual bool matches(const MapFeature* F) const;
		virtual QString asExpression(bool Precedence) const;

	private:
		std::vector<TagSelector*> Terms;
};

class TagSelectorAnd : public TagSelector
{
	public:
		TagSelectorAnd(const std::vector<TagSelector*> Terms);
		~TagSelectorAnd();

		virtual TagSelector* copy() const;
		virtual bool matches(const MapFeature* F) const;
		virtual QString asExpression(bool Precedence) const;

	private:
		std::vector<TagSelector*> Terms;
};


#endif
