#include "digitfeatures.h"

void set_num_classes(int num_classes)
{
	NUM_CLASSES = num_classes;
}
void set_extraction_alg(int extraction_alg)
{
	EXTRACTION_ALG = extraction_alg;
}

/*
void get_data_set(string directory, vector<vector<double> >& features_training, vector<int>& targets_training, vector<vector<double> >& encoded_targets_training,
			vector<vector<double> >& features_validation, vector<int>& targets_validation, vector<vector<double> >& encoded_targets_validation,
			vector<vector<double> >& features_testing, vector<int>& targets_testing, vector<vector<double> >& encoded_targets_testing,
			vector<Mat>& images_training, vector<Mat>& images_validation, vector<Mat>& images_testing,
			vector<string>& image_names_training, vector<string>& image_names_validation, vector<string>& image_names_testing)
{
	vector<Mat> binary_images;
	vector<Mat> gray_images;
	vector<int> targets;
	vector<string> image_names;
	get_processed_images(binary_images, gray_images, targets, directory, image_names);

	vector<vector<double> > features;
	if(EXTRACTION_ALG == STRUCTURAL_CHARS)
	{
		get_structural_characteristics_data_set(binary_images, features);
	}
	else if(EXTRACTION_ALG == GRADIENT_DIR)
	{
		get_gradient_directional_data_set(gray_images, features);
	}
	vector<vector<double> > encoded_targets = encode_targets(targets, NUM_CLASSES);


	vector<vector<double> > pruned_features;
	vector<int> pruned_targets;
	vector<vector<double> > pruned_encoded_targets;
	vector<Mat> pruned_binary_images;
	vector<string> pruned_image_names;

	prune_error_samples(features, encoded_targets, targets, pruned_features, pruned_encoded_targets, pruned_targets, binary_images, pruned_binary_images, image_names, pruned_image_names);

	split_data_set(pruned_features, pruned_targets, pruned_encoded_targets, pruned_binary_images, pruned_image_names,
		features_training, targets_training, encoded_targets_training,
		features_validation, targets_validation, encoded_targets_validation,
		features_testing, targets_testing, encoded_targets_testing,
		images_training, images_validation, images_testing,
		image_names_training, image_names_validation, image_names_testing);

}
*/

void get_data(Mat src, vector<double>& features)
{
	Mat binary_image = binary_processed_image(src);
//	Mat gray_image = gray_processed_image(src.clone());

	features.resize(0);
	if(EXTRACTION_ALG == STRUCTURAL_CHARS)
	{
		features.resize(280, 0);

		structural_characteristics(binary_image, features);
	}
//	else if(EXTRACTION_ALG == GRADIENT_DIR)
//	{
//		features.resize(128, 0);
//
//		gradient_directional(gray_image, features);
//	}
}

void split_data_set(vector<vector<double> >& features, vector<int>& targets, vector<vector<double> >& encoded_targets, vector<Mat>& images, vector<string>& image_names,
			vector<vector<double> >& features_training, vector<int>& targets_training, vector<vector<double> >& encoded_targets_training,
			vector<vector<double> >& features_validation, vector<int>& targets_validation, vector<vector<double> >& encoded_targets_validation,
			vector<vector<double> >& features_testing, vector<int>& targets_testing, vector<vector<double> >& encoded_targets_testing,
			vector<Mat>& images_training, vector<Mat>& images_validation, vector<Mat>& images_testing,
			vector<string>& image_names_training, vector<string>& image_names_validation, vector<string>& image_names_testing)
{
	vector<int> indexes(features.size(), 0);
	for(int i = 0; i < features.size(); i++)
	{
		indexes[i] = i;
	}
	random_shuffle(indexes.begin(), indexes.end());

	int total_prop = TRAINING_SET_PROP + VALIDATION_SET_PROP + TEST_SET_PROP;

	int num_training_samples = (((double) TRAINING_SET_PROP) / ((double) total_prop)) * ((double) features.size());
	int num_validation_samples = (((double) VALIDATION_SET_PROP) / ((double) total_prop)) * ((double) features.size());
	int num_test_samples = features.size() - num_training_samples - num_validation_samples;

	int i = 0;
	for(; i < num_training_samples; i++)
	{
		features_training.push_back(features[indexes[i]]);
		targets_training.push_back(targets[indexes[i]]);
		encoded_targets_training.push_back(encoded_targets[indexes[i]]);
		images_training.push_back(images[indexes[i]]);
		image_names_training.push_back(image_names[indexes[i]]);
	}
	for(; i < num_training_samples + num_validation_samples; i++)
	{
		features_validation.push_back(features[indexes[i]]);
		targets_validation.push_back(targets[indexes[i]]);
		encoded_targets_validation.push_back(encoded_targets[indexes[i]]);
		images_validation.push_back(images[indexes[i]]);
		image_names_validation.push_back(image_names[indexes[i]]);
	}
	for(; i < num_training_samples + num_validation_samples + num_test_samples; i++)
	{
		features_testing.push_back(features[indexes[i]]);
		targets_testing.push_back(targets[indexes[i]]);
		encoded_targets_testing.push_back(encoded_targets[indexes[i]]);
		images_testing.push_back(images[indexes[i]]);
		image_names_testing.push_back(image_names[indexes[i]]);
	}

	cout << "Total data set size: " << features.size() << endl;
	cout << "Training set size: " << num_training_samples << endl;
	cout << "Validation set size: " << num_validation_samples << endl;
	cout << "Test set size: " << num_test_samples << endl;
}

