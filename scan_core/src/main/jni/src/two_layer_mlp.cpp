#include "two_layer_mlp.h"

int mlp_two_layer_predict_class(vector<double>& input, vector<vector<double> >& W, vector<vector<double> >& V)
{
	int num_inputs = input.size();
	int num_hidden_units = W[0].size();
	int num_outputs = V[0].size();

	double bias = 1;

	vector<double> activation_in_hidden(num_hidden_units, 0);
	vector<double> activation_out_hidden(num_hidden_units, 0);
	vector<double> activation_in_output(num_outputs, 0);
	vector<double> activation_out_output(num_outputs, 0);

	for(int j = 0; j < num_hidden_units; j++)
	{
		activation_in_hidden[j] = 0;
		for(int i = 0; i < num_inputs; i++)
		{
			activation_in_hidden[j] += input[i] * W[i][j];
		}
		activation_in_hidden[j] += bias * W[num_inputs][j];
	}
	sigmoid(activation_in_hidden, activation_out_hidden);

	for(int j = 0; j < num_outputs; j++)
	{
		activation_in_output[j] = 0;
		for(int i = 0; i < num_hidden_units; i++)
		{
			activation_in_output[j] += activation_out_hidden[i] * V[i][j];
		}
		activation_in_output[j] += bias * V[num_hidden_units][j];
	}
	softmax(activation_in_output, activation_out_output);

	int max_class = -1;
	double max_prob = -1.f;
	for(int i = 0; i < activation_out_output.size(); i++)
	{
		if(activation_out_output[i] > max_prob)
		{
			max_prob = activation_out_output[i];
			max_class = i;
		}
	}
	return max_class;
}

void sigmoid(vector<double>& z, vector<double>& sigm)
{
	for(int j = 1; j < z.size(); j++)
	{
		sigm[j] = sigmoid(z[j]);
	}
}

double sigmoid(int x)
{
	double sigmoid = 1.f / (1.f + exp(-x));
	return sigmoid;
}

void softmax(vector<double>& z, vector<double>& softmax)
{
	softmax[0] = 1.f;

	double normalizer = 1.f;
	for(int j = 1; j < z.size(); j++)
	{
		softmax[j] = exp(-z[j]);
		normalizer += softmax[j];
	}
	
	for(int j = 0; j < z.size(); j++)
	{
		softmax[j] /= normalizer;
	}
}
