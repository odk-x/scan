CORE_SRCS := $(wildcard src/*.cpp)
CORE_HEADERS := $(wildcard src/*.h)
CORE_OBJS := ${CORE_SRCS:.cpp=.o}

JSON_PARSER_SRCS := $(wildcard jsoncpp-src-0.5.0/src/lib_json/*.cpp)
JSON_PARSER_OBJS := ${JSON_PARSER_SRCS:.cpp=.o}

ZXING_SRCS :=\
$(wildcard zxing/core/src/zxing/*.cpp)\
$(wildcard zxing/core/src/zxing/*/*.cpp)\
$(wildcard zxing/core/src/zxing/*/*/*.cpp)\
$(wildcard zxing/core/src/zxing/*/*/*/*.cpp)\
$(wildcard zxing/cli/src/*.cpp)

ZXING_OBJS := ${ZXING_SRCS:.cpp=.o}
BIGINT_SRCS := $(wildcard zxing/core/src/bigint/*.cc)
BIGINT_OBJS := ${BIGINT_SRCS:.cc=.o}

ALL_SRCS := $(CORE_SRCS) StatCollector.cpp
ALL_HEADERS := $(CORE_HEADERS) StatCollector.h configuration.h
ALL_OBJS := $(CORE_OBJS) StatCollector.o $(JSON_PARSER_OBJS) $(BIGINT_OBJS) $(ZXING_OBJS)

#You can use debug mode by cleaning then remaking
#with a DEBUG=Something command-line parameter
ifdef DEBUG
CFLAGS := \
-D DEBUG_PROCESSOR\
-D DEBUG_ALIGN_IMAGE\
-D SEGMENT_OUTPUT_DIRECTORY="debug_segment_images/"\
-D TRAINING_IMAGE_ROOT="training_examples"\
-D DEBUG_CLASSIFIER\
-D OUTPUT_BUBBLE_IMAGES
#-D ALWAYS_COMPUTE_TEMPLATE_FEATURES
endif
ifndef DEBUG
CFLAGS := \
-D DEBUG_PROCESSOR\
-D DEBUG_ALIGN_IMAGE
endif

#The OPENCV_INCLUDES will probably need to be adjusted if you aren't running this on a linux system.
#Even then, pkg-config might not be set up.
#Also, you might need to add this to your .bashrc
#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib
OPENCV_INCLUDES := `pkg-config opencv --cflags --libs`

INCLUDES := $(OPENCV_INCLUDES)\
-I./jsoncpp-src-0.5.0/include\
-I./src -I./\
-I./zxing/core/src -I./zxing/cli/src

#Only include boost for experiments so it doesn't always have to be installed.
Experiment MozExperiment: INCLUDES += -lboost_filesystem -lboost_system

ODKScan: ODKScan.run
	@echo "Made executable ODKScan.run"

#The Experiment target is for running the scan pipeline on a bunch of images
#then reporting the results.
#Example call:
#make Experiment TEMPLATE=assets/form_templates/checkbox_test_form INPUT_FOLDER=example_input/checkbox_test
#Notes:
#-Requires boost (to install on ubuntu: sudo apt-get install libboost-all-dev)
ifndef $(INPUT_FOLDER)
INPUT_FOLDER := example_input/scanExample
endif
ifndef $(OUTPUT_FOLDER)
OUTPUT_FOLDER := output
endif
ifndef $(TEMPLATE)
TEMPLATE := assets/form_templates/scanExample
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

#When debugging, it might be preferable to run the executable with gdb:
#gdb --args $< $(TEMPLATE) $(INPUT_FOLDER) $(OUTPUT_FOLDER) $(EXPECTED_JSON)

#Run the scan pipeline on the data collected in Mozambique
#Note: this data is not included in the repository.
MozExperiment: tests/Experiment2.run
	./$< assets/form_templates/moz_revised tests/MozExperiment tests/MozExperiment_out

#A test for zxing compilation
zxing: zxing/cli/main.run
	./$< 

#does linking
%.run: %.cpp $(ALL_SRCS) $(ALL_OBJS) $(ALL_HEADERS)
	g++ -g -o $@ $< $(ALL_OBJS) $(INCLUDES) $(CFLAGS)

#does compiling
.PRECIOUS: %.o
%.o: %.cpp $(ALL_HEADERS)
	@echo $(CFLAGS)
	g++ -Wall -c $< -o $@ $(INCLUDES) $(CFLAGS)

.IGNORE: clean
clean:
	rm $(ALL_OBJS)
	rm zxing/cli/main.run
	rm tests/*.run
	rm *.run

#Some helpful sources I used to make this makefile:
#http://stackoverflow.com/questions/5799820/makefile-and-c-project
#http://www.gnu.org/s/hello/manual/make/Special-Targets.html
#http://www.gnu.org/s/hello/manual/make/Conditional-Syntax.html#Conditional-Syntax
#http://en.wikipedia.org/wiki/Makefile
#http://stackoverflow.com/questions/2214575/passing-arguments-to-make-run
#http://stackoverflow.com/questions/5302390/is-it-possible-to-define-c-macro-in-makefile