void prune_error_samples(vector<vector<double> >& features, vector<vector<double> >& encoded_targets, vector<int>& targets, vector<vector<double> >& pruned_features, vector<vector<double> >& pruned_encoded_targets, vector<int>& pruned_targets, vector<Mat>& images, vector<Mat>& pruned_images, vector<string>& image_names, vector<string>& pruned_image_names)
{
	int number_of_pruned_samples = 0;
	for(int i = 0; i < features.size(); i++)
	{
		bool error_sample = false;
		for(int j = 0; j < features[i].size(); j++)
		{
			if(isnan(features[i][j]) || isinf(features[i][j]))
			{
				error_sample = true;
			}
		}
		if(!error_sample)
		{
			pruned_features.push_back(features[i]);
			pruned_encoded_targets.push_back(encoded_targets[i]);
			pruned_targets.push_back(targets[i]);
			pruned_images.push_back(images[i]);
			pruned_image_names.push_back(image_names[i]);
		}
		else
		{
			number_of_pruned_samples++;
		}
	}
	cout << "Number of faulty feature vectors pruned: " << number_of_pruned_samples << endl;
}

vector<vector<double> > encode_targets(vector<int>& targets, int num_classes)
{
	vector<vector<double> > encoded_targets(targets.size(), vector<double>(num_classes, 0));
	for(int i = 0; i < targets.size(); i++)
	{
		int target_class = targets[i];
		encoded_targets[i][target_class] = 1.f;
	}
	return encoded_targets;
}

/*void get_processed_images(vector<Mat>& binary_images, vector<Mat>& gray_images, vector<int>& targets, string directory, vector<string>& files)
{
	//namedWindow("binary_test", WINDOW_NORMAL);
	//namedWindow("original_test", WINDOW_NORMAL);

	DIR * dpdf;
	struct dirent * epdf;

	dpdf = opendir(("./" + directory).c_str());
	//vector<string> files;

	if(dpdf != NULL)
	{
		while(epdf = readdir(dpdf))
		{
			char *dot = strrchr(epdf->d_name, '.');
			if (dot && !strcmp(dot, ".jpg"))
			{
				files.push_back(epdf->d_name);
			}
		}
	}

	//cout << "Images read from data set:" << endl;

	for(int file_index = 0; file_index < files.size(); file_index++)
	{
		//cout << directory + "/" + files[file_index] << endl;
		Mat src = imread(directory + "/" + files[file_index], 1);
		Mat binary = binary_processed_image(src.clone());
		Mat gray = gray_processed_image(src.clone());

		*//*if(!strcmp(files[file_index].c_str(), "4-567.jpg"))
		{
			imshow("original_test", src);
			imshow("binary_test", binary);
			waitKey(0);
		}*//*

		char * filename = new char[files[file_index].size() + 1];
		copy(files[file_index].begin(), files[file_index].end(), filename);
		filename[files[file_index].size()] = '\0';

		string target_string = strtok(filename, "-");

		if(!strcmp(target_string.c_str(), "empty"))
		{
			binary_images.push_back(binary.clone());
			gray_images.push_back(gray.clone());
			targets.push_back(NUM_CLASSES - 1);
		}
		else if(atoi(target_string.c_str()) < NUM_CLASSES - 1 )
		{
			binary_images.push_back(binary.clone());
			gray_images.push_back(gray.clone());
			int target = atoi(target_string.c_str());
			targets.push_back(target);
		}
	}
	cout << "Data set size: " << targets.size() << endl;
}*/

