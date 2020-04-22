.PHONY: build

all: build

build: # @HELP build the source code
build: package

package: # @HELP ensure that the required dependencies are in place
	mvn package


