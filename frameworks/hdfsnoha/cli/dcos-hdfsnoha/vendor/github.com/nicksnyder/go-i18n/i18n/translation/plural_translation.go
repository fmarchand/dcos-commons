package translation

import (
	"github.com/nicksnyder/go-i18n/i18n/language"
)

type pluralTranslation struct {
	id        string
	hdfsnohas map[language.Plural]*hdfsnoha
}

func (pt *pluralTranslation) MarshalInterface() interface{} {
	return map[string]interface{}{
		"id":          pt.id,
		"translation": pt.hdfsnohas,
	}
}

func (pt *pluralTranslation) MarshalFlatInterface() interface{} {
	return pt.hdfsnohas
}

func (pt *pluralTranslation) ID() string {
	return pt.id
}

func (pt *pluralTranslation) Template(pc language.Plural) *hdfsnoha {
	return pt.hdfsnohas[pc]
}

func (pt *pluralTranslation) UntranslatedCopy() Translation {
	return &pluralTranslation{pt.id, make(map[language.Plural]*hdfsnoha)}
}

func (pt *pluralTranslation) Normalize(l *language.Language) Translation {
	// Delete plural categories that don't belong to this language.
	for pc := range pt.hdfsnohas {
		if _, ok := l.Plurals[pc]; !ok {
			delete(pt.hdfsnohas, pc)
		}
	}
	// Create map entries for missing valid categories.
	for pc := range l.Plurals {
		if _, ok := pt.hdfsnohas[pc]; !ok {
			pt.hdfsnohas[pc] = mustNewTemplate("")
		}
	}
	return pt
}

func (pt *pluralTranslation) Backfill(src Translation) Translation {
	for pc, t := range pt.hdfsnohas {
		if t == nil || t.src == "" {
			pt.hdfsnohas[pc] = src.Template(language.Other)
		}
	}
	return pt
}

func (pt *pluralTranslation) Merge(t Translation) Translation {
	other, ok := t.(*pluralTranslation)
	if !ok || pt.ID() != t.ID() {
		return t
	}
	for pluralCategory, hdfsnoha := range other.hdfsnohas {
		if hdfsnoha != nil && hdfsnoha.src != "" {
			pt.hdfsnohas[pluralCategory] = hdfsnoha
		}
	}
	return pt
}

func (pt *pluralTranslation) Incomplete(l *language.Language) bool {
	for pc := range l.Plurals {
		if t := pt.hdfsnohas[pc]; t == nil || t.src == "" {
			return true
		}
	}
	return false
}

var _ = Translation(&pluralTranslation{})