void get_structural_characteristics_data_set(vector<Mat>& binary_images, vector<vector<double> >& features)
{
	cout << "Constructing structural characteristics vectors for image set..." << endl;
	for(int i = 0; i < binary_images.size(); i++)
	{
		vector<double> feature_vector(280, 0);
		structural_characteristics(binary_images[i], feature_vector);
		features.push_back(feature_vector);
	}
}

void get_gradient_directional_data_set(vector<Mat>& gray_images, vector<vector<double> >& features)
{
	cout << "Constructing gradient directional vectors for image set..." << endl;
	for(int i = 0; i < gray_images.size(); i++)
	{
		vector<double> feature_vector(128, 0);
		gradient_directional(gray_images[i], feature_vector);
		features.push_back(feature_vector);
	}
}

void gradient_directional(Mat gray_unscaled, vector<double>& feature_vector)
{
	double grad_thresh = 50.f;
	double direction_quant = ((double) M_PI) / ((double) 4);

	Mat gray_image;
	resize(gray_unscaled, gray_image, Size(80, 120), 0, 0, INTER_LINEAR);
	gray_image.convertTo(gray_image, IPL_DEPTH_16S);//CV_16SC1

	Mat map_x;
	Mat map_y;
	Sobel(gray_image, map_x, -1, 1, 0, 3);
	Sobel(gray_image, map_y, -1, 0, 1, 3);

	vector<vector<vector<double> > > gradient_directional(4, vector<vector<double> >(4, vector<double>(8, 0)));

	vector<vector<double> > normalizers(4, vector<double>(4, 0));
	for(int i = 0; i < gray_image.rows; i++)
	{
		for(int j = 0; j < gray_image.cols; j++)
		{
			int grad_x = (int) map_x.at<short>(i, j);
			int grad_y = (int) map_y.at<short>(i, j);

			if(sqrt(pow(grad_x, 2) + pow(grad_y, 2)) > grad_thresh)
			{
				normalizers[i / 30][j / 20] += 1;
			}
		}
	}

	for(int i = 0; i < gray_image.rows; i++)
	{
		for(int j = 0; j < gray_image.cols; j++)
		{
			int grad_x = (int) map_x.at<short>(i, j);
			int grad_y = (int) map_y.at<short>(i, j);

			if(sqrt(pow(grad_x, 2) + pow(grad_y, 2)) > grad_thresh)
			{
				double direction = atan2(grad_y, grad_x);

				if(direction > -4 * direction_quant && direction <= -3 * direction_quant)
				{
					gradient_directional[i / 30][j / 20][0] += 1.f / normalizers[i / 30][j / 20];
				}
				else if(direction > -3 * direction_quant && direction <= -2 * direction_quant)
				{
					gradient_directional[i / 30][j / 20][1] += 1.f / normalizers[i / 30][j / 20];
				}
				else if(direction > -2 * direction_quant && direction <= -1 * direction_quant)
				{
					gradient_directional[i / 30][j / 20][2] += 1.f / normalizers[i / 30][j / 20];
				}
				else if(direction > -1 * direction_quant && direction <= 0)
				{
					gradient_directional[i / 30][j / 20][3] += 1.f / normalizers[i / 30][j / 20];
				}

				else if(direction > 0 && direction <= direction_quant)
				{
					gradient_directional[i / 30][j / 20][4] += 1.f / normalizers[i / 30][j / 20];
				}
				else if(direction > direction_quant && direction <= 2 * direction_quant)
				{
					gradient_directional[i / 30][j / 20][5] += 1.f / normalizers[i / 30][j / 20];
				}
				else if(direction > 2 * direction_quant && direction <= 3 * direction_quant)
				{
					gradient_directional[i / 30][j / 20][6] += 1.f / normalizers[i / 30][j / 20];
				}
				else if(direction > 3 * direction_quant && direction <= 4 * direction_quant)
				{
					gradient_directional[i / 30][j / 20][7] += 1.f / normalizers[i / 30][j / 20];
				}
			}
		}
	}

	int feature_index = 0;

	for(int i = 0; i < 4; i++)
	{
		for(int j = 0; j < 4; j++)
		{
			for(int k = 0; k < 8; k++, feature_index++)
			{
				feature_vector[feature_index] = gradient_directional[i][j][k];
			}
		}
	}
}

