.PHONY: test

docs: usage.md readme.md
	./edam.groovy -o docs

usage.md: edam.groovy
	./edam.groovy -h > usage.md

test:
	$(MAKE) -C test
