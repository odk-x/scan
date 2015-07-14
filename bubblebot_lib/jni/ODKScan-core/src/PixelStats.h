#ifndef PIXELSTATS_H
#define PIXELSTATS_H

#include <vector>
#include <string>
#include <sstream>
#include <unistd.h>
#include "configuration.h"

typedef struct seg_stats_st {
    int n;
    float m;
    float v;
} seg_stats;

/* This class stores ten different sets of seven mean/variance statistics; one for each
 * segment of each number. The statistics are updated by providing a pixel count along
 * with a number and segment number */
class PixelStats
{
protected:
    std::vector< std::vector<seg_stats> > stats;
    float normal_pdf(int x, float m, float s);
    //seg_stats temp;
public:
    PixelStats(){

    	stats = std::vector< std::vector<seg_stats> >();
    	for(int i = 0; i < 10; i++){

    		seg_stats tmp;
    		tmp.n = i;
    		tmp.m = 0;
    		tmp.v = 0;
    		std::vector<seg_stats> tmp_vector =
    		  std::vector<seg_stats>();
    		for(int j = 0; j < 7; j++){
    		    tmp_vector.push_back(tmp);
    		}
    		stats.push_back(tmp_vector);

    	}

    }

    void print_stats(void)
    {
    	std::ostringstream ss;
    	for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 7; j++) {
            	ss << "i: " << i << " j: " << j << " n: " << stats[i][j].n << ", v: " << stats[i][j].v << ", m: " << stats[i][j].m << "\n";
            }
        }
        std::string str = ss.str();
        const char * c = str.c_str();
        LOGI("%s", c);
        usleep(1000000);
    }

    void add_seg(int num, int seg, int px_count);
    float get_prob(int num, int seg, int px_count);
};

#endif // PIXELSTATS_H