void structural_characteristics(Mat binary_unscaled, vector<double>& feature_vector)
{
	Mat binary_image;
	resize(binary_unscaled, binary_image, Size(32, 32), 0, 0, INTER_NEAREST);

	vector<double> horizontal_histogram(binary_image.rows, 0);
	vector<double> vertical_histogram(binary_image.cols, 0);

	double normalizer = 0;
	for(int i = 0; i < binary_image.rows; i++)
	{
		for(int j = 0; j < binary_image.cols; j++)
		{
			if((int) binary_image.at<uchar>(i, j) == 0)
			{
				normalizer++;
			}
		}
	}
	if(normalizer <= 0)
	{
		return;
	}

	//Horizontal histogram.
	for(int i = 0; i < binary_image.rows; i++)
	{
		double row_sum = 0;
		for(int j = 0; j < binary_image.cols; j++)
		{
			if((int) binary_image.at<uchar>(i, j) == 0)
			{
				row_sum++;
			}
		}
		horizontal_histogram[i] = row_sum / normalizer;
	}

	//Vertical histogram.
	for(int j = 0; j < binary_image.cols; j++)
	{
		double col_sum = 0;
		for(int i = 0; i < binary_image.rows; i++)
		{
			if((int) binary_image.at<uchar>(i, j) == 0)
			{
				col_sum++;
			}
		}
		vertical_histogram[j] = col_sum / normalizer;
	}

	int angle_step = 5;
	vector<double> radial_histogram(72, 0);
	
	//Radial histogram.
	for(int k = 0; k < 72; k++)
	{
		double angle = angle_step * k;

		double radial_sum = 0;
		for(int i = 0; i < binary_image.rows / 2; i++)
		{
			int row = (binary_image.rows / 2) - ((double) i) * sin(angle);
			int col = (binary_image.rows / 2) + ((double) i) * cos(angle);
			if((int) binary_image.at<uchar>(row, col) == 0)
			{
				radial_sum++;
			}
		}
		radial_histogram[k] = radial_sum / normalizer;
	}

	vector<double> out_in_radial(72, 0);

	for(int k = 0; k < 72; k++)
	{
		double angle = angle_step * k;

		double radial_index = 0;
		for(int i = binary_image.rows / 2 - 1; i >= 0; i--)
		{
			int row = (binary_image.rows / 2) - ((double) i) * sin(angle);
			int col = (binary_image.rows / 2) + ((double) i) * cos(angle);
			if((int) binary_image.at<uchar>(row, col) == 0)
			{
				radial_index = i;
				break;
			}
		}
		out_in_radial[k] = radial_index / ((double) binary_image.rows / 2);
	}

	vector<double> in_out_radial(72, 0);

	for(int k = 0; k < 72; k++)
	{
		double angle = angle_step * k;

		double radial_index = 0;
		for(int i = 0; i < binary_image.rows / 2 - 1; i++)
		{
			int row = (binary_image.rows / 2) - ((double) i) * sin(angle);
			int col = (binary_image.rows / 2) + ((double) i) * cos(angle);
			if((int) binary_image.at<uchar>(row, col) == 0)
			{
				radial_index = i;
				break;
			}
		}
		in_out_radial[k] = radial_index / ((double) binary_image.rows / 2);
	}

	copy(horizontal_histogram.begin(), horizontal_histogram.end(), feature_vector.begin());
	copy(vertical_histogram.begin(), vertical_histogram.end(), feature_vector.begin() + horizontal_histogram.size());
	copy(radial_histogram.begin(), radial_histogram.end(), feature_vector.begin() + horizontal_histogram.size() + vertical_histogram.size());
	copy(out_in_radial.begin(), out_in_radial.end(), feature_vector.begin() + horizontal_histogram.size() + vertical_histogram.size() + radial_histogram.size());
	copy(in_out_radial.begin(), in_out_radial.end(), feature_vector.begin() + horizontal_histogram.size() + vertical_histogram.size() + radial_histogram.size() + out_in_radial.size());
	return;
}

