#include "PixelStats.h"

#include <cmath>
#include <iostream>

/* Inserts a new data point into the stored state. It has to recalculate
 * the mean and variance whenever this happens */
void PixelStats::add_seg(int num, int seg, int px_count) {
    seg_stats seg_st = stats[num][seg];
    float new_m = seg_st.m + (px_count - seg_st.m) / (seg_st.n + 1);
    float new_v = seg_st.v + (px_count - seg_st.m) * (px_count - new_m);
    stats[num][seg].m = new_m;
    stats[num][seg].v = new_v;
    stats[num][seg].n++;
}

/* Computes the probability that a given pixel count for a segment was
 * found in an image of a particular number num */
float PixelStats::get_prob(int num, int seg, int px_count) {
    seg_stats seg_st = stats[num][seg];
    float variance = seg_st.v / (seg_st.n - 1);
    float s = sqrt(variance);
    float mu = seg_st.m;
    return normal_pdf(1.0 * px_count, mu, s);
}

/* Computes the value of f(x), where f() is the pdf of
 * a normal distribution with mean mu and s.d. s */
float PixelStats::normal_pdf(int x, float mu, float s) {
    static const float inv_sqrt_2pi = 0.3989422804014327;
    float z = (x - mu) / s;
    return (inv_sqrt_2pi / s) * std::exp(-0.5f * z * z);
}


