% Edam - easy doc generation with pandoc
% Greg Schueler
% 1/16/2012

Edam is a simple [pandoc] wrapper to help generate documentation written in Groovy.  It is completely inspired by [Gouda][], which is a python script.

[pandoc]: http://johnmacfarlane.net/pandoc/
[Gouda]: http://www.unexpected-vortices.com/sw/gouda/docs/

See an example of it in use in the docs generated for this project.

All of the docs in the docs/ dir were generated using this command:

    edam.groovy -d `pwd` -o docs -O 'indexFileName=readme' --clean