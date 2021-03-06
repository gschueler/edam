% How it Works
% Greg Schueler
% 1/22/2012

## Conventions

Edam tries to use reasonable defaults as the conventions for its use, however most of the defaults can be changed if necessary.

* Source directory - current directory. Specify with `-d` flag.
* Output directory - same as source directory. Specify with `-o` flag.
* Templates directory - 'templates' dir in the source directory. Specify with `-t` flag.
* Output file name - basename of the source markdown file, with ".html".
* Index file name - "index.html"

Additionally, see the [Usage - Options](usage.html#options) section for further defaults that affect the generation of the output.

## Basic Mode

Edam takes all of the markdown-formatted `.txt` or `.md` in a Source directory:

    getting-started.md
    howto.md
    readme.md
    usage.md

And generates HTML pages for each one into the Output directory:

    getting-started.html
    howto.html
    index.html
    usage.html

The index (coming from 'index' or 'readme' file) will also contain a Table of Contents with links to each of the `.html` pages.

Each page will contain a set of navigation links to allow following the order of pages from one page to the next and back. 

No default CSS file is included, so it is up to you to provide a `style.css` to style the content.

## Recursive Mode

There is also a "recursive" mode, which will descend into subdirectories and apply the same logic.  Invoke it by using the `-r` command-line flag or setting the `recurseDepth` option to something non-zero to specify the depth to descend, with a negative number indicating forever.

Source Directory:

    my-project.md
    howto-guide/
        01-getting-started.md
        02-advanced.md

Output Directory: 
    
    index.html
    howto-guide/
        01-getting-started.html
        02-advanced.html
        index.html

Additionally, each Table of Contents in an upper-level will contain links to the front-page of each of its subdirectories' contents.  The navigation links in each subdirectory page will contain a set of breadcrumb links for going back up in the directory hierarchy.

Recursive mode will descend into each subdirectory of the Source directory.  You can modify the `recurseDirPattern` option to set a regular expression of directories that should be matched.

Recursive mode will *not* descend into these directories by default:

* 'templates' - any directory named templates
* outputDir - the output directory itself.
* Any directory name that matches the `recurseDirPatternIgnore` option as a regular expression.

Each subdirectory in the Source directory will produce a corresponding subdirectory in the output directory.

### Templates behavior

Recursive mode will always use the Templates directory used by the top-level directory for each subdirectory.  At this time it doesn't look for sub template directories. >> todo.txt

## Index pages

By convention, Edam tries to produce an `index.html` in each directory, so it follows a few rules to do this:

1. If you have a "readme.md" or "index.md" it becomes the Index.
2. If you have only one markdown file in the directory it becomes the Index.
4. If you have multiple markdown files, Append the Table of Contents to the Index page.
3. If there are multiple markdown files and no Index, the Table of Contents becomes the Index.

You can customize this behavior:

* Use a different name for the default index file: change the `-O indexFileName=(index|readme)` option.
* Skip rule #2 and force the single file to name itself: use `-O singleIndex=false`
* Split out the Table of Contents and Index into separate pages: use `--separate-toc`
* Don't output any Table of Contents, use `--no-toc`

## Auto-clean

Edam uses a few template and intermediate files when generating the appropriate source to feed to pandoc. By default all of the template and intermediate files are "auto-cleaned", that is, deleted, after use.  However, you may want to use the `--no-auto-clean` flag once to generate the `toc.conf` and/or template files, and then customize them to your liking.  They won't be overwritten if they already exist.

A typical use might be: use `--no-auto-clean`, customize one or two templates, and remove the rest of the generated files.  Then run edam as normal and the other files will be autocleaned to keep your sourcebase clean.

## Special files

Index file

:    The index file is considered the front page, and can be named `index` or `readme`. If there is a single document it is considered the index file.

`toc.conf`

:    This uses a simple non-markdown text format to list the source files in the order they should appear in the generated HTML docs. You can change the order of pages in the `toc.conf`, but the contents must match the source files in the directory, otherwise a warning will be printed (e.g. file is added or removed).
     
     Format:
     
         X:file.md:Title
         ...
    
     X is a number, although this number is ignored and the line ordering is used for the chapter order.

`toc.txt`

:    The Table of Contents source file links to all the other non-special pages. This file will be auto-generated. The markdown contents of the toc.txt are placed at the end of the `index` page, or in its own page with the `--separate-toc` flag.

`edam.conf`

:    A file containing Edam command-line arguments to process, one `<flag> [argument]` per-line.  You can use this in recursive mode to set options for a subdirectory context, and it will apply to all subdirectories.

     Example:

         --separate-toc
         -O pageFileName=docs-${index}
         -O tocTitle=Documentation

**Document files**

Each markdown file that is not a special file is a Document file.

## Document titles

Each document *should* have a Title specified in the pandoc metadata syntax, as the first line in the file:

    % Title

If not specified, then the file name will be used as the title.

The Title is used in linking to the document, and can be used in the output file name.

In *Recursive mode*, the title of an Index file is used in linking to the subdirectory it is in.

## Document naming

Each markdown file will generate a HTML file with the same base name.

Any "index" file will generate "index.html" (whichever basename it had). Override the index output file name with the `indexOutputFileName` option.

The HTML file name for Document files can be changed by the `pageFileName` option. The content of this
option can use `${index}`, `${name}` or `${title}` in combination to alter the generated filename.  Example: `${title}-${index}`, could produce `all-about-flamingos-12.html`.

## Navigation

If more than one output page is generated, then the navigation links are included at the top and bottom of each page. Depending on where the page is in the sequence, and the type of page, some navigation links may or may not be included.  The nav template can customize this behavior, or you can use CSS to hide/reposition the navigation links.

Disable all navigation links using the `--no-nav` flag.

Navigation links:

* Current page - included on every page.
* TOC/Index page - included on every page except the TOC page.
* Next page - included on every page except the first
* Previous page - included on every document page except the first document.

### Breadcrumbs

In recursive mode, each page in a subdirectory will also include the breadcrumb navigation links.  Each link will go to the index page of a higher-level directory.

## Templates

Edam includes some template files used in the final HTML output. The three HTML document templates (html.template, before.template, and after.template) are fed directly to pandoc, so the variable subsitution described [here](http://johnmacfarlane.net/pandoc/README.html#templates) is applicable.

`html.template`
:    The template for each HTML page.

`header.template`
:   Placed before the navigation and content.

`before.template`
:    Placed after the navigation and before the content of each page.

`after.template`
:    Placed after the content and before the bottom navigation of each page.

`footer.template`
:    Placed after the bottom navigation of each page.

The navigation template is used internally by edam to produce each navigation content for each page. It uses the same variable substitution syntax, but with a specific set of internal variables.

`nav.template`
:    Nav template for navigation links.

`navCrumbs.template`
:    Nav template for breadcrumb links.

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

### Breadcrumb Nav Variables

The default navCrumbs template contains a way to define the breadcrumb navigation:

    $if(crumbs)$
    <nav class="breadcrumb $navclass$">
        <ul>$for(crumb)$
            <li>$if(crumblink)$<a href="$crumblink$">$crumbtitle$</a>$endif$$if(crumbname)$$crumbname$$endif$</li>
        $endfor$</ul>
    </nav>
    $endif(crumbs)$

`$if(crumbs)$` and `$endif(crumbs)$`
:    Defines the content if there are any breadcrumbs available

`$navclass`
:    `top` or `bottom`, depending on which part of the page

`$for(crumb)$` and `$endfor$`
:    Contains the content used for every breadcrumb

`$if(crumblink)$` and `$endif$`
:    Renders this content if the breadcrumb has a link

`$crumblink$`
:    Link for the breadcrumb

`$crumbtitle$`
:    Title of the link

`$if(crumbname)$` and `$endif$`
:    Renders this content if the breadcrumb has no link only a name (e.g. empty intermediate subdirectory)

`$crumbname$`
:   Name of intermediate directory that has no link.

## Variable expansion

To make it easier to templatize your Markdown and template files, edam supports a method of variable expansion within all files, unlike pandoc which only supports the main document template.

Specify variables using the `-V var=value` flags, or `--variables <propertiesfile>` to include a Java Properties formatted set of variables.

By default each variable can be put in templates or Markdown files in the form: `${variablename}`.  The begin/end tokens for variables can be overridden with the options `tokenStart` and `tokenEnd`.