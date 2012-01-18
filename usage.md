% Usage

    edam.groovy [-h/--help] [-d <basedir>] [-t <templatesdir>] [-o <outputdir>] [--no-toc] [--no-nav] [--verbose] [--clean] [--separate-toc]
    [-O option=value [ -O ... ] ]

## Arguments

`-h/--help`
:   Show this help text.

`-d <basedir>`
:   The base directory containing the docs to convert. Defaults to the current directory.

`-t <templatesdir>`
:   The directory containing templates. Defaults to basedir/templates.

`-o <outputdir>`
:   The directory to write HTML files to. Defaults to the basedir.

`--no-toc`
:   Don't include the Table of Contents.

`--no-nav`
:   Don't include navigation links on each page.

`--verbose`
:   Be verbose about running pandoc

`--no-auto-clean`
:   Automatically clean up any generated files. Otherwise templates/toc.txt will be created.

`--separate-toc`
:   Put the Table of Contents on a separate page, instead of at the end of the index page.

`-O option=value`

:   Override an option value. Options are shown below.

## Options

Options define the conventional defaults used for generating output.  You can override any value with `-O option=value` on the commandline.

tocFileName
:    File name expected/generated as table-of-contents file. Default: `toc`

chapterTitle
:    Localized text to use for a chapter title. Default: `Chapter`

pageFileName
:    Template for generated filename for each page, variables: `${index}`,`${id}`. Default: `${id}`

cssFileName
:    File name of the css file to link in the HTML header. Default: `style.css`

tocTitle
:    Localized text to use for table of contents title. Default: `Table of Contents`

indexFileName
:    File name base expected as index markdown file. Default: `(index|readme)`

indexFileOutputName
:    File name base used as index HTML file. Default: `index`

chapterNumbers
:    True/false, use chapter numbers in navigation. Default: `true`

