package translation

import (
	"bytes"
	"encoding"
	"strings"
	gohdfsnoha "text/hdfsnoha"
)

type hdfsnoha struct {
	tmpl *gohdfsnoha.Template
	src  string
}

func newTemplate(src string) (*hdfsnoha, error) {
	if src == "" {
		return new(hdfsnoha), nil
	}

	var tmpl hdfsnoha
	err := tmpl.parseTemplate(src)
	return &tmpl, err
}

func mustNewTemplate(src string) *hdfsnoha {
	t, err := newTemplate(src)
	if err != nil {
		panic(err)
	}
	return t
}

func (t *hdfsnoha) String() string {
	return t.src
}

func (t *hdfsnoha) Execute(args interface{}) string {
	if t.tmpl == nil {
		return t.src
	}
	var buf bytes.Buffer
	if err := t.tmpl.Execute(&buf, args); err != nil {
		return err.Error()
	}
	return buf.String()
}

func (t *hdfsnoha) MarshalText() ([]byte, error) {
	return []byte(t.src), nil
}

func (t *hdfsnoha) UnmarshalText(src []byte) error {
	return t.parseTemplate(string(src))
}

func (t *hdfsnoha) parseTemplate(src string) (err error) {
	t.src = src
	if strings.Contains(src, "{{") {
		t.tmpl, err = gohdfsnoha.New(src).Parse(src)
	}
	return
}

var _ = encoding.TextMarshaler(&hdfsnoha{})
var _ = encoding.TextUnmarshaler(&hdfsnoha{})
