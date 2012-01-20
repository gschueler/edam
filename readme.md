% Edam - easy doc generation with pandoc
% Greg Schueler
% 1/16/2012

Edam is a simple [pandoc] wrapper written in Groovy to help generate documentation from Markdown text files.  

It is completely inspired by [Gouda][], which is a python script.

[pandoc]: http://johnmacfarlane.net/pandoc/
[Gouda]: http://www.unexpected-vortices.com/sw/gouda/docs/

All of the docs in the docs/ dir were generated using this command:

    edam.groovy -o docs

## Behavior

The purpose is to simply generate a set of HTML pages, with cross-navigation links, based on a set of Markdown text files in a directory.

Each Markdown file (suffix `.md` or `.txt`) in the basedir is generated into a HTML file.

**Special files**:

Index file

:    The index file is considered the front page, and can be named `index` or `readme`. If there is a single document it is considered the index file.

`toc.conf`

:    This uses a simple non-markdown text format to list the source files in the order they should appear in the generated HTML docs. This file will be generated if it does not exist.  You can change the order of pages in the `toc.conf`, but the contents must match the source files in the directory, otherwise a warning will be printed (e.g. file is added or removed).

`toc.txt`

:    The Table of Contents source file links to all the other non-special pages. This file will be auto-generated. The markdown contents of the toc.txt are placed at the end of the `index` page, or in its own page with the `--separate-toc` flag.

**Document files**

Each markdown file that is not a special file is a Document file.

## Document titles

Each document *should* have a Title specified in the pandoc metadata syntax, as the first line in the file:

    % Title

If not specified, then the file name will be used as the title.

The Title is used in linking to the document, and can be used in the output file name.

## Document naming

Each markdown file will generate a HTML file with the same base name.

Any "index" file will generate "index.html" (whichever basename it had). Override the index output file name with the `indexOutputFileName` option.

The HTML file name for Document files can be changed by the `pageFileName` option. The content of this
option can use `${index}`, `${name}` or `${title}` in combination to alter the generated filename.  Example: `${title}-${index}`, could produce `all-about-flamingos-12.html`.

## Document structure

To make the generated HTML as simple as possible there are a few different modes, based on the number and names of the documents in your directory.

**One Document Mode**

In this mode, the document is generated as `index.html`.  Turn it off with the `singleIndex=false` option and it will be named based on the `pageFileName`.

**One Chapter Mode**

If an index and one chapter file exist, then no TOC will be generated.

Link sequence:

1. Index
2. Chapter 1

**Multiple Chapter Mode**

Multiple chapters will generate a TOC. If an index file exists, then the TOC will be placed at the bottom of the Index content. If no index file exists it will simply be the only Index page content.

Link sequence:

1. Index + TOC
2. Chapters ...

**Separate TOC Chapter Mode**

If `--separate-toc` is used, and an Index page exists, the TOC and Index are generated as separate pages:

Link sequence:

1. Index
2. Toc page
3. Chapters

## Navigation

If more than one output page is generated, then the navigation links are included at the top and bottom of each page. Depending on where the page is in the sequence, and the type of page, some navigation links may or may not be included.  The nav template can customize this behavior, or you can use CSS to hide/reposition the navigation links.

Navigation links:

* Current page - included on every page.
* TOC/Index page - included on every page except the TOC page.
* Next page - included on every page except the first
* Previous page - included on every document page except the first document.

## Auto-clean

Edam uses a few template and intermediate files when generating the appropriate source to feed to pandoc. By default all of the template and intermediate files are "auto-cleaned", that is, deleted, after use.  However, you may want to use the `--no-auto-clean` flag once to generate the `toc.conf` and/or template files, and then customize them to your liking.  They won't be overwritten if they already exist.

A typical use might be: use `--no-auto-clean`, customize one or two templates, and remove the rest of the generated files.  Then run edam as normal and the other files will be autocleaned to keep your sourcebase clean.

## Templates

Edam includes some template files used in the final HTML output. The three HTML document templates (html.template, before.template, and after.template) are fed directly to pandoc, so the variable subsitution described [here](http://johnmacfarlane.net/pandoc/README.html#templates) is applicable.

`html.template`
:    The template for each HTML page.

`before.template`
:    Placed before the navigation and content of each page.

`after.template`
:    Placed after the content and bottom navigation of each page.

The navigation template is used internally by edam to produce each navigation content for each page. It uses the same variable substitution syntax, but with a specific set of internal variables.

`nav.template`
:    Nav template for the toc/index page.

### Nav Variables

The default nav template shows the possible nav template variables:

    <nav class="page $navclass$">
        <ul>
            <li class="current"><a href="$currentpagelink$">$currentpage$</a></li>
            $if(tocpage)$<li class="toc"><a href="$tocpagelink$">$tocpage$</a></li>$endif$
            $if(prevpage)$<li class="prev"><a href="$prevpagelink$">$prevpage$</a></li>$endif$
            $if(nextpage)$<li class="next"><a href="$nextpagelink$">$nextpage$</a></li>$endif$
        </ul>
    </nav>

`$navclass$`
:    `top` or `bottom`, depending on which part of the page it is included on.

`$*page$`
:    The title of the specified page: toc,prev,next, current.

`$*pagelink$`
:    The URL to the specified page: toc,prev,next,current.

The content between `$if(*page)$` and `$endif` will be included only if the specified page link should be shown (as described in the [Navigation](#navigation) section).

## Variable expansion

To make it easier to templatize your Markdown and template files, edam supports a method of variable expansion within all files, unlike pandoc which only supports the main document template.

Specify variables using the `-V var=value` flags, or `--variables <propertiesfile>` to include a Java Properties formatted set of variables.

By default each variable can be put in templates or Markdown files in the form: `${variablename}`.  The begin/end tokens for variables can be overridden with the options `tokenStart` and `tokenEnd`.
