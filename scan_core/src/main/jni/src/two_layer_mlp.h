#include <dirent.h>
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include <stdlib.h>
#include <stdio.h>
#include <iostream>
#include <vector>
#include <cmath>
#include <string>
#include <limits>
#include <time.h>
#include <fstream>

#include "digitfeatures.h"

using namespace std;

/* --- MLP FUNCTIONS --- */
void sigmoid(vector<double>& z, vector<double>& sigm);
double sigmoid(int x);
void softmax(vector<double>& z, vector<double>& softmax);
vector<vector<double> > encode_targets(vector<int>& targets, int num_classes);

int mlp_two_layer_predict_class(vector<double>& input, vector<vector<double> >& W, vector<vector<double> >& V);