Mat binary_processed_image(Mat& src)
{
  Mat dst = src;
	//cvtColor(src, dst, CV_RGB2GRAY, 1);

	threshold(dst, dst, 0, 255, CV_THRESH_BINARY | CV_THRESH_OTSU);

	bitwise_not(dst, dst);

	if(BINARY_REMOVE_DOTS)
	{
		remove_dots(dst, 5);
	}

	Mat cropped;
	if(BINARY_AUTOCROP)
	{
		Rect bound_box = bounding_box(dst);
		dst(bound_box).copyTo(cropped);
	}
	else
	{
		cropped = dst;
	}

	if(BINARY_REMOVE_BORDERS)
	{
		remove_top_border(cropped);
		remove_bottom_border(cropped);
		remove_left_border(cropped);
		remove_right_border(cropped);
		remove_dots(cropped, 3);
	}

	Mat eroded;
	bitwise_not(cropped, eroded);

	if(BINARY_THIN)
	{
//		Mat thinner;
//		thinner = getStructuringElement(MORPH_RECT, Size(7, 7));
//		erode(eroded, eroded, thinner);
//		dilate(eroded, eroded, thinner);
//
//		Mat thinner2;
//		thinner = getStructuringElement(MORPH_RECT, Size(3, 3));
//		dilate(eroded, eroded, thinner2);
//		erode(eroded, eroded, thinner2);
	}

	return eroded;
}

Mat gray_processed_image(Mat src)
{
  Mat dst = src;
//	cvtColor(src, dst, CV_RGB2GRAY, 1);

	for(int i = 0; i < dst.rows; i++)
	{
		for(int j = 0; j < dst.cols; j++)
		{
			dst.at<uchar>(i, j) = (uchar) (255 - (int) dst.at<uchar>(i, j));
		}
	}

	threshold(dst, dst, 0, 255, CV_THRESH_TOZERO | CV_THRESH_OTSU);

	if(GRAY_REMOVE_DOTS)
	{
		remove_dots(dst, 5);
	}

	Mat cropped;
	if(GRAY_AUTOCROP)
	{
		Rect bound_box = bounding_box(dst);
		dst(bound_box).copyTo(cropped);
	}
	else
	{
		cropped = dst;
	}

	if(GRAY_REMOVE_BORDERS)
	{
		remove_top_border(cropped);
		remove_bottom_border(cropped);
		remove_left_border(cropped);
		remove_right_border(cropped);
	}

	Mat eroded = cropped;
	
	for(int i = 0; i < eroded.rows; i++)
	{
		for(int j = 0; j < eroded.cols; j++)
		{
			eroded.at<uchar>(i, j) = (uchar) (255 - (int) eroded.at<uchar>(i, j));
		}
	}

	if(GRAY_THIN)
	{
		Mat thinner;
		thinner = getStructuringElement(MORPH_RECT, Size(7, 7));
		erode(eroded, eroded, thinner);
		dilate(eroded, eroded, thinner);

		Mat thinner2;
		thinner = getStructuringElement(MORPH_RECT, Size(3, 3));
		dilate(eroded, eroded, thinner2);
		erode(eroded, eroded, thinner2);
	}

	return eroded;
}

Rect bounding_box(Mat image)
{
	Mat image_copy = image.clone();

	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;
	
	findContours(image_copy, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0) );

	int max_contour = 0;
	double max_area = 0;
	for(int i = 0; i < contours.size(); i++)
	{
		vector<Point> contour = contours[i];
		Rect bounding_box;
		bounding_box = boundingRect(contour);
		double area = bounding_box.area();//contourArea(contours[i]);

		if(area > max_area)
		{
			max_area = area;
			max_contour = i;
		}
	}

	vector<Point> contour = contours[max_contour];
	
	Rect bounding_box;
	bounding_box = boundingRect(contour);

	return bounding_box;
}

double fraction_remove_stopped(vector<bool> stopped)
{
	double fraction = 0;
	for(int i = 0; i < stopped.size(); i++)
	{
		if(stopped[i])
		{
			fraction += 1;
		}
	}
	return fraction / ((double) stopped.size());
}

