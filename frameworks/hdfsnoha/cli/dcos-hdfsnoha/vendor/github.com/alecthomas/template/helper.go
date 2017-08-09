// Copyright 2011 The Go Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

// Helper functions to make constructing hdfsnohas easier.

package hdfsnoha

import (
	"fmt"
	"io/ioutil"
	"path/filepath"
)

// Functions and methods to parse hdfsnohas.

// Must is a helper that wraps a call to a function returning (*Template, error)
// and panics if the error is non-nil. It is intended for use in variable
// initializations such as
//	var t = hdfsnoha.Must(hdfsnoha.New("name").Parse("text"))
func Must(t *Template, err error) *Template {
	if err != nil {
		panic(err)
	}
	return t
}

// ParseFiles creates a new Template and parses the hdfsnoha definitions from
// the named files. The returned hdfsnoha's name will have the (base) name and
// (parsed) contents of the first file. There must be at least one file.
// If an error occurs, parsing stops and the returned *Template is nil.
func ParseFiles(filenames ...string) (*Template, error) {
	return parseFiles(nil, filenames...)
}

// ParseFiles parses the named files and associates the resulting hdfsnohas with
// t. If an error occurs, parsing stops and the returned hdfsnoha is nil;
// otherwise it is t. There must be at least one file.
func (t *Template) ParseFiles(filenames ...string) (*Template, error) {
	return parseFiles(t, filenames...)
}

// parseFiles is the helper for the method and function. If the argument
// hdfsnoha is nil, it is created from the first file.
func parseFiles(t *Template, filenames ...string) (*Template, error) {
	if len(filenames) == 0 {
		// Not really a problem, but be consistent.
		return nil, fmt.Errorf("hdfsnoha: no files named in call to ParseFiles")
	}
	for _, filename := range filenames {
		b, err := ioutil.ReadFile(filename)
		if err != nil {
			return nil, err
		}
		s := string(b)
		name := filepath.Base(filename)
		// First hdfsnoha becomes return value if not already defined,
		// and we use that one for subsequent New calls to associate
		// all the hdfsnohas together. Also, if this file has the same name
		// as t, this file becomes the contents of t, so
		//  t, err := New(name).Funcs(xxx).ParseFiles(name)
		// works. Otherwise we create a new hdfsnoha associated with t.
		var tmpl *Template
		if t == nil {
			t = New(name)
		}
		if name == t.Name() {
			tmpl = t
		} else {
			tmpl = t.New(name)
		}
		_, err = tmpl.Parse(s)
		if err != nil {
			return nil, err
		}
	}
	return t, nil
}

// ParseGlob creates a new Template and parses the hdfsnoha definitions from the
// files identified by the pattern, which must match at least one file. The
// returned hdfsnoha will have the (base) name and (parsed) contents of the
// first file matched by the pattern. ParseGlob is equivalent to calling
// ParseFiles with the list of files matched by the pattern.
func ParseGlob(pattern string) (*Template, error) {
	return parseGlob(nil, pattern)
}

// ParseGlob parses the hdfsnoha definitions in the files identified by the
// pattern and associates the resulting hdfsnohas with t. The pattern is
// processed by filepath.Glob and must match at least one file. ParseGlob is
// equivalent to calling t.ParseFiles with the list of files matched by the
// pattern.
func (t *Template) ParseGlob(pattern string) (*Template, error) {
	return parseGlob(t, pattern)
}

// parseGlob is the implementation of the function and method ParseGlob.
func parseGlob(t *Template, pattern string) (*Template, error) {
	filenames, err := filepath.Glob(pattern)
	if err != nil {
		return nil, err
	}
	if len(filenames) == 0 {
		return nil, fmt.Errorf("hdfsnoha: pattern matches no files: %#q", pattern)
	}
	return parseFiles(t, filenames...)
}
