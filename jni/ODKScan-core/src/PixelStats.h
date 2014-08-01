#ifndef PIXELSTATS_H
#define PIXELSTATS_H

#include <vector>
#include <iostream>

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
    std::vector<std::vector<seg_stats> > stats;
    float normal_pdf(int x, float m, float s);
    seg_stats temp;
public:
    PixelStats() : stats(10,std::vector<seg_stats>(7,temp)) {}
    void print_stats(void) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 7; j++) {
                std::cout << "n: " << stats[i][j].n << ", v: " << stats[i][j].v << ", m: " << stats[i][j].m << std::endl;
            }
        }
    }
    void add_seg(int num, int seg, int px_count);
    float get_prob(int num, int seg, int px_count);
};

#endif // PIXELSTATS_H