void remove_top_border(Mat& image)
{
	vector<bool> started(image.cols, false);
	vector<bool> stopped(image.cols, false);
	double majority_threshold = 0.5;
	for(int i = 0; i < 6/*image.rows - 1*/; i++)
	{
		for(int j = 0; j < image.cols; j++)
		{
			if(stopped[j])
			{
				continue;
			}
			if((int) image.at<uchar>(i, j) != 0)
			{
				started[j] = true;

				image.at<uchar>(i, j) = 0;
			}
			else if(started[j])
			{
				stopped[j] = true;
			}
		}
		double stopped_fraction = fraction_remove_stopped(stopped);
		if(stopped_fraction > majority_threshold)
		{
			return;
		}
	}
}

void remove_bottom_border(Mat& image)
{
	vector<bool> started(image.cols, false);
	vector<bool> stopped(image.cols, false);
	double majority_threshold = 0.5;
	for(int i = image.rows - 1; i >= image.rows - 6/*0*/; i--)
	{
		for(int j = 0; j < image.cols; j++)
		{
			if(stopped[j])
			{
				continue;
			}
			if((int) image.at<uchar>(i, j) != 0)
			{
				started[j] = true;

				image.at<uchar>(i, j) = 0;
			}
			else if(started[j])
			{
				stopped[j] = true;
			}
		}
		double stopped_fraction = fraction_remove_stopped(stopped);
		if(stopped_fraction > majority_threshold)
		{
			return;
		}
	}
}

void remove_left_border(Mat& image)
{
	vector<bool> started(image.rows, false);
	vector<bool> stopped(image.rows, false);
	double majority_threshold = 0.5;
	for(int j = 0; j < 6/*image.cols - 1*/; j++)
	{
		for(int i = 0; i < image.rows; i++)
		{
			if(stopped[i])
			{
				continue;
			}
			if((int) image.at<uchar>(i, j) != 0)
			{
				started[i] = true;

				image.at<uchar>(i, j) = 0;
			}
			else if(started[i])
			{
				stopped[i] = true;
			}
		}
		double stopped_fraction = fraction_remove_stopped(stopped);
		if(stopped_fraction > majority_threshold)
		{
			return;
		}
	}
}

void remove_right_border(Mat& image)
{
	vector<bool> started(image.rows, false);
	vector<bool> stopped(image.rows, false);
	double majority_threshold = 0.5;
	for(int j = image.cols - 1; j >= image.cols - 6/*0*/; j--)
	{
		for(int i = 0; i < image.rows; i++)
		{
			if(stopped[i])
			{
				continue;
			}
			if((int) image.at<uchar>(i, j) != 0)
			{
				started[i] = true;

				image.at<uchar>(i, j) = 0;
			}
			else if(started[i])
			{
				stopped[i] = true;
			}
		}
		double stopped_fraction = fraction_remove_stopped(stopped);
		if(stopped_fraction > majority_threshold)
		{
			return;
		}
	}
}

void remove_dots(Mat& image, int dot_radius)
{
	for(int row = 0; row < image.rows; row++)
	{
		for(int col = 0; col < image.cols; col++)
		{
			if((row <= 5 || row >= image.rows - 5) || (col <= 5 || col >= image.cols - 5))
			{
				bool all_zeros = true;
				for(int i = row - dot_radius; i < row + dot_radius; i++)
				{
					if(i < 0 || i >= image.rows || col - dot_radius < 0 || col + dot_radius >= image.cols)
					{
						continue;
					}
					if((int) image.at<uchar>(i, col - dot_radius) != 0)
					{
						all_zeros = false;
						break;
					}
					if((int) image.at<uchar>(i, col + dot_radius) != 0)
					{
						all_zeros = false;
						break;
					}
				}
				for(int j = col - dot_radius; j < col + dot_radius; j++)
				{
					if(j < 0 || j >= image.cols || row - dot_radius < 0 || row + dot_radius >= image.rows)
					{
						continue;
					}
					if((int) image.at<uchar>(row - dot_radius, j) != 0)
					{
						all_zeros = false;
						break;
					}
					if((int) image.at<uchar>(row + dot_radius, j) != 0)
					{
						all_zeros = false;
						break;
					}
				}

				if(all_zeros)
				{
					for(int i = row - dot_radius; i < row + dot_radius; i++)
					{
						for(int j = col - dot_radius; j < col + dot_radius; j++)
						{
							if(i < 0 || i >= image.rows || j < 0 || j >= image.cols)
							{
								continue;
							}
							image.at<uchar>(i, j) = 0;
						}
					}
				}
			}
    		}
	}
}
