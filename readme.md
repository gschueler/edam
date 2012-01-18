% Edam - easy doc generation with pandoc
% Greg Schueler
% 1/16/2012

Edam is a simple [pandoc] wrapper written in Groovy to help generate documentation from Markdown text files.  

It is completely inspired by [Gouda][], which is a python script.

[pandoc]: http://johnmacfarlane.net/pandoc/
[Gouda]: http://www.unexpected-vortices.com/sw/gouda/docs/

See an example of it in use in the docs generated for this project.

All of the docs in the docs/ dir were generated using this command:

    edam.groovy -o docs

## Behavior

The purpose is to simply generate a set of HTML pages, with cross-navigation links, based on a set of Markdown text files in a directory.

Each `.md` or `.txt` file in the basedir is used as a source file.  There are a couple special files:

`index`

:    The index file is considered the front page.  It can be named "index.md/.txt", or "readme.md/.txt" by default. The name can be overridden.

`toc`

:    The Table of Contents links to all the other non-index pages.  By default the toc file is placed at the end of the `index` page when generated, but it can be generated as a separate page with the `--separate-toc` flag.

`toc.conf`

:    This uses a simple non-markdown text format to list the source files in the order they should appear in the generated HTML docs. This file will be generated if it does not exist.  You can change the order of pages, but the contents should match the source files in the directory, otherwise a warning will be printed.

### Auto-clean

Edam uses a few template and intermediate files when generating the appropriate source to feed to pandoc. By default all of the template and intermediate files are "auto-cleaned", that is, deleted, after use.  However, you may want to use the `--no-auto-clean` flag once to generate the `toc.conf` or template files, and then customize them to your liking.  They won't be overwritten if they already exist.

A typical use might be: use `--no-auto-clean`, customize one or two templates, and remove the rest of the generated files.  Then run edam as normal and the necessary files will be generated and removed to keep your sourcebase clean.

### Templates

Edam includes some template files used in the final HTML output. The three HTML document templates (html.template, before.template, and after.template) are fed directly to pandoc, so the variable subsitution described [here](http://johnmacfarlane.net/pandoc/README.html#templates) is applicable.

However, the navigation templates are used internally by edam to produce each navigation content for each page, and use the same variable substitution syntax, but a specific set of variables.

`html.template`
:    The template for each HTML page.

`before.template`
:    Placed before the navigation and content of each page.

`after.template`
:    Placed after the content and bottom navigation of each page.

`navToc.template`
:    Nav template for the toc/index page.

`navFirst.template`
:    Nav template for the first content page.

`nav.template`
:    Nav template for all intermediate content pages except the last.

`navLast.template`
:    Nav template for the last content page.
