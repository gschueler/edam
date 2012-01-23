% Edam - easy doc generation with pandoc
% Greg Schueler
% 1/16/2012

Edam is a [pandoc] wrapper written in Groovy to help generate documentation from Markdown text files.  

It is completely inspired by [Gouda][], which is a python script.

[pandoc]: http://johnmacfarlane.net/pandoc/
[Gouda]: http://www.unexpected-vortices.com/sw/gouda/docs/

All of the docs in the docs/ dir were generated using this command:

    edam.groovy -o docs

## Take a set of Markdown text files...

Source directory:

    getting-started.md
    howto.md
    readme.md - "Welcome to my project..."
    usage.md

Output directory:

    getting-started.html
    howto.html
    index.html
    usage.html

The '(readme|index).(md|txt)' is the "Index" page and outputs to 'index.html'.

The Index will contain the Table of Contents at the bottom:

    Welcome to my project ...
    
    1. Getting Started
    2. How-To
    3. Usage

Every page will contain a set of navigation links (next, previous, table of contents):

    [Table of Contents] [<- Getting Started] [-> Usage]

(No default CSS file is included, so it is up to you to provide a `style.css` to style the content.)

**Recursive mode** will descend into subdirectories. The Table of Contents will contain links to the Index of each of its subdirectories:
    
    my-project.md
    howto-guide/
        01-getting-started.md
        02-advanced.md
    
    index.html
    howto-guide/
        01-getting-started.html
        02-advanced.html
        index.html

Each subdirectory page will also contain a set of breadcrumb links for going back up in the directory hierarchy.

    My Project >
    [Table of Contents] [-> Advanced]
    
    Getting Started
    ...