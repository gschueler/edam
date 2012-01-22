.PHONY: test

docs/index.html: docs/usage.md readme.md
	./edam.groovy -o html -r -O 'recurseDirPattern=docs'

docs/usage.md: edam.groovy
	./edam.groovy -h > docs/usage.md

test:
	$(MAKE) -C test
