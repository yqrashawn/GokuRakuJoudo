all:
	$(MAKE) compile
	$(MAKE) bin

compile:
	lein compile && lein uberjar

bin:
	sh graavl-compile.sh