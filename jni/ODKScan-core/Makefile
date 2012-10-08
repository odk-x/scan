CORE_SRCS := $(wildcard src/*.cpp)
CORE_HEADERS := $(wildcard src/*.h)
CORE_OBJS := ${CORE_SRCS:.cpp=.o}

JSON_PARSER_SRCS := $(wildcard jsoncpp-src-0.5.0/src/lib_json/*.cpp)
JSON_PARSER_OBJS := ${JSON_PARSER_SRCS:.cpp=.o}

ALL_SRCS := $(CORE_SRCS) StatCollector.cpp
ALL_HEADERS := $(CORE_HEADERS) StatCollector.h configuration.h
ALL_OBJS := $(CORE_OBJS) StatCollector.o $(JSON_PARSER_OBJS)

#The OPENCV_INCLUDES will probably need to be adjusted if you aren't running this on a linux system.
#Even then, pkg-config might not be set up.
#Also, you might need to add this to your .bashrc
#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib
OPENCV_INCLUDES := `pkg-config opencv --cflags --libs`

#Simplify?
INCLUDES := $(OPENCV_INCLUDES) -I./jsoncpp-src-0.5.0/include -I./src -I./

ODKScan: ODKScan.run
	@echo "Made executable ODKScan.run"

#Example call:
#make Experiment TEMPLATE=assets/form_templates/checkbox_test_form INPUT_FOLDER=example_input/checkbox_test
ifndef $(INPUT_FOLDER)
INPUT_FOLDER := example_input
endif
ifndef $(OUTPUT_FOLDER)
OUTPUT_FOLDER := output
endif
ifndef $(TEMPLATE)
TEMPLATE := assets/form_templates/example
endif
ifndef $(EXPECTED_JSON)
EXPECTED_JSON := $(INPUT_FOLDER)/output.json
endif
Experiment: tests/Experiment.run
	@rm -rf $(OUTPUT_FOLDER)
	@mkdir $(OUTPUT_FOLDER)
	@rm -rf debug_segment_images
	@mkdir debug_segment_images
	@rm -rf bubble_images
	@mkdir bubble_images
	./$< $(TEMPLATE) $(INPUT_FOLDER) $(OUTPUT_FOLDER) $(EXPECTED_JSON)

#does linking
%.run: %.cpp $(ALL_SRCS) $(ALL_OBJS) $(ALL_HEADERS)
	g++ -g -o $@ $< $(ALL_OBJS) $(INCLUDES)

#does compiling
.PRECIOUS: %.o
%.o: %.cpp $(ALL_HEADERS)
	g++ -Wall -c $< -o $@ $(INCLUDES)

.IGNORE: clean
clean:
	rm -rf debug_segment_images
	rm $(ALL_OBJS)
	rm tests/*.run
	rm *.run

#Some helpful sources I used to make this makefile:
#http://stackoverflow.com/questions/5799820/makefile-and-c-project
#http://www.gnu.org/s/hello/manual/make/Special-Targets.html
#http://www.gnu.org/s/hello/manual/make/Conditional-Syntax.html#Conditional-Syntax
#http://en.wikipedia.org/wiki/Makefile
#http://stackoverflow.com/questions/2214575/passing-arguments-to-make-run
