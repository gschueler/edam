% Edam - easy doc generation with pandoc
% Greg Schueler
% 1/16/2012

Edam is a [pandoc] wrapper written in Groovy to help generate documentation from Markdown text files.  

It is completely inspired by [Gouda][], which is a python script.

[pandoc]: http://johnmacfarlane.net/pandoc/
[Gouda]: http://www.unexpected-vortices.com/sw/gouda/docs/

All of the docs in the docs/ dir were generated using this command:

    edam.groovy -o docs

## At a glance

Edam takes all of the markdown-formatted `.txt` or `.md` in a directory:

    getting-started.md
    howto.md
    readme.md
    usage.md

And generates HTML pages for each one:

    getting-started.html
    howto.html
    index.html
    usage.html

The index (coming from 'index' or 'readme' file) will also contain a Table of Contents with links to each of the `.html` pages.

Each page will contain a set of navigation links to allow following the order of pages from one page to the next and back. 

No default CSS file is included, so it is up to you to provide a `style.css` to style the content.

There is also a "recursive" mode, which will descend into subdirectories and apply the same logic:
    
    my-project.md
    howto-guide/
        01-getting-started.md
        02-advanced.md
    
    index.html
    howto-guide/
        01-getting-started.html
        02-advanced.html
        index.html

Additionally, each Table of Contents in an upper-level will contain links to the front-page of each of its subdirectories' contents.  The navigation links in each subdirectory page will contain a set of breadcrumb links for going back up in the directory hierarchy.

