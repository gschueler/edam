
EDAM=../edam.groovy
DIRS=$(shell find ./ -d 1 -type d -name "test-*")

all: 
	@for i in $(DIRS) ; do \
		echo "Test dir $$i" ; \
		mkdir $$i/output; \
		groovy $(EDAM) -d $$i -o $$i/output; \
		diff -q -r $$i/output $$i/expected || exit 2; \
	done
	@-mkdir -p var-test/output
	groovy $(EDAM) -d var-test -o var-test/output --variables var-test/vars.properties
	diff -q -r var-test/output var-test/expected || exit 2
	@-mkdir -p singleoutput-test/output
	groovy $(EDAM) -d singleoutput-test -o singleoutput-test/output -O 'singleOutputFile=true' --verbose
	diff -q -r singleoutput-test/output singleoutput-test/expected || exit 2
	@-mkdir -p recurse-test/output
	groovy $(EDAM) -d recurse-test -o recurse-test/output -r -O 'recurseDirPattern=sub.*'
	diff -q -r recurse-test/output recurse-test/expected || exit 2
	@-mkdir -p recurse-multi-test/output
	groovy $(EDAM) -d recurse-multi-test -o recurse-multi-test/output -r -O 'recurseDirPattern=sub.*'
	diff -q -r recurse-multi-test/output recurse-multi-test/expected || exit 2

clean:
	@for i in $(DIRS) var-test singleoutput-test recurse-test recurse-multi-test ; do \
		echo "Clean Test dir $$i" ; \
		rm -rf $$i/*.html $$i/templates $$i/output ; \
	done
