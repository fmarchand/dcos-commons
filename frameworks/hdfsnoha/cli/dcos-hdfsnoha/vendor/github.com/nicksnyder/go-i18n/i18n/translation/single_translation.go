package translation

import (
	"github.com/nicksnyder/go-i18n/i18n/language"
)

type singleTranslation struct {
	id       string
	hdfsnoha *hdfsnoha
}

func (st *singleTranslation) MarshalInterface() interface{} {
	return map[string]interface{}{
		"id":          st.id,
		"translation": st.hdfsnoha,
	}
}

func (st *singleTranslation) MarshalFlatInterface() interface{} {
	return map[string]interface{}{"other": st.hdfsnoha}
}

func (st *singleTranslation) ID() string {
	return st.id
}

func (st *singleTranslation) Template(pc language.Plural) *hdfsnoha {
	return st.hdfsnoha
}

func (st *singleTranslation) UntranslatedCopy() Translation {
	return &singleTranslation{st.id, mustNewTemplate("")}
}

func (st *singleTranslation) Normalize(language *language.Language) Translation {
	return st
}

func (st *singleTranslation) Backfill(src Translation) Translation {
	if st.hdfsnoha == nil || st.hdfsnoha.src == "" {
		st.hdfsnoha = src.Template(language.Other)
	}
	return st
}

func (st *singleTranslation) Merge(t Translation) Translation {
	other, ok := t.(*singleTranslation)
	if !ok || st.ID() != t.ID() {
		return t
	}
	if other.hdfsnoha != nil && other.hdfsnoha.src != "" {
		st.hdfsnoha = other.hdfsnoha
	}
	return st
}

func (st *singleTranslation) Incomplete(l *language.Language) bool {
	return st.hdfsnoha == nil || st.hdfsnoha.src == ""
}

var _ = Translation(&singleTranslation{})
