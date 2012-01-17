% Usage
% Greg Schueler
% 1/16/2012

Usage is simple:

    edam.groovy [-h/--help] -d <basedir> [--css <cssfilename>] [-t <templatesdir>] [-o <outputdir>] [--no-toc] [--no-nav] [--verbose] [--clean] [--separate-toc]

## Arguments

`-d <basedir>`

:   The base directory containing the docs to convert. Required.

`--css <cssfilename>`

:    The name of the css file to include in the HTML header. Default: 'style.css'

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

`--clean`

:   Automatically clean up any generated files. Otherwise templates/toc.txt will be created.

`--separate-toc`

:   Put the Table of Contents on a separate page, instead of at the end of the index page.