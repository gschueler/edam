% Usage

    edam.groovy [-h/--help] [-d <basedir>] [-t <templatesdir>] [-o <outputdir>]
        [-r] [--no-toc] [--no-nav] [--verbose] [--clean] [--separate-toc]
        [-O option=value [ -O ... ] ] [-V var=value ...] [ --variables <propertiesfile> ]
        [-x [ extra pandoc args .. ] ]

## Arguments


`-h/--help`
:    Show this help text

`-d <basedir>`
:    The base directory containing the docs to convert. Defaults to the current directory.

`-t <templatesdir>`
:    The directory containing templates. Defaults to basedir/templates.

`-o <outputdir>`
:    The directory to write HTML files to. Defaults to the basedir.

`-r`
:    Recursively descend to subdirectories and apply Edam

`--no-toc`
:    Don't include the Table of Contents.

`--no-nav`
:    Don't include navigation links on each page.

`--verbose`
:    Be verbose about running pandoc.

`--no-auto-clean`
:    Automatically clean up any generated files. Otherwise templates/toc.txt will be created.

`--separate-toc`
:    Put the Table of Contents on a separate page, instead of at the end of the index page.

`-O option=value`
:    Override an option value. Options are shown below.

`-V var=value`
:    Define a variable to expand within templates and markdown content.

`--variables <propertiesfile>`
:    Load variables from a properties file.

## Options

Options define the conventional defaults used for generating output.  You can override any value with `-O option=value` on the commandline.

`singleIndex`
:    true/false, if only a single markdown file, use it as the index HTML file. Default: `true`

`chapterNumbers`
:    true/false, use chapter numbers in navigation. Default: `true`

`chapterTitle`
:    Localized text to use for a Chapter title. Default: `Chapter`

`sectionTitle`
:    Localized text to use for a Section title Default: `Section`

`pageFileName`
:    Template for generated filename for each page, variables: `${index}`,`${title}`,`${name}`. Default: `${name}`

`cssFileName`
:    File name of the css file to link in the HTML header. Default: `style.css`

`indexFileName`
:    File name base expected as index markdown file. Default: `(index|readme)`

`indexFileOutputName`
:    File name base used as index HTML file. Default: `index`

`tocFileName`
:    File name expected/generated as table-of-contents file. Default: `toc`

`tocTitle`
:    Localized text to use for table of contents title. Default: `Table of Contents`

`subTocTitle`
:    Localized text to use for subdirectory sections table of contents title. Default: `Sections`

`pandocCmd`
:    Pandoc command to run. Default: `pandoc`

`tokenStart`
:    Variable expansion start token. Default: `${`

`tokenEnd`
:    Variable expansion end token. Default: `}`

`singleOutputFile`
:    true/false, combine all files into one output file. Default: `false`

`pageLinkTitle`
:    Template for title for links to the page, variables: `${index}`,`${title}`,`${name}`. Default: `${title}`

`subLinkTitle`
:    Template for title for links to a sub dir section, variables: `${sectionTitle}`, `${title}`, `${index}`, `${name}` Default: `${title}`

`recurseDirPattern`
:    Regex to match dirs to include in recursive generation. Default: `.*`

`recurseDirPatternIgnore`
:    Regex to match dirs to ignore in recursive generation. Default: `^\..*$`

`recurseDepth`
:    Number of dirs to recurse into. -1 means no limit. Default: `0`

