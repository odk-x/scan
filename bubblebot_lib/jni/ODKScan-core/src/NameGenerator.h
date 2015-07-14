#ifndef NAME_GENERATOR
#define NAME_GENERATOR
#include <iostream>
#include <string>
#include <sys/stat.h>
/*
NameGenerate is a object for generating unique filenames.
It is mainly used to output debug images.
*/
class NameGenerator {
		int unique_name_counter;
		std::string initial_prefix;
	public:
		NameGenerator():unique_name_counter(0){}
		NameGenerator(std::string prefix, bool createDir){
			unique_name_counter = 0;
			initial_prefix = prefix;
			if(createDir){
				// I have no idea what these mode flags do...
				mkdir(prefix.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
			}
		}
		void setPrefix(std::string prefix){
			initial_prefix = prefix;
		}
		std::string intToString(int gc_temp) const{
			std::stringstream ss;
			while( true ) {
				ss << (char) ((gc_temp % 10) + '0');
				gc_temp = gc_temp / 10;
				if(gc_temp == 0) break;
			}
			std::string temp = ss.str();
			reverse(temp.begin(), temp.end());
			return temp;
		}
		//Returns prefix with a number concatinated to the end.
		//Each call increments the number so that the resulting string will always be unique.
		std::string get_unique_name(const std::string& prefix) {
			std::string stringIdx = intToString(unique_name_counter);
			unique_name_counter++;
			return initial_prefix + prefix + stringIdx;
		}
};
#endif
