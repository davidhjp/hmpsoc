ifeq ($(WINDIR),)
	S=:
else
	S=\;
endif

ifeq ($(ALLOC),)
	ALLOC_PARAM=
else
	ALLOC_PARAM=-j $(ALLOC)
endif

ifeq ($(CFG),)
	hmpsoc_cfg=
else
	hmpsoc_cfg=-c $(CFG)
endif


.PHONY: bin jar

all: bin

compileagrc:
	java -cp bin$(S)lib/\* JavaPrettyPrinter --ir $(T)

compile:
	make compileagrc -s T=$(T) | java -cp bin/$(S)lib/\* HMPSoC -v $(ALLOC_PARAM) $(hmpsoc_cfg)

bin:
	if [[ ! -d bin ]]; then \
		mkdir bin; \
	fi
	javac -cp lib/\*$(S)src/ src/HMPSoC.java -d bin

jar: bin
	if [[ ! -d jar ]]; then \
		mkdir jar; \
	fi
	jar -cvfm hmpsoc.jar manifest/manifest -C bin .
	mv hmpsoc.jar jar
	cp lib/jdom.jar lib/commons-cli-1.3.jar lib/gson-2.3.1.jar jar
	echo -e '#!/bin/bash\njava -jar $$(dirname $$0)/hmpsoc.jar $$@'> jar/hmpsoc
	chmod u+x jar/hmpsoc

clean:
	rm -rfv bin
	rm -rfv jar
	rm -rfv hmpsoc

